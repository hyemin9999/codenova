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

import com.woori.codenova.admin.service.AdminNoticeService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.NoticeForm;
import com.woori.codenova.service.UploadFileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {
	private final AdminNoticeService adminNoticeService;
	private final AdminUserService adminUserService;
	private final UploadFileService uploadFileService;

	private final String notice_list = "admin/notice/list";
	private final String notice_detail = "admin/notice/detail";
	private final String notice_form = "admin/notice/form";
	private final String redirect_list = "redirect:/admin/notice/list";
	private final String redirect_detail = "redirect:/admin/notice/detail/%s";

	@GetMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw,
			@RequestParam(value = "field", defaultValue = "all") String field) {

		Page<Notice> paging = adminNoticeService.getList(page, kw, field);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("field", field);

		return notice_list;
	}

	@GetMapping(value = "/detail/{id}")
	@PreAuthorize("isAuthenticated()")
	public String detail(Model model, @PathVariable("id") Integer id) {

		Notice item = this.adminNoticeService.getItem(id);

		if (item != null) {
			this.adminNoticeService.setViewCount(item);
			model.addAttribute("item", item);
		} else {
			model.addAttribute("message", "존재하지 않는 공지사항 입니다.");
		}

		return notice_detail;

	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String create(Model model, NoticeForm noticeForm) {

		model.addAttribute("mode", "create");
		return notice_form;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String create(Model model, @Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal) {

		String con = URLDecoder.decode(noticeForm.getContent(), StandardCharsets.UTF_8);

		model.addAttribute("skin", "common");

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "create");
			model.addAttribute("item", new Notice());
			noticeForm.setSubject(noticeForm.getSubject());
			noticeForm.setContent(con);

			return notice_form;
		}
		SiteUser author = this.adminUserService.getItem(principal.getName());

		this.adminNoticeService.create(noticeForm.getSubject(), con, author, noticeForm.getFileids());
		return redirect_list;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String modify(Model model, NoticeForm noticeForm, @PathVariable("id") Integer id, Principal principal) {

		model.addAttribute("mode", "modify");
		Notice item = this.adminNoticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "수정 권한이 없습니다.");
		}
		noticeForm.setSubject(item.getSubject());
		noticeForm.setContent(item.getContents());

		return notice_form;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String modify(Model model, @Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Integer id) {

		String con = URLDecoder.decode(noticeForm.getContent(), StandardCharsets.UTF_8);

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "mofidy");
			noticeForm.setSubject(noticeForm.getSubject());
			noticeForm.setContent(con);
			return notice_form;
		}
		Notice item = this.adminNoticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "수정 권한이 없습니다.");
			return notice_form;
		}

		this.adminNoticeService.modify(item, noticeForm.getSubject(), con, noticeForm.getFileids());
		return String.format(redirect_detail, id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Model model, Principal principal, @PathVariable("id") Integer id) {

		Notice item = this.adminNoticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "삭제 권한이 없습니다.");
		}

		try {
			this.adminNoticeService.delete(item);
			uploadFileService.deleteList(item.getUploadFile());
		} catch (Exception e) {
			model.addAttribute("message", "삭제에 실패했습니다.");
		}

		return redirect_list;
	}
}
