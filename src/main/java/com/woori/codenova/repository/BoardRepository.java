package com.woori.codenova.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.woori.codenova.entity.Board;

/**
 * BoardRepository
 *
 * - Spring Data JPA 리포지토리 인터페이스. - JpaRepository<Board, Integer> 상속으로 기본
 * CRUD/페이징/정렬 메서드 자동 제공. · T = Board (엔티티 타입) · ID = Integer (기본 키 타입)
 *
 * ※ 구현체는 런타임에 프록시로 자동 생성됩니다(빈 등록 자동). ※ 메서드 이름 규약(파생 쿼리 메서드)으로 SQL/JPAQL이 자동
 * 파싱됩니다.
 */
public interface BoardRepository extends JpaRepository<Board, Integer> {

	// ---------------------------------------------------------------------
	// 파생 쿼리 메서드 예시
	// ---------------------------------------------------------------------

	// ✅ 제목(subject)으로 게시글 1개 조회
	// - 반환 타입이 Board 이므로 0 or 1개를 기대. 다수면 예외 발생 가능.
	Board findBySubject(String subject);

	// ✅ 제목 + 내용으로 게시글 1개 조회
	// - AND 조건으로 매칭.
	Board findBySubjectAndContents(String subject, String contents);

	// ✅ 제목에 특정 문자열이 포함된 게시글 리스트 조회 (LIKE 검색)
	// - 파라미터에 와일드카드('%공지%')를 직접 전달해야 합니다.
	// 예) findBySubjectLike("%공지%")
	List<Board> findBySubjectLike(String subject);

	// ---------------------------------------------------------------------
	// 페이징/정렬
	// ---------------------------------------------------------------------

	// ✅ 전체 게시글 페이징 조회 (Pageable 객체 전달)
	// - Pageable에 페이지 번호/크기/정렬 옵션을 담아 호출.
	// - 반환 타입 Page<T>: 콘텐츠 목록 + 페이지 메타데이터 포함.
	Page<Board> findAll(Pageable pageable);

	// ✅ Specification과 페이징/정렬을 함께 사용
	// - 동적 조건(Specification<Board>) + Pageable 동시 적용.
	// - 예: 검색 키워드에 따라 조인/like/조건을 동적으로 구성해 전달.
	Page<Board> findAll(Specification<Board> spec, Pageable pageable);
}
