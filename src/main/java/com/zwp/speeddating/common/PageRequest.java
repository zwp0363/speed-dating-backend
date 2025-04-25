package com.zwp.speeddating.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 6898133181260227914L;

    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前页数
     */
    protected int pageNum;
}
