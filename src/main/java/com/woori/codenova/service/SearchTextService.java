package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.woori.codenova.dto.SearchTextDto;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SearchText;
import com.woori.codenova.repository.SearchTextRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SearchTextService {

	private final SearchTextRepository searchTextRepository;

	public List<SearchText> getList() {

		return searchTextRepository.findAll();
	}

	public List<SearchTextDto> getWords() {

		return searchTextRepository.findSearchTextCountsByText();
	}

	public SearchText create(String text, Category category) {

//		category
		SearchText item = new SearchText();
		item.setText(text);
		item.setSearchDate(LocalDateTime.now());
		item.setCategory(category);

		return searchTextRepository.save(item);
	}

}
