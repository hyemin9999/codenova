package com.woori.codenova.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.service.CategoryService;
import com.woori.codenova.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;
	private final UserService userService;

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/favorites/{id}")
	public String favorites(Principal principal, @PathVariable("id") Integer id) {

		Category item = this.categoryService.getItem(id);
		SiteUser user = this.userService.getUser(principal.getName());
		this.categoryService.favorites(item, user);

		// TODO :: 해당 게시판 페이지

		return String.format("redirect:/board//%s", id);
	}
}