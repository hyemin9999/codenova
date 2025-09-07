package com.woori.codenova;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.HandlerInterceptor;

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.CategoryRepository;
import com.woori.codenova.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CodenovaInterceptor implements HandlerInterceptor {

	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	public CodenovaInterceptor(CategoryRepository categoryRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
	}

	private List<Category> getCategoryList() {
		return categoryRepository.findAllByName();
	}

	private List<Category> getListByUsername(String username) {
		List<Category> clist = new ArrayList<>();

		SiteUser user = userRepository.findByUsername(username).orElse(null);
		if (user != null) {
			clist = userRepository.findCategoriesByUserId(user.getId());
		}
		return clist;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String url = request.getRequestURI();

		List<Category> menuList = getCategoryList();

		if (menuList != null && !menuList.isEmpty()) {
			if (url.startsWith("/board/") || url.startsWith("/admin/board/")) {
				request.setAttribute("type", "board");

				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null && authentication.getPrincipal() instanceof UserDetails
						&& url.startsWith("/admin/board/")) {
					UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // 로그인한 사용자 정보 가져오기
					if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
						menuList = getListByUsername(userDetails.getUsername());
					}

				}

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

			request.setAttribute("menus", menuList);
		}

		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}