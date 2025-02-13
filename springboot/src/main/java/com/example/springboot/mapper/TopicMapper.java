package com.example.springboot.mapper;

import com.example.springboot.entity.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yhz
 * @since 2023-03-31
 */
public interface TopicMapper extends BaseMapper<Topic> {

    @Update("update topic set hot = hot + #{value} where id = #{id}")
    void updateHot(Integer id, int value);
}
