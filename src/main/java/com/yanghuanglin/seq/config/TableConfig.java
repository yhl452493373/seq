package com.yanghuanglin.seq.config;

/**
 * 生成器对应的数据库表和字段配置
 *
 * @author yanghuanglin
 * @since 2022/1/30
 */
public class TableConfig {
    /**
     * 当前序号表名，闲置序号表会在该名称后增加后缀_unused,未锁定序号表会在该名称后增加unlock
     */
    private String table = "sequences";

    /**
     * 序号英文名称，和序号类型组成唯一组件
     */
    private String keyColumn = "key";

    /**
     * 序号类型
     */
    private String typeColumn = "type";

    /**
     * 序号值
     */
    private String seqColumn = "seq";

    /**
     * 未锁定序号使用时间
     */
    private String createTimeColumn = "create_time";

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table.toLowerCase();
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn.toLowerCase();
    }

    public String getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(String typeColumn) {
        this.typeColumn = typeColumn.toLowerCase();
    }

    public String getSeqColumn() {
        return seqColumn;
    }

    public void setSeqColumn(String seqColumn) {
        this.seqColumn = seqColumn.toLowerCase();
    }

    public String getCreateTimeColumn() {
        return createTimeColumn;
    }

    public void setCreateTimeColumn(String createTimeColumn) {
        this.createTimeColumn = createTimeColumn.toLowerCase();
    }
}