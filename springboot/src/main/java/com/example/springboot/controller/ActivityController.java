package com.example.springboot.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.Dynamic;
import com.example.springboot.entity.Reserve;
import com.example.springboot.entity.User;
import com.example.springboot.service.IReserveService;
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
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IActivityService;
import com.example.springboot.entity.Activity;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private IActivityService activityService;

    @Resource
    IUserService userService;

    @Resource
    IReserveService reserveService;

    @AutoLog("新增活动")
    @PostMapping
    @SaCheckPermission("activity.add")
    public Result save(@RequestBody Activity activity) {
        User user = SessionUtils.getUser();
        activity.setUserId(user.getId());
        activity.setLeftNums(activity.getNums());  // 初始的时候剩余可预约的数量等于总数
        activityService.save(activity);
        return Result.success();
    }

    @AutoLog("编辑活动")
    @PutMapping
    @SaCheckPermission("activity.edit")
    public Result update(@RequestBody Activity activity) {
        activityService.updateById(activity);
        return Result.success();
    }

    @AutoLog("删除活动")
    @DeleteMapping("/{id}")
    @SaCheckPermission("activity.delete")
    public Result delete(@PathVariable Integer id) {
        activityService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除活动")
    @PostMapping("/del/batch")
    @SaCheckPermission("activity.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        activityService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        List<Activity> list = activityService.list();
        // 按照结束时间 取最新10个
        return Result.success(list.stream().sorted((d1, d2) -> d2.getEndTime().compareTo(d1.getEndTime())).limit(10));
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        return Result.success(activityService.getById(id));
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<Activity>().orderByDesc("end_time"); // 按照结束时间排序
        queryWrapper.like(!"".equals(name), "name", name);
        Page<Activity> page = activityService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Activity> records = page.getRecords();
        List<Reserve> reserveList = reserveService.list();
        User user = SessionUtils.getUser();
        for (Activity record : records) {
            record.setCreator(userService.getById(record.getUserId()).getName());
            record.setHasReserve(reserveList.stream().anyMatch(reserve -> reserve.getActivityId().equals(record.getId()) && reserve.getUserId().equals(user.getId())));
        }
        return Result.success(page);
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    @SaCheckPermission("activity.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Activity> list = activityService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Activity信息表", "UTF-8");
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
    @SaCheckPermission("activity.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Activity> list = reader.readAll(Activity.class);

        activityService.saveBatch(list);
        return Result.success();
    }

}
