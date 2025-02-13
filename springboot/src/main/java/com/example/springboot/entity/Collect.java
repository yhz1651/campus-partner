package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.time.LocalDateTime;
import cn.hutool.core.annotation.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.example.springboot.common.LDTConfig;
import lombok.Getter;
import lombok.Setter;

/**
* <p>
* 
* </p>
*
* @author yhz
* @since 2023-03-29
*/
@Getter
@Setter
@ApiModel(value = "Collect对象", description = "")
public class Collect implements Serializable {

private static final long serialVersionUID = 1L;

    // id
    @ApiModelProperty("id")
    @Alias("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 软删除
    @ApiModelProperty("软删除")
    @Alias("软删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 动态
    @ApiModelProperty("动态")
    @Alias("动态")
    private Integer dynamicId;

    // 收藏人
    @ApiModelProperty("收藏人")
    @Alias("收藏人")
    private Integer userId;

    // 被收藏人
    @ApiModelProperty("被收藏人")
    @Alias("被收藏人")
    private Integer becollectId;

    // 收藏时间
    @ApiModelProperty("收藏时间")
    @Alias("收藏时间")
    private String time;
}