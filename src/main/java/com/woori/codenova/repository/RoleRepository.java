package com.woori.codenova.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.woori.codenova.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	// 페이징 + 검색
	Page<Role> findAll(Specification<Role> specification, Pageable pageable);

	Optional<Role> findByName(String name);

	Optional<Role> findByGrade(Integer grade);

//	// 회원 관리에서 사용할 역할 목록 반환 처리 - 이름순으로 정렬
//	@Query("SELECT new com.woori.codenova.dto.AdminRoleDto(r.id, r.name, false) FROM Role r "
//			+ "WHERE r.grade != 1 ORDER BY name ASC ")
//	List<AdminRoleDto> findAllByAuthority();

	@Query(value = "SELECT * FROM Role " + "WHERE grade != 1 ORDER BY name ASC ", nativeQuery = true)
	List<Role> findAllByGrade();
}
