package com.woori.codenova.ApiTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.woori.codenova.dto.GoogleInfResponse;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.entity.SocialUser;
import com.woori.codenova.service.SocialUserService;
import com.woori.codenova.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080") // 뭔지 모름
public class SocialLoginController {

//	@Value("${google.client_id}")
//	private String googleClientId;
//	@Value("${google.client_pw}")
//	private String googleClientPw;
//	@Value("${social.secret-key}")
//	private String socialSecretKey;
//	@Value("${google.redirect.url}")
//	private String googleRedirect;

	private final UserService userService;
	private final KakaoService kakaoService;
	private final SocialUserService socialUserService;
	private final SecurityContextRepository securityContextRepository;
	private final PasswordEncoder passwordEncoder;
	private final SendMailService sendMailService;
	private final GoogleService googleService;

	@RequestMapping(value = "/api/v1/oauth2/google", method = RequestMethod.POST)
	@ResponseBody
	public String loginUrlGoogle() {
		return googleService.loginUrlGoogle();
	}

//	@RequestMapping(value = "/api/v1/oauth2/google", method = RequestMethod.GET)
	@GetMapping("/api/v1/oauth2/google")
	public RedirectView loginGoogle(@RequestParam(value = "code") String authCode, HttpSession session,
			HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {

		// 1. code 파라미터가 없는 비정상적인 접근인지 확인
		if (authCode == null || authCode.isEmpty()) {
			log.warn("인증 코드 없이 /callback에 직접 접근 시도됨.");
			return new RedirectView("/"); // 코드가 없으면 메인 페이지로 리다이렉트
		}

		GoogleInfResponse userInfo = googleService.UserInfo(authCode);
		String provider = "google";
		String providerId = userInfo.getSub(); // 구글의 고유 식별 코드는 'sub' 입니다.
		String email = userInfo.getEmail();
		String nickname = userInfo.getName();

		log.info("Email: {}", email);
		log.info("User Unique ID (sub): {}", providerId);
		log.info("Name: {}", nickname);

		// 추출한 정보로 UserInfoResponse 객체를 생성합니다.
//		GoogleUserinfoResponse userInfoResponse = new GoogleUserinfoResponse(email, sub, name);

		// 생성한 객체를 ResponseEntity에 담아 클라이언트에게 반환합니다.
		// HTTP 상태 코드 200 (OK)와 함께 JSON 형태로 응답됩니다.
		return processSocialLogin(provider, providerId, email, nickname, session, request, response,
				redirectAttributes);
	}

	@GetMapping("/callback")
	public RedirectView callback(@RequestParam("code") String authCode, HttpSession session,
			RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
//		String accessToken = kakaoService.getAccessTokenFromKakao(authCode);
//		KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
		KakaoUserInfoResponseDto userInfo = kakaoService.getaccessToken(authCode);

		// 1. code 파라미터가 없는 비정상적인 접근인지 확인
		if (authCode == null || authCode.isEmpty()) {
			log.warn("인증 코드 없이 /callback에 직접 접근 시도됨.");
			return new RedirectView("/"); // 코드가 없으면 메인 페이지로 리다이렉트
		}

		String provider = "kakao";
		String providerId = userInfo.getId().toString();
		String email = userInfo.getKakaoAccount().getEmail();
		String nickname = userInfo.getKakaoAccount().getProfile().getNickName();
		return processSocialLogin(provider, providerId, email, nickname, session, request, response,
				redirectAttributes);

	}

//	@GetMapping("/callback")
	private RedirectView processSocialLogin(String provider, String providerId, String email, String nickname,
			HttpSession session, HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttributes) {
		log.info("[{}] 로그인 처리 시작. Provider ID: {}", provider.toUpperCase(), providerId);

// 1. DB에서 providerId로 사용자 조회
		Optional<SocialUser> userOptional = socialUserService.findByProviderId(providerId);

		if (userOptional.isPresent()) {
// CASE A: 이미 가입한 사용자인 경우
			SocialUser socialUser = userOptional.get();
			SiteUser linkedSiteUser = socialUser.getSiteUser();

			if (linkedSiteUser != null) {
				log.info("[{} LOGIN] 기존 회원 로그인: {}", provider.toUpperCase(), linkedSiteUser.getUsername());
				performLogin(linkedSiteUser, session, request, response);
				return new RedirectView("/");
			} else {
// CASE B: 소셜 계정은 있으나 연동된 로컬 계정이 없는 경우
				Optional<SiteUser> userByEmailOptional = userService.findByEmail(socialUser.getEmail());
				if (userByEmailOptional.isPresent()) {
					log.info("[{} LOGIN] 연동되지 않은 소셜 계정. 연동 확인 페이지로 리다이렉트합니다. Email: {}", provider.toUpperCase(),
							socialUser.getEmail());
					redirectAttributes.addFlashAttribute("provider", provider);
					redirectAttributes.addFlashAttribute("providerId", providerId);
					redirectAttributes.addFlashAttribute("email", socialUser.getEmail());
					return new RedirectView("/user/Check/integration");
				} else {
					log.warn("[{} LOGIN] Orphan SocialUser 발견. 신규 가입으로 처리합니다.", provider.toUpperCase());
					SiteUser newSiteUser = userService.createAndSaveSocialUser(nickname, email);
					socialUser.setSiteUser(newSiteUser);
					socialUserService.update(socialUser);
					performLogin(newSiteUser, session, request, response);
					return new RedirectView("/");
				}
			}
		} else {
// CASE C: 신규 소셜 로그인 사용자
			Optional<SiteUser> userByEmailOptional = userService.findByEmail(email);
			if (userByEmailOptional.isPresent()) {
// CASE C-1: 이메일이 같은 로컬 회원이 이미 존재 -> 소셜 계정 생성 후 연동
				SiteUser existingSiteUser = userByEmailOptional.get();
				return createNewSocialUserAndLogin(provider, providerId, email, nickname, session, request, response,
						existingSiteUser);
			} else {
// CASE C-2: 완전 신규 회원 -> 로컬 계정과 소셜 계정 모두 생성 후 연동
				log.info("[{} LOGIN] New user. Redirecting to social signup page.", provider.toUpperCase());
				return createNewUserAndLogin(provider, providerId, email, nickname, session, request, response);
			}
		}
	}

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

		// SecurityContext에 인증된 사용자 정보를 저장
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

	private RedirectView createNewSocialUserAndLogin(String provider, String providerId, String email, String nickname,
			HttpSession session, HttpServletRequest request, HttpServletResponse response, SiteUser existingSiteUser) {

		// 2. 신규 SocialUser 생성 및 저장
		SocialUser newSocialUser = new SocialUser();
		newSocialUser.setNickname(nickname);
		newSocialUser.setEmail(email);
		newSocialUser.setProvider(provider);
		newSocialUser.setProviderId(providerId);

		// ★★★ 중요: SiteUser와 SocialUser를 서로 연결
		newSocialUser.setSiteUser(existingSiteUser);

		socialUserService.create(newSocialUser); // 올바른 객체(newSocialUser)를 저장
		log.info("[API NEW SIGNUP] 신규 SocialUser 저장 완료: {}", email);

		// 3. 생성된 계정으로 즉시 로그인 처리
		performLogin(existingSiteUser, session, request, response);

		return new RedirectView("/"); // 회원가입 후 메인 페이지로 이동
	}

	private RedirectView createNewUserAndLogin(String provider, String providerId, String email, String nickname,
			HttpSession session, HttpServletRequest request, HttpServletResponse response) {

		// 1. 신규 SiteUser 생성 및 저장
		SiteUser newSiteUser = new SiteUser();
		newSiteUser.setUsername(nickname); // 임시로 닉네임을 아이디로 사용. 정책에 따라 변경 가능
		newSiteUser.setEmail(email);
		String passwordkey = (sendMailService.createCode());
		newSiteUser.setPassword(passwordEncoder.encode(passwordkey));
		// 소셜 로그인은 별도 비밀번호가 없으므로, socialCreate 같은 전용 메서드를 사용하는 것이 좋습니다.
		// 이 메서드 내부에서는 임의의 값으로 비밀번호를 설정해야 Spring Security가 정상 작동합니다.
		userService.Socialcreate(newSiteUser);
		log.info("[API NEW SIGNUP] 신규 SiteUser 자동 가입 완료: {}", newSiteUser.getUsername());

		// 2. 신규 SocialUser 생성 및 저장
		SocialUser newSocialUser = new SocialUser();
		newSocialUser.setNickname(nickname);
		newSocialUser.setEmail(email);
		newSocialUser.setProvider(provider);
		newSocialUser.setProviderId(providerId);

		// ★★★ 중요: SiteUser와 SocialUser를 서로 연결
		newSocialUser.setSiteUser(newSiteUser);

		socialUserService.create(newSocialUser); // 올바른 객체(newSocialUser)를 저장
		log.info("[API NEW SIGNUP] 신규 SocialUser 저장 완료: {}", email);

		// 3. 생성된 계정으로 즉시 로그인 처리
		performLogin(newSiteUser, session, request, response);

		return new RedirectView("/"); // 회원가입 후 메인 페이지로 이동
	}

	@GetMapping("/user/Check/integration") // 또는 "/user/link-account"
	public String showLinkAccountPage(@ModelAttribute("provider") String provider,
			@ModelAttribute("providerId") String providerId, @ModelAttribute("email") String email, Model model) {

		// 1. 입장권(Flash Attribute) 검사
		// callback을 거치지 않고 직접 URL로 들어오면 email 값은 null이 됩니다.
		if (email == null || email.isEmpty()) {
			log.warn("비정상적인 접근: 계정 연동 페이지 직접 접근 시도");
			return "redirect:/"; // 입장권이 없으면 메인 페이지로 쫓아냄
		}

		// 2. 정상적인 접근인 경우
		// @ModelAttribute 어노테이션 덕분에 Flash Attribute로 받은 값들이
		// 자동으로 Model에 다시 담겨서 Thymeleaf로 전달됩니다.
		// 따라서 model.addAttribute(...)를 또 호출할 필요가 없습니다.

		return "checkIntegration"; // 실제 html 파일 경로
	}

	@PostMapping("/user/link/process")
	public String linkAccountProcess(@RequestParam("provider") String provider,
			@RequestParam("providerId") String providerId, @RequestParam("email") String email,
			HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		// 1. 이메일로 기존 SiteUser 계정을 찾습니다.
		SiteUser siteUser = userService.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("해당 이메일의 사용자를 찾을 수 없습니다."));

		// 2. Provider ID로 SocialUser를 찾습니다.
		SocialUser socialUser = socialUserService.findByProviderId(providerId)
				.orElseThrow(() -> new RuntimeException("소셜 사용자 정보를 찾을 수 없습니다."));

		// 3. ★★★ SocialUser에 SiteUser를 연결(연동)합니다.
		socialUser.setSiteUser(siteUser);
		socialUserService.update(socialUser); // DB에 변경 사항 저장

		log.info("[ACCOUNT LINK] 계정 연동 완료. User: {}, Provider: {}", siteUser.getEmail(), provider);

		// 4. 연동 완료 후, 해당 계정으로 즉시 로그인 처리
		performLogin(siteUser, session, request, response);

		// 5. 메인 페이지로 리다이렉트
		return "redirect:/";
	}

}
