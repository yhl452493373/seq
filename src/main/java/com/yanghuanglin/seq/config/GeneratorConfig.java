package com.yanghuanglin.seq.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
     * 数据库操作模板
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * 事务处理模板
     */
    private TransactionTemplate transactionTemplate;

    /**
     * 事务管理器
     */
    private DataSourceTransactionManager transactionManager;

    /**
     * 自动创建表
     */
    private Boolean autoCreate = true;

    /**
     * 序号每次增加的步长
     */
    private Integer step = 1;

    /**
     * 默认序号类型
     */
    private String type="DEFAULT";

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

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public DataSourceTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Boolean getAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(Boolean autoCreate) {
        this.autoCreate = autoCreate;
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

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }
}
