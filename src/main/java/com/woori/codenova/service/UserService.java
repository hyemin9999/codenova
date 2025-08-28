package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.woori.codenova.DataNotFoundException;
import com.woori.codenova.NonExistentMemberException;
import com.woori.codenova.ApiTest.ApiProps;
import com.woori.codenova.ApiTest.KakaoUserInfoResponseDto;
import com.woori.codenova.ApiTest.RedisService;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.UserForm;
import com.woori.codenova.repository.UserRepository;

import jakarta.transaction.Transactional;

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

	@Value("${social.secret-key}")
	private String socialSecretKey;

	// 만들기로 가져옴
	public SiteUser create(String username, String password, String email, String provider,
			KakaoUserInfoResponseDto userInfo) {
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
//		user.setProvider(provider);
//		if (!"local".equals(provider)) {
//			user.setProviderId(userInfo.getId().toString());
//		}
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

	// 비밀번호 변경 코드 작성중
	public void resetPassword(String uuid, String newPassword) {
		String email = redisService.getValues(uuid);
		if (email == null) {
//			throw new InvalidUuidException();
			throw new IllegalArgumentException("링크가 유효하지 않거나 만료되었습니다.");
		}
		SiteUser user = userRepository.findByEmail(email).orElseThrow(NonExistentMemberException::new);

//		user.updatePassword(passwordEncoder.encode(newPassword));
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		// UUId 사용이후 redis에서 삭제하여 재사용을 방지한다.
//		redisService.deleteValues(email);
		// 이메일 삭제하면 uuid가 그대로 남아있으니 주의바람
		redisService.deleteValues(uuid);
	}

	// 비밀번호 변경 첫 페이지 넘기기전 유효성 검사
	public void resetFirstPasswordCheck(String uuid) {
		String uuidCk = redisService.getValues(uuid);
		if (uuidCk == null) {
			throw new IllegalArgumentException("만료된 링크입니다. 다시 요청해주세요.");
		}
	}

//uuid 확인 로직 왜 이메일을 반환?
//아이디찾기에 사용중
	//
	public String SendFindIdEmail(String uuid) {
		String email = redisService.getValues(uuid);
		if (email == null) {
			throw new IllegalArgumentException("유효하지 않거나 만료된 링크입니다.");
		}
		redisService.deleteValues(uuid);
		return email;
	}

	// 전달받은 이메일로 회원이 존재하는지 확인하는 메서드
	public void checkUserByEmail(String email) {
		// memberRepository를 사용하여 이메일로 회원을 찾습니다.
		// Optional을 반환하는 findByEmail 메서드를 사용해 회원이 없을 경우 예외를 던집니다.
		userRepository.findByEmail(email).orElseThrow(() -> new NonExistentMemberException("존재하지 않는 회원입니다."));
	}

	public String Email(String email) {
		// 1. userRepository를 통해 이메일로 회원을 찾고, 결과를 Optional 객체로 받습니다.
		Optional<SiteUser> userOptional = userRepository.findByEmail(email);

		// 2. Optional 객체가 비어있지 않은지(회원이 존재하는지) 확인합니다.
		if (userOptional.isPresent()) {
			// 3. 회원이 존재하면 입력받은 이메일을 반환합니다.
			SiteUser user = userOptional.get();
			return user.getUsername();
		} else {
			// 4. 회원이 존재하지 않으면 예외를 던지거나 null을 반환합니다.
			// 예외를 던지는 것이 더 명확한 오류 처리에 도움이 됩니다.
			throw new NonExistentMemberException("존재하지 않는 회원입니다.");
		}
	}

	// 이메일과 UUID를 함께 받아서 검증하는 코드 (회원가입용)
	public void CheckEmailCodeText(String email, String uuid) {
		// Redis에서 해당 이메일 키에 연결된 값을 가져옴
		String storedEmail = redisService.getValues(email);
		// 1. 키가 존재하는지 (만료되지 않았는지) 확인
		if (storedEmail == null) {
			throw new IllegalArgumentException("만료된 링크입니다. 다시 요청해주세요.");
		}

		// 2. 클라이언트가 보낸 UUID와 Redis에 저장된 UUID가 일치하는지 확인
		if (!storedEmail.equals(uuid)) {
			throw new IllegalArgumentException("유효하지 않은 인증 정보입니다.");
		}
		redisService.deleteValues(uuid);
		// 모든 검증 통과 후 로직 진행
	}

	public Optional<SiteUser> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional
	public void linkSocialAccount(SiteUser user, String provider, String providerId) {
//		user.setProvider(provider);
//		user.setProviderId(providerId);
		userRepository.save(user);
	}

	@Transactional
	public SiteUser registerNewSocialUser(UserForm userForm, KakaoUserInfoResponseDto userInfo) {
		SiteUser user = new SiteUser();

		// 1. UserForm에서 정보 설정
//		user.setUsername(userForm.getUsername());
//		user.setPassword(passwordEncoder.encode(userForm.getPassword1())); // 비밀번호 암호화

		// 2. KakaoUserInfoResponseDto에서 정보 설정
//		user.setEmail(userInfo.getKakaoAccount().getEmail());
//		user.setProvider("kakao");
//		user.setProviderId(userInfo.getId().toString());

		// 3. 기타 정보 설정
//		user.setCareteDate(LocalDateTime.now());
		// newUser.setAuthority(...); // 기본 권한 설정

		return userRepository.save(user);
	}

	@Transactional
	public SiteUser Socialcreate(SiteUser siteUser) {
		return userRepository.save(siteUser);
	}

	public SiteUser createAndSaveSocialUser(String nickname, String email) {
		SiteUser newSiteUser = new SiteUser();
		newSiteUser.setUsername(nickname);
		newSiteUser.setEmail(email);
		// socialCreate와 유사하게 비밀번호 등 설정
		return userRepository.save(newSiteUser);
	}

}
