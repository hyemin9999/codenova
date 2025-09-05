package com.woori.codenova.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	// 페이징 + 검색
	Page<Role> findAll(Specification<Role> specification, Pageable pageable);

	Optional<Role> findByName(String name);

	Optional<Role> findByGrade(Integer grade);

	@Query(value = "SELECT * FROM Role " + "WHERE grade != 1 ORDER BY name ASC ", nativeQuery = true)
	List<Role> findAllByGrade();

	List<Role> findAllByAuthority(Category item);
}
