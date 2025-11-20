package com.example.demo.form;

import lombok.Data;

@Data
public class EditPasswordForm {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
