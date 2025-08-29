package com.woori.codenova.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.woori.codenova.InvalidUuidException;
import com.woori.codenova.NonExistentMemberException;
import com.woori.codenova.UserFindIdForm;
import com.woori.codenova.ApiTest.KakaoUserInfoResponseDto;
import com.woori.codenova.ApiTest.ResetPasswordReq;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.UserForm;
import com.woori.codenova.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

	private final UserService userService;

	@Value("${kakao.client_id}")
	private String client_id;

	@Value("${google.client_id}")
	private String googleclient_id;

	@Value("${kakao.redirect_uri}")
	private String redirect_uri;

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
	public String login(Model model) {
		String Kakaolocation = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id
				+ "&redirect_uri=" + redirect_uri;

		String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleclient_id
				+ "&redirect_uri=http://localhost:8080/api/v1/oauth2/google&response_type=code&scope=email%20profile%20openid&access_type=offline";
		model.addAttribute("Kakaolocation", Kakaolocation);
		model.addAttribute("Googlelocation", reqUrl);

		return "login_form";
	}

	@GetMapping("/findid")
	public String findId(UserFindIdForm userFindIdForm) { // Model 객체를 매개변수로 추가
		return "find_id";
	}
//	// 아이디 찾기 매핑 연결후 테스트중
//	@GetMapping("/findid")
////	public String findId(Model model) { // Model 객체를 매개변수로 추가
//	public String findId(UserFindIdForm userFindIdForm) { // Model 객체를 매개변수로 추가
////		model.addAttribute("userCreateForm", new UserCreateForm()); // 빈 객체를 모델에 담음
//		return "find_id";
//	}

// ================================
// ========== 오류 페이지 출력 ====

// ==================

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

	// 아이디 찾기 이메일 검증 테스트중 ======================
	// =======================================================
//	@GetMapping("/findid")
//	public String Findid() {
//		return "find_id";
//	}

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
//		
//		model.addAttribute("uuid", uuid);
//		model.addAttribute("resetPasswordReq", new ResetPasswordReq());
//		return "find_id_clear";

	}

	// =================================================================
	// =============비밀번호 테스트중=============

	@GetMapping("/resetpassword")
	public String restepassword() {
		return "resetPasswordForm";
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

	@GetMapping("/login12")
	public String KakaoLoignPage(Model model) {
		String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id
				+ "&redirect_uri=" + redirect_uri;
		model.addAttribute("location", location);
		return "kakaologin";
	}

	@GetMapping("/api/signup")
	public String KakaoSignup(HttpSession session, Model model) {
		KakaoUserInfoResponseDto userInfo = (KakaoUserInfoResponseDto) session.getAttribute("kakaoUserInfo");

		// 사용자가 직접 url 치고오면 돌려보내기
		if (userInfo == null) {
			return "redirect:/user/login";
		}
//		model.addAttribute("userForm", new UserForm());
//		model.addAttribute("email", userInfo.getKakaoAccount().getEmail());

		// 이메일 정보는 세션에서 가져와 모델에 추가
		// 이렇게 하면 GET 요청으로 올 때마다 이메일 값이 유지됩니다.
		model.addAttribute("email", userInfo.getKakaoAccount().getEmail());

		// userForm이 비어있으면 새로 생성 (최초 접근 시)
		if (model.getAttribute("userForm") == null) {
			model.addAttribute("userForm", new UserForm());
		}

		return "kakao_signup_form";
	}

//	@PostMapping("/api/signup")
//	public String KakaosignupClear(@Valid UserForm userForm, BindingResult bindingResult, HttpSession session) {
//
//		// (보안 검증) 폼의 이메일과 세션의 이메일이 일치하는지 확인
//		KakaoUserInfoResponseDto userInfo = (KakaoUserInfoResponseDto) session.getAttribute("kakaoUserInfo");
//		if (userInfo == null || !userInfo.getKakaoAccount().getEmail().equals(userForm.getEmail())) {
//			return "redirect:/user/api/signup?error=invalid_access";
//		}
//		// 유효성 오류 발생시 돌려보냄
//		if (bindingResult.hasErrors()) {
//			return "kakao_signup_form";
//		}
//		// 비밀번호와 비번확인 검증 시나리오
//		if (!userForm.getPassword1().equals(userForm.getPassword2())) {
//			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
//			return "kakao_signup_form";
//		}
//
//		// 서비스 계층 호출하려다남은 잔재
////		userService.createUserTest(userCreateForm.getUserid(), userCreateForm.getEmail());
//
//		try {
//			userService.create(userForm.getUsername(), userForm.getPassword1(), userForm.getEmail(), "kakao", userInfo);
//		} catch (Exception e) {
//			e.printStackTrace();
//			bindingResult.reject("singupFailed", e.getMessage());
//			return "kakao_signup_form";
//
//		}
////		userService.registerNewSocialUser(userForm, userInfo);
//		session.removeAttribute("kakaoUserInfo");
//		// 회원가입 끝나면 메인화면에 연결된 곳으로 보내버림
//		return "redirect:/";
//		// 회원가입 축하페이지 테스트용 코드
////		return "signupsuccess_form";
//	}
}
