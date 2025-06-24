package gon.til.repository;

import gon.til.entity.Card;
import gon.til.entity.CardTag;
import gon.til.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTagRepository extends JpaRepository<CardTag, Long> {
    List<CardTag> findByCard(Card card);
    List<CardTag> findByCardId(Long cardId);

    List<CardTag> findByTag(Tag tag);
    List<CardTag> findByTagId(Long tagId);
}
