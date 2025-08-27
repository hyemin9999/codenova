package com.woori.codenova.admin.controller;

import java.security.Principal;
import java.util.ArrayList;
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

import com.woori.codenova.admin.form.AdminRoleForm;
import com.woori.codenova.admin.service.AdminCategoryService;
import com.woori.codenova.admin.service.AdminRoleService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/role")
@RequiredArgsConstructor
public class AdminRoleController {

	private final AdminRoleService adminRoleService;
	private final AdminCategoryService adminCategoryService;
	private final AdminUserService adminUserService;

	@GetMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminRoleForm adminRoleForm,
			BindingResult bindingResult) {

		System.out.println("list role :: GET ::");

		list(model, page, kw, adminRoleForm, "list");

		return "admin/role_list";
	}

	@PostMapping("/list")
	@PreAuthorize("isAuthenticated()")
	public String create(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminRoleForm adminRoleForm,
			BindingResult bindingResult) {

		System.out.println("list role :: POST ::");

		list(model, page, kw, adminRoleForm, "create");

		if (bindingResult.hasErrors()) {
			return "admin/role_list";
		}

		this.adminRoleService.create(adminRoleForm.getName(), 2, adminRoleForm.getSelectedList());
		return "redirect:/admin/role/list";
	}

	@GetMapping("/list/{id}")
	@PreAuthorize("isAuthenticated()")
	public String listById(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminRoleForm adminRoleForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

		System.out.println("list/id role :: GET ::");

		listById(model, page, kw, principal, adminRoleForm, id, "list");

		return "admin/role_list";
	}

	@PostMapping("/list/{id}")
	@PreAuthorize("isAuthenticated()")
	public String modify(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminRoleForm adminRoleForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

		System.out.println("list/id role :: POST ::");

		Role item = this.adminRoleService.getItem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 역할 입니다.");
			bindingResult.reject("존재하지 않는 역할 입니다.");
		}

		listById(model, page, kw, principal, adminRoleForm, id, "modify");

		if (bindingResult.hasErrors()) {
			return "admin/role_list";
		}

//		if (!item.getAuthor().getUsername().equals(principal.getName())) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
//		}

		this.adminRoleService.modify(item, adminRoleForm.getName(), item.getGrade(), adminRoleForm.getSelectedList());
		return "redirect:/admin/role/list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Principal principal, @PathVariable("id") Integer id) {

		Role item = this.adminRoleService.getItem(id);
//		item.getAuthority().clear();

//		if (!item.getAuthor().getUsername().equals(principal.getName())) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
//		}

//		List<SiteUser> ulist = this.adminUserService.getList(id);
//		for (SiteUser siteUser : ulist) {
//			siteUser.getAuthority().remove(item);
//		}

//		this.adminRoleService.delete(item);
		return "redirect:/admin/role/list";
	}

	public void list(Model model, Integer page, String kw, AdminRoleForm adminRoleForm, String mode) {
		Page<Role> paging = adminRoleService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "create");

		List<Category> optionList = adminCategoryService.getlist();
		adminRoleForm.setOptionList(optionList);
	}

	public void listById(Model model, Integer page, String kw, Principal principal, AdminRoleForm adminRoleForm,
			Integer id, String mode) {

		Role item = this.adminRoleService.getItem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 역할 입니다.");
		}

		Page<Role> paging = adminRoleService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "modify");

		if (mode == "list") {
			adminRoleForm.setName(item.getName());
		}

		List<Category> optionList = adminCategoryService.getlist();
		adminRoleForm.setOptionList(optionList);

		if (mode == "list") {

			List<Category> selectedlist = new ArrayList<>(item.getAuthority());
//			if ((selectedlist.size() + 1) == optionList.size()) {
//				adminCategoryService.addAllItem(selectedlist);
//			}

			adminRoleForm.setSelectedList(selectedlist);
		}

//		SiteUser user = this.adminUserService.getItem(principal.getName());
//		if (user != null && !user.getAuthority().isEmpty()
//				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
//			model.addAttribute("mode", "modify");
//		}
	}

}
