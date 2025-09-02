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
import com.woori.codenova.admin.service.AdminCategoryService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Notice;
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
	private final AdminCategoryService adminCategoryService;

	private final String redirect_list = "redirect:/admin/board/list/%s";
	private final String redirect_detail = "redirect:/admin/board/detail/%s";

	private final String board_list = "admin/board_list";
	private final String board_detail = "admin/board_detail";
	private final String board_form = "admin/board_form";

	@GetMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw,
			@RequestParam(value = "field", defaultValue = "") String field) {

		Page<Board> paging = adminBoardService.getList(page, kw, field, 0);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("field", field);

		return board_list;
	}

	@GetMapping("/list/{cid}")
	public String listCateogry(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw,
			@RequestParam(value = "field", defaultValue = "all") String field, @PathVariable("cid") Integer cid) {

		Page<Board> paging = adminBoardService.getList(page, kw, field, cid);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("field", field);

		return board_list;
	}

	@GetMapping(value = "/detail/{id}")
	@PreAuthorize("isAuthenticated()")
	public String detail(Model model, @PathVariable("id") Integer id, CommentForm commentForm) {

		Board item = this.adminBoardService.getItem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 게시글 입니다.");
		} else {
			this.adminBoardService.setViewCount(item);
			model.addAttribute("item", item);
		}

		return "admin/board_detail";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create/{cid}")
	public String create(Model model, BoardForm boardForm, @PathVariable("cid") Integer cid) {

		model.addAttribute("mode", "create");
		return board_form;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{cid}")
	public String create(Model model, @Valid BoardForm boardForm, BindingResult bindingResult, Principal principal,
			@PathVariable("cid") Integer cid) {

		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);
		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "create");
			model.addAttribute("item", new Notice());
			boardForm.setSubject(boardForm.getSubject());
			boardForm.setContents(con);

			return board_form;
		}
		SiteUser author = this.adminUserService.getItem(principal.getName());

		if (cid == 0) {
			cid = adminCategoryService.getAllByName().get(0).getId();
		}

		this.adminBoardService.create(boardForm.getSubject(), con, author, cid, boardForm.getFileids());
		return String.format(redirect_list, cid);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String modify(Model model, BoardForm boardForm, @PathVariable("id") Integer id, Principal principal) {

		model.addAttribute("mode", "modify");
		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "수정권한이 없습니다.");
		}
		boardForm.setSubject(item.getSubject());
		boardForm.setContents(item.getContents());

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
			model.addAttribute("message", "수정권한이 없습니다.");
		}

		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);
		this.adminBoardService.modify(item, boardForm.getSubject(), con, boardForm.getFileids());
		return String.format("redirect:/admin/board/detail/%s", id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Principal principal, @PathVariable("id") Integer id) {

		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
//			model.addAttribute("message", "수정권한이 없습니다.");
		}
//		this.boardService.delete(item);
		return "redirect:/admin/board/list";
	}

}
