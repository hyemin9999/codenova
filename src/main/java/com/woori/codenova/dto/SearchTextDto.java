package com.woori.codenova.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchTextDto {
	private String text;
	private Long count;

	public SearchTextDto toEntity() {
		return new SearchTextDto(text, count);
	}
}
