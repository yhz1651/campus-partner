package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.springboot.common.LDTConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Activity对象", description = "")
public class Activity implements Serializable {

private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 活动内容
    @ApiModelProperty("活动内容")
    @Alias("活动内容")
    private String content;

    // 开始时间
    @ApiModelProperty("开始时间")
    @Alias("开始时间")
    private String startTime;

    // 结束时间
    @ApiModelProperty("结束时间")
    @Alias("结束时间")
    private String endTime;

    // 发起人id
    @ApiModelProperty("发起人id")
    @Alias("发起人id")
    private Integer userId;

    // 发起人姓名
    @TableField(exist = false)
    private String creator;

    // 活动地点
    @ApiModelProperty("活动地点")
    @Alias("活动地点")
    private String location;

    // 活动经费
    @ApiModelProperty("活动经费")
    @Alias("活动经费")
    private BigDecimal fee;

    // 活动图片
    @ApiModelProperty("活动图片")
    @Alias("活动图片")
    private String img;

    // 活动文件
    @ApiModelProperty("活动文件")
    @Alias("活动文件")
    private String file;

    // 活动状态
    @ApiModelProperty("活动状态")
    @Alias("活动状态")
    private String status;

    // 总人数
    @ApiModelProperty("总人数")
    @Alias("总人数")
    private Integer nums;

    // 剩余可预约人数
    @ApiModelProperty("剩余可预约人数")
    @Alias("剩余可预约人数")
    private Integer leftNums;

    // 创建时间
    @ApiModelProperty("创建时间")
    @Alias("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    // 更新时间
    @ApiModelProperty("更新时间")
    @Alias("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime updateTime;

    // 逻辑删除
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 是否预约
    @TableField(exist = false)
    private Boolean hasReserve; // 是否预约
}