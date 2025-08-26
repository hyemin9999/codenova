package com.woori.codenova.admin.form;

import java.util.List;

import com.woori.codenova.entity.Category;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRoleForm {
	private Integer id;

	@NotEmpty(message = "역할 이름은 필수항목입니다.")
	private String name;

	private List<Category> optionList;
	private List<Category> selectedList;

}
