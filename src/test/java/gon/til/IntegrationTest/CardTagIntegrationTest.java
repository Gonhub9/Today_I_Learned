package gon.til.IntegrationTest;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.cardtag.CardTagRequest;
import gon.til.domain.dto.kanbancolumn.KanbanColumnResponse;
import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.tag.TagCreateRequest;
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
@DisplayName("CardTag 통합 테스트")
public class CardTagIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long projectId;
    private Long cardId;
    private Long tagId1;
    private Long tagId2;

    @BeforeEach
    void setUp() throws Exception {
        // User, Project, Card, Tags 생성
        UserSignupRequest signupRequest = new UserSignupRequest("cardTagUser", "cardtag@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "cardtag@example.com");
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        ProjectCreateRequest createRequest = new ProjectCreateRequest("CardTag Test Project", "설명", "DS");
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

        CardCreateRequest cardCreateRequest = new CardCreateRequest(columnId, "카드 태그 테스트용 카드", "내용");
        MvcResult cardResult = mockMvc.perform(post("/api/v1/cards/columns/" + columnId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        cardId = objectMapper.readTree(cardResult.getResponse().getContentAsString()).get("id").asLong();

        // 태그 2개 생성
        TagCreateRequest createTagRequest1 = new TagCreateRequest("BE", "PASTEL_BLUE");
        MvcResult tagResult1 = mockMvc.perform(post("/api/v1/tags/projects/" + projectId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTagRequest1)))
                .andExpect(status().isCreated())
                .andReturn();
        tagId1 = objectMapper.readTree(tagResult1.getResponse().getContentAsString()).get("id").asLong();

        TagCreateRequest createTagRequest2 = new TagCreateRequest("FE", "PASTEL_GREEN");
        MvcResult tagResult2 = mockMvc.perform(post("/api/v1/tags/projects/" + projectId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTagRequest2)))
                .andExpect(status().isCreated())
                .andReturn();
        tagId2 = objectMapper.readTree(tagResult2.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("카드에 태그를 성공적으로 추가한다")
    void addTagToCard_Success() throws Exception {
        CardTagRequest addTagRequest = new CardTagRequest(tagId1);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cards/" + cardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].id", is(tagId1.intValue())));
    }

    @Test
    @DisplayName("카드에 여러 태그를 추가하고 확인한다")
    void addMultipleTagsToCard_Success() throws Exception {
        // 첫 번째 태그 추가
        CardTagRequest addTagRequest1 = new CardTagRequest(tagId1);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest1)))
                .andExpect(status().isCreated());

        // 두 번째 태그 추가
        CardTagRequest addTagRequest2 = new CardTagRequest(tagId2);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest2)))
                .andExpect(status().isCreated());

        // 카드에 태그 2개가 모두 있는지 확인
        mockMvc.perform(get("/api/v1/cards/" + cardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)));
    }

    @Test
    @DisplayName("카드에서 태그를 성공적으로 삭제한다")
    void removeTagFromCard_Success() throws Exception {
        // 먼저 태그를 추가
        CardTagRequest addTagRequest = new CardTagRequest(tagId1);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest)))
                .andExpect(status().isCreated());

        // 태그 삭제
        mockMvc.perform(delete("/api/v1/cards/" + cardId + "/tags/" + tagId1)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        // 태그가 삭제되었는지 확인
        mockMvc.perform(get("/api/v1/cards/" + cardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(0)));
    }

    @Test
    @DisplayName("존재하지 않는 태그를 카드에 추가하려고 하면 실패한다")
    void addNonExistentTagToCard_Fail() throws Exception {
        long nonExistentTagId = 999L;
        CardTagRequest addTagRequest = new CardTagRequest(nonExistentTagId);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이미 연결된 태그를 다시 추가하려고 하면 실패한다")
    void addAlreadyExistingTagToCard_Fail() throws Exception {
        // 태그를 추가
        CardTagRequest addTagRequest = new CardTagRequest(tagId1);
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest)))
                .andExpect(status().isCreated());

        // 동일한 태그를 다시 추가
        mockMvc.perform(post("/api/v1/cards/" + cardId + "/tags")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTagRequest)))
                .andExpect(status().isConflict());
    }
}
