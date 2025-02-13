package com.example.springboot.service;

import com.example.springboot.entity.Dynamic;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IDynamicService extends IService<Dynamic> {

    void updateView(Integer id);
}
