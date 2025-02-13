package com.example.springboot.service;

import com.example.springboot.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yhz
 * @since 2023-04-01
 */
public interface IMessageService extends IService<Message> {

    void createMessage(User user, User creator, String type, Integer typeId, String operation);
}
