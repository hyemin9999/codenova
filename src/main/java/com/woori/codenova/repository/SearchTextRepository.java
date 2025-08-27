package com.woori.codenova.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.woori.codenova.dto.SearchTextDto;
import com.woori.codenova.entity.SearchText;

public interface SearchTextRepository extends JpaRepository<SearchText, Integer> {

//	List<SearchText> findAll(Specification<SearchText> specification);

	@Query("SELECT new com.woori.codenova.dto.SearchTextDto(s.text, COUNT(s)) FROM SearchText s GROUP BY s.text")
	List<SearchTextDto> findSearchTextCountsByText();

}
