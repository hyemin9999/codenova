package com.woori.codenova.admin.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.woori.codenova.admin.form.AdminUserForm;
import com.woori.codenova.admin.form.AdminUserModifyForm;
import com.woori.codenova.admin.service.AdminBoardService;
import com.woori.codenova.admin.service.AdminRoleService;
import com.woori.codenova.admin.service.AdminUserService;
import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Role;
import com.woori.codenova.entity.SiteUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService adminUserService; // 사용자
	private final AdminRoleService adminRoleService; // 역할
	private final AdminBoardService adminBoardService; // 게시글
//	private final AdminCommentService adminCommentService;// 댓글

	private final String user_list = "admin/user";
	private final String redirect_list = "redirect:/admin/user";

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminUserForm adminUserForm,
			BindingResult bindingResult) {

		list(model, page, kw, adminUserForm, "list");

		return user_list;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/list")
	public String create(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminUserForm adminUserForm,
			BindingResult bindingResult) {

		list(model, page, kw, adminUserForm, "create");

		if (bindingResult.hasErrors()) {
			return user_list;
		}

		try {
			this.adminUserService.create(adminUserForm.getUsername(), adminUserForm.getPassword1(),
					adminUserForm.getEmail(), adminUserForm.getSelectedList());

			return redirect_list;
		} catch (DataIntegrityViolationException e) {

			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");

			return user_list;
		} catch (Exception e) {

			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());

			return user_list;
		}
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/list/{id}")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, AdminUserModifyForm adminUserModifyForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {

		listById(model, page, kw, principal, adminUserModifyForm, id, "list");

		SiteUser item = this.adminUserService.getItem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 회원 입니다.");
		}

		return user_list;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping(value = "/list/{id}")
	public String modify(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw, @Valid AdminUserModifyForm adminUserModifyForm,
			BindingResult bindingResult, Principal principal, @PathVariable("id") Long id) {

		System.out.println("list/id user :: POST ::");

		SiteUser item = this.adminUserService.getItem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 회원 입니다.");
			bindingResult.reject("존재하지 않는 회원 입니다.");
		}

		listById(model, page, kw, principal, adminUserModifyForm, id, "modify");

		if (bindingResult.hasErrors()) {
			return user_list;
		}

		this.adminUserService.modify(item, adminUserModifyForm.getPassword1(), adminUserModifyForm.getSelectedList());
		return String.format(redirect_list);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String delete(Model model, Principal principal, @PathVariable("id") Long id) {

		SiteUser item = this.adminUserService.getItem(id);
//		if (!item.getAuthor().getUsername().equals(principal.getName())) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
//		}
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 회원 입니다.");
		}

		// 사용자 삭제시

		// 게시글 - 추천/즐겨찾기
		List<Board> blist = adminBoardService.getListByAuthor(item);

		if (blist != null && !blist.isEmpty()) {
			adminBoardService.deleteList(blist);

		}
//		List<Board> bvlist = adminBoardService.getListByVorter(id);
//		List<Board> bflist = adminBoardService.getListByFavorites(id);

		// 댓글 삭제
//		List<Comment> clist = adminCommentService.getListByAuthorId(id);

		// 좋아요 - 게시글, 댓글, 게시판

		// 추천 삭제 - 게시글, 댓글

		// 역할 삭제
		item.getAuthority().clear();
//
//		List<Role> rlist = this.adminRoleService.getList(id);
//		for (SiteUser siteUser : ulist) {
//			siteUser.getAuthority().remove(item);
//		}

//		this.adminUserService.delete(item);
		return "redirect:/admin/user/list";
	}

	public void list(Model model, Integer page, String kw, AdminUserForm adminUserForm, String mode) {

		Page<SiteUser> paging = adminUserService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "create");

		List<Role> optionList = adminRoleService.getlist();
		adminUserForm.setOptionList(optionList);

	}

	public void listById(Model model, Integer page, String kw, Principal principal,
			AdminUserModifyForm adminUserModifyForm, Long id, String mode) {

		SiteUser item = this.adminUserService.getItem(id);
		if (item == null) {
			model.addAttribute("message", "존재하지 않는 회원 입니다.");
		}

		Page<SiteUser> paging = adminUserService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		model.addAttribute("mode", "modify");

		if (mode == "list" && item != null) {
			adminUserModifyForm.setUsername(item.getUsername());
			adminUserModifyForm.setEmail(item.getEmail());
		}
//		SiteUser user = this.adminUserService.getItem(principal.getName());
//		if (user != null && !user.getAuthority().isEmpty()
//				&& user.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) {
//			model.addAttribute("mode", "modify");
//		}
		List<Role> optionList = adminRoleService.getlist();
		adminUserModifyForm.setOptionList(optionList);

		if (mode == "list" && item != null) {
			List<Role> selectedlist = new ArrayList<>(item.getAuthority()); // 사용자에게 할당된 역할목록?
			adminUserModifyForm.setSelectedList(selectedlist);
		}
	}

}
