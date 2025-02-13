package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.springboot.common.LDTConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author ikun
 * @since 2023-02-18
 */
@Getter
@Setter
@ApiModel(value = "Im对象", description = "")
public class Im implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 用户编号
    @ApiModelProperty("用户编号")
    @Alias("用户编号")
    private String uid;

    // 消息文字
    @ApiModelProperty("消息文字")
    @Alias("消息文字")
    private String text;

    // 发送时间
    @ApiModelProperty("发送时间")
    @Alias("发送时间")
    private String time;

//    // 发送时间
//    @ApiModelProperty("发送时间")
//    @Alias("发送时间")
//    @TableField(fill = FieldFill.INSERT)
//    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
//    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
//    private LocalDateTime createTime;

    // 软删除
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    @TableField(exist = false)  // 数据库不存在这个字段
    private User user;

}