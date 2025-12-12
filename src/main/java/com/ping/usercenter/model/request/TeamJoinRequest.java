package com.ping.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 加入队伍请求体
 *
 * @author qiuping
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -7823687681313654980L;

    /**
     * 队伍 id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String password;
}
