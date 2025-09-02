package com.woori.codenova;

import java.util.List;

import org.springframework.web.servlet.HandlerInterceptor;

import com.woori.codenova.entity.Category;
import com.woori.codenova.repository.CategoryRepository;
import com.woori.codenova.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CategoryInterceptor implements HandlerInterceptor {

	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	public CategoryInterceptor(CategoryRepository categoryRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
	}

	private List<Category> getCategoryList() {
		return categoryRepository.findAllByName();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String url = request.getRequestURI();

		List<Category> menuList = getCategoryList();
		request.setAttribute("menus", menuList);

		if (url.startsWith("/board/") || url.startsWith("/admin/board/")) {
			request.setAttribute("type", "board");
			Category item = menuList.get(0);

			String[] str = url.split("/");

			if (str.length > 0) {
				String last = str[str.length - 1];
				if (last.matches("\\d+")) {
					Integer cid = Integer.parseInt(str[str.length - 1]);
					item = menuList.stream().filter(o -> o.getId().equals(cid)).findAny().orElse(item);
				}
			}

			request.setAttribute("cid", item.getId());
			request.setAttribute("menuName", item.getName());
		}

		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}