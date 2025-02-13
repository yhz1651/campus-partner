package com.example.springboot.service;

import com.example.springboot.entity.Information;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yhz
 * @since 2023-03-29
 */
public interface IInformationService extends IService<Information> {

    void updateView(Integer id);
}
