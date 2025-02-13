package com.example.springboot.service;

import com.example.springboot.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IPermissionService extends IService<Permission> {

    List<Permission> tree();

    void deletePermission(Integer id);
}
