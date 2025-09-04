package com.woori.codenova.admin.controller;

import java.security.Principal;
import java.util.List;

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
import com.woori.codenova.admin.service.AdminRoleService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Role;
import com.woori.codenova.entity.SiteUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

	private final AdminCategoryService adminCategoryService;
	private final AdminUserService adminUserService;
	private final AdminRoleService adminRoleService;

	private final String category_list = "admin/category";
	private final String redirect_list = "redirect:/admin/category";

	@GetMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult) {

		list(model, page, kw, adminCategoryForm, "list");

		return category_list;
	}

	@GetMapping("/list/{id}")
	@PreAuthorize("isAuthenticated()")
	public String detail(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

		listById(model, page, kw, principal, adminCategoryForm, id, "list");

		Category item = this.adminCategoryService.getitem(id);
		if (item != null) {
			adminCategoryForm.setName(item.getName());
		} else {
			model.addAttribute("message", "존재하지 않는 게시판 입니다.");
		}

		SiteUser user = this.adminUserService.getItem(principal.getName());
		if (user != null && !user.getAuthority().isEmpty()
				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
			model.addAttribute("mode", "modify");
		}

		return category_list;
	}

	@PostMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String create(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult) {

		list(model, page, kw, adminCategoryForm, "create");
		if (bindingResult.hasErrors()) {
			return category_list;
		}

		this.adminCategoryService.create(adminCategoryForm.getName());
		return redirect_list;
	}

	@PostMapping("/list/{id}")
	@PreAuthorize("isAuthenticated()")
	public String modify(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminCategoryForm adminCategoryForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

		listById(model, page, kw, principal, adminCategoryForm, id, "modify");

		if (bindingResult.hasErrors()) {
			return category_list;
		}

		Category item = this.adminCategoryService.getitem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 게시판 입니다.");
		}

		SiteUser user = this.adminUserService.getItem(principal.getName());
		if (user != null && !user.getAuthority().isEmpty()
				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
			model.addAttribute("mode", "modify");
		}

//		Category item = this.categoryService.getitem(id);
//		if (!item.getAuthor().getUsername().equals(principal.getName())) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
//		}

		this.adminCategoryService.modify(item, adminCategoryForm.getName());
		return redirect_list;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Model model, Principal principal, @PathVariable("id") Integer id) {

		Category item = this.adminCategoryService.getitem(id);

		SiteUser user = this.adminUserService.getItem(principal.getName());
		if (user != null && !user.getAuthority().isEmpty()
				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
			model.addAttribute("message", "삭제권한이 없습니다.");
		}

		item.getFavorite().clear();
		List<Role> rlist = adminRoleService.deleteList(item);
		if (rlist != null && !rlist.isEmpty()) {
			for (Role role : rlist) {
				role.getAuthority().clear();
			}
		}

		this.adminCategoryService.delete(item);
		return redirect_list;
	}

	public void list(Model model, Integer page, String kw, AdminCategoryForm adminCategoryForm, String mode) {
		Page<Category> paging = adminCategoryService.getlist(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "create");

	}

	public void listById(Model model, Integer page, String kw, Principal principal, AdminCategoryForm adminCategoryForm,
			Integer id, String mode) {

		Category item = this.adminCategoryService.getitem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 게시판 입니다.");
		}

		Page<Category> paging = adminCategoryService.getlist(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "modify");

		if (mode == "list" && item != null) {
			adminCategoryForm.setName(item.getName());
		}
	}

}
