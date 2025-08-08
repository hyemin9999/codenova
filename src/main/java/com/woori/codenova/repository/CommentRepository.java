package com.woori.codenova.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Comment;

/**
 * CommentRepository
 *
 * - 댓글(Comment) 엔티티를 처리하는 JPA 리포지토리 - JpaRepository<Comment, Integer>를 상속받아
 * CRUD 기능을 자동으로 제공
 *
 * - Comment: 관리 대상 엔티티 클래스 - Integer: Comment의 기본 키(id)의 타입
 */
public interface CommentRepository extends JpaRepository<Comment, Integer> {

	// ✅ 필요 시 사용자 정의 쿼리 메서드를 추가할 수 있음

	// 예시:
	// List<Comment> findByBoardId(Integer boardId);
	// → 특정 게시글에 속한 댓글 목록 조회
}
