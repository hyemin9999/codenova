package com.woori.codenova.admin.form;

import java.util.List;

import com.woori.codenova.entity.Role;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserModifyForm {

	private Long id;
	private String username;

	private String password1;

	@Email
	private String email;

	private List<Role> optionList;
	private List<Role> selectedList;
}
