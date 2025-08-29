package com.woori.codenova.ApiTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woori.codenova.service.UserService;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
@RestController
//RestController로 바꿔서 잘못된 html찾으려는거 수정
@RequestMapping("/api")
public class ApiController {

	private final SendMailService mailService;
	private final UserService userService;
	private final KakaoService kakaoService;

	@PostMapping("/send-reset-password")
	public SendResetPasswordEmailRes sendResetPasswrod(
			@Validated @RequestBody SendResetPasswordEmailReq resetPasswordEmailReq) {
		userService.checkUserByEmail(resetPasswordEmailReq.getEmail());
		String uuid = mailService.sendResetPasswordEmail(resetPasswordEmailReq.getEmail());
		return SendResetPasswordEmailRes.builder().uuid(uuid).build();
	}

	@PostMapping("/findIdEmail")
	public SendFindIdEmailRes sendFindEmail(@Validated @RequestBody SendFindIdEmailReq sendFindEmailReq) {
		userService.checkUserByEmail(sendFindEmailReq.getEmail());
		String uuid = mailService.sendFindIdEmail(sendFindEmailReq.getEmail());
//		mailService.EmailSendCountChecker(uuid);
		return SendFindIdEmailRes.builder().uuid(uuid).build();
	}

	// uuid form 재사용 테스트중
	// 회원가입시 이메일 인증 코드
	@PostMapping("/checkEmail")
	public SendFindIdEmailRes sendSingup(@Validated @RequestBody SendFindIdEmailReq sendFindEmailReq) {
		String uuid = mailService.sendSingEmail(sendFindEmailReq.getEmail());
		return SendFindIdEmailRes.builder().uuid(uuid).build();
	}

	@PostMapping("/checkListUuid")
	public ResponseEntity<String> cehckListUuidText(@RequestBody CheckEmailcode checkList, Model model) {
		String uuidtext = checkList.getUuid().toUpperCase();
		String email = checkList.getEmail();
		try {
			userService.CheckEmailCodeText(email, uuidtext);
			// 검증 성공 시
			return ResponseEntity.ok("{\"message1\": \"확인되었습니다.\"}");
		} catch (IllegalArgumentException e) {
			// 검증 실패 시
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message1\": \"" + e.getMessage() + "\"}");
		}
	}

}
