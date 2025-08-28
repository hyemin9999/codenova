package com.woori.codenova.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.SocialUser;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {

	Optional<SocialUser> findByNickname(String nickname);

	Optional<SocialUser> findByEmail(String email);

//	Optional<SocialUser> findByID(String email);

	Optional<SocialUser> findByProviderId(String providerId);
}
