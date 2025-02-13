package com.example.springboot.service.impl;

import com.example.springboot.entity.Im;
import com.example.springboot.mapper.ImMapper;
import com.example.springboot.service.IImService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ImServiceImpl extends ServiceImpl<ImMapper, Im> implements IImService {

}
