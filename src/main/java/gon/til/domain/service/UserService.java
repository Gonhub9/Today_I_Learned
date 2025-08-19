package gon.til.domain.service;

import gon.til.domain.dto.user.UserLoginRequest;
import gon.til.domain.dto.user.UserSignupRequest;
import gon.til.domain.entity.User;
import gon.til.domain.repository.UserRepository;
import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import gon.til.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            @Lazy PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 유저 생성
    @Transactional
    public User createUser(UserSignupRequest request) {
        validateUser(request.getUsername(), request.getEmail());

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .displayName(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword) // 암호화된 비밀번호 저장
                .build();

        return userRepository.save(user);
    }

    // 로그인
    @Transactional
    public String login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_USER_EMAIL));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GlobalException(GlobalErrorCode.INVALID_PASSWORD);
        }

        return jwtTokenProvider.createToken(user.getEmail());
    }

    // 이메일로 유저 찾기
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND_USER_EMAIL));
    }

    // 유저 검증
    private void validateUser(String displayName, String email) {

        // 사용자명 검증
        if (userRepository.existsByDisplayName(displayName)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_USER_NAME);
        }

        // 이메일 검증
        if(userRepository.existsByEmail(email)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_USER_EMAIL);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾지 못했습니다. email: " + email));
    }
}
