package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Dynamic;
import com.example.springboot.entity.User;
import com.example.springboot.service.IMessageService;
import com.example.springboot.service.IUserService;
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
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IRelationService;
import com.example.springboot.entity.Relation;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author yhz
* @since 2023-04-02
*/
@RestController
@RequestMapping("/relation")
public class RelationController {

    @Resource
    private IRelationService relationService;

    @Resource
    private IUserService userService;

    @Resource
    private IMessageService messageService;

    @AutoLog("新增关注")
    @PostMapping
    @SaIgnore
    public Result save(@RequestBody Relation relation) {
        User user = SessionUtils.getUser();
        relation.setFollowId(user.getId()); // 关注人id
        relation.setTime(DateUtil.now()); // 关注时间
        try {
            relationService.save(relation);
            // 加积分
            userService.updateScore(3, user.getId());

            // 消息通知
            User user1 = userService.getById(relation.getUserId());
            messageService.createMessage(user1, user, "follow", relation.getId(), "关注了你");

        } catch (Exception e) {
            // 唯一冲突就删除
            relationService.remove(new UpdateWrapper<Relation>().eq("follow_id", user.getId())
                    .eq("user_id", relation.getUserId()));
            // 消息通知
            User user1 = userService.getById(relation.getUserId());
            messageService.createMessage(user1, user, "follow", relation.getId(), "取消了关注你");
        }
        return Result.success();
    }

    @AutoLog("编辑关注")
    @PutMapping
    @SaCheckPermission("relation.edit")
    public Result update(@RequestBody Relation relation) {
        relationService.updateById(relation);
        return Result.success();
    }

    @AutoLog("删除关注")
    @DeleteMapping("/{id}")
    @SaCheckPermission("relation.delete")
    public Result delete(@PathVariable Integer id) {
        relationService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除关注")
    @PostMapping("/del/batch")
    @SaCheckPermission("relation.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        relationService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        return Result.success(relationService.list());
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        return Result.success(relationService.getById(id));
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String type, // 区分用户
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Relation> queryWrapper = new QueryWrapper<Relation>().orderByDesc("time");
        queryWrapper.like(!"".equals(name), "name", name);

        List<Relation> relationList = relationService.list();
        User user = SessionUtils.getUser(); // 当前的用户
        if ("user".equals(type)) {  // 如果type是user，表示筛选用户自己的数据
            queryWrapper.eq("follow_id", user.getId());
        }
        Page<Relation> page = relationService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Relation> records = page.getRecords();
        for (Relation record : records) {
            record.setUser(userService.getById(record.getUserId()));
            boolean hasFollow = relationList.stream().anyMatch(relation -> relation.getFollowId().equals(user.getId()) && relation.getUserId().equals(record.getUserId()));
            boolean hasBeFollow = relationList.stream().anyMatch(relation -> relation.getUserId().equals(user.getId()) && relation.getFollowId().equals(record.getUserId()));
            if ("USER".equals(user.getRole())){
                record.getUser().setHasFollow(hasFollow);
                record.getUser().setHasBeFollow(hasBeFollow);
            }
        }
        return Result.success(page);
    }

    @GetMapping("/pageFans")
    @SaIgnore
    public Result findPageFans(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String type, // 区分用户
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Relation> queryWrapper = new QueryWrapper<Relation>().orderByDesc("time");
        queryWrapper.like(!"".equals(name), "name", name);

        List<Relation> relationList = relationService.list();
        User user = SessionUtils.getUser(); // 当前的用户
        if ("user".equals(type)) {  // 如果type是user，表示筛选用户自己的数据
            queryWrapper.eq("user_id", user.getId());
        }
        Page<Relation> page = relationService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Relation> records = page.getRecords();
        for (Relation record : records) {
            record.setUser(userService.getById(record.getFollowId()));
            boolean hasFollow = relationList.stream().anyMatch(relation -> relation.getUserId().equals(user.getId()) && relation.getFollowId().equals(record.getFollowId()));
            boolean hasBeFollow = relationList.stream().anyMatch(relation -> relation.getFollowId().equals(user.getId()) && relation.getUserId().equals(record.getFollowId()));
            record.getUser().setHasFollow(hasFollow);
            record.getUser().setHasBeFollow(hasBeFollow);
        }
        return Result.success(page);
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    @SaCheckPermission("relation.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Relation> list = relationService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Relation信息表", "UTF-8");
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
    @SaCheckPermission("relation.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Relation> list = reader.readAll(Relation.class);

        relationService.saveBatch(list);
        return Result.success();
    }

}
