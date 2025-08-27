package gon.til.domain.controller;

import gon.til.domain.dto.kanbancolumn.KanbanColumnCreateRequest;
import gon.til.domain.dto.kanbancolumn.KanbanColumnResponse;
import gon.til.domain.dto.kanbancolumn.KanbanColumnUpdateRequest;
import gon.til.domain.entity.User;
import gon.til.domain.service.KanbanColumnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "KanbanColumn", description = "칸반 컬럼 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kanban-columns")
public class KanbanColumnController {

    private final KanbanColumnService kanbanColumnService;

    @PostMapping("/boards/{boardId}")
    public ResponseEntity<KanbanColumnResponse> createColumn(
        @PathVariable Long boardId,
        @AuthenticationPrincipal User user,
        @Valid @RequestBody KanbanColumnCreateRequest request) {
        KanbanColumnResponse response = kanbanColumnService.createColumn(boardId, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/boards/{boardId}")
    public ResponseEntity<List<KanbanColumnResponse>> getColumnsByBoard(
        @PathVariable Long boardId,
        @AuthenticationPrincipal User user) {
        List<KanbanColumnResponse> responses = kanbanColumnService.getColumnsByBoard(boardId, user.getId());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<KanbanColumnResponse> updateColumnTitle(
        @PathVariable Long columnId,
        @AuthenticationPrincipal User user,
        @Valid @RequestBody KanbanColumnUpdateRequest request) {
        KanbanColumnResponse response = kanbanColumnService.updateColumnTitle(columnId, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/boards/{boardId}/positions")
    public ResponseEntity<List<KanbanColumnResponse>> updateColumnPositions(
        @PathVariable Long boardId,
        @AuthenticationPrincipal User user,
        @RequestBody List<Long> columnIds) {
        List<KanbanColumnResponse> responses = kanbanColumnService.updateColumnPositions(boardId, user.getId(), columnIds);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(
        @PathVariable Long columnId,
        @AuthenticationPrincipal User user) {
        kanbanColumnService.deleteColumn(columnId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
