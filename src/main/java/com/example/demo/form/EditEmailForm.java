package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;


@Data
public class EditEmailForm {
	@NotBlank(message = "現在のパスワードを入力してください")
    private String currentPassword;

    @NotBlank(message = "新しいメールアドレスを入力してください")
    @Email(message = "正しいメールアドレス形式で入力してください")
    private String newEmail;
}
