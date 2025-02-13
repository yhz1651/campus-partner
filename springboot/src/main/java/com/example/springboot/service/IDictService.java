package com.example.springboot.service;

import com.example.springboot.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IDictService extends IService<Dict> {

    List<Dict> findIcons();
}
