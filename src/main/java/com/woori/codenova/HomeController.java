package com.woori.codenova;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Spring MVC 컨트롤러로 등록
public class HomeController {

	// 루트 URL("/") 접속 시 실행되는 메서드
	@GetMapping("/")
	public String root() {
		// "/" 경로로 들어오면 "/board/list"로 리다이렉트
//		return "redirect:/board/list";
		return "index";
	}

	// 이것이 선언되어야 redirect:/main 이 사용 가능해짐
	@GetMapping("/main")
	public String Main1() {
		return "main";
	}

	// 회원가입 축하 메세지 확인중
	// http://localhost:8080/signupsuccess_form
	@GetMapping("/signupsuccess_form")
	public String signupsuccess() {
		return "signupsuccess_form";
	}

	@GetMapping(value = { "/i", "/index" })
	public String index() {
		return "index";
	}

	@GetMapping(value = { "/notice", "/notice/" })
	public String notice() {
		return "redirect:/notice/list";
	}

	@GetMapping(value = { "/board", "/board/" })
	public String board() {
		return "redirect:/board/list";
	}

	@GetMapping(value = { "/admin/u", "/admin/user", "/admin/user/", })
	public String admin() {
		return "redirect:/admin/user/list";
	}

	@GetMapping(value = { "/admin/r", "/admin/role", "/admin/role/" })
	public String adminRole() {
		return "redirect:/admin/role/list";
	}

	@GetMapping(value = { "/admin", "/admin/", "/admin/n", "/admin/notice", "/admin/notice/" })
	public String adminNotice() {
		return "redirect:/admin/notice/list";
	}

	@GetMapping(value = { "/admin/c", "/admin/category", "/admin/category/" })
	public String adminCategory() {
		return "redirect:/admin/category/list";
	}

	@GetMapping(value = { "/admin/b", "/admin/board", "/admin/board/" })
	public String adminBoard() {
		return "redirect:/admin/board/list";
	}
}
