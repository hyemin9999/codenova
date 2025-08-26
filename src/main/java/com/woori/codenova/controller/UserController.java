package com.woori.codenova.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.woori.codenova.UserFindIdForm;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.UserForm;
import com.woori.codenova.form.UserModifyForm;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	// 회원가입 링크로 보내버림
	@GetMapping("/signup")
	public String signup(UserForm userForm) {
		return "signup_form";
	}

	// BindingResult는 필드내 모든 클래스 조건을 검색하기에 (id,passwrod,email 전부)
	// email 하나만 찾을려면 새로운 클래스를 만드는 방법 뿐이라 부득이 하게 새로 추가함

	@PostMapping("/signup")
	public String signup(@Valid UserForm userForm, BindingResult bindingResult) {

		// 유효성 오류 발생시 돌려보냄
		if (bindingResult.hasErrors()) {
			return "signup_form";
		}
		// 비밀번호와 비번확인 검증 시나리오
		if (!userForm.getPassword1().equals(userForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "signup_form";
		}

		try {
			userService.create(userForm.getUsername(), userForm.getPassword1(), userForm.getEmail());
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("singupFailed", e.getMessage());
			return "signup_form";

		}
		// 회원가입 끝나면 메인화면에 연결된 곳으로 보내버림
		return "redirect:/";
	}

	// 로그인폼으로 보내버림 -> 로그인 처리는 UserSecurityService 에서 처리함
	@GetMapping("/login")
	public String login() {
		return "login_form";
	}

	@GetMapping("/findid")
	public String findId(UserFindIdForm userFindIdForm) { // Model 객체를 매개변수로 추가
		return "find_id";
	}

	@PostMapping("/findid")
	public String findId1(@Valid UserFindIdForm userFindIdForm, BindingResult bindingResult, Model model) {
		log.info("사용자가 입력한 이메일: {}", userFindIdForm.getEmail());

		if (bindingResult.hasErrors()) {
			log.warn("유효성 검사 실패! 원인: {}", bindingResult.getAllErrors());

			return "login_form";
		}

		try {
			// 1. 서비스에서 반환된 SiteUser 객체를 받습니다.
			SiteUser siteUser = userService.find_id(userFindIdForm.getEmail());

			// 2. SiteUser 객체에서 아이디(username)를 추출합니다.
			String foundUsername = siteUser.getUsername();

			// 3. 추출한 아이디를 모델에 담습니다.
			model.addAttribute("username", foundUsername);

			// 4. 아이디를 보여줄 뷰로 이동합니다.
			return "find_id_clear";

		} catch (IllegalArgumentException e) {
			// 이메일이 없을 경우 서비스에서 던진 예외를 여기서 처리
			bindingResult.reject("emailNotFound", e.getMessage());
			return "main"; // 다시 폼 페이지로 돌아감
		}

	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/info")
	public String detail(Model model, UserModifyForm userModifyForm, Principal principal) {
		model.addAttribute("mode", "info");

		SiteUser item = this.userService.getUser(principal.getName());
		if (item == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		userModifyForm.setId(item.getId());
		userModifyForm.setUsername(item.getUsername());
		userModifyForm.setEmail(item.getEmail());

		return "user_detail";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping(value = "/info")
	public String detail(Model model, @Valid UserModifyForm userModifyForm, BindingResult bindingResult,
			Principal principal) {

		SiteUser item = this.userService.getUser(principal.getName());

		model.addAttribute("mode", "modify");

		userModifyForm.setId(item.getId());
		userModifyForm.setUsername(item.getUsername());
		userModifyForm.setEmail(item.getEmail());

		if (bindingResult.hasErrors()) {

			return "user_detail";
		}

		if (!item.getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		if (!userModifyForm.getPassword1().equals(userModifyForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "변경 비밀번호가 일치하지 않습니다.");
			return "user_detail";
		}

		if (!passwordEncoder.matches(userModifyForm.getPassword(), item.getPassword())) {
			bindingResult.rejectValue("password", "passwordInCorrect", "현재 비밀번호가 일치하지 않습니다.");
			return "user_detail";
		}

		this.userService.modify(item, userModifyForm.getPassword1());
		model.addAttribute("mode", "info");
		return String.format("redirect:/user/info");
	}

}
