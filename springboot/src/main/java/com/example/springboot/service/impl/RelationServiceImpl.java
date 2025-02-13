package com.example.springboot.service.impl;

import com.example.springboot.entity.Relation;
import com.example.springboot.mapper.RelationMapper;
import com.example.springboot.service.IRelationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yhz
 * @since 2023-04-02
 */
@Service
public class RelationServiceImpl extends ServiceImpl<RelationMapper, Relation> implements IRelationService {

}
