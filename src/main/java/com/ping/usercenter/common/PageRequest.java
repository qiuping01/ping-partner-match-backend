package com.ping.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的分页请求类
 *
 * @author qiuping
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 6593536700388868085L;

    /**
     * 当前页号
     */
    private int pageNum = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;
}
