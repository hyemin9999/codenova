package com.woori.codenova.admin.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCategoryForm {
	private Integer id;

	@NotEmpty(message = "게시판 이름은 필수항목입니다.")
	private String name;

}
