package com.example.springboot.service.impl;

import com.example.springboot.entity.Topic;
import com.example.springboot.mapper.TopicMapper;
import com.example.springboot.service.ITopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yhz
 * @since 2023-03-31
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements ITopicService {

    @Resource
    TopicMapper topicMapper;

    @Override
    public void updateHot(Integer id, int value) {
        topicMapper.updateHot(id, value);
    }
}
