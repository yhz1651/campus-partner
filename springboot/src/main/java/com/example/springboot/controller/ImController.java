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
import com.example.springboot.entity.User;
import com.example.springboot.service.IUserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IImService;
import com.example.springboot.entity.Im;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author ikun
* @since 2023-02-18
*/
@RestController
@RequestMapping("/im")
public class ImController {

    @Resource
    private IImService imService;

    @Resource
    private IUserService userService;

    @AutoLog("新增聊天记录")
    @PostMapping
    @SaIgnore
    public Result save(@RequestBody Im im) {
        imService.save(im);
        return Result.success();
    }

    @AutoLog("编辑聊天记录")
    @PutMapping
    @SaCheckPermission("im.edit")
    public Result update(@RequestBody Im im) {
        imService.updateById(im);
        return Result.success();
    }

    @AutoLog("删除聊天记录")
    @DeleteMapping("/{id}")
    @SaCheckPermission("im.delete")
    public Result delete(@PathVariable Integer id) {
        imService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除聊天记录")
    @PostMapping("/del/batch")
    @SaCheckPermission("im.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        imService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/init/{limit}")
    @SaIgnore
    public Result findAllInit(@PathVariable Integer limit) {
        List<Im> ims = imService.list(new QueryWrapper<Im>().orderByDesc("id").last("limit " + limit));
        for (Im im : ims) {
            User user = userService.getOne(new QueryWrapper<User>().eq("uid", im.getUid()));
            im.setUser(user);
        }
        return Result.success(ims.stream().sorted(Comparator.comparing(Im::getId)).collect(Collectors.toList()));
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        return Result.success(imService.list());
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        return Result.success(imService.getById(id));
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Im> queryWrapper = new QueryWrapper<Im>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "text", name);
        Page<Im> page = imService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Im> records = page.getRecords();
        for (Im record : records) {
            User user = userService.getOne(new QueryWrapper<User>().eq("uid", record.getUid()));
            record.setUser(user);
        }
        return Result.success(page);
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    @SaCheckPermission("im.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Im> list = imService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Im信息表", "UTF-8");
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
    @SaCheckPermission("im.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Im> list = reader.readAll(Im.class);

        imService.saveBatch(list);
        return Result.success();
    }

}
