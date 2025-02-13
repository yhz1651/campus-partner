package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import cn.hutool.core.annotation.Alias;
import com.example.springboot.common.LDTConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "User对象", description = "")
public class User implements Serializable {

private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 用户名
    @ApiModelProperty("用户名")
    @Alias("用户名")
    private String username;

    // 密码
    @ApiModelProperty("密码")
    @Alias("密码")
    private String password;

    // 昵称
    @ApiModelProperty("昵称")
    @Alias("昵称")
    private String name;

    // 性别
    @ApiModelProperty("性别")
    @Alias("性别")
    private String sex;

    // 出生日期
    @ApiModelProperty("出生日期")
    @Alias("出生日期")
    private String birthday;

    // 邮箱
    @ApiModelProperty("邮箱")
    @Alias("邮箱")
    private String email;

    // 地址
    @ApiModelProperty("地址")
    @Alias("地址")
    private String address;

    // 学校
    @ApiModelProperty("学校")
    @Alias("学校")
    private String school;

    // 简介
    @ApiModelProperty("简介")
    @Alias("简介")
    private String profile;

    // 用户唯一id
    @ApiModelProperty("用户唯一id")
    @Alias("用户唯一id")
    private String uid;

    @ApiModelProperty("头像")
    @Alias("头像")
    private String avatar;

    // 逻辑删除 0存在  id删除
    @ApiModelProperty("逻辑删除 0存在  id删除")
    @Alias("逻辑删除 0存在  id删除")
    @TableLogic(value = "0", delval = "id")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LDTConfig.CmzLdtDeSerializer.class)
    @JsonSerialize(using = LDTConfig.CmzLdtSerializer.class)
    private LocalDateTime updateTime;

    @ApiModelProperty("角色")
    @Alias("角色")
    private String role;

    @ApiModelProperty("积分")
    @Alias("积分")
    private Integer score;

    @ApiModelProperty("是否禁用")
    @Alias("是否禁用")
    private Boolean status;

    @ApiModelProperty("是否自动审核预约")
    @Alias("是否自动审核预约")
    private Boolean autoReview;

    @TableField(exist = false)
    private Integer followCount;  // 关注数量

    @TableField(exist = false)
    private Integer beFollowCount;  // 被关注数量

    @TableField(exist = false)
    private Integer bePraiseCount;  // 获赞数量

    @TableField(exist = false)
    private Boolean hasFollow; // 是否关注他

    @TableField(exist = false)
    private Boolean hasBeFollow; // 是否被他关注

    @TableField(exist = false)
    private Integer age; // 年龄





}