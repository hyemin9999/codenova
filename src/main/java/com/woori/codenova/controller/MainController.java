package com.woori.codenova.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
//메인화면으로 보내버리는 매핑
public class MainController {

	// redirect는 사용자가 페이지가 넘어가는걸 인식가능하기에
	// 기본주소만 치면 자동으로 메인화면으로 보내버림
	// 또한 redirect는 이미 선언된 주소만 뿌릴수 있음
	@GetMapping("/")
	public String Maintest() {
		return "redirect:/main";
	}

}
