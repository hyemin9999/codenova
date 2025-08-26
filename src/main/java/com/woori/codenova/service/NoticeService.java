package com.woori.codenova.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.NoticeRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeService {

	private final NoticeRepository noticeRepository;

	// 목록 - 페이징 - 검색
	public Page<Notice> getList(int page, String kw) {

		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));

		// TODO :: 게시판 - 카테고리
		Specification<Notice> spec = search(kw);

		return noticeRepository.findAll(spec, pageable);
	}

	// 조회 - 상세
	public Notice getItem(Integer id) {

		return noticeRepository.findById(id).orElse(null);
	}

	// 조회 - 조회수
	public void setViewCount(Notice item) {

		int viewCount = item.getViewCount();
		item.setViewCount(viewCount + 1);
		noticeRepository.save(item);
	}

	// 등록
	public void create(String subject, String contents, SiteUser uesr) {
		Notice item = new Notice();
		item.setSubject(subject);
		item.setContents(contents);
		item.setCreateDate(LocalDateTime.now());
		item.setAuthor(uesr);
		item.setViewCount(0);

		noticeRepository.save(item);
	}

	// 수정
	public void modify(Notice item, String subject, String content) {
		item.setSubject(subject);
		item.setContents(content);
		item.setModifyDate(LocalDateTime.now());

		// TODO :: 게시판 수정가능여부

		noticeRepository.save(item);
	}

	// 삭제
	public void delete(Notice item) {

		// TODO :: 공지사항 삭제시 연결된 게시글과의 관계 제거 필!!!

		noticeRepository.delete(item);
	}

	// 검색
	private Specification<Notice> search(String kw) {
		return new Specification<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Notice> r, CriteriaQuery<?> q, CriteriaBuilder cb) {

				q.distinct(true); // 중복을 제거

				Join<Notice, SiteUser> u = r.join("author", JoinType.LEFT);// 공지와 작성자

				// TODO:: 제목, 내용, 작성자ID
				return cb.or(cb.like(r.get("subject"), "%" + kw + "%"), cb.like(r.get("contents"), "%" + kw + "%"),
						cb.like(u.get("username"), "%" + kw + "%"));
			}
		};
	}
}
