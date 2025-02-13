package com.example.springboot.controller.domain;

import com.example.springboot.entity.Permission;
import com.example.springboot.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private User user; // 用户信息
    private String token; // token
    private List<Permission> menus; // 菜单栏信息
    private List<Permission> auths; // sa-token权限
}
