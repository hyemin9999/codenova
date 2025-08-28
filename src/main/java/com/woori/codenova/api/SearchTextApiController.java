package com.woori.codenova.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woori.codenova.dto.SearchTextDto;
import com.woori.codenova.service.SearchTextService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SearchTextApiController {

	private final SearchTextService searchTextService;

	@GetMapping("/api/words")
	public List<SearchTextDto> getlist() {

		return searchTextService.getWords();
	}
}
