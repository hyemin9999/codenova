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
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.entity.UploadFile;
import com.woori.codenova.repository.BoardRepository;
import com.woori.codenova.repository.CategoryRepository;
import com.woori.codenova.repository.UploadFileRepository;

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
	private final UploadFileRepository uploadFileRepository;

	// 목록 - 페이징 - 검색 종류
	public Page<Board> getList(int page, String kw, String field, Integer cid) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));

		if (cid == 0) {
			cid = categoryRepository.findAllByName().get(0).getId();
		}
		Specification<Board> spec = search(kw, field, cid);

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

	// 조회 - 조회수
	public void setViewCount(Board item) {

		int viewCount = item.getViewCount();
		item.setViewCount(viewCount + 1);
		boardRepository.save(item);
	}

	// 등록
	public void create(String subject, String contents, SiteUser uesr, Integer cid, List<Long> fileids) {
		Board item = new Board();
		item.setSubject(subject);
		item.setContents(contents);
		item.setCreateDate(LocalDateTime.now());
		item.setAuthor(uesr);
		item.setViewCount(0);

		Category citem = categoryRepository.findById(cid).orElse(null);
		item.setCategory(citem);

		boardRepository.save(item);
		setFileByBoard(fileids, item);
	}

	// 수정
	public void modify(Board item, String subject, String content, List<Long> fileids) {
		item.setSubject(subject);
		item.setContents(content);
		item.setModifyDate(LocalDateTime.now());

		boardRepository.save(item);
		setFileByBoard(fileids, item);
	}

	// 삭제 - 실제 item 삭제를 안하고, 제목, 작성자, 내용의 데이터를 날림.
	public void delete(Board item) {

		item.getFavorite().clear();
		item.getVoter().clear();

		boardRepository.delete(item);
	}

	public void deleteList(List<Board> list) {
		boardRepository.deleteAll(list);
	}

	public void setFileByBoard(List<Long> fileids, Board item) {
		if (fileids != null && fileids.size() != 0) {
			for (Long fileid : fileids) {
				UploadFile file = uploadFileRepository.findById(fileid).orElse(null);
				if (file != null) {
					file.setBoard(item);
					uploadFileRepository.save(file);
				}
			}
		}
	}

	// 검색
	private Specification<Board> search(String kw, String field, Integer cid) {
		return new Specification<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Board> r, CriteriaQuery<?> q, CriteriaBuilder cb) {

				q.distinct(true); // 중복을 제거

				Join<Board, SiteUser> u1 = r.join("author", JoinType.LEFT);
				Join<Board, Category> ca = r.join("category", JoinType.LEFT);

				Predicate byTitle = cb.like(r.get("subject"), "%" + kw + "%"); // 제목
				Predicate byContent = cb.like(r.get("contents"), "%" + kw + "%"); // 내용
				Predicate byAuthor = cb.like(u1.get("username"), "%" + kw + "%"); // 글쓴이(작성자)

				Predicate category = cb.equal(ca.get("id"), cid);

				switch (field) {
				case "title":
					return byTitle;
				case "content":
					return byContent;
				case "author":
					return byAuthor;
				case "all":
				default:
					return cb.and(cb.or(byTitle, byContent), category);
				}
			}
		};
	}
}
