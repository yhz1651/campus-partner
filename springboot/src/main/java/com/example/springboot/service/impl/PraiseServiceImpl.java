package com.example.springboot.service.impl;

import com.example.springboot.entity.Praise;
import com.example.springboot.mapper.PraiseMapper;
import com.example.springboot.service.IPraiseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PraiseServiceImpl extends ServiceImpl<PraiseMapper, Praise> implements IPraiseService {

}
