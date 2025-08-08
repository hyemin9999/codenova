package com.woori.codenova.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // final 필드에 대해 생성자 자동 생성 (의존성 주입용)
@Service // 해당 클래스가 Service 계층임을 명시하여 스프링이 관리하게 함
public class CommentService {

	// 댓글 저장을 위한 리포지토리 의존성 주입
	private final CommentRepository commentRepository;

	/**
	 * 댓글 생성 메서드
	 *
	 * @param board   댓글이 달릴 대상 게시글
	 * @param content 사용자로부터 입력받은 댓글 내용
	 */
	public void create(Board board, String content) {
		// 새로운 댓글 엔티티 생성
		Comment comment = new Comment();

		// 댓글 내용 설정
		comment.setContents(content);

		// 댓글 작성 시간 설정
		comment.setCreateDate(LocalDateTime.now());

		// 댓글이 연결될 게시글 설정 (양방향 연관 관계)
		comment.setBoard(board);

		// 댓글 DB에 저장
		this.commentRepository.save(comment);
	}
}
