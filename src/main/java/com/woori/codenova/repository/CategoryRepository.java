package com.woori.codenova.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
