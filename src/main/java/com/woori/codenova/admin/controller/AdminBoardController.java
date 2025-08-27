package com.woori.codenova.admin.controller;

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

import com.woori.codenova.admin.service.AdminBoardService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.BoardForm;
import com.woori.codenova.form.CommentForm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {
	private final AdminBoardService adminBoardService;
	private final AdminUserService adminUserService;

	@GetMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {

		Page<Board> paging = adminBoardService.getList(page, kw);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);

		return "admin/board_list";
	}

	@GetMapping(value = "/detail/{id}")
	@PreAuthorize("isAuthenticated()")
	public String detail(Model model, @PathVariable("id") Integer id, CommentForm commentForm) {

		Board item = this.adminBoardService.getItem(id);
		this.adminBoardService.setViewCount(item);
		model.addAttribute("item", item);

		return "admin/board_detail";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String create(Model model, BoardForm boardForm) {

		model.addAttribute("mode", "create");
		return "admin/board_form";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String create(Model model, @Valid BoardForm boardForm, BindingResult bindingResult, Principal principal) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "create");
			return "admin/board_form";
		}
		SiteUser author = this.adminUserService.getItem(principal.getName());
		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);

		// TODO :: 넘겨받은 게시판 값 넘겨줘야 함

		this.adminBoardService.create(boardForm.getSubject(), con, author);
		return "redirect:/admin/board/list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String modify(Model model, BoardForm boardForm, @PathVariable("id") Integer id, Principal principal) {

		model.addAttribute("mode", "modify");
		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}
		boardForm.setSubject(item.getSubject());
		boardForm.setContents(item.getContents());

		// TODO :: 게시판 수정가능 여부?? - 없으면 좋겠다

		return "admin/board_form";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String modify(Model model, @Valid BoardForm boardForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Integer id) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "mofidy");
			return "admin/board_form";
		}
		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		// TODO :: 게시판 수정가능 여부?? - 없으면 좋겠다

		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);
		this.adminBoardService.modify(item, boardForm.getSubject(), con);
		return String.format("redirect:/admin/board/detail/%s", id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Principal principal, @PathVariable("id") Integer id) {

		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
		}
//		this.boardService.delete(item);
		return "redirect:/admin/board/list";
	}

}
