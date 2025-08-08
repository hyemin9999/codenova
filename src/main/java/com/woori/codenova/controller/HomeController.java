package com.woori.codenova.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Spring MVC 컨트롤러로 등록
public class HomeController {

	// 루트 URL("/") 접속 시 실행되는 메서드
	@GetMapping("/")
	public String root() {
		// "/" 경로로 들어오면 "/board/list"로 리다이렉트
		return "redirect:/board/list";
	}
}
