package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.woori.codenova.DataNotFoundException;
import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

/**
 * CommentService
 *
 * - 댓글(Comment) 도메인의 핵심 비즈니스 로직을 담당하는 서비스 계층. - 트랜잭션 경계는 호출부(컨트롤러/상위 서비스)에서
 * 관리하되, 기본 CRUD는 Spring Data JPA 리포지토리를 통해 위임 수행. - 예외/검증 로직을 한 군데로 모아 컨트롤러를
 * 가볍게 유지.
 */
@RequiredArgsConstructor // final 필드에 대해 생성자를 자동 생성(생성자 주입)
@Service // 스프링 컴포넌트 스캔 대상(서비스 빈 등록)
public class CommentService {

	// 댓글 저장/조회/삭제 등을 담당하는 Spring Data JPA 리포지토리
	private final CommentRepository commentRepository;

	/**
	 * 댓글 생성
	 *
	 * @param board   댓글이 달릴 대상 게시글 엔티티(필수)
	 * @param content 댓글 본문(빈 값 불가: 상위 계층에서 @Valid 로 검증)
	 * @param author  댓글 작성자(현재 로그인 사용자)
	 * @return 저장 완료된 댓글 엔티티(영속 상태; PK 포함)
	 *
	 *         동작: - 파라미터로 전달된 board/author 를 연관관계로 설정 - 작성 시각(createDate)을 현재 시각으로
	 *         세팅 - 리포지토리를 통해 INSERT 실행
	 *
	 *         주의: - Board ↔ Comment 양방향 관계에서 FK 소유자는 Comment.board 이므로 여기에서 board 를
	 *         반드시 세팅해야 FK 값이 들어갑니다.
	 */
	public Comment create(Board board, String content, SiteUser author) {
		// 새로운 엔티티 인스턴스 생성(비영속 상태)
		Comment comment = new Comment();

		// 본문/작성시각/연관관계 설정
		comment.setContents(content);
		comment.setCreateDate(LocalDateTime.now());
		comment.setBoard(board);
		comment.setAuthor(author);

		// 영속화(INSERT) 및 영속 엔티티 반환
		this.commentRepository.save(comment);
		return comment;
	}

	/**
	 * 댓글 단건 조회
	 *
	 * @param id 댓글 PK
	 * @return 존재하면 댓글 엔티티, 없으면 예외 발생
	 *
	 *         예외: - 존재하지 않을 경우 DataNotFoundException("answer not found") 발생 →
	 *         컨트롤러에서 처리되어 404 응답 등을 유도할 수 있음.
	 */
	public Comment getComment(Integer id) {
		Optional<Comment> comment = this.commentRepository.findById(id);
		if (comment.isPresent()) {
			return comment.get();
		} else {
			throw new DataNotFoundException("answer not found");
		}
	}

	/**
	 * 댓글 수정
	 *
	 * @param comment 수정 대상(영속 상태가 보장되면 더티 체킹으로도 반영 가능)
	 * @param content 변경할 본문
	 *
	 *                동작: - 내용/수정시각 갱신 후 save 호출(상황에 따라 merge/flush 수행) - 작성자 권한 검증은
	 *                상위 계층(컨트롤러)에서 이미 완료되었다는 전제
	 */
	public void modify(Comment comment, String content) {
		comment.setContents(content);
		comment.setModifyDate(LocalDateTime.now());
		this.commentRepository.save(comment);
	}

	/**
	 * 댓글 삭제
	 *
	 * @param comment 삭제 대상 엔티티
	 *
	 *                동작: - 리포지토리 delete 호출 → DELETE 실행 - FK 제약 관계: · Comment 가 자식,
	 *                Board 가 부모이므로 일반적으로 단독 삭제 가능 · 다대다 추천(Comment_Voter) 조인 테이블은
	 *                JPA가 적절히 정리 권한: - 작성자/관리자 권한 검증은 상위 계층에서 사전 완료
	 */
	public void delete(Comment comment) {
		this.commentRepository.delete(comment);
	}

	/**
	 * 댓글 추천(좋아요)
	 *
	 * @param comment  추천 대상 댓글
	 * @param siteUser 추천을 수행하는 사용자(중복 방지는 Set 특성에 의존)
	 *
	 *                 동작: - Comment.voter(Set<SiteUser>) 에 사용자 추가 - save 호출로 조인
	 *                 테이블(Comment_Voter)에 INSERT 반영 주의: - 동일 사용자의 중복 추천은 Set 이므로
	 *                 자연스럽게 방지됨(이미 존재하면 변화 없음) - 중복 추천 방식을 더 엄격히 하려면 서비스 레벨에서 존재
	 *                 체크/예외 처리 가능
	 */
	public void vote(Comment comment, SiteUser siteUser) {
		boolean removed = comment.getVoter().removeIf(u -> Objects.equals(u.getId(), siteUser.getId()));
		if (!removed) {
			comment.getVoter().add(siteUser);
		}
		this.commentRepository.save(comment);
	}

	public void favorite(Comment comment, SiteUser siteUser) {
		// 이미 내가 추천했으면 제거, 아니면 추가 (ID 기준 비교: equals/hashCode 미구현 대비)
		boolean removed = comment.getFavorite().removeIf(u -> Objects.equals(u.getId(), siteUser.getId()));
		if (!removed) {
			comment.getFavorite().add(siteUser);
		}
		this.commentRepository.save(comment);
	}
}
