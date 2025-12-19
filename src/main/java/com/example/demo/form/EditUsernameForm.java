package com.example.demo.form;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class EditUsernameForm {
    @NotBlank(message = "新しいユーザー名を入力してください")
    private String newUsername;
}