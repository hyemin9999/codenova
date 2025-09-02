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

import com.woori.codenova.entity.Notice;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.entity.UploadFile;
import com.woori.codenova.repository.CategoryRepository;
import com.woori.codenova.repository.NoticeRepository;
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
public class AdminNoticeService {
	private final NoticeRepository noticeRepository;
	private final CategoryRepository categoryRepository;
	private final UploadFileRepository uploadFileRepository;

	// 목록 - 페이징 - 검색
	public Page<Notice> getList(int page, String kw, String field) {

		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));

		// 매니저가 권한이 있는 공지사항 목록이 보이도록 처리

		Specification<Notice> spec = search(kw, field);

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
	public void create(String subject, String contents, SiteUser uesr, List<Long> fileids) {
		Notice item = new Notice();
		item.setSubject(subject);
		item.setContents(contents);
		item.setCreateDate(LocalDateTime.now());
		item.setAuthor(uesr);
		item.setViewCount(0);

		noticeRepository.save(item);
		setFileByNotice(fileids, item);
	}

	// 수정
	public void modify(Notice item, String subject, String content, List<Long> fileids) {
		item.setSubject(subject);
		item.setContents(content);
		item.setModifyDate(LocalDateTime.now());

		noticeRepository.save(item);
		setFileByNotice(fileids, item);
	}

	// 삭제
	public void delete(Notice item) {

		noticeRepository.delete(item);
	}

	public void setFileByNotice(List<Long> fileids, Notice item) {
		if (fileids != null && fileids.size() != 0) {
			for (Long fileid : fileids) {
				UploadFile file = uploadFileRepository.findById(fileid).orElse(null);
				if (file != null) {
					file.setNotice(item);
					uploadFileRepository.save(file);
				}
			}
		}
	}

	// 검색
	private Specification<Notice> search(String kw, String field) {
		return new Specification<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Notice> r, CriteriaQuery<?> q, CriteriaBuilder cb) {

				q.distinct(true); // 중복을 제거

				// TODO :: 내용에 이미지가 있는경우 저장파일이름에서는 검색이 안됐으면좋겠다.
				Join<Notice, SiteUser> u = r.join("author", JoinType.LEFT);// 공지와 작성자

				Predicate byTitle = cb.like(r.get("subject"), "%" + kw + "%"); // 제목
				Predicate byContent = cb.like(r.get("contents"), "%" + kw + "%"); // 내용
				Predicate byAuthor = cb.like(u.get("username"), "%" + kw + "%"); // 글쓴이(작성자)

				// TODO:: 제목, 내용, 작성자ID
				// ✅ 선택한 검색대상에 맞춰 조건 분기
				switch (field) {
				case "title":
					return byTitle;
				case "content":
					return byContent;
				case "author":
					return byAuthor;
				case "all":
				default:
					// 제목+내용(+작성자/댓글/댓글작성자) — 기존처럼 확장 검색
					return cb.or(byTitle, byContent);
				}
			}
		};
	}

}
