package com.woori.codenova.ApiTest;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SendMailService {

	private final RedisService redisService;

	@Value("${spring.mail.username}")
	private String fromEmail;
	@Value("${props.reset-password-url}")
	private String resetPwUrl;

	@Autowired
	JavaMailSender mailSender;

	public String makeUuid() {
		return UUID.randomUUID().toString();
	}

	@Transactional
	public String sendResetPasswordEmail(String email) {
		String uuid = makeUuid();
		String title = "요청하신 비밀번호 재설정 입니다.";
		String content = "비밀번호 재설정 링크를 드립니다" + "<br><br>" + "아래 링크를 클릭하시면 해당 비밀번호 재설정 페이지로 이동합니다." + "<br>"
				+ "<a href=\"" + resetPwUrl + "/" + uuid + "\">" + resetPwUrl + "/" + uuid + "</a>" + "<br><br>"
				+ "해당 링크는 24시간 동안만 유효하니 참고바랍니다." + "<br>";
		mailSend(email, title, content);
		saveUuidAndEmail(uuid, email);
		return uuid;
	}

	public void mailSend(String toMail, String title, String content) {
		MimeMessage message = mailSender.createMimeMessage();
		// true 매개값?? 을 주면 mulitpart 형식의 메세지 전달이 가능하다?
		// 문자 인코딩 설정도 가능하다는데 잘 모르겠음 따로 찾아봄
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(new InternetAddress(fromEmail, "코드노바"));
			helper.setTo(toMail);
			helper.setSubject(title);
			// ture가 전달되면 > html형식으로 전송? 작성없을시 단순 텍스트로 보낸다고함
			// 다음에 찾아봄
			helper.setText(content, true);
			mailSender.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	// UUID와 Email을 redis에 저장한다고함
	@Transactional
	public void saveUuidAndEmail(String uuid, String email) {
		// 24 시간이라는데 확인요망
		long uuidValidTime = 60 * 60 * 24 * 1000L;
		redisService.setValuesWitchTimeout(uuid, email, uuidValidTime);
	}
}
