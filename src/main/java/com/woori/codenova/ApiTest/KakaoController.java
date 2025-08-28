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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
@RequestMapping("")
public class KakaoController {

	private final UserService userService;
	private final KakaoService kakaoService;
	private final SocialUserService socialUserService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final PasswordEncoder passwordEncoder;

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
		String email = userInfo.getKakaoAccount().getEmail();
		String nickname = userInfo.getKakaoAccount().getProfile().getNickName();

		// 1. DB에서 사용자 조회
		Optional<SocialUser> userOptional = socialUserService.findByProviderId(providerId);

//		Optional<SiteUser> userByEmailOptional = userService.findByEmail(socialUser.getEmail());

		// 2. 소셜 회원 등록된거 확인 분기 처리
		if (userOptional.isPresent()) {
			// CASE A: 이미 가입한 사용자인 경우
			SocialUser socialUser = userOptional.get();
			SiteUser linkedSiteUser = socialUser.getSiteUser();// 연동된 siteuser의 정보 조사

			if (linkedSiteUser != null) {
				log.info("[KAKAO LOGIN] 기존 회원 로그인: {}", linkedSiteUser.getUsername());
				performLogin(linkedSiteUser, session, request, response);
				return new RedirectView("/"); // 기존 : 메인페이지 / 확인용 findid

//			performLogin(siteUser, session, request, response);
//			return new RedirectView("/user/findid"); // 메인 페이지로 리다이렉트

			} else {
				// CASE B: 식별코드가 존재하나 연동 계정이 없는 경우 (null값)
				// 기존 SiteUser에 이메일 존재시 로컬회원가입 유저 는 연동 여부 체크
//			String email = userInfo.getKakaoAccount().getEmail();
				Optional<SiteUser> userByEmailOptional = userService.findByEmail(socialUser.getEmail());

				if (userByEmailOptional.isPresent()) {
					// ✅ Case B-1: 이메일은 존재 -> 계정 자동 연동 및 로그인
					log.info("[KAKAO LOGIN] 연동되지 않은 소셜 계정. 연동 확인 페이지로 리다이렉트합니다. Email: {}", socialUser.getEmail());

//

					redirectAttributes.addFlashAttribute("provider", provider);
					redirectAttributes.addFlashAttribute("providerId", providerId);
					redirectAttributes.addFlashAttribute("email", socialUser.getEmail());
					// 연동페이지를 제작시 임시저장할 용도

					return new RedirectView("/user/Check/integration");
					// 여기에 프론트엔드 연동하는 html이나 링크 여부 코드 적기
				} else {
					// Case B-2: 이메일도 일치하지 않는 예외적 경우 -> 신규 가입 처리 (이 부분을 추가)
					// 연결된 계정이 다른경우엔 새 siteuser 생성후 그곳으로 연동
					log.warn("[KAKAO LOGIN] Orphan SocialUser 발견. 신규 가입으로 처리합니다.");
//					return createNewUserAndLogin(provider, providerId, email, nickname, session, request, response);
					SiteUser newSiteUser = userService.createAndSaveSocialUser(nickname, email); // SiteUser만 생성하는 서비스
																									// 메서드 필요
					socialUser.setSiteUser(newSiteUser);
					socialUserService.update(socialUser); // socialUser 업데이트
					performLogin(newSiteUser, session, request, response);
					return new RedirectView("/");
				}
			}
		} else {
			// 소셜 아이디가 없는 상태일경우 로컬회원정보 검증
			Optional<SiteUser> userByEmailOptional = userService.findByEmail(email);

			// 로컬 회원정보 여부 확인중
			// 존재할시 if문을 타고 가서 소셜id생성수 연동
			if (userByEmailOptional.isPresent()) {
//			Optional<SiteUser> siue = userService.findByEmail(email);
				SiteUser existingSiteUser = userByEmailOptional.get();
				return createNewSocialUserAndLogin(provider, providerId, email, nickname, session, request, response,
						existingSiteUser);
			} else {
				// 위에서 소셜계정 등록(식별코드로 분류) 가 없으면 곳바로 이곳에서 처리
				// 이곳에서 로컬계정 존재여부를 다시 파악후 존재시 다시 연동시도

				// Case C: 정말 완전 신규 회원 -> 소셜 회원가입 페이지로 이동
				log.info("[KAKAO LOGIN] New user. Redirecting to social signup page.");
				return createNewUserAndLogin(provider, providerId, email, nickname, session, request, response);
			}
		}
//		return new RedirectView("/");
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
		log.info("[KAKAO SIGNUP] 신규 SocialUser 저장 완료: {}", email);

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
		// 소셜 로그인은 별도 비밀번호가 없으므로, socialCreate 같은 전용 메서드를 사용하는 것이 좋습니다.
		// 이 메서드 내부에서는 임의의 값으로 비밀번호를 설정해야 Spring Security가 정상 작동합니다.
		userService.Socialcreate(newSiteUser);
		log.info("[KAKAO SIGNUP] 신규 SiteUser 자동 가입 완료: {}", newSiteUser.getUsername());

		// 2. 신규 SocialUser 생성 및 저장
		SocialUser newSocialUser = new SocialUser();
		newSocialUser.setNickname(nickname);
		newSocialUser.setEmail(email);
		newSocialUser.setProvider(provider);
		newSocialUser.setProviderId(providerId);

		// ★★★ 중요: SiteUser와 SocialUser를 서로 연결
		newSocialUser.setSiteUser(newSiteUser);

		socialUserService.create(newSocialUser); // 올바른 객체(newSocialUser)를 저장
		log.info("[KAKAO SIGNUP] 신규 SocialUser 저장 완료: {}", email);

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
