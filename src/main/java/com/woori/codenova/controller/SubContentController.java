package com.woori.codenova.controller;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class SubContentController {
	
	@GetMapping("/aireview")
	public String aireview() {
		return "aireview";
	}
	
	@GetMapping("/aimbti")
	public String aimbti() {
		return "aimbti";
	}
	
	
}
