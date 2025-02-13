package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.RandomUtil;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.User;
import com.example.springboot.utils.SessionUtils;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IInformationService;
import com.example.springboot.entity.Information;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author yhz
* -@since 2023-03-29-
*/
@RestController
@RequestMapping("/information")
public class InformationController {

    @Resource
    private IInformationService informationService;

    @AutoLog("新增资讯")
    @PostMapping
    @SaCheckPermission("information.add")
    public Result save(@RequestBody Information information) {
        information.setTime(DateUtil.now());
        informationService.save(information);
        return Result.success();
    }

    @AutoLog("编辑资讯")
    @PutMapping
    @SaCheckPermission("information.edit")
    public Result update(@RequestBody Information information) {
        informationService.updateById(information);
        return Result.success();
    }

    @AutoLog("删除资讯")
    @DeleteMapping("/{id}")
    @SaCheckPermission("information.delete")
    public Result delete(@PathVariable Integer id) {
        informationService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除资讯")
    @PostMapping("/del/batch")
    @SaCheckPermission("information.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        informationService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
//        // 固定返回小于等于5的个数
//        List<Information> list = informationService.list();
//        Set<Information> set = new HashSet<>();  // set自动去重
//        if (list.size() <= 10) {
//            return Result.success(list);
//        } else {
//            while (set.size() < 10) {
//                int num = RandomUtil.randomInt(0, list.size());  // 获取一个随机的序号
//                set.add(list.get(num));
//            }
//            return Result.success(set);
//        }
        List<Information> list = informationService.list();
        // 按照结束时间 取最新10个
        return Result.success(list.stream().sorted((d1, d2) -> d2.getTime().compareTo(d1.getTime())).limit(10));
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        informationService.updateView(id);
        return Result.success(informationService.getById(id));
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Information> queryWrapper = new QueryWrapper<Information>().orderByDesc("time");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(informationService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    @SaCheckPermission("information.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Information> list = informationService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Information信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

    }

    /**
    * excel 导入
    * @param file
    * @throws Exception
    */
    @PostMapping("/import")
    @SaCheckPermission("information.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Information> list = reader.readAll(Information.class);

        informationService.saveBatch(list);
        return Result.success();
    }

}
