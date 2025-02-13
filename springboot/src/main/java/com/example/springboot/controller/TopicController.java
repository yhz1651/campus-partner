package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.*;
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
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.ITopicService;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author yhz
* @since 2023-03-31
*/
@RestController
@RequestMapping("/topic")
public class TopicController {

    @Resource
    private ITopicService topicService;

    @AutoLog("新增话题")
    @PostMapping
    @SaCheckPermission("topic.add")
    public Result save(@RequestBody Topic topic) {
        topicService.save(topic);
        return Result.success();
    }

    @AutoLog("编辑话题")
    @PutMapping
    @SaCheckPermission("topic.edit")
    public Result update(@RequestBody Topic topic) {
        topicService.updateById(topic);
        return Result.success();
    }

    @AutoLog("删除话题")
    @DeleteMapping("/{id}")
    @SaCheckPermission("topic.delete")
    public Result delete(@PathVariable Integer id) {
        topicService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除话题")
    @PostMapping("/del/batch")
    @SaCheckPermission("topic.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        topicService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        return Result.success(topicService.list());
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        return Result.success(topicService.getById(id));
    }


    @GetMapping("/hot")
    @SaIgnore
    public Result hot() {
        List<Topic> list = topicService.list();
        // 取热度最高的8条话题
        return Result.success(list.stream().sorted((d1, d2) -> d2.getHot().compareTo(d1.getHot())).limit(8));
    }


    @GetMapping("/page")
    @SaCheckPermission("topic.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<Topic>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(topicService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    @SaCheckPermission("topic.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Topic> list = topicService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Topic信息表", "UTF-8");
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
    @SaCheckPermission("topic.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Topic> list = reader.readAll(Topic.class);

        topicService.saveBatch(list);
        return Result.success();
    }

}
