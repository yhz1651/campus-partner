package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.example.springboot.controller.domain.ImgDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.springboot.common.LDTConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Dynamic对象", description = "")
@TableName(autoResultMap = true)
public class Dynamic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 软删除
    @ApiModelProperty("软删除")
    @Alias("软删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 内容
    @ApiModelProperty("内容")
    @Alias("内容")
    private String content;

    // 用户id
    @ApiModelProperty("用户id")
    @Alias("用户id")
    private Integer userId;

    // 时间
    @ApiModelProperty("时间")
    @Alias("时间")
    private String time;

    // 浏览量
    @ApiModelProperty("浏览量")
    @Alias("浏览量")
    private Integer view;

    @TableField(exist = false)  // 数据库不存在这个字段
    private User user;
    @TableField(exist = false)
    private Boolean hasPraise; // 是否点赞
    @TableField(exist = false)
    private Boolean hasCollect; // 是否收藏
    @TableField(exist = false)
    private Integer hot;  // 热度
    @TableField(exist = false)
    private Integer praiseCount;  // 点赞数量
    @TableField(exist = false)
    private Integer collectCount;  // 收藏数量
    @TableField(exist = false)
    private Integer commentCount;  // 评论数量

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<String> topics;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<ImgDTO> imgs;


}