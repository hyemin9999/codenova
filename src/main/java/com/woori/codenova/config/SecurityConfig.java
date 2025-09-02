package com.woori.codenova.config;

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

				.requestMatchers(new AntPathRequestMatcher("/admin/user/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/admin/role/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/admin/category/**")).hasRole("ADMIN")
				.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAnyRole("ADMIN", "MANAGER")
				.requestMatchers(new AntPathRequestMatcher("/user/login"),
						new AntPathRequestMatcher("/user/find-password"), new AntPathRequestMatcher("/user/find-id"))
				.anonymous().requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
				.csrf((csrf) -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
				.headers((headers) -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
				.formLogin((formLogin) -> formLogin.loginPage("/user/login").defaultSuccessUrl("/"))
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true))
				.exceptionHandling((exceptionHandling) -> exceptionHandling
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.sendRedirect("/");
						}));

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