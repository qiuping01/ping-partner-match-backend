package com.ping.usercenter.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 4470179640842926870L;

    /**
     * 删除id
     */
    private Long id;
}
