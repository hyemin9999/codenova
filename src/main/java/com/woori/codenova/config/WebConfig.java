package com.woori.codenova.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.woori.codenova.CodenovaInterceptor;
import com.woori.codenova.repository.CategoryRepository;
import com.woori.codenova.repository.UserRepository;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private UserRepository userRepository;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(new CodenovaInterceptor(categoryRepository, userRepository))
				.addPathPatterns("/**", "/admin/board/**", "/admin/notice/**", "/user/login", "user/signup")
				.excludePathPatterns("/admin/user/**", "/admin/role/**", "/admin/category/**", "/tui-editor/**",
						"/.well-known/**", "/error/**", "/api/words/**", "/css/**", "/js/**");

	}
}