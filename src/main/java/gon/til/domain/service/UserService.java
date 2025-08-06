package gon.til.domain.service;

import gon.til.domain.dto.user.UserSignupRequest;
import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 유저 생성
    @Transactional
    public User createUser(UserSignupRequest request) {
        validateUser(request.getUsername(), request.getEmail());

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword) // 암호화된 비밀번호 저장
                .build();

        return userRepository.save(user);
    }

    // 이메일로 유저 찾기
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_USER_EMAIL));
    }

    // 유저 검증
    private void validateUser(String username, String email) {

        // 사용자명 검증
        if (userRepository.existsByUsername(username)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_USER_NAME);
        }

        // 이메일 검증
        if(userRepository.existsByEmail(email)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_USER_EMAIL);
        }
    }
}
