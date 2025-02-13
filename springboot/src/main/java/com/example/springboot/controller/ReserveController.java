package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.Constants;
import com.example.springboot.common.GlobalStatus;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IActivityService;
import com.example.springboot.service.IMessageService;
import com.example.springboot.service.IUserService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IReserveService;
import com.example.springboot.entity.Reserve;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reserve")
public class ReserveController {

    @Resource
    private IReserveService reserveService;
    @Resource
    IActivityService activityService;
    @Resource
    IUserService userService;

    @Resource
    IMessageService messageService;

    @AutoLog("新增预约")
    @PostMapping
    @SaIgnore
    public Result save(@RequestBody Reserve reserve) {
        Activity activity = activityService.getById(reserve.getActivityId());
        String status = activity.getStatus();
        if (!"未开始".equals(status)) {
            throw new ServiceException("对不起，活动已开始或结束");
        }

        // 用户发起预约的时候，校验下他是否已经预约过了
        User user = SessionUtils.getUser();
        Integer userId = user.getId();
        Reserve reserved = reserveService.getOne(new QueryWrapper<Reserve>().eq("user_id", userId).eq("activity_id", reserve.getActivityId()));
        if (!Objects.isNull(reserved)) { // 不为空，说明已经预约过
            throw new ServiceException("对不起，您已预约过这个活动了");
        }

        // 扣除活动的剩余名额
        if (activity.getLeftNums() < 1) {
            throw new ServiceException("对不起，该活动没有剩余名额");
        }
        activity.setLeftNums(activity.getLeftNums() - 1);
        activityService.updateById(activity);

        reserve.setUserId(user.getId());
        reserve.setTime(DateUtil.now());
        reserveService.save(reserve);

        // 发消息给管理员
        List<User> adminList = userService.list(new QueryWrapper<User>().eq("role", "ADMIN"));
        for (User user1 : adminList) {
            messageService.createMessage(user1, user, "activity", reserve.getActivityId(), "预约了");
        }
        return Result.success();
    }

    @AutoLog("编辑预约")
    @PutMapping
    @SaCheckPermission(value = {"reserve.edit", "reserve.approve", "reserve.deny"}, mode = SaMode.OR) // 有一个权限就生效
    public Result update(@RequestBody Reserve reserve) {
        reserveService.updateById(reserve);

        // 审核不通过，返还名额
        if (Objects.equals(reserve.getStatus(), "审核不通过")) {
            Activity activity = activityService.getById(reserve.getActivityId());
            activity.setLeftNums(activity.getLeftNums() + 1);
            activityService.updateById(activity);
        }

        // 发消息给管理员
        User admin = userService.getOne(new QueryWrapper<User>().eq("role", "ADMIN"));
        User user = userService.getById(reserve.getUserId());
        messageService.createMessage(user, admin, "reserve", reserve.getActivityId(), "审核了");
        return Result.success();
    }

    @AutoLog("删除预约")
    @DeleteMapping("/{id}")
//    @SaCheckPermission(value = {"reserve.cancel", "reserve.delete"}, mode = SaMode.OR) // 有一个权限就生效
    @SaIgnore
    public Result delete(@PathVariable Integer id) {
        // 取消预约  归还名额
        Reserve reserve = reserveService.getById(id);
        if (!Objects.equals(reserve.getStatus(), "审核不通过")) {
            Activity activity = activityService.getById(reserve.getActivityId());
            activity.setLeftNums(activity.getLeftNums() + 1);
            activityService.updateById(activity);
        }

        reserveService.removeById(id);
        return Result.success();
    }

    @AutoLog("自动审核")
    @GetMapping("/autoReview")
    @SaCheckPermission("reserve.approve")
    public Result autoReview(@RequestParam Boolean autoReview) {
        User user = SessionUtils.getUser(); // 当前用户
        if (user != null) user.setAutoReview(autoReview);
        userService.updateById(user);
        GlobalStatus.isAutoReview = autoReview;
        return Result.success();
    }

    @AutoLog("批量删除预约")
    @PostMapping("/del/batch")
    @SaCheckPermission("reserve.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        reserveService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        return Result.success(reserveService.list());
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        return Result.success(reserveService.getById(id));
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String username,
                           @RequestParam(defaultValue = "") String type, // 区分用户
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Reserve> queryWrapper = new QueryWrapper<Reserve>().orderByDesc("id");

        // 权限隔离，普通用户只能看到自己的数据，管理员可以看到所有的数据
        User user = SessionUtils.getUser(); // 当前的用户
        if ("user".equals(type)) { // 如果是普通用户
            queryWrapper.eq("user_id", user.getId()); // 只查询自己的数据
        }

        Page<Reserve> page = reserveService.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 根据活动名称查询
        QueryWrapper<Activity> queryWrapper2 = new QueryWrapper<Activity>().like(StrUtil.isNotBlank(name), "name", name);
        List<Activity> records2 = activityService.list(queryWrapper2);

        // 根据用户名称查询
        QueryWrapper<User> queryWrapper3 = new QueryWrapper<User>().like(StrUtil.isNotBlank(username), "name", username);
        List<User> records3 = userService.list(queryWrapper3);

        // 遍历预约表，设置其他属性
        List<Reserve> records = page.getRecords();
        List<Reserve> results = new ArrayList<>();
        for (Reserve record : records) {
            boolean flag1 = false;
            boolean flag2 = false;
            for (Activity record2 : records2) { // 在activity中遍历
                if (Objects.equals(record.getActivityId(), record2.getId())) {
                    record.setActivity(activityService.getById(record.getActivityId()));
                    flag1 = true;
                }
            }
            for (User record3 : records3) { // 在user中遍历
                if (Objects.equals(record.getUserId(), record3.getId())) {
                    record.setUser(userService.getById(record.getUserId()));
                    flag2 = true;
                }
            }
            if (flag1 && flag2) { // 两个查询条件都满足时，返回值
                results.add(record);
            }
        }
        page.setRecords(results);
        return Result.success(page);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("reserve.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Reserve> list = reserveService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Reserve信息表", "UTF-8");
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
    @SaCheckPermission("reserve.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Reserve> list = reader.readAll(Reserve.class);

        reserveService.saveBatch(list);
        return Result.success();
    }

}
