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

import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.repository.CategoryRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AdminCategoryService {
	private final CategoryRepository categoryRepository;

	// 목록 - 페이징 - 검색
	public Page<Category> getlist(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));

		Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));
		Specification<Category> spec = search(kw);

		return categoryRepository.findAll(spec, pageable);
	}

	// 목록 - 정렬 - 역할관리에서 사용
	public List<Category> getlist() {
		List<Category> list = categoryRepository.findAllByName();

		return list;// addAllItem(list);
	}

	public List<Category> addAllItem(List<Category> list) {

		Category item = new Category();
		item.setName("전체");
		item.setId(0);
		list.add(0, item);

		return list;
	}

//	// 목록
//	public List<Category> getlist(int roleId) {
//		Specification<Category> spec = search(roleId);
//		return categoryRepository.findAll(spec);
//	}

	// 조회 - 상세
	public Category getitem(Integer id) {
		return categoryRepository.findById(id).orElse(null);
	}

	// 등록
	public void create(String name) {
		Category item = new Category();
		item.setName(name);
		item.setCreateDate(LocalDateTime.now());

		categoryRepository.save(item);
	}

	// 수정
	public void modify(Category item, String name) {
		item.setName(name);
		item.setModifyDate(LocalDateTime.now());

		categoryRepository.save(item);
	}

	// 삭제
	public void delete(Category item) {
		// TODO :: 게시판 삭제시 연결된 공지사항, 게시글(+ 댓글) 처리를 어떻게 해야하는지...
		// 게시글이 있는 게시판은 삭제하지 못하도록 처리??
		// this.categoryRepository.delete(item);
		// isDelete 처리가 복잡하니 일단 다 날림^^
		this.categoryRepository.delete(item);
	}

	// 즐겨찾기
	public void favorites(Category item, SiteUser siteUser) {
		item.getFavorite().add(siteUser);

		categoryRepository.save(item);
	}

	// 검색
	private Specification<Category> search(String kw) {
		return new Specification<>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Category> r, CriteriaQuery<?> q, CriteriaBuilder cb) {

				q.distinct(true); // 중복을 제거
				return cb.or(cb.like(r.get("name"), "%" + kw + "%"));
			}
		};
	}

//	private Specification<Category> search(Integer roleId) {
//		return new Specification<>() {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Predicate toPredicate(Root<Category> r, CriteriaQuery<?> q, CriteriaBuilder cb) {
//
//				q.distinct(true); // 중복을 제거
//				Join<SiteUser, Role> role = r.join("authority", JoinType.LEFT);// 게시글과 작성자
//				return cb.equal(role.get("Id"), roleId); // role id;
//			}
//		};
//	}
}
