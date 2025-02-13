package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.ActivityMapper;
import com.example.springboot.mapper.CommentMapper;
import com.example.springboot.mapper.DynamicMapper;
import com.example.springboot.mapper.MessageMapper;
import com.example.springboot.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yhz
 * @since 2023-04-01
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Resource
    DynamicMapper dynamicMapper;

    @Resource
    CommentMapper commentMapper;

    @Resource
    ActivityMapper activityMapper;

    @Override
    public void createMessage(User user, User creator, String type, Integer typeId, String operation) {
        if (Objects.equals(user.getId(), creator.getId())) return;
        Message message = new Message();
        message.setTime(DateUtil.now());
        message.setUserId(user.getId());
        message.setCreatorId(creator.getId());
        String content = new String();
        if ("dynamic".equals(type)) {
            Dynamic dynamic = dynamicMapper.selectById(typeId);
            content = " <a target='_blank' href='/front/userDetail?id=" + creator.getId() + "'>" + creator.getName() + "</a> " + operation + "你的动态" + " <a target='_blank' href='/front/dynamicDetail?id=" + typeId + "'>" + dynamic.getName() + "</a>";
        } else if ("comment".equals(type)) {
            Comment comment = commentMapper.selectById(typeId);
            Dynamic dynamic = dynamicMapper.selectById(comment.getDynamicId());
            content = " <a target='_blank' href='/front/userDetail?id=" + creator.getId() + "'>" + creator.getName() + "</a> " + operation + "你的评论" + " <a target='_blank' href='/front/dynamicDetail?id=" + dynamic.getId() + "'>" + comment.getContent() + "</a>";
        } else if ("follow".equals(type)) {
            content = " <a target='_blank' href='/front/userDetail?id=" + creator.getId() + "'>" + creator.getName() + "</a> " + operation;
        } else if ("activity".equals(type)) {
            Activity activity = activityMapper.selectById(typeId);
            content = " <a target='_blank' href='/front/userDetail?id=" + creator.getId() + "'>" + creator.getName() + "</a> " + operation + "活动" + " <a target='_blank' href='/reserve'>" + activity.getName() + "</a>" + " 请尽快审核！";
        } else if ("reserve".equals(type)) {
            Activity activity = activityMapper.selectById(typeId);
            content = "管理员" + operation + "你的活动预约" + " <a target='_blank' href='/front/myReserve'>" + activity.getName() + "</a>" + " 请查看审核结果！";
        }
        message.setContent(content);
        save(message);
    }
}
