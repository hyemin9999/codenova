package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.woori.codenova.DataNotFoundException;
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
		user.setCreateDate(LocalDateTime.now());
		// user.setPassword(password);
		// 아랫줄에서 불러온것으로 대체됨
		// 주석 처리한건 위에 final로 선언되어 가져온것
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

	public SiteUser getUser(String username) {
		Optional<SiteUser> User = this.userRepository.findByUsername(username);
		if (User.isPresent()) {
			return User.get();
		} else {
			throw new DataNotFoundException("siteuser not found");
		}
	}

	// 수정 - 비밀번호뿐.
	public void modify(SiteUser item, String password) {

		item.setPassword(passwordEncoder.encode(password));
		item.setModifyDate(LocalDateTime.now());

		userRepository.save(item);
	}
}
