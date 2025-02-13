package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.controller.domain.LoginDTO;
import com.example.springboot.controller.domain.UserRequest;
import com.example.springboot.entity.Im;
import com.example.springboot.entity.Permission;
import com.example.springboot.entity.User;

import java.util.List;

public interface IUserService extends IService<User> {

    LoginDTO login(UserRequest user);

    void register(UserRequest user);

    String passwordReset(UserRequest userRequest);

    void logout(String uid);

    User saveUser(User user);

    List<Permission> getPermissions(String roleFlag);

    void passwordChange(UserRequest userRequest);

    void sendEmail(String email, String type);

    void updateScore(int score, Integer userId);

}
