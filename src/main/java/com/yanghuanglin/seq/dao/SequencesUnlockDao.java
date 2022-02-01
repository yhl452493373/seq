package com.yanghuanglin.seq.dao;

import com.yanghuanglin.seq.po.SequencesUnlock;

import java.util.Date;
import java.util.List;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
public interface SequencesUnlockDao {
    /**
     * 保存使用中的序号
     */
    boolean save(SequencesUnlock sequencesUnlock);

    /**
     * 删除使用中的序号
     */
    boolean delete(SequencesUnlock sequencesUnlock);

    /**
     * 列出所有使用中的序号
     */
    List<SequencesUnlock> listAll();

    /**
     * 列出指定时间段内使用中的序号
     */
    List<SequencesUnlock> listByDate(Date begin, Date end);

    /**
     * 删除所有使用中的序号
     */
    boolean deleteAll();

    /**
     * 删除指定时间段内使用中的序号
     */
    boolean deleteByDate(Date begin, Date end);

    void createTable();
}
