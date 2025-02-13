package com.example.springboot.service.impl;

import com.example.springboot.entity.Log;
import com.example.springboot.mapper.LogMapper;
import com.example.springboot.service.ILogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {

}
