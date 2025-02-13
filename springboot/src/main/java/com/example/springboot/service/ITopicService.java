package com.example.springboot.service;

import com.example.springboot.entity.Topic;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yhz
 * @since 2023-03-31
 */
public interface ITopicService extends IService<Topic> {

    void updateHot(Integer id, int value);
}
