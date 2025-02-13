package com.example.springboot.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import com.example.springboot.common.annotation.AutoLog;
import com.example.springboot.controller.domain.LoginDTO;
import com.example.springboot.controller.domain.UserRequest;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.DynamicMapper;
import com.example.springboot.mapper.TopicMapper;
import com.example.springboot.service.*;
import com.example.springboot.utils.SpringContextUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "无权限接口列表")
@RestController
@Slf4j
public class WebController {

    @Resource
    IUserService userService;
    @Resource
    IPermissionService permissionService;
    @Resource
    IRoleService roleService;
    @Resource
    IFileService fileService;
    @Resource
    IDynamicService dynamicService;
    @Resource
    IActivityService activityService;

    @Resource
    ITopicService topicService;

    @Resource
    IInformationService informationService;

    @Resource
    INoticeService noticeService;

    @Resource
    IImService iImService;

    @GetMapping(value = "/")
    @ApiOperation(value = "版本校验接口")
    public String version() {
        String ver = "partner-back-0.0.1-SNAPSHOT";  // 应用版本号
        Package aPackage = WebController.class.getPackage();
        String title = aPackage.getImplementationTitle();
        String version = aPackage.getImplementationVersion();
        if (title != null && version != null) {
            ver = String.join("-", title, version);
        }
        return ver;
    }

    @AutoLog("用户登录")
    @ApiOperation(value = "用户登录接口")
    @PostMapping("/login")
    public Result login(@RequestBody UserRequest user) {
        LoginDTO res = userService.login(user);
        return Result.success(res);
    }

    @AutoLog("用户退出登录")
    @ApiOperation(value = "用户退出登录接口")
    @GetMapping("/logout/{uid}")
    public Result logout(@PathVariable String uid) {
        userService.logout(uid);
        return Result.success();
    }

    @AutoLog("用户注册")
    @ApiOperation(value = "用户注册接口")
    @PostMapping("/register")
    public Result register(@RequestBody UserRequest user) {
        userService.register(user);
        return Result.success();
    }

    @AutoLog("用户重置密码")
    @ApiOperation(value = "密码重置接口")
    @PostMapping("/password/reset")
    public Result passwordReset(@RequestBody UserRequest userRequest) {
        String newPass = userService.passwordReset(userRequest);
        return Result.success(newPass);
    }

    @AutoLog("用户修改密码")
    @PostMapping("/password/change")
    public Result passwordChange(@RequestBody UserRequest userRequest) {
        userService.passwordChange(userRequest);
        return Result.success();
    }

    @AutoLog("编辑用户")
    @PutMapping("/updateUser")
    public Result updateUser(@RequestBody User user) {
        Object loginId = StpUtil.getLoginId();
        if (!loginId.equals(user.getUid())) {
            Result.error("无权限");
        }
        userService.updateById(user);
        return Result.success(user);
    }

    @GetMapping("/echarts/users")
    public Result users() {
        List<User> list = userService.list();
        Set<String> address = list.stream().map(User::getAddress).collect(Collectors.toSet()); // 会有空值
        List<Dict> dictList = new ArrayList<>();
        // 循环用户地址  组装数据
        for (String addr : address) {
            if (addr == null) { // 跳过null值
                continue;
            }
            Dict dict = Dict.create().set("name", addr).set("value", list.stream().filter(v -> addr.equals(v.getAddress())).count());
            dictList.add(dict);
        }
        return Result.success(dictList);
    }

    @GetMapping("/echarts/sex")
    public Result sex() {
        List<User> list = userService.list();
        Set<String> sex = list.stream().map(User::getSex).collect(Collectors.toSet()); // 会有空值
        List<Dict> dictList = new ArrayList<>();
        // 循环用户地址  组装数据

        for (String s : sex) {
            if (s == null) { // 跳过null值
                continue;
            }
            Dict dict = Dict.create().set("name", s).set("value", list.stream().filter(v -> s.equals(v.getSex())).count());
            dictList.add(dict);
        }
        return Result.success(dictList);
    }

    @GetMapping("/echarts/dashboard")
    public Result dashboard() {
        List<User> users = userService.list();
        List<Role> roles = roleService.list();
        List<Permission> permissions = permissionService.list();
        List<File> files = fileService.list();
        List<Dynamic> dynamics = dynamicService.list();
        List<Activity> activities = activityService.list();
        List<Topic> topics = topicService.list();
        List<Information> information = informationService.list();
        List<Notice> notices = noticeService.list();
        List<Im> ims = iImService.list();
        Dict dict = Dict.create();
        dict.set("users", users.size());
        dict.set("roles", roles.size());
        dict.set("permissions", permissions.size());
        dict.set("files", files.size());
        dict.set("dynamics", dynamics.size());
        dict.set("activities", activities.size());
        dict.set("topics", topics.size());
        dict.set("information", information.size());
        dict.set("notices", notices.size());
        dict.set("ims", ims.size());

        return Result.success(dict);
    }

    @GetMapping("/echarts/dynamicCount")
    public Result dynamicCount() {
        DynamicMapper dynamicMapper = SpringContextUtil.getBean(DynamicMapper.class);
        List<Dict> list = CollUtil.newArrayList();
        List<Dynamic> dynamics = dynamicMapper.selectList(null);
        Set<String> dates = dynamics.stream().filter(dynamic -> dynamic.getTime() != null).map(dynamic -> dynamic.getTime().substring(0, 11)).collect(Collectors.toSet());
        for (String date : dates) {
            Dict dict = Dict.create();
            dict.set("name", date).set("value", dynamics.stream().filter(dynamic -> dynamic.getTime().contains(date)).count());
            list.add(dict);
        }
        return Result.success(list.stream().sorted(Comparator.comparing(dict -> dict.getStr("name"))));
    }

    @GetMapping("/echarts/topics")
    public Result topics() {
        TopicMapper topicMapper = SpringContextUtil.getBean(TopicMapper.class);
        List<Topic> topics = topicMapper.selectList(new QueryWrapper<Topic>().orderByDesc("hot"));
        return Result.success(topics.stream().limit(20));
    }


    @ApiOperation(value = "邮箱验证接口")
    @GetMapping("/email")
    public Result sendEmail(@RequestParam String email, @RequestParam String type) {  //  ?email=xxx&type=xxx
        long start = System.currentTimeMillis();
        userService.sendEmail(email, type);
        log.info("发送邮件花费时间：{}", System.currentTimeMillis() - start);
        return Result.success();
    }

}
