package com.yanghuanglin.seq.dao.impl;

import com.yanghuanglin.seq.config.TableConfig;
import com.yanghuanglin.seq.dao.SequencesUnlockDao;
import com.yanghuanglin.seq.po.SequencesUnlock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Date;
import java.util.List;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
@SuppressWarnings("SqlResolve")
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
        String sql = "delete from `%s_unlock` where `%s`=? and `%s`=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn());
        if (sequencesUnlock.getSeq() != null) {
            sql += " and `%s`=?";
            sql = String.format(sql, tableConfig.getSeqColumn());
            return this.jdbcTemplate.update(sql, sequencesUnlock.getKey(), sequencesUnlock.getType(), sequencesUnlock.getSeq()) != 0;
        } else {
            return this.jdbcTemplate.update(sql, sequencesUnlock.getKey(), sequencesUnlock.getType()) != 0;
        }
    }

    @Override
    public List<SequencesUnlock> listAll() {
        String sql = "select * from `%s_unlock`";
        sql = String.format(sql, tableConfig.getTable());
        return this.jdbcTemplate.query(sql, rowMapper());
    }

    @Override
    public List<SequencesUnlock> listByDate(Date begin, Date end) {
        String sql;
        if (begin != null && end != null) {
            sql = "select * from `%s_unlock` where `%s`>=? and `%s`<=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.query(sql, rowMapper(), begin, end);
        } else if (begin != null) {
            sql = "select * from `%s_unlock` where `%s`>=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.query(sql, rowMapper(), begin);
        } else if (end != null) {
            sql = "select * from `%s_unlock` where `%s`<=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.query(sql, rowMapper(), end);
        } else {
            return listAll();
        }
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
        String sql;
        if (begin != null && end != null) {
            sql = "delete from `%s_unlock` where `%s`>=? and `%s`<=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.update(sql, begin, end) != 0;
        } else if (begin != null) {
            sql = "delete from `%s_unlock` where `%s`>=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.update(sql, begin) != 0;
        } else if (end != null) {
            sql = "delete from `%s_unlock` where `%s`<=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.update(sql, end) != 0;
        } else {
            return deleteAll();
        }
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `%s_unlock` ( " +
                " `%s` VARCHAR ( 64 ) NOT NULL COMMENT '??????????????????'," +
                " `%s` VARCHAR ( 64 ) NOT NULL COMMENT '????????????'," +
                " `%s` BIGINT ( 20 ) NOT NULL COMMENT '?????????????????????'," +
                " `%s` DATETIME NOT NULL COMMENT '????????????'," +
                " PRIMARY KEY ( `%s`, `%s` ,`%s` ) " +
                " ) COMMENT '??????????????????'";
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
