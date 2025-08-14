package com.woori.codenova.ApiTest;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.woori.codenova.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api")
public class ApiController {

	private final SendMailService mailService;
	private final UserService userService;

	@PostMapping("/send-reset-password")
	public SendResetPasswordEmailRes sendResetPasswrod(
			@Validated @RequestBody SendResetPasswordEmailReq resetPasswordEmailReq) {
		userService.checkUserByEmail(resetPasswordEmailReq.getEmail());
		String uuid = mailService.sendResetPasswordEmail(resetPasswordEmailReq.getEmail());
		return SendResetPasswordEmailRes.builder().uuid(uuid).build();
	}
}
