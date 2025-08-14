package gon.til.IntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gon.til.domain.dto.user.UserSignupRequest;
import java.util.HashMap;
import java.util.Map;
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
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입, 로그인, 로그아웃 통합 테스트")
    void userLifecycleTest() throws Exception {
        // given: 회원가입 정보
        UserSignupRequest signupRequest = new UserSignupRequest("TestUser", "testuser@example.com", "password123");

        // when: 회원가입 요청
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                // then: 회원가입 성공
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.username").value("TestUser"));

        // given: 로그인 정보
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "testuser@example.com");
        loginRequest.put("password", "password123");


        // when: 로그인 요청
        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // then: 로그인 성공 및 토큰 발급
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // when: 로그아웃 요청
        mockMvc.perform(post("/api/v1/users/logout")
                        .header("Authorization", "Bearer " + token))
                // then: 로그아웃 성공
                .andExpect(status().isOk());
    }
}
