package com.yanghuanglin.seq.config;

import javax.sql.DataSource;

/**
 * 生成器配置
 *
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class GeneratorConfig {
    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * 自动创建表
     */
    private Boolean autoCreate = true;

    /**
     * 序号每次增加的步长
     */
    private Integer step = 1;

    /**
     * 格式化后序号字符串的最小长度，不足的部分补零
     */
    private Integer minLength = 1;

    /**
     * 默认序号类型
     */
    private String type = "DEFAULT";

    /**
     * 月份是否补零。为false时，1月显示为1，为true时，1月显示为01
     */
    private Boolean monthZeroFilling = true;

    /**
     * 日期是否补零。为false时，1日显示为1，为true时，1日显示为01
     */
    private Boolean dayZeroFilling = true;

    /**
     * 表和字段配置
     */
    private TableConfig tableConfig = new TableConfig();

    public GeneratorConfig() {
    }

    public GeneratorConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Boolean getAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(Boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        if (step == 0)
            step = 1;
        this.step = step;
    }

    public Boolean getMonthZeroFilling() {
        return monthZeroFilling;
    }

    public void setMonthZeroFilling(Boolean monthZeroFilling) {
        this.monthZeroFilling = monthZeroFilling;
    }

    public Boolean getDayZeroFilling() {
        return dayZeroFilling;
    }

    public void setDayZeroFilling(Boolean dayZeroFilling) {
        this.dayZeroFilling = dayZeroFilling;
    }

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }
}
