package com.example.springboot.service.impl;

import com.example.springboot.entity.Notice;
import com.example.springboot.mapper.NoticeMapper;
import com.example.springboot.service.INoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

}
