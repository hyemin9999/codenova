package com.woori.codenova.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Board;

/**
 * BoardRepository
 *
 * - JpaRepository<Board, Integer>를 상속받아 기본적인 CRUD, 페이징, 정렬 기능을 자동으로 제공받음 -
 * Board: 대상 엔티티 클래스 - Integer: 엔티티의 기본 키 타입 (id 필드)
 */
public interface BoardRepository extends JpaRepository<Board, Integer> {

	// ✅ 제목(subject)으로 게시글 1개 조회
	Board findBySubject(String subject);

	// ✅ 제목 + 내용으로 게시글 1개 조회
	Board findBySubjectAndContents(String subject, String contents);

	// ✅ 제목에 특정 문자열이 포함된 게시글 리스트 조회 (LIKE 검색)
	// 예: "%공지%" → 제목에 "공지"가 포함된 모든 게시글
	List<Board> findBySubjectLike(String subject);

	// ✅ 전체 게시글 페이징 조회 (Pageable 객체 전달)
	Page<Board> findAll(Pageable pageable);
}
