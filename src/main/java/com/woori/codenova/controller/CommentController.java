package com.woori.codenova.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.woori.codenova.entity.Board;
import com.woori.codenova.service.BoardService;
import com.woori.codenova.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("comment") // 모든 URL은 /comment로 시작됨
@RequiredArgsConstructor // 생성자 주입을 자동 생성 (final 필드 포함)
@Controller // Spring MVC의 컨트롤러로 등록
public class CommentController {

	// 게시판 서비스 (댓글이 달리는 게시글을 가져오기 위해 필요)
	private final BoardService boardService;

	// 댓글 서비스 (댓글 생성 처리 담당)
	private final CommentService commentService;

	// 댓글 작성 처리
	@PostMapping("/create/{id}") // POST 요청: /comment/create/{게시글ID}
	public String createComment(Model model, @PathVariable("id") Integer id, // URL 경로에서 게시글 ID 추출
			@Valid CommentForm commentForm, // 댓글 폼 데이터 검증
			BindingResult bindingResult) { // 검증 결과를 담는 객체

		// 게시글 조회 (댓글이 달릴 대상 게시글)
		Board board = this.boardService.getBoard(id);

		// 유효성 검사 실패 시, 게시글 상세 페이지로 돌아가 오류 표시
		if (bindingResult.hasErrors()) {
			model.addAttribute("board", board); // 다시 board_detail에 필요한 데이터 전달
			return "board_detail";
		}

		// 댓글 저장
		this.commentService.create(board, commentForm.getContents());

		// 게시글 상세 페이지로 리다이렉트
		return String.format("redirect:/board/detail/%s", id);
	}
}
