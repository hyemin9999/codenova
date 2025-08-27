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

import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.NoticeForm;
import com.woori.codenova.service.NoticeService;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;
	private final UserService userService;

	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw,
			@RequestParam(value = "field", defaultValue = "all") String field) {

		Page<Notice> paging = noticeService.getList(page, kw, field);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("field", field);

		return "notice_list";
	}

	@GetMapping(value = "/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id) {

		Notice item = this.noticeService.getItem(id);

		if (item != null) {
			this.noticeService.setViewCount(item);
			model.addAttribute("item", item);
		}
		return "notice_detail";

	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String create(Model model, NoticeForm noticeForm) {

		model.addAttribute("mode", "create");
		return "notice_form";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String create(Model model, @Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "create");
			return "notice_form";
		}
		SiteUser author = this.userService.getUser(principal.getName());
		String con = URLDecoder.decode(noticeForm.getContent(), StandardCharsets.UTF_8);

		this.noticeService.create(noticeForm.getSubject(), con, author);
		return "redirect:/notice/list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String modify(Model model, NoticeForm noticeForm, @PathVariable("id") Integer id, Principal principal) {

		model.addAttribute("mode", "modify");
		Notice item = this.noticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}
		noticeForm.setSubject(item.getSubject());
		noticeForm.setContent(item.getContents());

		// TODO :: 게시판 수정가능 여부?? - 없으면 좋겠다

		return "notice_form";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String modify(Model model, @Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Integer id) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "mofidy");
			return "notice_form";
		}
		Notice item = this.noticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		// TODO :: 게시판 수정가능 여부?? - 없으면 좋겠다

		String con = URLDecoder.decode(noticeForm.getContent(), StandardCharsets.UTF_8);
		this.noticeService.modify(item, noticeForm.getSubject(), con);
		return String.format("redirect:/notice/detail/%s", id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Principal principal, @PathVariable("id") Integer id) {

		Notice item = this.noticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
		}
		this.noticeService.delete(item);
		return "redirect:/notice/list";
	}
}
