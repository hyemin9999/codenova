package com.woori.codenova.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.CommentForm;
import com.woori.codenova.service.BoardService;
import com.woori.codenova.service.CommentService;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("comment") // 모든 요청은 /comment 하위 URL로 매핑
@RequiredArgsConstructor   // final 필드 생성자 자동 주입
@Controller
public class CommentController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    // ===============================================================
    // 📌 댓글 작성 (POST)
    // ===============================================================
    @PreAuthorize("isAuthenticated()") // 로그인 사용자만 가능
    @PostMapping("/create/{id}")
    public String createComment(Model model,
                                @PathVariable("id") Integer id,          // 게시글 ID
                                @Valid CommentForm commentForm,          // 입력된 댓글 폼
                                BindingResult bindingResult,
                                Principal principal) {                   // 로그인 사용자

        // 해당 게시글 조회
        Board board = this.boardService.getBoard(id);

        // 로그인 사용자 조회
        SiteUser siteUser = this.userService.getUser(principal.getName());

        // 입력 검증 실패 시 다시 상세 화면으로
        if (bindingResult.hasErrors()) {
            model.addAttribute("board", board);
            return "board_detail";
        }

        // 댓글 생성 후 → 해당 댓글 앵커(#comment_id)로 이동
        Comment comment = this.commentService.create(board, commentForm.getContents(), siteUser);
        return String.format("redirect:/board/detail/%s#comment_%s", comment.getBoard().getId(), comment.getId());
    }

    // ===============================================================
    // 📌 댓글 수정 폼 (GET)
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String commentModify(CommentForm commentForm,
                                @PathVariable("id") Integer id,
                                Principal principal) {

        // 댓글 조회
        Comment comment = this.commentService.getComment(id);

        // 작성자 본인만 수정 가능
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 기존 댓글 내용을 폼에 세팅
        commentForm.setContents(comment.getContents());
        return "comment_form";
    }

    // 📌 댓글 수정 처리 (POST)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String commentModify(@Valid CommentForm commentForm,
                                BindingResult bindingResult,
                                @PathVariable("id") Integer id,
                                Principal principal) {

        // 유효성 검사 실패 시 수정 폼 다시 보여줌
        if (bindingResult.hasErrors()) {
            return "comment_form";
        }

        // 댓글 조회
        Comment comment = this.commentService.getComment(id);

        // 작성자 권한 확인
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 수정 처리
        this.commentService.modify(comment, commentForm.getContents());

        // 수정 후 해당 댓글 앵커로 리다이렉트
        return String.format("redirect:/board/detail/%s#comment_%s", comment.getBoard().getId(), comment.getId());
    }

    // ===============================================================
    // 📌 댓글 삭제
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String commentDelete(Principal principal,
                                @PathVariable("id") Integer id) {
        Comment comment = this.commentService.getComment(id);

        // 작성자 본인만 삭제 가능
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }

        // 삭제 후 게시글 상세 페이지로 리다이렉트
        this.commentService.delete(comment);
        return String.format("redirect:/board/detail/%s", comment.getBoard().getId());
    }

    // ===============================================================
    // 📌 댓글 추천 (좋아요 토글)
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String commentVote(Principal principal,
                              @PathVariable("id") Integer id) {
        Comment comment = this.commentService.getComment(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());

        // 추천 추가/취소
        this.commentService.vote(comment, siteUser);

        // 다시 해당 댓글 위치로 이동
        return String.format("redirect:/board/detail/%s#comment_%s", comment.getBoard().getId(), comment.getId());
    }

    // ===============================================================
    // 📌 댓글 즐겨찾기 (토글)
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/favorite/{id}")
    public String commentFavorite(Principal principal,
                                  @PathVariable("id") Integer id) {
        Comment comment = this.commentService.getComment(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());

        // 즐겨찾기 추가/취소
        this.commentService.favorite(comment, siteUser);

        // 게시글 상세 페이지로 이동 (댓글 앵커는 없음)
        return String.format("redirect:/board/detail/%s", comment.getBoard().getId());
    }
}
