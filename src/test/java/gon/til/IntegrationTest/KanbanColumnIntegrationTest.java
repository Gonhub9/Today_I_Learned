package gon.til.IntegrationTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gon.til.domain.dto.kanbancolumn.KanbanColumnCreateRequest;
import gon.til.domain.dto.kanbancolumn.KanbanColumnResponse;
import gon.til.domain.dto.kanbancolumn.KanbanColumnUpdateRequest;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("KanbanColumn 통합 테스트")
public class KanbanColumnIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long boardId;

    @BeforeEach
    void setUp() throws Exception {
        UserSignupRequest signupRequest = new UserSignupRequest("kanbanUser", "kanban@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "kanban@example.com");
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        ProjectCreateRequest createRequest = new ProjectCreateRequest("Kanban Test Project", "설명", "BE");
        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        boardId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("mainBoardId").asLong();
    }

    @Test
    @DisplayName("보드 ID로 모든 컬럼 조회")
    void getColumns_Success() throws Exception {
        mockMvc.perform(get("/api/v1/kanban-columns/boards/" + boardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("To Do")))
                .andExpect(jsonPath("$[1].title", is("In Progress")))
                .andExpect(jsonPath("$[2].title", is("Done")));
    }

    @Test
    @DisplayName("새로운 컬럼 생성")
    void createColumn_Success() throws Exception {
        KanbanColumnCreateRequest createColumnRequest = new KanbanColumnCreateRequest(boardId, "New Column");

        mockMvc.perform(post("/api/v1/kanban-columns/boards/" + boardId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createColumnRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Column")));

        mockMvc.perform(get("/api/v1/kanban-columns/boards/" + boardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    @DisplayName("컬럼 제목 수정")
    void updateColumnTitle_Success() throws Exception {
        List<KanbanColumnResponse> columns = getColumns();
        Long columnToUpdateId = columns.get(0).getId();

        KanbanColumnUpdateRequest updateRequest = new KanbanColumnUpdateRequest("Updated Title");

        mockMvc.perform(put("/api/v1/kanban-columns/" + columnToUpdateId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    @DisplayName("컬럼 위치 변경")
    void updateColumnPositions_Success() throws Exception {
        List<KanbanColumnResponse> originalColumns = getColumns();
        List<Long> originalIds = originalColumns.stream().map(KanbanColumnResponse::getId).collect(Collectors.toList());
        Collections.reverse(originalIds);

        mockMvc.perform(patch("/api/v1/kanban-columns/boards/" + boardId + "/positions")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalIds)))
                .andExpect(status().isOk());

        List<KanbanColumnResponse> updatedColumns = getColumns();
        List<Long> updatedIds = updatedColumns.stream().map(KanbanColumnResponse::getId).collect(Collectors.toList());

        assertThat(updatedIds).isEqualTo(originalIds);
    }

    @Test
    @DisplayName("컬럼 삭제")
    void deleteColumn_Success() throws Exception {
        List<KanbanColumnResponse> columns = getColumns();
        Long columnToDeleteId = columns.get(0).getId();

        mockMvc.perform(delete("/api/v1/kanban-columns/" + columnToDeleteId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    private List<KanbanColumnResponse> getColumns() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/kanban-columns/boards/" + boardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        return objectMapper.readValue(content, new TypeReference<List<KanbanColumnResponse>>() {});
    }
}
