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
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.Role;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.BoardRepository;
import com.woori.codenova.repository.CategoryRepository;
import com.woori.codenova.repository.CommentRepository;
import com.woori.codenova.repository.RoleRepository;
import com.woori.codenova.repository.UserRepository;
import com.woori.codenova.service.BoardService;

@SpringBootTest
class CodenovaApplicationTests {

	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardService boardService;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleRepository roleReporitory;
	@Autowired
	private CategoryRepository categoryRepository;

//	@Test
	void passwordChangeTest() {
		SiteUser user = userRepository.findByEmail("user1@test.com").orElse(null);
		user.setPassword(passwordEncoder.encode("!A1234"));
		userRepository.save(user);
	}

//	@Test
	void testJpa_01() {

		Category c2 = categoryRepository.findByname("Java").orElse(null);
		SiteUser u1 = userRepository.findByUsername("user").orElse(null);

		// 질문 저장하기
		Board q1 = new Board();
		q1.setSubject("수정과 삭제 테스트");
		q1.setContents("이 게시글은 수정 시간과 삭제 여부도 테스트합니다.");
		q1.setViewCount(123);
		q1.setCreateDate(LocalDateTime.of(2025, 8, 1, 10, 0));
		q1.setModifyDate(LocalDateTime.of(2025, 8, 3, 15, 30));
		q1.setDelete(true);
		q1.setDeleteDate(LocalDateTime.of(2025, 8, 4, 9, 0));
		q1.setAuthor(u1);

		q1.setCategory(c2);
		boardRepository.save(q1);

		Category c3 = categoryRepository.findByname("SQL").orElse(null);
		Board q2 = new Board();
		q2.setSubject("두 번째 게시글 테스트");
		q2.setContents("이 게시글은 두 번째 테스트입니다. 수정/삭제도 포함됩니다.");
		q2.setViewCount(45);
		q2.setCreateDate(LocalDateTime.of(2025, 8, 2, 14, 0));
		q2.setModifyDate(LocalDateTime.of(2025, 8, 5, 17, 15));
		q2.setDelete(false);
		q2.setDeleteDate(null);
		q2.setCategory(c3);
		q2.setAuthor(u1);

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
		SiteUser u1 = userRepository.findByUsername("admin").orElse(null);

		Category c2 = categoryRepository.findByname("Spring boot").orElse(null);
//		Category c3 = categoryRepository.findByname("SQL").orElse(null);
//		Category c4 = categoryRepository.findByname("Java").orElse(null);
		for (int i = 1; i <= 300; i++) {

			String subject = String.format("테스트 데이터입니다:[%03d]", i);
			String contents = "내용무";
			this.boardService.create(subject, contents, u1, c2.getId(), null);
//			this.boardService.create(subject, contents, u1, c2.getId(), null);
//			this.boardService.create(subject, contents, u1, c4.getId(), null);
		}
	}

//	@Test
	void insertRoles() {
		Role r2 = new Role();
		r2.setName("관리자");
		r2.setGrade(1);
		r2.setCreateDate(LocalDateTime.now());
		roleReporitory.save(r2);
	}

//	@Test
	void insertUsers() {
		SiteUser u1 = new SiteUser();
		u1.setUsername("admin");
		u1.setPassword(passwordEncoder.encode("1234"));
		u1.setEmail("admin@email.com");
		u1.setCreateDate(LocalDateTime.now());
		userRepository.save(u1);

		SiteUser u2 = new SiteUser();
		u2.setUsername("user");
		u2.setPassword(passwordEncoder.encode("1234"));
		u2.setEmail("user@email.com");
		u2.setCreateDate(LocalDateTime.now());
		userRepository.save(u2);
	}

//	@Test
	@Transactional
	@Rollback(value = false)
	void insertUser_Authority() {

		SiteUser u1 = userRepository.findByUsername("admin").orElse(null);
		assertTrue(u1 != null);

		Role r1 = roleReporitory.findByGrade(1).orElse(null);
		assertTrue(r1 != null);

		u1.getAuthority().add(r1);
		userRepository.save(u1);

	}

//	@Test
	void insertCategory() {

		Category c2 = new Category();
		c2.setName("자유");
		c2.setCreateDate(LocalDateTime.now());
		categoryRepository.save(c2);

		Category c3 = new Category();
		c3.setName("Java");
		c3.setCreateDate(LocalDateTime.now());
		categoryRepository.save(c3);

		Category c4 = new Category();
		c4.setName("SQL");
		c4.setCreateDate(LocalDateTime.now());
		categoryRepository.save(c4);

		Category c5 = new Category();
		c5.setName("Thymeleaf");
		c5.setCreateDate(LocalDateTime.now());
		categoryRepository.save(c5);

		Category c6 = new Category();
		c6.setName("Spring boot");
		c6.setCreateDate(LocalDateTime.now());
		categoryRepository.save(c6);

		Category c7 = new Category();
		c7.setName("Python");
		c7.setCreateDate(LocalDateTime.now());
		categoryRepository.save(c7);
	}

}