package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.woori.codenova.entity.Board;
import com.woori.codenova.repository.BoardRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // final 필드를 자동으로 생성자 주입
@Service // 이 클래스가 서비스 컴포넌트임을 Spring에 알림
public class BoardService {

	// 의존성 주입: 게시글 DB 접근을 위한 리포지토리
	private final BoardRepository boardRepository;

	// ✅ 게시글 전체 목록 조회 (페이징 없이 전부)
	public List<Board> getList() {
		return this.boardRepository.findAll(); // 모든 게시글 반환
	}

	// ✅ ID로 게시글 조회 (단일)
	public Board getBoard(Integer id) {
		Optional<Board> board = this.boardRepository.findById(id); // ID로 조회
		if (board.isPresent()) {
			return board.get(); // 존재하면 반환
		} else {
			return null; // 없으면 null
		}
	}

	// ✅ 게시글 등록
	public void create(String subject, String contents) {
		Board q = new Board(); // 새 게시글 객체 생성
		q.setSubject(subject); // 제목 설정
		q.setContents(contents); // 내용 설정
		q.setCreateDate(LocalDateTime.now()); // 생성일시 설정
		this.boardRepository.save(q); // 저장
	}

	// ✅ 페이징 처리된 게시글 목록 조회 (page: 0부터 시작)
	public Page<Board> getList(int page) {
		// 정렬 조건: 생성일시 기준 내림차순
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		// 한 페이지에 10개씩, 지정된 페이지와 정렬 조건을 사용
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));

		// 페이지네이션된 결과 반환
		return this.boardRepository.findAll(pageable);
	}
}
