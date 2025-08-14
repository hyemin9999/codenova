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

/**
 * CommentController - 댓글 생성/수정/삭제/추천을 처리하는 MVC 컨트롤러. - 실제 비즈니스 로직은
 * CommentService/BoardService 로 위임.
 *
 * 매핑 규칙 - 클래스 레벨 @RequestMapping("comment") : 모든 엔드포인트는 "comment" 로 시작. (선행
 * 슬래시가 없는 형태를 그대로 유지합니다. 현재 동작과 동일)
 *
 * 보안 - @PreAuthorize("isAuthenticated()") : 로그인 사용자만 접근 허용.
 *
 * 유효성 검증 - @Valid CommentForm + BindingResult 를 사용해 서버 측 검증. - 검증 실패 시 기존 화면으로
 * 복귀하여 오류 메시지 표시(템플릿에서 처리).
 */
@RequestMapping("comment") // 모든 URL은 /comment로 시작됨
@RequiredArgsConstructor // 생성자 주입을 자동 생성 (final 필드 포함)
@Controller // Spring MVC의 컨트롤러로 등록
public class CommentController {

	// 게시판 서비스 (댓글이 달리는 게시글을 가져오기 위해 필요)
	private final BoardService boardService;

	// 댓글 서비스 (댓글 생성/수정/삭제/추천 처리 담당)
	private final CommentService commentService;

	// 사용자 조회 서비스 (Principal.username 기반)
	private final UserService userService;

	// ---------------------------------------------------------------------
	// 댓글 작성 처리 (POST): /comment/create/{게시글ID}
	// - 인증 사용자만 접근 가능
	// - 검증 실패 시 게시글 상세 페이지로 복귀하여 오류 표시
	// - 성공 시 해당 댓글 앵커(#comment_{id})로 스크롤 이동
	// ---------------------------------------------------------------------
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}") // POST 요청: /comment/create/{게시글ID}
	public String createComment(Model model, @PathVariable("id") Integer id, // URL 경로에서 게시글 ID 추출
			@Valid CommentForm commentForm, // 댓글 폼 데이터 검증 대상
			BindingResult bindingResult, // 검증 결과를 담는 객체
			Principal principal // 현재 로그인 사용자
	) {

		// 댓글이 달릴 대상 게시글 조회
		Board board = this.boardService.getBoard(id);

		// 댓글 작성자(로그인 사용자) 조회
		SiteUser siteUser = this.userService.getUser(principal.getName());

		// 유효성 검사 실패 시 → 게시글 상세 화면으로 되돌아가 오류 메시지 표시
		// (board_detail.html 에서 formErrorsFragment 등으로 렌더링)
		if (bindingResult.hasErrors()) {
			model.addAttribute("board", board); // 상세 템플릿에서 필요한 데이터 전달
			return "board_detail";
		}

		// 댓글 생성 서비스 호출 (내용/작성자/대상 게시글 설정)
		Comment comment = this.commentService.create(board, commentForm.getContents(), siteUser);

		// 생성 후 상세 페이지로 리다이렉트 + 해당 댓글 앵커 위치로 이동
		return String.format("redirect:/board/detail/%s#comment_%s", comment.getBoard().getId(), comment.getId());
	}

	// ---------------------------------------------------------------------
	// 수정 폼(GET): /comment/modify/{id}
	// - 인증 사용자 + 작성자 본인만 접근 가능
	// - 기존 내용을 CommentForm에 채워 폼에 초기값 표시
	// ---------------------------------------------------------------------
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String commentModify(CommentForm commentForm, @PathVariable("id") Integer id, Principal principal) {
		// 수정 대상 댓글 조회
		Comment comment = this.commentService.getComment(id);

		// 권한 확인: 현재 로그인 사용자와 댓글 작성자 동일 여부
		if (!comment.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		// 기존 내용 폼에 세팅 → 템플릿에서 th:field 로 바인딩되어 표시
		commentForm.setContents(comment.getContents());

		// 수정 폼 템플릿 렌더링
		return "comment_form";
	}

	// ---------------------------------------------------------------------
	// 수정 처리(POST): /comment/modify/{id}
	// - 인증 사용자 + 작성자 본인만 처리 가능
	// - 실패 시 폼 재표시, 성공 시 댓글 앵커로 리다이렉트
	// ---------------------------------------------------------------------
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String commentModify(@Valid CommentForm commentForm, // 서버측 유효성 검사 대상
			BindingResult bindingResult, // 검사 결과
			@PathVariable("id") Integer id, Principal principal // 현재 로그인 사용자
	) {
		// 검증 실패 → 수정 폼 다시 렌더링
		if (bindingResult.hasErrors()) {
			return "comment_form";
		}

		// 수정 대상 댓글 조회
		Comment comment = this.commentService.getComment(id);

		// 권한 확인
		if (!comment.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		// 수정 반영
		this.commentService.modify(comment, commentForm.getContents());

		// 상세 페이지로 리다이렉트 + 해당 댓글 위치로 이동
		return String.format("redirect:/board/detail/%s#comment_%s", comment.getBoard().getId(), comment.getId());
	}

	// ---------------------------------------------------------------------
	// 삭제(GET): /comment/delete/{id}
	// - 인증 사용자 + 작성자 본인만 삭제 가능
	// - 성공 시 게시글 상세로 리다이렉트
	// ---------------------------------------------------------------------
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String commentDelete(Principal principal, @PathVariable("id") Integer id) {
		// 삭제 대상 댓글 조회
		Comment comment = this.commentService.getComment(id);

		// 권한 확인
		if (!comment.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
		}

		// 삭제 처리
		this.commentService.delete(comment);

		// 소속 게시글 상세로 이동 (댓글 앵커 없이 목록 상단부터)
		return String.format("redirect:/board/detail/%s", comment.getBoard().getId());
	}

	// ---------------------------------------------------------------------
	// 추천(GET): /comment/vote/{id}
	// - 인증 사용자만 가능
	// - 중복 추천 방지는 Set<SiteUser> 등 컬렉션/제약으로 서비스/엔티티에서 관리
	// ---------------------------------------------------------------------
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String commentVote(Principal principal, @PathVariable("id") Integer id) {
		// 대상 댓글 조회
		Comment comment = this.commentService.getComment(id);

		// 추천 사용자(로그인 사용자) 조회
		SiteUser siteUser = this.userService.getUser(principal.getName());

		// 추천 처리
		this.commentService.vote(comment, siteUser);

		// 상세 페이지로 리다이렉트 + 해당 댓글 위치로 이동
		return String.format("redirect:/board/detail/%s#comment_%s", comment.getBoard().getId(), comment.getId());
	}
}
