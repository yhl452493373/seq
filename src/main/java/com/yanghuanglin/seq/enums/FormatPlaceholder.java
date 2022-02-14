package com.yanghuanglin.seq.enums;

/**
 * 格式占位符枚举
 *
 * @author yanghuanglin
 * @since 2022/2/14
 */
public enum FormatPlaceholder {
    /**
     * 序号格式字符中的年
     */
    YEAR("#year#"),

    /**
     * 序号格式字符中的月
     */
    MONTH("#month#"),

    /**
     * 序号格式字符中的日
     */
    DAY("#day#"),

    /**
     * 序号格式字符中的格式化后的序号
     */
    SEQ("#seq#");

    /**
     * 调用toString()后的字符串，这个字符串用于正则匹配（替换格式字符串中的对应占位符）和反向查找（将占位符字符串转换为对应枚举）
     */
    private final String placeholder;

    /**
     * 构造函数
     *
     * @param placeholder 格式占位符
     */
    FormatPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * 将格式占位符转为对应枚举
     *
     * @param placeholder 格式占位符
     * @return 对应枚举
     */
    public static FormatPlaceholder of(String placeholder) {
        FormatPlaceholder[] enumConstants = FormatPlaceholder.class.getEnumConstants();
        for (FormatPlaceholder enumConstant : enumConstants) {
            if (enumConstant.getPlaceholder().equals(placeholder))
                return enumConstant;
        }
        return null;
    }
}
