package com.woori.codenova.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WeatherApiController {
	@GetMapping("/weather")
	public String weather() {
		return "weather";
	}
}
