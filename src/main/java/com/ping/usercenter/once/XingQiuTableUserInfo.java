package com.ping.usercenter.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 星球表格用户信息
 * 将对象和表格关联上
 */
@Data
public class XingQiuTableUserInfo {

    /**
     * 星球编号
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     * 用户名称
     */
    @ExcelProperty("成员昵称")
    private String username;
}
