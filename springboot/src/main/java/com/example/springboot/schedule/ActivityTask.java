package com.example.springboot.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Constants;
import com.example.springboot.common.GlobalStatus;
import com.example.springboot.entity.Activity;
import com.example.springboot.entity.Message;
import com.example.springboot.entity.Reserve;
import com.example.springboot.entity.User;
import com.example.springboot.service.IActivityService;
import com.example.springboot.service.IMessageService;
import com.example.springboot.service.IReserveService;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时任务
 */

@Component
@Slf4j
public class ActivityTask {

    @Resource
    IActivityService activityService;

    @Resource
    IReserveService reserveService;

    @Resource
    IUserService userService;

    @Resource
    IMessageService messageService;

    @Scheduled(fixedRate = 60 * 1000)  // 1分钟执行一次
    public void task() {
        log.info("我是定时任务，我更新活动状态的任务开始");

        /**
         * 1、更新活动表的活动状态：已结束、进行中、已结束
         * */
        List<Activity> updateList = new ArrayList<>();
        List<Activity> list = activityService.list();
        // 扫描整个活动表
        for (Activity activity : list) {
//            if ("已结束".equals(activity.getStatus())) { // 已结束就不改了
//                continue;
//            }
            String startTime = activity.getStartTime();
            String endTime = activity.getEndTime();
            DateTime startDateTime = DateUtil.parse(startTime, "yyyy-MM-dd HH:mm");  // 开始时间
            DateTime endDateTime = DateUtil.parse(endTime, "yyyy-MM-dd HH:mm");   // 结束时间
            DateTime now = new DateTime();   // 当前时间
            if (now.isAfterOrEquals(startDateTime) && now.isBeforeOrEquals(endDateTime)) {
                activity.setStatus("进行中");
                updateList.add(activity);
            } else if (now.isAfter(endDateTime)) {
                activity.setStatus("已结束");
                updateList.add(activity);
            } else {
                activity.setStatus("未开始");
                updateList.add(activity);
            }
        }
//        if (updateList.size() > 0) {
//            log.info("定时任务正在更新活动状态...");
//        }
        activityService.updateBatchById(updateList);  // 批量更新数据
        log.info("我是定时任务，我更新活动状态的任务结束");


        /**
         * 2、更新预约表的预约状态，如果当前活动已经结束，但是还没审核，则自动取消
         * */
        log.info("我是定时任务，我更新预约表数据的任务开始");
        List<Reserve> reserveList = reserveService.list(new QueryWrapper<Reserve>().eq("status", "待审核"));
        // 扫描预约表
        for (Reserve reserve : reserveList) {
            Integer activityId = reserve.getActivityId();
            Activity activity = activityService.getById(activityId);
            StringBuilder time = new StringBuilder(activity.getEndTime());
            time.append(":00"); // 加上秒，否则parseDateTime会报错
            DateTime endTime = DateUtil.parseDateTime(time);
            // 如果当前活动已经结束，但是还没审核，则自动取消
            if (endTime.isBefore(new Date())) {
                activity.setLeftNums(activity.getLeftNums() + 1);  // 预约名额返还
                activityService.updateById(activity);
                reserveService.removeById(reserve.getId());  // 取消预约
                User user = userService.getById(reserve.getUserId());
                log.info("定时任务取消了{} 预约的 {}", user.getName(), activity.getName());
            }
        }
        log.info("我是定时任务，我更新预约表数据的任务结束");


        /**
         * 3、自动审核
         * */
        List<Reserve> reserveList2 = reserveService.list(new QueryWrapper<Reserve>().eq("status", "待审核"));
        if (GlobalStatus.isAutoReview) {
            log.info("我是定时任务，自动审核开始");
            for (Reserve reserve : reserveList2) {
                reserve.setStatus("审核通过");
                reserveService.updateById(reserve);
            }
            log.info("我是定时任务，自动审核结束");
        }
    }


}

