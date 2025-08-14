package com.woori.codenova.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.SiteUser;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

	// 아이디 호출 및 유효성 검사를 위해 호출
	Optional<SiteUser> findByUsername(String username);

	// 이메일 호출 및 유효성 검사를 위해 호출
	Optional<SiteUser> findByEmail(String email);

}