package com.ping.usercenter.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 *
 * @author qiuping
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 7376388125629222827L;

    /**
     * id
     */
    private long id;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 标签列表 json
     */
    private String tags;

    /**
     * 状态 0正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer UserRole;

    /**
     * 星球编号
     */
    private String planetCode;
}

