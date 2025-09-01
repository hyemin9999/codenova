package com.woori.codenova;

import java.util.List;

import org.springframework.web.servlet.HandlerInterceptor;

import com.woori.codenova.entity.Category;
import com.woori.codenova.repository.CategoryRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CategoryInterceptor implements HandlerInterceptor {

	private final CategoryRepository categoryRepository;

	public CategoryInterceptor(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	private List<Category> getCategoryList() {
		return categoryRepository.findAllByName();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		List<Category> menuList = getCategoryList();
		request.setAttribute("menus", menuList);

		String url = request.getRequestURI();

		if (url.startsWith("/board/")) {

			String[] str = url.split("/");
			if (str.length > 0) {
				request.setAttribute("cid", str[str.length - 1]);
			}

		}

		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}