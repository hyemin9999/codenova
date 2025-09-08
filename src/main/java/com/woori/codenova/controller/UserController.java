package com.woori.codenova.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.woori.codenova.InvalidUuidException;
import com.woori.codenova.NonExistentMemberException;
import com.woori.codenova.dto.ResetPasswordReq;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.UserFindIdForm;
import com.woori.codenova.form.UserForm;
import com.woori.codenova.form.UserModifyForm;
import com.woori.codenova.service.SendMailService;
import com.woori.codenova.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final SendMailService sendMailService;

	@Value("${naver.client_id}")
	private String naverclient_id;
	@Value("${naver.redirect_uri}")
	private String naverRedirect_uri;
	@Value("${kakao.client_id}")
	private String client_id;
	@Value("${kakao.redirect_uri}")
	private String redirect_uri;
	@Value("${google.client_id}")
	private String googleclient_id;

	// 회원가입 링크로 보내버림
	@GetMapping("/signup")
	public String signup(UserForm userForm, Model model, HttpSession session) {
		String stateUuid = sendMailService.makeUuid();
		session.setAttribute("state", stateUuid);

		String Kakaolocation = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id
				+ "&redirect_uri=" + redirect_uri;

		String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleclient_id
				+ "&redirect_uri=http://localhost:8080/api/v1/oauth2/google&response_type=code&scope=email%20profile%20openid&access_type=offline";

		String Naverlocation = "https://nid.naver.com/oauth2.0/authorize?" + "response_type=code&client_id="
				+ naverclient_id + "&state=" + stateUuid + "&redirect_uri=" + naverRedirect_uri;

		model.addAttribute("Kakaolocation", Kakaolocation);
		model.addAttribute("Googlelocation", reqUrl);
		model.addAttribute("Naverlocation", Naverlocation);
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

			userService.create(userForm.getUsername(), userForm.getPassword1(), userForm.getEmail(), "local", null);
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
	public String login(Model model, HttpSession session) {
		String stateUuid = sendMailService.makeUuid();
		session.setAttribute("state", stateUuid);

		String Kakaolocation = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id
				+ "&redirect_uri=" + redirect_uri;

		String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleclient_id
				+ "&redirect_uri=http://localhost:8080/api/v1/oauth2/google&response_type=code&scope=email%20profile%20openid&access_type=offline";

		String Naverlocation = "https://nid.naver.com/oauth2.0/authorize?" + "response_type=code&client_id="
				+ naverclient_id + "&state=" + stateUuid + "&redirect_uri=" + naverRedirect_uri;

		model.addAttribute("Kakaolocation", Kakaolocation);
		model.addAttribute("Googlelocation", reqUrl);
		model.addAttribute("Naverlocation", Naverlocation);

		return "login_form";
	}

	@GetMapping("/find-id")
	public String findId(UserFindIdForm userFindIdForm) { // Model 객체를 매개변수로 추가
		return "find_id";
	}

	// 아이디 찾기 이메일 검증 테스트중 ======================
	// =======================================================

	@GetMapping("/find-id/{uuid}")
	public String FindidClear(@PathVariable("uuid") String uuid, Model model) {
		try {
			String email = userService.SendFindIdEmail(uuid);
			String username = userService.Email(email);
			model.addAttribute("username", username);
			return "find_id_clear";
		} catch (IllegalArgumentException e) {
			// UUID가 유효하지 않거나 만료된 경우
			model.addAttribute("errorMessage", "링크가 유효하지 않거나 만료되었습니다.");
			return "test_check_error";
		} catch (NonExistentMemberException e) {
			// 이메일로 회원을 찾지 못한 경우
			model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
			return "test_check_error";
		}

	}

	// =================================================================
	// =============비밀번호 테스트중=============

	@GetMapping("/find-password")
	public String restepassword() {
		return "find_password";
	}

	@GetMapping("/reset-password/{uuid}")
	public String showResetPasswordForm(@PathVariable("uuid") String uuid, Model model) {
		// UUID는 UserService에서 검증하므로, 여기서는 폼을 보여주기만 합니다.
		// 유효하지 않은 UUID라면 UserService.resetPassword() 호출 시 예외가 발생합니다.
		try {
			userService.resetFirstPasswordCheck(uuid);

			model.addAttribute("uuid", uuid);
			model.addAttribute("resetPasswordReq", new ResetPasswordReq());
			return "resetPassword";

		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
			model.addAttribute("errorMessage", e.getMessage());
			return "test_check_error";
		}
	}

	@PostMapping("/reset-password/setting/{uuid}")
	public String resetPassword(@PathVariable("uuid") String uuid, @Validated ResetPasswordReq resetPasswordReq,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {

		// 1. 유효성 검사 실패 시 처리
		if (bindingResult.hasErrors()) {
			// 오류를 플래시 속성에 담아 리다이렉트
			redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors());
			return "redirect:/user/reset-password/" + uuid;
		}

		// 2. 비밀번호와 비밀번호 확인 일치 여부 확인
		if (!resetPasswordReq.getNewPassword().equals(resetPasswordReq.getNewPasswordConfirm())) {
			bindingResult.rejectValue("newPasswordConfirm", "passwordMismatch", "비밀번호가 일치하지 않습니다.");
			redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors());
			return "redirect:/user/reset-password/" + uuid;
		}

		try {
			// 3. UserService를 통해 비밀번호 재설정 로직 실행
			userService.resetPassword(uuid, resetPasswordReq.getNewPassword());
		} catch (IllegalArgumentException | InvalidUuidException | NonExistentMemberException e) {
			// 4. UUID 또는 회원 정보 오류 발생 시 처리
			bindingResult.reject("resetPasswordError", e.getMessage());
			redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors());
			return "redirect:/user/reset-password/" + uuid;
		}

		// 5. 성공 시 로그인 페이지로 리다이렉트
		redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
		return "redirect:/user/login";
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
