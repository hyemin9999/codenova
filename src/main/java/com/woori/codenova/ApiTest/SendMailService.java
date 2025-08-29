package com.woori.codenova.ApiTest;

import java.io.UnsupportedEncodingException;
import java.util.Random;
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
	@Value("${props.find-id-url}")
	private String findIdUrl;

	@Autowired
	JavaMailSender mailSender;

	public String makeUuid() {
		return UUID.randomUUID().toString();
	}

//임의의 6자리 변수 생성기
	public String createCode() {
		Random random = new Random();
		StringBuilder key = new StringBuilder();

		for (int i = 0; i < 6; i++) { // 인증 코드 6자리
			int index = random.nextInt(2); // 0~1까지 랜덤, 랜덤값으로 switch문 실행

			switch (index) {
			case 0 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
			case 1 -> key.append(random.nextInt(10)); // 숫자
			}
		}
		return key.toString();
	}

	@Transactional
	public String sendResetPasswordEmail(String email) {
		String uuid = makeUuid();
		String title = "요청하신 비밀번호 재설정 입니다.";
		String content = "비밀번호 재설정 링크를 드립니다" + "<br><br>" + "아래 링크를 클릭하시면 해당 비밀번호 재설정 페이지로 이동합니다." + "<br>"
				+ "<a href=\"" + resetPwUrl + "/" + uuid + "\">" + resetPwUrl + "/" + uuid + "</a>" + "<br><br>"
				+ "해당 링크는 5분 동안만 유효하니 참고바랍니다." + "<br>";
		mailSend(email, title, content);
		saveUuidAndEmail(uuid, email);
		return uuid;
	}

	// 아이디 찾기 테스트용 복붙
	@Transactional
	public String sendFindIdEmail(String email) {
		String uuid = makeUuid();
		String title = "요청하신 아이디 찾기 입니다.";
		String content = "아이디 찾기 링크를 드립니다" + "<br><br>" + "아래 링크를 클릭하시면 아이디 찾기 페이지로 이동합니다." + "<br>" + "<a href=\""
				+ findIdUrl + "/" + uuid + "\">" + findIdUrl + "/" + uuid + "</a>" + "<br><br>"
				+ "해당 링크는 5분 동안만 유효하니 참고바랍니다." + "<br>";
		mailSend(email, title, content);
		saveUuidAndEmail(uuid, email);
		return uuid;
	}

	// 테스트중 =====================

	@Transactional
	public String sendSingEmail(String email) {
		String uuid = createCode();
		String title = "요청하신 회원가입 인증번호 입니다.";
		String content = "코드노바 사이트의 회원가입 인증번호입니다." + "<br><br>" + "아래 코드를 회원가입 페이지에 입력해주세요" + "<br>" + uuid + "<br><br>"
				+ "해당 문자는 3분 동안만 유효하니 참고바랍니다." + "<br>";
		mailSend(email, title, content);
		saveUuidAndEmailCheck(uuid, email);
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
		// 60초 * 5 * 1000밀리초? == 총합 5분만 유효
		long uuidValidTime = 60 * 5 * 1000L;
		redisService.setValuesWitchTimeout(uuid, email, uuidValidTime);
	}

	@Transactional
	public void saveUuidAndEmailCheck(String uuid, String email) {
		// 60초 * 3 * 1000밀리초? == 총합 3분만 유효
		long uuidValidTime = 60 * 3 * 1000L;
		redisService.setValuesWitchTimeout(email, uuid, uuidValidTime);
		// 찾는키 , 해당값 , 만료시간 /
	}

//	public void EmailSendCountChecker(String Uuid, String email) {
//		long CheckEmailCount = sendEmailCheckRepository.countByEmailAddress(email);
//		if(CheckEmailCount >= 3) { //일치하는 이메일이 몇개인지 카운트
//			Optional<SendEmail> lastTopEmailSet = sendEmailCheckRepository.findTopByEmailAddressOrderByRequestedAtDesc(email);
//			
//			lastTopEmailSet.set
//			throw new RuntimeException("요청 횟수가 초과되었습니다");
//		}
//				
//		SendEmail sendEmail = new SendEmail();
//		sendEmail.setEmail(email);
//		sendEmail.setSendUuid(Uuid);
//		sendEmail.setSendTime(LocalDateTime.now());
////		sendEmail.setRequestCount(CheckEmailCount+1);
//	

}
