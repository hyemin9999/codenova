package com.woori.codenova.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.woori.codenova.form.UserCreateForm;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	// 회원가입 링크로 보내버림
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "signup_form";
	}

	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {

		// 유효성 오류 발생시 돌려보냄
		if (bindingResult.hasErrors()) {
			return "signup_form";
		}
		// 비밀번호와 비번확인 검증 시나리오
		if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "signup_form";
		}

		// 서비스 계층 호출하려다남은 잔재
//		userService.createUserTest(userCreateForm.getUserid(), userCreateForm.getEmail());

		try {
			userService.create(userCreateForm.getUsername(), userCreateForm.getPassword1(), userCreateForm.getEmail());
			// 서비스 계층에서 중복사용자 걸러내는 로직 추가해보았음
//	} catch (DataIntegrityViolationException e) {
//			e.printStackTrace();
//			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
//			return "signup_form";
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

}
