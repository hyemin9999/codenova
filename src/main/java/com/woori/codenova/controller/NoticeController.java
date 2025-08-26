package com.woori.codenova.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.woori.codenova.entity.Notice;
import com.woori.codenova.service.NoticeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

	private final NoticeService noticeService;

	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {

		Page<Notice> paging = noticeService.getList(page, kw);

		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);

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
}
