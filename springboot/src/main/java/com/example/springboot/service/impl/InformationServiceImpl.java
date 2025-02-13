package com.example.springboot.service.impl;

import com.example.springboot.entity.Information;
import com.example.springboot.mapper.InformationMapper;
import com.example.springboot.service.IInformationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yhz
 * @since 2023-03-29
 */
@Service
public class InformationServiceImpl extends ServiceImpl<InformationMapper, Information> implements IInformationService {

    @Resource
    InformationMapper informationMapper;

    @Override
    public void updateView(Integer id) {
        informationMapper.updateView(id);
    }
}
