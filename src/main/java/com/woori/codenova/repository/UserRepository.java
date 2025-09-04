package com.woori.codenova.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

	// 아이디 호출 및 유효성 검사를 위해 호출
	Optional<SiteUser> findByUsername(String username);

	// 이메일 호출 및 유효성 검사를 위해 호출
	Optional<SiteUser> findByEmail(String email);

	// provider와 providerId로 사용자를 찾는 메서드
//	Optional<SiteUser> findByProviderAndProviderId(String provider, String providerId);

	// 이메일로 사용자를 찾는 메서드 (계정 연동을 위해)
//	Optional<SiteUser> findByEmail(String email);
	// 페이징 + 검색
	Page<SiteUser> findAll(Specification<SiteUser> specification, Pageable pageable);

	Optional<SiteUser> findByUsernameAndEmail(String username, String email);

	List<SiteUser> findAll(Specification<SiteUser> specification);

	@Query(value = "select distinct c.* from site_user u " + "right join user_authority ua on ua.user_id = u.id "
			+ "right join role_authority ra on ra.role_id = ua.role_id "
			+ "right join category c on c.id = ra.category_id " + "where u.id = :uid ", nativeQuery = true)
	List<Category> findCategoriesByUserId(@Param("uid") Long uid);

}
