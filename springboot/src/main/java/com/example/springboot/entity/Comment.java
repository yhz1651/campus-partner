package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
@ApiModel(value = "Comment对象", description = "")
public class Comment implements Serializable {

private static final long serialVersionUID = 1L;

    // id
    @ApiModelProperty("id")
    @Alias("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 逻辑删除
    @ApiModelProperty("逻辑删除")
    @Alias("逻辑删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    // 内容
    @ApiModelProperty("内容")
    @Alias("内容")
    private String content;

    // 用户
    @ApiModelProperty("用户")
    @Alias("用户")
    private Integer userId;

    // 动态
    @ApiModelProperty("动态")
    @Alias("动态")
    private Integer dynamicId;

    // 时间
    @ApiModelProperty("时间")
    @Alias("时间")
    private String time;

    // 父级id
    @ApiModelProperty("父级id")
    @Alias("父级id")
    private Integer pid;

    // 属地
    @ApiModelProperty("属地")
    @Alias("属地")
    private String location;

//    // 父级用户id
//    @ApiModelProperty("父级用户id")
//    @Alias("父级用户id")
//    private Integer puserId;

    @TableField(exist = false)
    private List<Comment> children;

    @TableField(exist = false)
    private User user;

    private Integer puserId;
    // 回复的对象
    @TableField(exist = false)
    private User pUser;


    @TableField(exist = false)
    private Boolean hasPraise; // 是否点赞

    @TableField(exist = false)
    private Integer praiseCount;  // 点赞数量

}