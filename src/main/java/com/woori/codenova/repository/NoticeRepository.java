package com.woori.codenova.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
	// 페이징 + 검색
	Page<Notice> findAll(Specification<Notice> specification, Pageable pageable);

}
