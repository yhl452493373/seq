package com.yanghuanglin.seq.dao.impl;

import com.yanghuanglin.seq.config.TableConfig;
import com.yanghuanglin.seq.dao.SequencesDao;
import com.yanghuanglin.seq.po.Sequences;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
@SuppressWarnings("SqlResolve")
public class SequencesDaoImpl implements SequencesDao {
    private final JdbcTemplate jdbcTemplate;
    private final TableConfig tableConfig;

    public SequencesDaoImpl(JdbcTemplate jdbcTemplate, TableConfig tableConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableConfig = tableConfig;
    }

    @Override
    public Sequences find(Sequences sequences) {
        String sql = "select * from `%s` where `%s`=? and `%s`=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn());
        try {
            return this.jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Sequences result = new Sequences();
                result.setKey(rs.getString(tableConfig.getKeyColumn()));
                result.setType(rs.getString(tableConfig.getTypeColumn()));
                result.setSeq(rs.getLong(tableConfig.getSeqColumn()));
                return result;
            }, sequences.getKey(), sequences.getType());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public boolean save(Sequences sequences) {
        String sql = "insert into `%s`(`%s`,`%s`,`%s`) values(?,?,?)";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn(), tableConfig.getSeqColumn());
        int result = this.jdbcTemplate.update(sql, sequences.getKey(), sequences.getType(), sequences.getSeq());
        return result != 0;
    }

    @Override
    public boolean update(Sequences sequences) {
        String sql = "update `%s` set `%s`=? where `%s`=? and `%s`=?";
        sql = String.format(sql, tableConfig.getTable(), tableConfig.getSeqColumn(), tableConfig.getKeyColumn(), tableConfig.getTypeColumn());
        int result = this.jdbcTemplate.update(sql, sequences.getSeq(), sequences.getKey(), sequences.getType());
        return result != 0;
    }

    @Override
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `%s` ( " +
                " `%s` VARCHAR ( 64 ) NOT NULL COMMENT '序号英文名称'," +
                " `%s` VARCHAR ( 64 ) NOT NULL COMMENT '序号类型'," +
                " `%s` BIGINT ( 20 ) NOT NULL COMMENT '已使用到的序号'," +
                " PRIMARY KEY ( `%s`, `%s` ) " +
                " ) COMMENT '当前序号表'";
        sql = String.format(sql, tableConfig.getTable(),
                tableConfig.getKeyColumn(),
                tableConfig.getTypeColumn(),
                tableConfig.getSeqColumn(),
                tableConfig.getKeyColumn(),
                tableConfig.getTypeColumn());
        this.jdbcTemplate.execute(sql);
    }
}
