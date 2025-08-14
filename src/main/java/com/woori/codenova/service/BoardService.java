package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.BoardRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // final 필드를 매개변수로 갖는 생성자를 자동 생성(생성자 주입)
@Service // 스프링 빈 등록: 비즈니스 로직 계층(트랜잭션/도메인 로직 위치)
public class BoardService {

	// 의존성 주입: 게시글 저장소 (Spring Data JPA 프록시 구현체가 주입됨)
	private final BoardRepository boardRepository;

	/**
	 * 검색용 Specification 빌더 - 동적 쿼리를 위해 JPA Criteria 를 사용. - 제목/내용/작성자(username)/댓글
	 * 내용/댓글 작성자(username) 중 하나라도 키워드(kw)를 포함하면 매칭되도록 OR 조건을 구성. - LEFT JOIN 으로
	 * 작성자/댓글/댓글작성자 연관을 묶고, 중복 제거를 위해 distinct(true) 지정.
	 */
	private Specification<Board> search(String kw) {
		return new Specification<>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Board> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true); // 조인으로 인한 결과 중복 제거 (SELECT DISTINCT)
				// board.author (질문 작성자)와의 LEFT JOIN
				Join<Board, SiteUser> u1 = q.join("author", JoinType.LEFT);
				// board.commentList (댓글 리스트)와의 LEFT JOIN
				Join<Board, Comment> a = q.join("commentList", JoinType.LEFT);
				// comment.author (댓글 작성자)와의 LEFT JOIN
				Join<Comment, SiteUser> u2 = a.join("author", JoinType.LEFT);

				// OR 조건: 제목/내용/질문작성자/댓글내용/댓글작성자 중 하나라도 like 매칭
				return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
						cb.like(q.get("contents"), "%" + kw + "%"), // 내용
						cb.like(u1.get("username"), "%" + kw + "%"), // 질문 작성자
						cb.like(a.get("contents"), "%" + kw + "%"), // 답변(댓글) 내용
						cb.like(u2.get("username"), "%" + kw + "%") // 답변(댓글) 작성자
				);
			}
		};
	}

	// ✅ 게시글 전체 목록 조회 (페이징 없이 전부 반환)
	public List<Board> getList() {
		return this.boardRepository.findAll(); // 단순 전체 조회
	}

	// ✅ ID로 게시글 단건 조회
	public Board getBoard(Integer id) {
		Optional<Board> board = this.boardRepository.findById(id); // PK 기반 단건 조회
		if (board.isPresent()) {
			return board.get(); // 존재 시 엔티티 반환
		} else {
			return null; // 없으면 null 반환(호출부에서 NPE 주의 필요)
		}
	}

	// ✅ 게시글 생성
	public void create(String subject, String contents, SiteUser user) {
		Board q = new Board(); // 새 엔티티 인스턴스 생성
		q.setSubject(subject); // 제목 설정
		q.setContents(contents); // 내용 설정(마크다운 등)
		q.setCreateDate(LocalDateTime.now()); // 생성 일시 설정
		q.setAuthor(user); // 작성자 설정
		this.boardRepository.save(q); // 영속화(INSERT)
	}

	// ✅ 페이징 처리된 게시글 목록 조회 (page: 0부터 시작)
	public Page<Board> getList(int page, String kw) {
		// 정렬: 생성일시 내림차순
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		// 페이지 요청 객체: 페이지 번호, 페이지 크기, 정렬
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));

		// 검색 스펙 생성(키워드 기반 동적 조건)
		Specification<Board> spec = search(kw);

		// 스펙 + 페이징으로 조회
		return this.boardRepository.findAll(spec, pageable);
	}

	// ✅ 게시글 수정
	public void modify(Board board, String subject, String content) {
		board.setSubject(subject); // 제목 변경
		board.setContents(content); // 내용 변경
		board.setModifyDate(LocalDateTime.now()); // 수정 일시 갱신
		this.boardRepository.save(board); // 더티 체킹 또는 merge 로 UPDATE 반영
	}

	/**
	 * ✅ 게시글 삭제 (연관 데이터 고려) - 트랜잭션 경계 안에서 수행하여 연관 삭제/조인테이블 정리 순서를 보장. - voter(다대다)
	 * 컬렉션은 먼저 clear()로 중간 테이블 연결을 끊어 FK 제약 이슈를 예방(안전빵)한 후 board 삭제를 위임. -
	 * 댓글(Comment)은 Board 엔티티의 연관 설정(cascade = REMOVE, orphanRemoval = true) 또는
	 * DB/FK ON DELETE CASCADE 설정에 의해 자동 제거됨(설정에 따름).
	 */
	@Transactional
	public void delete(Board board) {
		// (선택) 다대다 조인테이블(Board_Voter) 정리: FK 제약 환경에서 안전하게 삭제되도록 선행 처리
		if (board.getVoter() != null)
			board.getVoter().clear();
		// 실제 삭제 위임(연관 전이/고아 제거/DDL 설정에 따라 자식/조인 행이 정리됨)
		boardRepository.delete(board);
	}

	// ✅ 게시글 추천(좋아요) 처리
	public void vote(Board board, SiteUser siteUser) {
		board.getVoter().add(siteUser); // Set<SiteUser> 이므로 동일 사용자 중복 추가 방지
		this.boardRepository.save(board); // 변경 감지로 조인 테이블에 INSERT 반영
	}
}
