package com.woori.codenova.service;

import org.springframework.stereotype.Service;

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	// 조회 - 상세
	public Category getItem(Integer id) {
		return categoryRepository.findById(id).orElse(null);
	}

	// 즐겨찾기
	public void favorites(Category item, SiteUser siteUser) {
		item.getFavorite().add(siteUser);
		categoryRepository.save(item);
	}
}
