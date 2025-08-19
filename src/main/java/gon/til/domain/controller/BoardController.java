package gon.til.domain.controller;

import gon.til.domain.dto.board.BoardCreateRequest;
import gon.til.domain.dto.board.BoardResponse;
import gon.til.domain.dto.board.BoardUpdateRequest;
import gon.til.domain.entity.Board;
import gon.til.domain.entity.User;
import gon.til.domain.service.BoardService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/boards")
    public ResponseEntity<List<BoardResponse>> getAllBoards() {
        List<BoardResponse> boards = boardService.findAllBoards();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/projects/{projectId}/boards/{boardId}")
    public ResponseEntity<BoardResponse> getBoardId(
            @PathVariable("projectId") Long projectId,
            @PathVariable("boardId") Long boardId) {
        Board board = boardService.getBoardByProject(projectId, boardId);
        return ResponseEntity.status(HttpStatus.OK).body(BoardResponse.from(board));
    }

    @PostMapping("/projects/{projectsId}/boards")
    public ResponseEntity<BoardResponse> createBoard(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody BoardCreateRequest request) {
        Board board = boardService.createBoard(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BoardResponse.from(board));
    }

    @PutMapping("/boards/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BoardUpdateRequest request) {

        Board board = boardService.updateBoardTitle(boardId, user.getId(), request);

        return ResponseEntity.status(HttpStatus.OK).body(BoardResponse.from(board));
    }

    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @AuthenticationPrincipal User user,
            @PathVariable Long boardId) {

        boardService.deleteBoard(user.getId(), boardId);

        return ResponseEntity.noContent().build();
    }
}
