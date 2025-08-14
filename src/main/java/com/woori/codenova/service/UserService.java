package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.woori.codenova.InvalidUuidException;
import com.woori.codenova.NonExistentMemberException;
import com.woori.codenova.ApiTest.ApiProps;
import com.woori.codenova.ApiTest.RedisService;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.UserRepository;

//import com.mysite.sbb.DataNotFoundException;

import lombok.RequiredArgsConstructor;

//@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

	// 객체 자동 주입 (만들지 않아도 자동생성)
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 이메일 인증 및 비밀번호 재설정에 사용
	private final ApiProps props;
	private final RedisService redisService;

	// 만들기로 가져옴
	public SiteUser create(String username, String password, String email) {
		// 이메일, 아이디 중복 사용자 확인중
		if (userRepository.findByUsername(username).isPresent()) {
			throw new IllegalArgumentException("이미 사용중인 사용자ID입니다.");
		}
		if (userRepository.findByEmail(email).isPresent()) {
			throw new IllegalArgumentException("이미 사용중인 이메일입니다..");
		}
		SiteUser user = new SiteUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setCareteDate(LocalDateTime.now());
		// user.setPassword(password);
		// 아랫줄에서 불러온것으로 대체됨
		// 주석 처리한건 위에 final로 선언되어 가져온것
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode(password));
		this.userRepository.save(user);
		return user;
	}

	public SiteUser find_id(String email) {
		Optional<SiteUser> siteUserOptional = userRepository.findByEmail(email);
		if (siteUserOptional.isEmpty()) {
			throw new IllegalArgumentException("이메일 주소를 다시 확인해주세요");
		}

		return siteUserOptional.get();
	}

	// 비밀번호 변경 코드 작성중
	public void resetPassword(String uuid, String newPassword) {
		String email = redisService.getValues(uuid);
		if (email == null) {
			throw new InvalidUuidException();
		}
		SiteUser user = userRepository.findByEmail(email).orElseThrow(NonExistentMemberException::new);

		user.updatePassword(passwordEncoder.encode(newPassword));
		// UUId 사용이후 redis에서 삭제하여 재사용을 방지한다.
		redisService.deleteValues(email);
	}

	// 전달받은 이메일로 회원이 존재하는지 확인하는 메서드
	public void checkUserByEmail(String email) {
		// memberRepository를 사용하여 이메일로 회원을 찾습니다.
		// Optional을 반환하는 findByEmail 메서드를 사용해 회원이 없을 경우 예외를 던집니다.
		userRepository.findByEmail(email).orElseThrow(() -> new NonExistentMemberException("존재하지 않는 회원입니다."));
	}
}
