package com.woori.codenova.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.BoardForm;
import com.woori.codenova.form.CommentForm;
import com.woori.codenova.service.BoardService;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/board") // 이 컨트롤러의 모든 핸들러는 "/board"로 시작하는 URL을 처리
@RequiredArgsConstructor // final 필드(boardService, userService)에 대한 생성자 자동 생성(생성자 주입)
@Controller // 스프링 MVC 컴포넌트(뷰 반환형 컨트롤러)
public class BoardController {

	private final BoardService boardService; // 게시글 도메인 서비스
	private final UserService userService; // 사용자 조회/인증 관련 서비스

	// ===============================================================
	// 목록 페이지
	// ===============================================================
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page, // 0부터 시작하는 페이지 인덱스
			@RequestParam(value = "kw", defaultValue = "") String kw) { // 검색 키워드(빈 문자열이면 전체)
		// 지정한 페이지/키워드로 페이징 조회
		Page<Board> paging = this.boardService.getList(page, kw);

		// 뷰 템플릿에서 사용할 모델 속성 등록
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);

		// templates/board_list.html 렌더링
		return "board_list";
	}

	// ===============================================================
	// 상세 페이지
	// ===============================================================
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id, CommentForm commentForm) {
		// PathVariable 로 넘어온 id로 게시글 단건 조회
		Board board = this.boardService.getBoard(id);

		// 뷰에 전달
		model.addAttribute("board", board);

		// templates/board_detail.html 렌더링
		return "board_detail";
	}

	// ===============================================================
	// 생성 폼 (GET)
	// - 인증 사용자만 접근 가능
	// ===============================================================
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String boardCreate(BoardForm boardForm) {
		// 폼 바인딩용 빈 객체(boardForm)를 파라미터로 받아 템플릿에 바인딩
		// templates/board_form.html 렌더링
		return "board_form";
	}

	// ===============================================================
	// 생성 처리 (POST)
	// - 인증 사용자만 접근 가능
	// - @Valid + BindingResult 로 서버측 유효성 검사
	// ===============================================================
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String boardCreate(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal) {
		// 유효성 실패 시 같은 폼 다시 렌더링(오류 메시지는 form_errors 프래그먼트 등에서 표시)
		if (bindingResult.hasErrors()) {
			return "board_form";
		}
		// 인증 사용자 조회(Principal.name = username)
		SiteUser siteUser = this.userService.getUser(principal.getName());

		// (에디터/전송 과정에서 인코딩된 경우를 대비해) 내용 디코딩
		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);

		// 게시글 생성
		this.boardService.create(boardForm.getSubject(), con, siteUser);

		// 생성 뒤 목록으로 리다이렉트(새로고침 중복 제출 방지: PRG 패턴)
		return "redirect:/board/list";
	}

	// ===============================================================
	// 수정 폼 (GET)
	// - 인증 사용자 + 작성자 본인만 접근
	// ===============================================================
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String boardModify(BoardForm boardForm, @PathVariable("id") Integer id, Principal principal) {
		// 수정 대상 조회
		Board board = this.boardService.getBoard(id);

		// 접근 권한 체크: 현재 로그인 사용자와 글쓴이 동일 여부
		if (!board.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		// 기존 값 폼에 세팅(템플릿에서 th:field 로 바인딩되어 초기값 표시)
		boardForm.setSubject(board.getSubject());
		boardForm.setContents(board.getContents());

		// 동일 폼 템플릿 재활용
		return "board_form";
	}

	// ===============================================================
	// 수정 처리 (POST)
	// - 인증 사용자 + 작성자 본인만 처리 가능
	// ===============================================================
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String boardModify(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Integer id) {
		// 서버 유효성 실패 시 폼으로 되돌아감
		if (bindingResult.hasErrors()) {
			return "board_form";
		}

		// 수정 대상 조회
		Board board = this.boardService.getBoard(id);

		// 권한 확인
		if (!board.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		// 내용 디코딩 후 수정 반영
		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);
		this.boardService.modify(board, boardForm.getSubject(), con);

		// 수정 후 상세 페이지로 이동
		return String.format("redirect:/board/detail/%s", id);
	}

	// ===============================================================
	// 삭제 (GET)
	// - 인증 사용자 + 작성자 본인만 삭제 가능
	// - 실제 삭제 로직은 서비스에서 처리(연관 데이터/트랜잭션 포함)
	// ===============================================================
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String boardDelete(Principal principal, @PathVariable("id") Integer id) {
		// 대상 조회
		Board board = this.boardService.getBoard(id);

		// 권한 확인
		if (!board.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
		}

		// 서비스에 위임(연쇄 삭제/조인 테이블 정리 등은 서비스/엔티티 설정에서 처리)
		this.boardService.delete(board);

		// 홈으로 리다이렉트
		return "redirect:/";
	}

	// ===============================================================
	// 추천 (GET)
	// - 인증 사용자만 가능
	// - 중복 추천 방지는 Set<SiteUser> 등 컬렉션/제약으로 처리
	// ===============================================================
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String boardVote(Principal principal, @PathVariable("id") Integer id) {
		// 대상 조회
		Board board = this.boardService.getBoard(id);

		// 추천 사용자 조회
		SiteUser siteUser = this.userService.getUser(principal.getName());

		// 추천 처리
		this.boardService.vote(board, siteUser);

		// 다시 상세로(앵커 등은 템플릿에서 처리)
		return String.format("redirect:/board/detail/%s", id);
	}

}
