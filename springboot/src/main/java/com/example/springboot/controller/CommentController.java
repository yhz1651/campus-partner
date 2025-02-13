package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.Dynamic;
import com.example.springboot.entity.Praise;
import com.example.springboot.entity.User;
import com.example.springboot.service.*;
import com.example.springboot.service.impl.DynamicServiceImpl;
import com.example.springboot.service.impl.UserServiceImpl;
import com.example.springboot.utils.IpUtils;
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
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.entity.Comment;

import org.springframework.web.bind.annotation.RestController;

import static net.sf.jsqlparser.parser.feature.Feature.comment;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yhz
 * @since 2023-03-29
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private ICommentService commentService;

    @Resource
    private IUserService userService;

    @Resource
    private IDynamicService dynamicService;

    @Resource
    private IMessageService messageService;

    @Resource
    private IPraiseService praiseService;


    @AutoLog("新增评论")
    @PostMapping
    @SaIgnore
    public Result save(@RequestBody Comment comment) {
        User user = SessionUtils.getUser();
        comment.setUserId(user.getId());
        comment.setTime(DateUtil.now());

        // 获取IP地址
        Dict ipAndCity = IpUtils.getIPAndCity();
        if (!Objects.isNull(ipAndCity.get("city"))) {
            comment.setLocation(ipAndCity.get("city").toString());
        } else {
            comment.setLocation("未知");
        }

        commentService.save(comment);
        // 加积分
        userService.updateScore(3, user.getId());

        // 消息通知
        // 给动态作者发通知
        Dynamic dynamic = dynamicService.getById(comment.getDynamicId());
        User user1 = userService.getById(dynamic.getUserId());
        messageService.createMessage(user1, user, "dynamic", dynamic.getId(), "评论了");

        // 给评论作者发通知
        if (comment.getPuserId() != null) { // 回复别人的评论
            User user2 = userService.getById(comment.getPuserId());
            Comment comment1 = commentService.getById(comment.getPid());
            messageService.createMessage(user2, user, "comment", comment1.getId(), "回复了");
        }

        return Result.success();
    }

    @AutoLog("编辑评论")
    @PutMapping
    @SaCheckPermission("comment.edit")
    public Result update(@RequestBody Comment comment) {
        commentService.updateById(comment);
        return Result.success();
    }

    @AutoLog("删除评论")
    @DeleteMapping("/{id}")
    @SaIgnore
    public Result delete(@PathVariable Integer id) {
        // 消息通知
        // 给动态作者发通知
        Comment comment = commentService.getById(id);
        Dynamic dynamic = dynamicService.getById(comment.getDynamicId());
        User user = SessionUtils.getUser();
        User user1 = userService.getById(dynamic.getUserId());
        messageService.createMessage(user1, user, "dynamic", dynamic.getId(), "删除了评论");

        // 给评论作者发通知
        if (comment.getPuserId() != null) { // 回复别人的评论
            User user2 = userService.getById(comment.getPuserId());
            Comment comment1 = commentService.getById(comment.getPid());
            messageService.createMessage(user2, user, "comment", comment1.getId(), "删除了评论");
        }

        List<Comment> children = commentService.list(new QueryWrapper<Comment>().eq("pid", id));
        for (Comment child : children) {
            // 删除子评论
            commentService.removeById(child.getId());
        }
        commentService.removeById(id);

        return Result.success();
    }

    @AutoLog("批量删除评论")
    @PostMapping("/del/batch")
    @SaCheckPermission("comment.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        commentService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("comment.list")
    public Result findAll() {
        return Result.success(commentService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("comment.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(commentService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("comment.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<Comment>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "content", name);
        return Result.success(commentService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/tree")
    @SaIgnore
    public Result tree(@RequestParam Integer dynamicId) {
        List<User> userList = userService.list();
        List<Comment> list = commentService.list(new QueryWrapper<Comment>().eq("dynamic_id", dynamicId));
        // 给comments里的每个对象设置一个user属性
        list = list.stream().peek(comments -> userList.stream().filter(user -> user.getId().equals(comments.getUserId())).findFirst().ifPresent(comments::setUser)).collect(Collectors.toList());

        // 设置点赞数量 是否点赞
        List<Praise> praiseList = praiseService.list();
        User user = SessionUtils.getUser();
        for (Comment comment : list) {
            comment.setHasPraise(praiseList.stream().anyMatch(praise -> praise.getUserId().equals(user.getId()) && praise.getFid().equals(comment.getId())));
            comment.setPraiseCount((int) praiseList.stream().filter(praise -> praise.getFid().equals(comment.getId())).count());
        }

        List<Comment> first = list.stream().filter(comments -> comments.getPid() == null).collect(Collectors.toList());// 一级评论
        for (Comment comments : first) {
            Integer pid = comments.getId();
            List<Comment> second = list.stream().filter(comments1 -> Objects.equals(pid, comments1.getPid())).collect(Collectors.toList());// 二级评论

            // 给second里的每个对象设置一个puser属性
            second = second.stream().peek(comments1 -> userList.stream()
                    .filter(u -> u.getId().equals(comments1.getPuserId())).findFirst()
                    .ifPresent(comments1::setPUser)).collect(Collectors.toList());
            comments.setChildren(second);  // 一级评论设置二级评论
        }

        return Result.success(first);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("comment.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Comment> list = commentService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Comment信息表", "UTF-8");
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
    @SaCheckPermission("comment.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Comment> list = reader.readAll(Comment.class);

        commentService.saveBatch(list);
        return Result.success();
    }

}
