package gon.til.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gon.til.domain.dto.board.BoardUpdateRequest;
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
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Board 통합 테스트")
public class BoardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long projectId;
    private Long boardId;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 회원가입
        UserSignupRequest signupRequest = new UserSignupRequest("boardUser", "board@example.com", "password123");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // 2. 로그인
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "board@example.com");
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // 3. 프로젝트 생성 (기본 보드 자동 생성)
        ProjectCreateRequest createRequest = new ProjectCreateRequest("테스트 프로젝트", "설명", "BE");
        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        projectId = objectMapper.readTree(response).get("id").asLong();
        boardId = objectMapper.readTree(response).get("mainBoardId").asLong();
    }

    @Test
    @DisplayName("보드 상세 조회")
    void getBoard_Success() throws Exception {
        mockMvc.perform(get("/api/v1/boards/projects/" + projectId + "/boards/" + boardId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(boardId.intValue())))
                .andExpect(jsonPath("$.title", is("테스트 프로젝트 Board")));
    }

    @Test
    @DisplayName("보드 제목 수정")
    void updateBoard_Success() throws Exception {
        BoardUpdateRequest updateRequest = new BoardUpdateRequest("수정된 보드 제목");

        mockMvc.perform(put("/api/v1/boards/" + boardId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("수정된 보드 제목")));
    }

    @Test
    @DisplayName("사용자의 모든 보드 조회")
    void getAllBoards_Success() throws Exception {
        mockMvc.perform(get("/api/v1/boards")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(boardId.intValue())));
    }

    @Test
    @DisplayName("보드 삭제")
    void deleteBoard_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/boards/" + boardId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }
}
