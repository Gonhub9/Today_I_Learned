package gon.til.domain.dto.board;

import gon.til.domain.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardResponse {

    private final Long id;
    private final String title;
    private final Long projectId;

    public static BoardResponse from(Board board) {
        return new BoardResponse(
            board.getId(),
            board.getTitle(),
            board.getProject().getId()
        );
    }
}
