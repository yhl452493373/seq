package com.yanghuanglin.seq.dao.impl;

import com.yanghuanglin.seq.config.TableConfig;
import com.yanghuanglin.seq.dao.SequencesUnusedDao;
import com.yanghuanglin.seq.po.SequencesUnused;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
@SuppressWarnings("SqlResolve")
public class SequencesUnusedDaoImpl implements SequencesUnusedDao {
    private final JdbcTemplate jdbcTemplate;
    private final TableConfig tableConfig;

    public SequencesUnusedDaoImpl(JdbcTemplate jdbcTemplate, TableConfig tableConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableConfig = tableConfig;
    }

    @Override
    public SequencesUnused findMinSeq(SequencesUnused sequencesUnused) {
        String sql = "select * from `%s_unused` where `%s`=? and `%s`=? order by `%s` asc limit 0,1";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
        try {
            return this.jdbcTemplate.queryForObject(sql, rowMapper(), sequencesUnused.getKey(), sequencesUnused.getType());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public SequencesUnused findMaxSeq(SequencesUnused sequencesUnused) {
        try {
            String sql = "select * from `%s_unused` where `%s`=? and `%s`=? order by `%s` desc limit 0,1";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
            return this.jdbcTemplate.queryForObject(sql, rowMapper(), sequencesUnused.getKey(), sequencesUnused.getType());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public boolean delete(SequencesUnused sequencesUnused) {
        String sql = "delete from `%s_unused` where `%s`=? and `%s`=? and `%s`=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
        int result = this.jdbcTemplate.update(sql, sequencesUnused.getKey(), sequencesUnused.getType(), sequencesUnused.getSeq());
        return result != 0;
    }

    @Override
    public boolean save(SequencesUnused sequencesUnused) {
        String sql = "insert into `%s_unused`(`%s`,`%s`,`%s`,`%s`) values(?,?,?,?)";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn(), tableConfig.getCreateTimeColumn());
        int result = this.jdbcTemplate.update(sql, sequencesUnused.getKey(), sequencesUnused.getType(), sequencesUnused.getSeq(), sequencesUnused.getCreateTime());
        return result != 0;
    }

    @Override
    public boolean saveBatch(List<SequencesUnused> sequencesUnusedList) {
        String sql = "insert into `%s_unused`(`%s`,`%s`,`%s`,`%s`) values(?,?,?,?)";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn(), tableConfig.getCreateTimeColumn());
        int[] result = this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SequencesUnused sequencesUnused = sequencesUnusedList.get(i);
                ps.setString(1, sequencesUnused.getKey());
                ps.setString(2, sequencesUnused.getType());
                ps.setLong(3, sequencesUnused.getSeq());
                ps.setTimestamp(4, new Timestamp(sequencesUnused.getCreateTime().getTime()));
            }

            @Override
            public int getBatchSize() {
                return sequencesUnusedList.size();
            }
        });
        return result.length != 0;
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `%s_unused` ( " +
                " `%s` VARCHAR ( 64 ) NOT NULL COMMENT '序号英文名称'," +
                " `%s` VARCHAR ( 64 ) NOT NULL COMMENT '序号类型'," +
                " `%s` BIGINT ( 20 ) NOT NULL COMMENT '闲置的的序号'," +
                " `%s` DATETIME NOT NULL COMMENT '设为闲置序号的时间'," +
                " PRIMARY KEY ( `%s`, `%s`, `%s` ) " +
                " ) COMMENT '闲置序号表'";
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

    @Override
    public boolean deleteAll() {
        String sql = "delete from `%s_unused`";
        sql = String.format(sql, tableConfig.getTable());
        int result = this.jdbcTemplate.update(sql);
        return result != 0;
    }

    @Override
    public boolean deleteByDate(Date begin, Date end) {
        String sql;
        if (begin != null && end != null) {
            sql = "delete from `%s_unused` where `%s`>=? and `%s`<=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.update(sql, begin, end) != 0;
        } else if (begin != null) {
            sql = "delete from `%s_unused` where `%s`>=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.update(sql, begin) != 0;
        } else if (end != null) {
            sql = "delete from `%s_unused` where `%s`<=?";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getCreateTimeColumn());
            return this.jdbcTemplate.update(sql, end) != 0;
        } else {
            return deleteAll();
        }
    }

    private RowMapper<SequencesUnused> rowMapper() {
        return (rs, rowNum) -> {
            SequencesUnused sequencesUnused = new SequencesUnused();
            sequencesUnused.setKey(rs.getString(tableConfig.getKeyColumn()));
            sequencesUnused.setType(rs.getString(tableConfig.getTypeColumn()));
            sequencesUnused.setSeq(rs.getLong(tableConfig.getSeqColumn()));
            sequencesUnused.setCreateTime(rs.getDate(tableConfig.getCreateTimeColumn()));
            return sequencesUnused;
        };
    }
}
