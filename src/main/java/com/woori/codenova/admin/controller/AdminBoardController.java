package com.woori.codenova.admin.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.woori.codenova.admin.service.AdminBoardService;
import com.woori.codenova.admin.service.AdminCategoryService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.BoardForm;
import com.woori.codenova.form.CommentForm;
import com.woori.codenova.service.UploadFileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {
	private final AdminBoardService adminBoardService;
	private final AdminUserService adminUserService;
	private final UploadFileService uploadFileService;
	private final AdminCategoryService adminCategoryService;

	private final String board_list = "admin/board/list";
	private final String board_detail = "admin/board/detail";
	private final String board_form = "admin/board/form";
	private final String redirect_list = "redirect:/admin/board/list/%s";
	private final String redirect_detail = "redirect:/admin/board/detail/%s";

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
			// 게시판명 보여주기
			Category citem = item.getCategory();
			model.addAttribute("menuName", citem.getName());
			model.addAttribute("cid", citem.getId());

			this.adminBoardService.setViewCount(item);
			model.addAttribute("item", item);
		}

		return board_detail;
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
		model.addAttribute("item", item);
		boardForm.setSubject(item.getSubject());
		boardForm.setContents(item.getContents());

		return board_form;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String modify(Model model, @Valid BoardForm boardForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Integer id) {

		String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "mofidy");
			boardForm.setSubject(boardForm.getSubject());
			boardForm.setContents(con);
			return board_form;
		}
		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "수정권한이 없습니다.");
			return board_form;
		}

		this.adminBoardService.modify(item, boardForm.getSubject(), con, boardForm.getFileids());
		return String.format(redirect_detail, id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Model model, Principal principal, @PathVariable("id") Integer id) {

		Board item = this.adminBoardService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "삭제 권한이 없습니다.");
		}

		String cid = item.getCategory().getId().toString();

		try {
			this.adminBoardService.delete(item);
			uploadFileService.deleteList(item.getUploadFile());
		} catch (Exception e) {
			model.addAttribute("message", "삭제에 실패했습니다.");
		}

		return String.format(redirect_list, cid);
	}

}
