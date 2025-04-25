package com.zwp.speeddating.model.dto;

import com.zwp.speeddating.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 队伍查询封装类
 */
@EqualsAndHashCode(callSuper = true)
// callSuper = true: 表示在生成 equals() 和 hashCode() 方法时，需要包含父类 (PageRequest) 的字段
@Data
public class TeamQuery extends PageRequest { // TeamQuery 类 拥有 PageRequest 类所有的字段和方法 (例如 pageSize 和 pageNum)
    /**
     * id
     */
    private Long id;

    /**
     * id列表
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
}
