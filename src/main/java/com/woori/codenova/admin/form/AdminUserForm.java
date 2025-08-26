package com.woori.codenova.admin.form;

import java.util.List;

import com.woori.codenova.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserForm {
	@Size(min = 4, max = 10, message = "사용자ID는 4 ~ 10 글자 사이여야 합니다.")
	@NotEmpty(message = "사용자ID는 필수항목입니다.")
	private String username;

	@NotEmpty(message = "비밀번호는 필수항목입니다.")
	private String password1;

	@NotEmpty(message = "이메일은 필수항목입니다.")
	@Email
	private String email;

	private List<Role> optionList;
	private List<Role> selectedList;

}
