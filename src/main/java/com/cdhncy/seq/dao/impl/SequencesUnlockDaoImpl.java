package com.cdhncy.seq.dao.impl;

import com.cdhncy.seq.config.TableConfig;
import com.cdhncy.seq.dao.SequencesUnlockDao;
import com.cdhncy.seq.po.SequencesUnlock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Date;
import java.util.List;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class SequencesUnlockDaoImpl implements SequencesUnlockDao {
    private final JdbcTemplate jdbcTemplate;
    private final TableConfig tableConfig;

    public SequencesUnlockDaoImpl(JdbcTemplate jdbcTemplate, TableConfig tableConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableConfig = tableConfig;
    }

    @Override
    public boolean save(SequencesUnlock sequencesUnlock) {
        String sql = "insert into `%s_unlock`(`%s`,`%s`,`%s`,`%s`) values(?,?,?,?)";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn(), tableConfig.getCreateTimeColumn());
        int result = this.jdbcTemplate.update(sql, sequencesUnlock.getKey(), sequencesUnlock.getType(), sequencesUnlock.getSeq(), sequencesUnlock.getCreateTime());
        return result != 0;
    }

    @Override
    public boolean delete(SequencesUnlock sequencesUnlock) {
        String sql = "delete from `%s_unlock` where `%s`=? and `%s`=? and `%s`=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
        int result = this.jdbcTemplate.update(sql, sequencesUnlock.getKey(), sequencesUnlock.getType(), sequencesUnlock.getSeq());
        return result != 0;
    }

    @Override
    public List<SequencesUnlock> listAll() {
        String sql = "select * from `%s_unlock`";
        sql = String.format(sql, tableConfig.getTable());
        return this.jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public List<SequencesUnlock> listByDate(Date begin, Date end) {
        String sql = "select * from `%s_unlock` where `%s`>=? and `%s`<=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn(), tableConfig.getCreateTimeColumn());
        return this.jdbcTemplate.query(sql, rowMapper(), begin, end);
    }

    @Override
    public boolean deleteAll() {
        String sql = "delete from `%s_unlock`";
        sql = String.format(sql, tableConfig.getTable());
        int result = this.jdbcTemplate.update(sql);
        return result != 0;
    }

    @Override
    public boolean deleteByDate(Date begin, Date end) {
        String sql = "delete from `%s_unlock` where `%s`>=? and `%s`<=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn(), tableConfig.getCreateTimeColumn());
        int result = this.jdbcTemplate.update(sql, begin, end);
        return result != 0;
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `%s_unlock` ( " +
                " `%s` VARCHAR ( 255 ) NOT NULL COMMENT '序号英文名称'," +
                " `%s` VARCHAR ( 255 ) NOT NULL COMMENT '序号类型'," +
                " `%s` BIGINT ( 2 ) NOT NULL COMMENT '尚未锁定的序号'," +
                " `%s` DATETIME NOT NULL COMMENT '使用时间'," +
                " PRIMARY KEY ( `%s`, `%s` ,`%s` ) " +
                " ) COMMENT '未锁定序号表'";
        sql = String.format(sql, tableConfig.getTable(),
                tableConfig.getKeyColumn(),
                tableConfig.getTypeColumn(),
                tableConfig.getSeqColumn(),
                tableConfig.getCreateTimeColumn(),
                tableConfig.getKeyColumn(),
                tableConfig.getTypeColumn(),
                tableConfig.getSeqColumn());
        this.jdbcTemplate.execute(sql);
    }

    private RowMapper<SequencesUnlock> rowMapper() {
        return (rs, rowNum) -> {
            SequencesUnlock sequencesUnlock = new SequencesUnlock();
            sequencesUnlock.setKey(rs.getString(tableConfig.getKeyColumn()));
            sequencesUnlock.setType(rs.getString(tableConfig.getTypeColumn()));
            sequencesUnlock.setSeq(rs.getLong(tableConfig.getSeqColumn()));
            sequencesUnlock.setCreateTime(rs.getDate(tableConfig.getCreateTimeColumn()));
            return sequencesUnlock;
        };
    }
}
