package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Objects;

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
@ApiModel(value = "Information对象", description = "")
public class Information implements Serializable {

private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // 标题
    @ApiModelProperty("标题")
    @Alias("标题")
    private String name;

    // 内容
    @ApiModelProperty("内容")
    @Alias("内容")
    private String content;

    // 发布时间
    @ApiModelProperty("发布时间")
    @Alias("发布时间")
    private String time;

    // 浏览量
    @ApiModelProperty("浏览量")
    @Alias("浏览量")
    private Integer view;

    // 公告附件
    @ApiModelProperty("公告附件")
    @Alias("公告附件")
    private String file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Information information = (Information) o;
        return Objects.equals(id, information.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}