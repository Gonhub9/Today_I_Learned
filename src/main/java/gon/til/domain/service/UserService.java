package gon.til.domain.service;

import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 유저 생성
    public User createUser(String username, String email, String password) {
        validateUser(username, email);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
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
