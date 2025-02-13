package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.entity.Dict;
import com.example.springboot.mapper.DictMapper;
import com.example.springboot.service.IDictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements IDictService {

    @Override
    @Cacheable(value="findIcons")
    public List<Dict> findIcons() {
        return list(new QueryWrapper<Dict>().eq("type", "icon"));
    }
}
