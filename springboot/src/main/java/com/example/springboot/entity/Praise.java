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

@Getter
@Setter
@ApiModel(value = "Praise对象", description = "")
public class Praise implements Serializable {

private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 类型
    @ApiModelProperty("类型")
    @Alias("类型")
    private String type;

    // 模块内容id
    @ApiModelProperty("模块内容id")
    @Alias("模块内容id")
    private Integer fid;

    // 用户id
    @ApiModelProperty("用户id")
    @Alias("用户id")
    private Integer userId;

    // 被点赞用户id
    @ApiModelProperty("被点赞用户id")
    @Alias("被点赞用户id")
    private Integer bepraiseId;

    // 点赞时间
    @ApiModelProperty("点赞时间")
    @Alias("点赞时间")
    private String time;

    // 逻辑删除
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;
}