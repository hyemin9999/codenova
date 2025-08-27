package com.woori.codenova.ApiTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.UserForm;
import com.woori.codenova.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class KakaoController {

	private final UserService userService;
	private final KakaoService kakaoService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;

//	@GetMapping("/callback")
//	public @ResponseBody String kakaoCallback(@RequestParam("code") String code) { // Data를 리턴해주는 컨트롤러 함수, 쿼리스트링에 있는
//																					// code값을 받을 수 있음
//		return "카카오 인증 완료" + code;
//	}
	@Value("${social.secret-key}")
	private String socialSecretKey;

	@GetMapping("/callback")
	public RedirectView callback(@RequestParam("code") String code, HttpSession session,
			RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
		String accessToken = kakaoService.getAccessTokenFromKakao(code);
		KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);

		String provider = "kakao";
		String providerId = userInfo.getId().toString();

		// 1. DB에서 사용자 조회
		Optional<SiteUser> userOptional = userService.findByProviderAndProviderId(provider, providerId);

		// 2. 분기 처리
		if (userOptional.isPresent()) {
			// CASE A: 이미 가입한 사용자인 경우
			SiteUser siteUser = userOptional.get();
			log.info("[KAKAO LOGIN] 기존 회원 로그인: {}", siteUser.getUsername());

			// ** 여기에 실제 로그인 처리 로직 구현 (예: Spring Security 세션 생성, JWT 발급) **
			// securityContextHolder.getContext().setAuthentication(...);

//			// 1. Spring Security용 인증 토큰 생성
//			Authentication authentication = new UsernamePasswordAuthenticationToken(
//					// Principal 객체 (일반적으로 UserDetails 구현체 또는 사용자 ID)
//					siteUser.getUsername(),
//					// Credentials (비밀번호, 소셜 로그인은 보통 null)
//					null,
//					// Authorities (권한 목록)
//					Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 또는 siteUser.getAuthority()에서
//																						// 가져옴
//			);
//
//			// 2. SecurityContext에 인증 정보 등록
//			SecurityContextHolder.getContext().setAuthentication(authentication);
//
//			// 3. 세션에 사용자 정보 저장 (선택적)
//			session.setAttribute("loginUser", siteUser); // 예시

			performLogin(siteUser, session, request, response);

			return new RedirectView("/user/findid"); // 메인 페이지로 리다이렉트

		} else {
			// CASE B: 식별코드가 신규이며 이메일 검증 실시
			String email = userInfo.getKakaoAccount().getEmail();
			Optional<SiteUser> userByEmailOptional = userService.findByEmail(email);

			if (userByEmailOptional.isPresent()) {
				// ✅ Case B-1: 이메일은 존재 -> 계정 자동 연동 및 로그인
				SiteUser existingUser = userByEmailOptional.get();
				log.info("[KAKAO LOGIN] 기존 계정({})에 카카오 계정({}) 연동 및 로그인", existingUser.getUsername(), providerId);

				// 1. 서비스 호출하여 계정 연동 (provider 정보 업데이트)
				userService.linkSocialAccount(existingUser, provider, providerId);

				// 2. 즉시 로그인 처리
				performLogin(existingUser, session, request, response);

				return new RedirectView("/user/resetpassword");
			} else {
				// Case B-2: 정말 완전 신규 회원 -> 소셜 회원가입 페이지로 이동
				log.info("[KAKAO LOGIN] New user. Redirecting to social signup page.");
				session.setAttribute("kakaoUserInfo", userInfo);
				return new RedirectView("/user/api/signup");
			}
		}
	}

//	private void performLogin(SiteUser user, HttpSession session) {
//		// 1. Spring Security용 인증 토큰 생성
//		Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
//				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // 실제 권한에 맞게 수정
//		);
//
//		// 2. SecurityContext에 인증 정보 등록
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//
//		// 3. 세션에 사용자 정보 저장 (선택적)
//		session.setAttribute("loginUser", user);
//	}

	private void performLogin(SiteUser user, HttpSession session, HttpServletRequest request,
			HttpServletResponse response) {
		// 1. 사용자 정보를 기반으로 인증 토큰(Authentication) 생성
		log.info("✅ 1. performLogin 메소드 시작: 사용자명 = {}", user.getUsername());
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		UserDetails userDetails = new User(user.getUsername(), "", authorities);

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());
//				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

		// 2. AuthenticationManager에게 인증을 위임
		// - 이 과정에서 UserDetailsService의 loadUserByUsername이 내부적으로 실행되어
		// 사용자 정보를 확인하고, 성공 시 완전한 Authentication 객체를 반환합니다.
//		Authentication authentication = authenticationManager.authenticate(authenticationToken);

		// 3. SecurityContext에 인증된 사용자 정보를 저장
		// - 이 과정을 통해 사용자는 "공식적으로" 로그인 상태가 되며, 세션이 유지됩니다.
		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("✅ 2. SecurityContextHolder에 인증 정보 저장 완료.");

		Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
		if (authenticatedUser != null && authenticatedUser.isAuthenticated()) {
			log.info("✅ 3. 확인: SecurityContext에 사용자가 인증된 상태로 저장되었습니다. Principal: {}", authenticatedUser.getPrincipal());
			SecurityContext context = SecurityContextHolder.getContext();
			securityContextRepository.saveContext(context, request, response);
			log.info("✅ 4. SecurityContext를 HttpSession에 강제 저장 완료.");
		} else {
			log.error("❌ 3. 확인: SecurityContext에 인증 정보가 제대로 저장되지 않았습니다!");
		}
	}

	// session.setAttribute("loginUser", user); // 이 줄은 선택사항이며, 굳이 필요 없습니다.

	@PostMapping("/kakao/signup")
	public String KakaosignupClear(@Valid UserForm userForm, BindingResult bindingResult, HttpSession session,
			HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes,
			Model model) {

		// (보안 검증) 폼의 이메일과 세션의 이메일이 일치하는지 확인
		KakaoUserInfoResponseDto userInfo = (KakaoUserInfoResponseDto) session.getAttribute("kakaoUserInfo");
//		if (userInfo == null || !userInfo.getKakaoAccount().getEmail().equals(userForm.getEmail())) {
		if (userInfo == null) {
			return "redirect:/user/api/signup?error=invalid_access";
		}
		// 유효성 오류 발생시 돌려보냄
		if (bindingResult.hasErrors()) {
//			redirectAttributes.addFlashAttribute("email", userForm.getEmail());
//
//			// 유효성 검사 오류 정보도 함께 전달
//			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm",
//					bindingResult);

			// 오류가 발생했으므로 리다이렉트를 통해 다시 가입 페이지로 돌아감
			model.addAttribute("userForm", userForm);
//			return "redirect:/user/api/signup";
			return "kakao_signup_form";
		}
		if (!userInfo.getKakaoAccount().getEmail().equals(userForm.getEmail())) {
			// 이메일 불일치 시 오류로 처리
			return "redirect:/user/api/signup?error=invalid_access";
		}
		// 비밀번호와 비번확인 검증 시나리오
		if (!userForm.getPassword1().equals(userForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "kakao_signup_form";
		}

		// 서비스 계층 호출하려다남은 잔재
//		userService.createUserTest(userCreateForm.getUserid(), userCreateForm.getEmail());

		try {
			userService.create(userForm.getUsername(), userForm.getPassword1(), userForm.getEmail(), "kakao", userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("singupFailed", e.getMessage());
			return "kakao_signup_form";

		}
//		userService.registerNewSocialUser(userForm, userInfo);
		SiteUser newUser = userService.findByEmail(userForm.getEmail()).orElse(null);

		if (newUser != null) {
			// 회원가입 성공 후 즉시 로그인 처리
			performLogin(newUser, session, request, response);
		}
		session.removeAttribute("kakaoUserInfo");
		// 회원가입 끝나면 메인화면에 연결된 곳으로 보내버림
		return "redirect:/";
		// 회원가입 축하페이지 테스트용 코드
//		return "signupsuccess_form";
	}
}

//}