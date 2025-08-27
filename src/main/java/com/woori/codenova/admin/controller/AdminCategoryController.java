package com.woori.codenova.admin.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.woori.codenova.admin.form.AdminCategoryForm;
import com.woori.codenova.admin.service.AdminCategoryService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

	private final AdminCategoryService adminCategoryService;
	private final AdminUserService adminUserService;

	@GetMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult) {

		Page<Category> paging = adminCategoryService.getlist(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "info");

		return "admin/category_list";
	}

	@GetMapping("/list/{id}")
	@PreAuthorize("isAuthenticated()")
	public String detail(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

		Page<Category> paging = adminCategoryService.getlist(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);

		Category item = this.adminCategoryService.getitem(id);
		if (item != null) {
			adminCategoryForm.setName(item.getName());
		}

		SiteUser user = this.adminUserService.getItem(principal.getName());
		if (user != null && !user.getAuthority().isEmpty()
				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
			model.addAttribute("mode", "modify");
		}

		return "admin/category_list";
	}

	@PostMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String create(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult) {

		Page<Category> paging = adminCategoryService.getlist(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "info");

		if (bindingResult.hasErrors()) {
			return "admin/category_list";
		}

		this.adminCategoryService.create(adminCategoryForm.getName());
		return "redirect:/admin/category/list";
	}

	@PostMapping("/list/{id}")
	@PreAuthorize("isAuthenticated()")
	public String modify(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

		Page<Category> paging = adminCategoryService.getlist(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);

		Category item = this.adminCategoryService.getitem(id);

		if (item != null) {
			adminCategoryForm.setName(item.getName());
		}

		SiteUser user = this.adminUserService.getItem(principal.getName());
		if (user != null && !user.getAuthority().isEmpty()
				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
			model.addAttribute("mode", "modify");
		}

		if (bindingResult.hasErrors()) {
			return "admin/category_list";
		}

//		Category item = this.categoryService.getitem(id);
//		if (!item.getAuthor().getUsername().equals(principal.getName())) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
//		}

		this.adminCategoryService.modify(item, adminCategoryForm.getName());
		return "redirect:/admin/category/list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Principal principal, @PathVariable("id") Integer id) {

		Category item = this.adminCategoryService.getitem(id);
//		if (!item.getAuthor().getUsername().equals(principal.getName())) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
//		}

		// TODO :: 카테고리 삭제시 게시글 (댓글)삭제됨, 검색어도 삭제여부 확인

//		this.categoryService.delete(item);
		return "redirect:/admin/category/list";
	}

}
