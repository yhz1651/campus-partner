package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDate;

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
@ApiModel(value = "Notice对象", description = "")
public class Notice implements Serializable {

private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 名称
    @ApiModelProperty("名称")
    @Alias("名称")
    private String name;

    // 内容
    @ApiModelProperty("内容")
    @Alias("内容")
    private String content;

    // 时间
    @ApiModelProperty("时间")
    @Alias("时间")
    private String time;

    // 创建人
    @ApiModelProperty("创建人")
    @Alias("创建人")
    private String user;

    // 创建人id
    @ApiModelProperty("创建人id")
    @Alias("创建人id")
    private Integer userid;
}