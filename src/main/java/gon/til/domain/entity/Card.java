package gon.til.domain.entity;

import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "cards")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Lob    // 긴 텍스트를 위한 어노테이션
    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer position;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // KanbanColumn 연관관계 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kanban_column_id")
    private KanbanColumn kanbanColumn;

    // Tag 연관관계 (N : M) - 중간 테이블을 통해
    @Builder.Default
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CardTag> cardTags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // 태그 추가
    public void addTag(Tag tag) {
        // 이미 존재하는지 확인
        boolean alreadyExists = this.cardTags.stream()
            .anyMatch(cardTag -> cardTag.getTag().equals(tag));

        if (alreadyExists) {
            throw new GlobalException(GlobalErrorCode.DUPLICATE_CARD_TAG);
        }

        CardTag cardTag = new CardTag(this, tag);
        this.cardTags.add(cardTag);
    }

    // 태그 삭제
    public void removeTag(Long tagId) {
        this.cardTags.removeIf(cardTag -> cardTag.getTag().getId().equals(tagId));
    }

    // 카드 수정
    public void updateCard(String title, String content) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
    }

    // 포지션 수정
    public void updatePosition(KanbanColumn column, Integer position) {
        if (position != null) this.position = position;
        if (kanbanColumn != null) this.kanbanColumn = column;
    }
}
