package com.woori.codenova.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.UserRepository;

//import com.mysite.sbb.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	// 객체 자동 주입 (만들지 않아도 자동생성)
	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

//	public UserEntity createUserTest(String userid, String email) {
//		if (userRepository.findByuserid(userid).isPresent()) {
//			throw new IllegalArgumentException("이미 사용중인 사용자ID입니다.");
//		}
//		if (userRepository.findByemail(email).isPresent()) {
//			throw new IllegalArgumentException("이미 사용중인 이메일입니다..");
//		}
////		return null;
//	}

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

//	public SiteUser getUser(String userid) {
//		Optional<SiteUser> User = this.userRepository.findByUserId(userid);
//		if (User.isPresent()) {
//			return User.get();
//		} else {
//			throw new DataNotFoundException("siteuser not found");
//		}
//	}
}
