package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.Dynamic;
import com.example.springboot.entity.Message;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.DynamicMapper;
import com.example.springboot.mapper.MessageMapper;
import com.example.springboot.service.IDynamicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.utils.SessionUtils;
import com.example.springboot.utils.SpringContextUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DynamicServiceImpl extends ServiceImpl<DynamicMapper, Dynamic> implements IDynamicService {

    @Resource
    DynamicMapper dynamicMapper;
    @Override
    public void updateView(Integer id) {
        dynamicMapper.updateView(id);
    }

}
