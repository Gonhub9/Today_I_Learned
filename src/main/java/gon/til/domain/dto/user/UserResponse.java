package gon.til.domain.dto.user;

import gon.til.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String displayName;
    private final String email;

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getDisplayName(),
            user.getEmail()
        );
    }
}
