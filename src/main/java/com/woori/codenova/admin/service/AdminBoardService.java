package com.woori.codenova.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.BoardRepository;
import com.woori.codenova.repository.CategoryRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AdminBoardService {

	private final BoardRepository boardRepository;
	private final CategoryRepository categoryRepository;

	// 목록 - 페이징 - 검색
	public Page<Board> getList(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));

		// 게시판 - 카테고리
		Specification<Board> spec = search(kw);

		return boardRepository.findAll(spec, pageable);
	}

	// 조회 - 상세
	public Board getItem(Integer id) {
		return boardRepository.findById(id).orElse(null);
	}

	// 사용자가 작성한 글 목록 반환
	public List<Board> getListByAuthor(SiteUser id) {
		return boardRepository.findByAuthor(id);
	}

//	public List<Board> getListByVorter(Long id) {
//		return boardRepository.getVoterId(id);
//	}
//
//	public List<Board> getListByFavorites(Long id) {
//		return boardRepository.getFavoritesId(id);
//	}

	// 조회 - 조회수
	public void setViewCount(Board item) {

		int viewCount = item.getViewCount();
		item.setViewCount(viewCount + 1);
		boardRepository.save(item);
	}

	// 등록
	public void create(String subject, String contents, SiteUser uesr) {
		Board item = new Board();
		item.setSubject(subject);
		item.setContents(contents);
		item.setCreateDate(LocalDateTime.now());
		item.setAuthor(uesr);
		item.setViewCount(0);
		// TODO :: 게시판 - 카테고리 // 초기 게시판
		Category citem = categoryRepository.findById(1).orElse(null);
		item.setCategory(citem);

		boardRepository.save(item);
	}

	// 수정
	public void modify(Board item, String subject, String content) {
		item.setSubject(subject);
		item.setContents(content);
		item.setModifyDate(LocalDateTime.now());

		// TODO :: 게시판 - 카테고리 수정도 가능하도록
		Category citem = categoryRepository.findById(1).orElse(null);
		item.setCategory(citem);

		boardRepository.save(item);
	}

	// 삭제 - 실제 item 삭제를 안하고, 제목, 작성자, 내용의 데이터를 날림.
	public void delete(Board item) {
//			item.setSubject("");
//			item.setContents("");
//			item.setDelete(true);
//			item.setDeleteDate(LocalDateTime.now());
//			item.setAuthor(null);
		//
//			// TODO :: 게시글 삭제되어도 게시판과의 연결을 어떻게??
//			// TODO :: 게시글 삭제시 파일 삭제
//			boardRepository.save(item);

		// isDelete 값 false인것만 가지고 처리하는게 힘들어서 그냥 연결된거 일단 다 날림^^

		boardRepository.delete(item);
	}

	public void deleteList(List<Board> list) {
		boardRepository.deleteAll(list);
	}

	// 검색
	private Specification<Board> search(String kw) {
		return new Specification<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Board> r, CriteriaQuery<?> q, CriteriaBuilder cb) {

				q.distinct(true); // 중복을 제거

				// TODO :: entity class 수정여부에 따라 바뀌어야 할듯?

				Join<Board, SiteUser> u = r.join("author", JoinType.LEFT);// 게시글과 작성자
				Join<Board, Comment> c = r.join("commentList", JoinType.LEFT);// 게시글과 작성자
				Join<Comment, SiteUser> u1 = c.join("author", JoinType.LEFT);// 댓글과 작성자

				Predicate orP = cb.or(cb.like(r.get("subject"), "%" + kw + "%"), // 게시글 제목
						cb.like(r.get("contents"), "%" + kw + "%"), // 댓글 내용
						cb.like(u.get("username"), "%" + kw + "%"), // 게시글 작성자
						cb.like(c.get("contents"), "%" + kw + "%"), // 댓글 내용
						cb.like(u1.get("username"), "%" + kw + "%")); // 댓글 작성자

				// 제목, 내용, 작성자ID, 댓글(내용, 작성자ID)
				return cb.and(cb.equal(r.get("isDelete"), false), orP); // 삭제여부 false인거중에 검색기능 처리
			}
		};
	}
}
