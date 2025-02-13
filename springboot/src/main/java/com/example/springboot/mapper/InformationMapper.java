package com.example.springboot.mapper;

import com.example.springboot.entity.Information;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yhz
 * @since 2023-03-29
 */
public interface InformationMapper extends BaseMapper<Information> {

    @Update("update information set view = view + 1 where id = #{id}")
    void updateView(Integer id);
}
