package com.zwp.speeddating.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 7458055790657432188L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
