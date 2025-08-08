package com.woori.codenova.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.woori.codenova.entity.Board;
import com.woori.codenova.service.BoardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/board") // 모든 URL이 "/board"로 시작하도록 지정
@RequiredArgsConstructor // final 필드를 자동으로 생성자 주입
@Controller // Spring MVC 컨트롤러로 등록
public class BoardController {

	private final BoardService boardService; // 게시판 서비스 의존성 주입

	// 게시글 목록 페이지
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
		// 페이지 번호에 해당하는 게시글 목록 조회
		Page<Board> paging = this.boardService.getList(page);
		// 뷰에 전달
		model.addAttribute("paging", paging);
		// board_list.html 렌더링
		return "board_list";
	}

	// 게시글 상세 페이지
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id, CommentForm commentForm) {
		// 특정 ID의 게시글 조회
		Board board = this.boardService.getBoard(id);
		// 뷰에 게시글 정보 전달
		model.addAttribute("board", board);
		// board_detail.html 렌더링
		return "board_detail";
	}

	// 게시글 작성 폼 요청 (GET)
	@GetMapping("/create")
	public String boardCreate(BoardForm boardForm) {
		// board_form.html 템플릿 렌더링
		return "board_form";
	}

	// 게시글 작성 처리 (POST)
	@PostMapping("/create")
	public String boardCreate(@Valid BoardForm boardForm, BindingResult bindingResult) {
		// 유효성 검사 실패 시, 폼 다시 표시
		if (bindingResult.hasErrors()) {
			return "board_form";
		}
		// 게시글 저장
		this.boardService.create(boardForm.getSubject(), boardForm.getContents());
		// 목록 페이지로 리다이렉트
		return "redirect:/board/list";
	}

}
