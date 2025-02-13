package com.example.springboot.service.impl;

import com.example.springboot.entity.Reserve;
import com.example.springboot.mapper.ReserveMapper;
import com.example.springboot.service.IReserveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ReserveServiceImpl extends ServiceImpl<ReserveMapper, Reserve> implements IReserveService {

}
