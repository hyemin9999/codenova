package com.woori.codenova.controller;

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

import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.NoticeForm;
import com.woori.codenova.service.NoticeService;
import com.woori.codenova.service.UploadFileService;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;
	private final UserService userService;
	private final UploadFileService uploadFileService;

	private final String notice_list = "notice/list";
	private final String notice_detail = "notice/detail";
	private final String notice_form = "notice/form";
	private final String redirect_list = "redirect:/notice/list";
	private final String redirect_detail = "redirect:/notice/detail/%s";

	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw,
			@RequestParam(value = "field", defaultValue = "all") String field) {

		Page<Notice> paging = noticeService.getList(page, kw, field);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("field", field);

		return notice_list;
	}

	@GetMapping(value = "/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id) {

		Notice item = this.noticeService.getItem(id);

		if (item != null) {
			this.noticeService.setViewCount(item);
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

		String con = URLDecoder.decode(noticeForm.getContents(), StandardCharsets.UTF_8);

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "create");
			model.addAttribute("item", new Notice());
			noticeForm.setContents(con);

			return notice_form;
		}

		SiteUser author = this.userService.getUser(principal.getName());

		this.noticeService.create(noticeForm.getSubject(), con, author, noticeForm.getFileids());
		return redirect_list;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String modify(Model model, NoticeForm noticeForm, @PathVariable("id") Integer id, Principal principal) {

		model.addAttribute("mode", "modify");
		Notice item = this.noticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "수정 권한이 없습니다.");
		}
		noticeForm.setSubject(item.getSubject());
		noticeForm.setContents(item.getContents());

		return notice_form;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String modify(Model model, @Valid NoticeForm noticeForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Integer id) {

		String con = URLDecoder.decode(noticeForm.getContents(), StandardCharsets.UTF_8);

		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "mofidy");
			return notice_form;
		}
		Notice item = this.noticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "수정 권한이 없습니다.");
		}

		this.noticeService.modify(item, noticeForm.getSubject(), con, noticeForm.getFileids());
		return String.format(redirect_detail, id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Model model, Principal principal, @PathVariable("id") Integer id) {

		Notice item = this.noticeService.getItem(id);
		if (!item.getAuthor().getUsername().equals(principal.getName())) {
			model.addAttribute("message", "삭제 권한이 없습니다.");
		}

		try {
			this.noticeService.delete(item);
			uploadFileService.deleteList(item.getUploadFile());
		} catch (Exception e) {
			model.addAttribute("message", "삭제에 실패했습니다.");
		}
		return redirect_list;
	}
}