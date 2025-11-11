package com.ping.usercenter.model.enums;

/**
 * 队伍状态枚举
 *
 * @author qiuping
 */
public enum TeamStatusEnum {

    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    ENCRYPT(2, "加密");

    /**
     * 状态值
     */
    private final int value;

    /**
     * 状态值详情
     */
    private final String text;

    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
