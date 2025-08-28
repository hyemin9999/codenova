package com.woori.codenova.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	// username으로 선언해야 로그인 가능 (String userId)하면 실행불가함
	// 또한 여기 정보는 login_form에서 가져오는 거니 참고할것
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<SiteUser> _siteUser = this.userRepository.findByUsername(username);
		if (_siteUser.isEmpty()) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다");
		}

		SiteUser siteUser = _siteUser.get();

		List<GrantedAuthority> authorities = new ArrayList<>();

		if (!siteUser.getAuthority().isEmpty()) {
			if (siteUser.getAuthority().stream().anyMatch(a -> a.getGrade().equals(1))) // 슈퍼관리자
			{
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			} else {
				authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
			}
			System.out.println("siteUser.getAuthority() :: ADMIN");

		} else {
			System.out.println("siteUser.getAuthority() :: USER");
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}
		// 새로운 User권한을 주며 기록함
		return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
	}
}