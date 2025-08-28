package com.woori.codenova.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Optional<Category> findByname(String name);
}
