package com.ping.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 *
 * @author qiuping
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 2894161420019021808L;

    /**
     * 队伍 id
     */
    private Long teamId;

}
