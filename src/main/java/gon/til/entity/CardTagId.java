package gon.til.entity;

import jakarta.persistence.Entity;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode  // 복합 키는 equals, hashCode 필수
public class CardTagId implements Serializable {
    private Long cardId;
    private Long tagId;
}
