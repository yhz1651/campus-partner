package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.annotation.AutoLog;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IPermissionService;
import com.example.springboot.entity.Permission;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private IPermissionService permissionService;

    @AutoLog("新增权限")
    @PostMapping
    @SaCheckPermission("permission.add")
    public Result save(@RequestBody Permission permission) {
        permissionService.save(permission);
        return Result.success();
    }

    @AutoLog("编辑权限")
    @PutMapping
    @SaCheckPermission("permission.edit")
    public Result update(@RequestBody Permission permission) {
        permissionService.updateById(permission);
        return Result.success();
    }

    @AutoLog("删除权限")
    @DeleteMapping("/{id}")
    @SaCheckPermission("permission.delete")
    public Result delete(@PathVariable Integer id) {
        permissionService.deletePermission(id);
        return Result.success();
    }

    @AutoLog("批量删除权限")
    @PostMapping("/del/batch")
    @SaCheckPermission("permission.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        for (Integer id : ids) {
            permissionService.deletePermission(id);
        }
        return Result.success();
    }

    @GetMapping("/tree")
    @SaCheckPermission("permission.list")
    public Result tree() {
        return Result.success(permissionService.tree().stream().sorted(Comparator.comparing(Permission::getOrders)).collect(Collectors.toList()));
    }

    @GetMapping("/pathName")
    @SaIgnore
    public Result findPathName(@RequestParam String str) {
        String path = str.substring(1);
        Permission permission = permissionService.getOne(new QueryWrapper<Permission>().eq("path", path));
        if (permission != null) {
            String pathName = permission.getName();
            return Result.success(pathName);
        } else {
            return Result.success();
        }

    }

    @GetMapping
    @SaCheckPermission("permission.list")
    public Result findAll() {
        return Result.success(permissionService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("permission.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(permissionService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("permission.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<Permission>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(permissionService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("permission.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Permission> list = permissionService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Permission信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

    }

    /**
     * excel 导入
     *
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    @SaCheckPermission("permission.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Permission> list = reader.readAll(Permission.class);

        permissionService.saveBatch(list);
        return Result.success();
    }

}
