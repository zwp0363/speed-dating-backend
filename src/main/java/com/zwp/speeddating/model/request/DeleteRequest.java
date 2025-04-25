package com.zwp.speeddating.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  通用删除请求体
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 7458055790657432188L;

    /**
     *
     */
    private Long id;

}
