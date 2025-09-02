package com.woori.codenova;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.woori.codenova.entity.SiteUser;

public class PrincipalDetails implements UserDetails {

	private SiteUser user;

	public PrincipalDetails(SiteUser user) {
		this.user = user;
	}

	// Thymeleaf 등에서 user 객체를 직접 사용하고 싶을 때를 위한 getter
	public SiteUser getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		// 사용자의 권한을 반환합니다. (예: "ROLE_USER")
		authorities.add(() -> "ROLE_USER");
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	// 계정이 만료되지 않았는지 리턴 (true: 만료안됨)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	// 계정이 잠겨있지 않은지 리턴 (true: 잠기지 않음)
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	// 비밀번호가 만료되지 않았는지 리턴 (true: 만료안됨)
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// 계정이 활성화(사용가능)인지 리턴 (true: 활성화)
	@Override
	public boolean isEnabled() {
		return true;
	}
}