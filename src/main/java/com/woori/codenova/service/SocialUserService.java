package com.woori.codenova.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.woori.codenova.entity.SocialUser;
import com.woori.codenova.repository.SocialUserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SocialUserService {

	private final SocialUserRepository socialUserRepository;

	public SocialUser create(SocialUser socialUser) {
		return socialUserRepository.save(socialUser);
	}

	public Optional<SocialUser> findByProviderId(String providerId) {
		return socialUserRepository.findByProviderId(providerId);
	}

	public SocialUser update(SocialUser socialUser) {
		return socialUserRepository.save(socialUser);
	}

//	public void SocialUserFind() {
//	SocialUser socialUser = socialUserRepository.findById(socialUserId)
//            .orElseThrow(() -> new EntityNotFoundException("SocialUser not found"));
//	}
}
