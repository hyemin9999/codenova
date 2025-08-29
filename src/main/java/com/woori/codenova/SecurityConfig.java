package com.woori.codenova;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
//클래스 레벨 타입의 시큐리티
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean // 현재 모든 페이지 권한을 설정하였으나 "사용자"에겐 주지 않은 상황
	// 403 error 발생
	// 모든 페이지에 접근 가능
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
				.requestMatchers(new AntPathRequestMatcher("/admin/user/**")).hasAnyRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/admin/role/**")).hasAnyRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/admin/category/**")).hasAnyRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAnyRole("ADMIN", "MANAGER")
				.requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
				// DB나 특정링크 접속권한을 주지만 내부 데이터 접근불가
				// 즉 로그인창만 넘어가며 나머진 거부됨
				.csrf((csrf) -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
				// h2-console을 보안에서 허용한다.
				// 즉 접근경로가 h2-console 뿐인데 그걸로 접속하는걸 허용한다
				.headers((headers) -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
				// 로그인 페이지 -> /user/login , 로그인 성공시 -> / 로 돌아간다
				.formLogin((formLogin) -> formLogin.loginPage("/user/login").defaultSuccessUrl("/"))
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	// 인증과 인가(권한) 부여를 처리함
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}