package com.woori.codenova;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.BoardRepository;
import com.woori.codenova.repository.CommentRepository;
import com.woori.codenova.repository.UserRepository;
import com.woori.codenova.service.BoardService;

@SpringBootTest
class CodenovaApplicationTests {

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private BoardService boardService;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void passwordChangeTest() {
		SiteUser user = userRepository.findByEmail("user1@test.com").orElse(null);
		user.setPassword(passwordEncoder.encode("!A1234"));
		userRepository.save(user);
	}

//	 @Test
	void testJpa_01() {
		// 질문 저장하기
		Board q1 = new Board();
		q1.setSubject("수정과 삭제 테스트");
		q1.setContents("이 게시글은 수정 시간과 삭제 여부도 테스트합니다.");
		q1.setViewCount(123);
		q1.setCreateDate(LocalDateTime.of(2025, 8, 1, 10, 0));
		q1.setModifyDate(LocalDateTime.of(2025, 8, 3, 15, 30));
		q1.setDelete(true);
		q1.setDeleteDate(LocalDateTime.of(2025, 8, 4, 9, 0));

		boardRepository.save(q1);

		Board q2 = new Board();
		q2.setSubject("두 번째 게시글 테스트");
		q2.setContents("이 게시글은 두 번째 테스트입니다. 수정/삭제도 포함됩니다.");
		q2.setViewCount(45);
		q2.setCreateDate(LocalDateTime.of(2025, 8, 2, 14, 0));
		q2.setModifyDate(LocalDateTime.of(2025, 8, 5, 17, 15));
		q2.setDelete(false);
		q2.setDeleteDate(null);

		boardRepository.save(q2);
	}

//	 @Test
	void testJpa_02() {
		// findAll()
		List<Board> all = this.boardRepository.findAll();
		assertEquals(2, all.size());

		Board q = all.get(0);
		assertEquals("수정과 삭제 테스트", q.getSubject());
	}

// 	 @Test
	void testJpa_03() {
		Optional<Board> op = this.boardRepository.findById(1);
		if (op.isPresent()) {
			Board q = op.get();
			assertEquals("수정과 삭제 테스트", q.getSubject());
		}
	}

//	 @Test
	void testJpa_04() {
		// findBySubject()
		Board q = this.boardRepository.findBySubject("수정과 삭제 테스트");
		assertEquals(1, q.getId());
	}

// 	 @Test
	void testJpa_05() {
		// findBySubjectAndContents()
		Board q = this.boardRepository.findBySubjectAndContents("수정과 삭제 테스트", "이 게시글은 수정 시간과 삭제 여부도 테스트합니다.");
		assertEquals(1, q.getId());
	}

// 	 @Test
	void testJpa_06() {
		// findBySubjectLike()
		List<Board> qList = this.boardRepository.findBySubjectLike("수정%");
		Board q = qList.get(0);
		assertEquals("수정과 삭제 테스트", q.getSubject());
	}

// 	 @Test
	void testJpa_07() {
		// 질문 데이터 수정하기
		Optional<Board> op = this.boardRepository.findById(1);
		assertTrue(op.isPresent());
		Board q = op.get();
		q.setSubject("수정된 제목입니다");
		this.boardRepository.save(q);
	}

// 	 @Test
	void testJpa_08() {
		// 질문 데이터 삭제하기
		assertEquals(2, this.boardRepository.count());
		Optional<Board> op = this.boardRepository.findById(1);
		assertTrue(op.isPresent());
		Board q = op.get();
		this.boardRepository.delete(q);
		assertEquals(1, this.boardRepository.count());
	}

//	 @Test
	void testJpa_09() {
		// 답변 데이터 저장하기 --> 어떤 질문의 답변인지...
		Optional<Board> op = this.boardRepository.findById(2);
		assertTrue(op.isPresent());
		Board q = op.get();

		Comment a = new Comment();
		a.setContents("q에 달린 첫 댓글입니다.");
		a.setCreateDate(LocalDateTime.now());
		a.setModifyDate(null);
		a.setDelete(false);
		a.setDeleteDate(null);
		a.setBoard(q); // 게시글 연결

		commentRepository.save(a);
	}

// 	 @Test
	void testJpa_10() {
		// 답변 데이터 조회 하기
		Optional<Comment> oa = this.commentRepository.findById(1);
		assertTrue(oa.isPresent());
		Comment a = oa.get();
		assertEquals(2, a.getBoard().getId());
	}

//	@Transactional
	// @Test
	void testJpa_11() {
		// transactional 사용
		// 답변 데이터를 통해 질문 데이터 찾기 vs '질문 데이터를 통해 답변 데이터 찾기'
		Optional<Board> op = this.boardRepository.findById(2);
		assertTrue(op.isPresent());
		Board q = op.get();

		List<Comment> commentList = q.getCommentList(); // 여기까지 일반적으로 db 세션이 유지됨. 이후 끈어짐

		assertEquals(1, commentList.size());
		assertEquals("q에 달린 첫 댓글입니다.", commentList.get(0).getContents());
	}

	// 테스트 데이터 300개 생성

	@Test
	void testJpa_12() {
		for (int i = 1; i <= 300; i++) {
			String subject = String.format("테스트 데이터입니다:[%03d]", i);
			String contents = "내용무";
			this.boardService.create(subject, contents, null);
		}
	}
	
}