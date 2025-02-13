package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springboot.common.annotation.AutoLog;
import com.example.springboot.entity.Comment;
import com.example.springboot.entity.Dynamic;
import com.example.springboot.entity.User;
import com.example.springboot.service.*;
import com.example.springboot.service.impl.DynamicServiceImpl;
import com.example.springboot.utils.SessionUtils;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.entity.Praise;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/praise")
public class PraiseController {

    @Resource
    private IPraiseService praiseService;

    @Resource
    private IUserService userService;

    @Resource
    private IDynamicService dynamicService;

    @Resource
    private IMessageService messageService;

    @Resource
    private ICommentService commentService;

    @AutoLog("新增点赞")
    @PostMapping
    @SaIgnore
    public Result save(@RequestBody Praise praise) {
        User user = SessionUtils.getUser();
        praise.setUserId(user.getId());
        praise.setTime(DateUtil.now());
        String type = new String();
        int id = 0;
        if ("dynamic".equals(praise.getType())) {
            Dynamic dynamic = dynamicService.getById(praise.getFid());
            praise.setBepraiseId(dynamic.getUserId());
            type = "dynamic";
            id = dynamic.getId();
        } else if ("comment".equals(praise.getType())) {
            Comment comment = commentService.getById(praise.getFid());
            praise.setBepraiseId(comment.getUserId());
            type = "comment";
            id = comment.getId();
        }
        try {
            praiseService.save(praise);

            // 加积分
            userService.updateScore(1, user.getId());

            // 消息通知
            User user1 = userService.getById(praise.getBepraiseId());
            messageService.createMessage(user1, user, type, id, "点赞了");
        } catch (DuplicateKeyException e) {
            // 唯一冲突就删除
            praiseService.remove(new UpdateWrapper<Praise>().eq("user_id", user.getId()).eq("fid", praise.getFid())
                    .eq("type", praise.getType()));
            // 消息通知
            User user1 = userService.getById(praise.getBepraiseId());
            messageService.createMessage(user1, user, type, id, "取消了点赞");
        }

        return Result.success();
    }

    @AutoLog("编辑点赞")
    @PutMapping
    @SaCheckPermission("praise.edit")
    public Result update(@RequestBody Praise praise) {
        praiseService.updateById(praise);
        return Result.success();
    }

    @AutoLog("删除点赞")
    @DeleteMapping("/{id}")
    @SaCheckPermission("praise.delete")
    public Result delete(@PathVariable Integer id) {
        praiseService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除点赞")
    @PostMapping("/del/batch")
    @SaCheckPermission("praise.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        praiseService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("praise.list")
    public Result findAll() {
        return Result.success(praiseService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("praise.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(praiseService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("praise.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Praise> queryWrapper = new QueryWrapper<Praise>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "fid", name);
        return Result.success(praiseService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("praise.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Praise> list = praiseService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Praise信息表", "UTF-8");
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
    @SaCheckPermission("praise.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Praise> list = reader.readAll(Praise.class);

        praiseService.saveBatch(list);
        return Result.success();
    }

}
