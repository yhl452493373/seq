package com.cdhncy.seq.dao.impl;

import com.cdhncy.seq.config.TableConfig;
import com.cdhncy.seq.dao.SequencesUnusedDao;
import com.cdhncy.seq.po.SequencesUnused;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class SequencesUnusedDaoImpl implements SequencesUnusedDao {
    private final JdbcTemplate jdbcTemplate;
    private final TableConfig tableConfig;

    public SequencesUnusedDaoImpl(JdbcTemplate jdbcTemplate, TableConfig tableConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableConfig = tableConfig;
    }

    @Override
    public SequencesUnused findMinSeq(SequencesUnused sequencesUnused) {
        String sql = "select * from `%s_ununsed` where `%s`=? and `%s`=? order by `%s` asc limit 0,1";
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
            String sql = "select * from `%s_ununsed` where `%s`=? and `%s`=? order by `%s` desc limit 0,1";
            sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
            return this.jdbcTemplate.queryForObject(sql, rowMapper(), sequencesUnused.getKey(), sequencesUnused.getType());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public boolean delete(SequencesUnused sequencesUnused) {
        String sql = "delete from `%s_ununsed` where `%s`=? and `%s`=? and `%s`=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
        int result = this.jdbcTemplate.update(sql, sequencesUnused.getKey(), sequencesUnused.getType(), sequencesUnused.getSeq());
        return result != 0;
    }

    @Override
    public boolean saveBatch(List<SequencesUnused> sequencesUnusedList) {
        String sql = "insert into `%s_ununsed`(`%s`,`%s`,`%s`) values(?,?,?)";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
        int[] result = this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SequencesUnused sequencesUnused = sequencesUnusedList.get(i);
                ps.setString(1, sequencesUnused.getKey());
                ps.setString(2, sequencesUnused.getType());
                ps.setLong(3, sequencesUnused.getSeq());
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
        String sql = "CREATE TABLE IF NOT EXISTS `%s_ununsed` ( " +
                " `%s` VARCHAR ( 255 ) NOT NULL COMMENT '序号英文名称'," +
                " `%s` VARCHAR ( 255 ) NOT NULL COMMENT '序号类型'," +
                " `%s` BIGINT ( 2 ) NOT NULL COMMENT '闲置的的序号'," +
                " PRIMARY KEY ( `%s`, `%s`, `%s` ) " +
                " ) COMMENT '闲置序号表'";
        sql = String.format(sql, tableConfig.getTable(),
                tableConfig.getKeyColumn(),
                tableConfig.getTypeColumn(),
                tableConfig.getSeqColumn(),
                tableConfig.getKeyColumn(),
                tableConfig.getTypeColumn(),
                tableConfig.getSeqColumn());
        this.jdbcTemplate.execute(sql);
    }

    private RowMapper<SequencesUnused> rowMapper() {
        return (rs, rowNum) -> {
            SequencesUnused result = new SequencesUnused();
            result.setKey(rs.getString(tableConfig.getKeyColumn()));
            result.setType(rs.getString(tableConfig.getTypeColumn()));
            result.setSeq(rs.getLong(tableConfig.getSeqColumn()));
            return result;
        };
    }
}
