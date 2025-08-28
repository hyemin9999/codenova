package com.woori.codenova.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Comment;

/**
 * CommentRepository
 *
 * - Spring Data JPA 리포지토리 인터페이스. - JpaRepository<Comment, Integer>를 상속하여 기본
 * CRUD/페이징/정렬 메서드를 자동 제공합니다. · T = Comment : 관리 대상 엔티티 · ID = Integer : 기본 키 타입
 *
 * 동작 메모 - 구현체는 런타임에 프록시로 자동 생성되어 스프링 빈으로 등록됩니다. - 트랜잭션은 보통 Service
 * 계층(@Transactional)에서 관리합니다. - 기본 제공 메서드 예) save, findById, findAll, delete 등.
 *
 * 확장 포인트(필요 시 추가, 현재는 예시로 주석만 남깁니다) - 파생 쿼리 메서드: 메서드명 규약으로 자동 쿼리 생성 예)
 * List<Comment> findByBoardId(Integer boardId); 예) Page<Comment>
 * findByAuthorUsername(String username, Pageable pageable);
 */
public interface CommentRepository extends JpaRepository<Comment, Integer> {

	// ✅ 필요 시 사용자 정의 쿼리 메서드를 추가할 수 있음
	// 아래는 예시이며, 현재 코드는 변경 없이 주석만 추가합니다.

	// 예시:
	// List<Comment> findByBoardId(Integer boardId);
	// → 특정 게시글에 속한 댓글 목록 조회

	// 예시:
	// long countByBoardId(Integer boardId);
	// → 특정 게시글의 댓글 개수 집계

	// 예시:
	// void deleteByBoard(Board board);
	// → 게시글 단위로 댓글 일괄 삭제(연쇄 삭제를 코드로 처리하고 싶을 때)
	Page<Comment> findByBoard(Board board, Pageable pageable);
}
