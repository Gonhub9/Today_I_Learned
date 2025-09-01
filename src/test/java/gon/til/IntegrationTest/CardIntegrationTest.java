package gon.til.IntegrationTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gon.til.domain.dto.card.CardCreateRequest;
import gon.til.domain.dto.card.CardResponse;
import gon.til.domain.dto.card.CardShiftRequest;
import gon.til.domain.dto.card.CardUpdateRequest;
import gon.til.domain.dto.kanbancolumn.KanbanColumnResponse;
import gon.til.domain.dto.project.ProjectCreateRequest;
import gon.til.domain.dto.user.UserSignupRequest;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Card 통합 테스트")
public class CardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long projectId;
    private Long boardId;
    private List<KanbanColumnResponse> columns;

    @BeforeEach
    void setUp() throws Exception {
        UserSignupRequest signupRequest = new UserSignupRequest("cardUser", "card@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "card@example.com");
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        ProjectCreateRequest createRequest = new ProjectCreateRequest("Card Test Project", "설명", "FE");
        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String projectResponse = createResult.getResponse().getContentAsString();
        projectId = objectMapper.readTree(projectResponse).get("id").asLong();
        boardId = objectMapper.readTree(projectResponse).get("mainBoardId").asLong();

        MvcResult columnsResult = mockMvc.perform(get("/api/v1/kanban-columns/boards/" + boardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        columns = objectMapper.readValue(columnsResult.getResponse().getContentAsString(), new TypeReference<List<KanbanColumnResponse>>() {});
    }

    @Test
    @DisplayName("카드 생성 및 상세 조회")
    void createAndGetCard_Success() throws Exception {
        Long todoColumnId = columns.get(0).getId();
        CardCreateRequest cardCreateRequest = new CardCreateRequest(todoColumnId, "새로운 카드", "카드 내용");

        MvcResult createCardResult = mockMvc.perform(post("/api/v1/cards/columns/" + todoColumnId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("새로운 카드")))
                .andReturn();

        Long newCardId = objectMapper.readTree(createCardResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/cards/" + newCardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(newCardId.intValue())))
                .andExpect(jsonPath("$.title", is("새로운 카드")));
    }

    @Test
    @DisplayName("카드 수정")
    void updateCard_Success() throws Exception {
        Long cardId = createTestCard();
        CardUpdateRequest updateRequest = new CardUpdateRequest("수정된 카드 제목", "수정된 내용");

        mockMvc.perform(put("/api/v1/cards/" + cardId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("수정된 카드 제목")))
                .andExpect(jsonPath("$.content", is("수정된 내용")));
    }

    @Test
    @DisplayName("카드 이동")
    void shiftCard_Success() throws Exception {
        Long cardId = createTestCard();
        Long inProgressColumnId = columns.get(1).getId();

        CardShiftRequest shiftRequest = new CardShiftRequest(inProgressColumnId, 1);

        mockMvc.perform(patch("/api/v1/cards/" + cardId + "/shift")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shiftRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnId", is(inProgressColumnId.intValue())));
    }

    @Test
    @DisplayName("카드 삭제")
    void deleteCard_Success() throws Exception {
        Long cardId = createTestCard();

        mockMvc.perform(delete("/api/v1/cards/" + cardId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("프로젝트의 모든 카드 조회")
    void getAllCardsInProject_Success() throws Exception {
        createTestCard();
        createTestCard();

        mockMvc.perform(get("/api/v1/cards/project/" + projectId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private Long createTestCard() throws Exception {
        Long todoColumnId = columns.get(0).getId();
        CardCreateRequest cardCreateRequest = new CardCreateRequest(todoColumnId, "테스트 카드", "테스트 내용");

        MvcResult createCardResult = mockMvc.perform(post("/api/v1/cards/columns/" + todoColumnId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(createCardResult.getResponse().getContentAsString()).get("id").asLong();
    }
}
