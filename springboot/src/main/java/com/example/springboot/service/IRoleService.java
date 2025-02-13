package com.example.springboot.service;

import com.example.springboot.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IRoleService extends IService<Role> {

    void savePermissions(Integer roleId, List<Integer> permissionIds);

    void deleteRole(Integer id);
}
