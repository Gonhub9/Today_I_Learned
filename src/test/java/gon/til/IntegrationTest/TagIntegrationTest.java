package gon.til.IntegrationTest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.cardtag.CardTagRequest;
import gon.til.domain.dto.kanbancolumn.KanbanColumnResponse;
import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.tag.TagCreateRequest;
import gon.til.domain.dto.tag.TagUpdateRequest;
import gon.til.domain.dto.user.UserSignupRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Tag 및 CardTag 통합 테스트")
public class TagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long projectId;
    private Long cardId;

    @BeforeEach
    void setUp() throws Exception {
        UserSignupRequest signupRequest = new UserSignupRequest("tagUser", "tag@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "tag@example.com");
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        ProjectCreateRequest createRequest = new ProjectCreateRequest("Tag Test Project", "설명", "DS");
        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        projectId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        Long boardId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("mainBoardId").asLong();
        MvcResult columnsResult = mockMvc.perform(get("/api/v1/kanban-columns/boards/" + boardId).header("Authorization", token)).andReturn();
        List<KanbanColumnResponse> columns = objectMapper.readValue(columnsResult.getResponse().getContentAsString(), new com.fasterxml.jackson.core.type.TypeReference<List<KanbanColumnResponse>>() {});
        Long columnId = columns.getFirst().getId();

        CardCreateRequest cardCreateRequest = new CardCreateRequest(columnId, "태그 테스트용 카드", "내용");
        MvcResult cardResult = mockMvc.perform(post("/api/v1/cards/columns/" + columnId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        cardId = objectMapper.readTree(cardResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("태그 생성, 조회, 수정, 삭제")
    void tagLifecycleTest() throws Exception {
        // 1. Create
        TagCreateRequest createTagRequest = new TagCreateRequest("Backend", "PASTEL_RED");
        MvcResult tagResult = mockMvc.perform(post("/api/v1/tags/projects/" + projectId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTagRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Backend")))
                .andExpect(jsonPath("$.color", is("#FFADAD")))
                .andReturn();
        Long tagId = objectMapper.readTree(tagResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Get All
        mockMvc.perform(get("/api/v1/tags/projects/" + projectId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Backend")));

        // 3. Update
        TagUpdateRequest updateTagRequest = new TagUpdateRequest("BE", "PASTEL_BLUE");
        mockMvc.perform(put("/api/v1/tags/" + tagId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTagRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("BE")))
                .andExpect(jsonPath("$.color", is("#9BF6FF")));

        // 4. Delete
        mockMvc.perform(delete("/api/v1/tags/" + tagId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카드에 태그 추가 및 삭제")
    void addAndRemoveTagFromCardTest() throws Exception {
        // 1. Create a tag
        TagCreateRequest createTagRequest = new TagCreateRequest("Urgent", "PASTEL_YELLOW");
        MvcResult tagResult = mockMvc.perform(post("/api/v1/tags/projects/" + projectId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTagRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.color", is("#FDFFB6")))
                .andReturn();
        Long tagId = objectMapper.readTree(tagResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Add tag to card
        CardTagRequest addTagRequest = new CardTagRequest(tagId);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest)))
                .andExpect(status().isCreated());

        // 3. Verify tag is added
        mockMvc.perform(get("/api/v1/cards/" + cardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].name", is("Urgent")));

        // 4. Remove tag from card
        mockMvc.perform(delete("/api/v1/cards/" + cardId + "/tags/" + tagId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }
}
