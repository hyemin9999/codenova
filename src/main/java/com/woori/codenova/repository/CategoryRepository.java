package com.woori.codenova.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.woori.codenova.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	Optional<Category> findByname(String name);

	// 페이징 + 검색
	Page<Category> findAll(Specification<Category> specification, Pageable pageable);

//	List<Category> findAll(Specification<Category> specification);

	@Query(value = "SELECT * FROM Category " + "ORDER BY name ASC ", nativeQuery = true)
	List<Category> findAllByName();
}
