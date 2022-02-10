package com.yanghuanglin.seq.dao;

import com.yanghuanglin.seq.po.SequencesUnused;

import java.util.List;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
public interface SequencesUnusedDao {

    /**
     * 根据key，type查找seq最小的空闲序号
     */
    SequencesUnused findMinSeq(SequencesUnused sequencesUnused);

    /**
     * 根据key，type查找seq最大的空闲序号
     */
    SequencesUnused findMaxSeq(SequencesUnused sequencesUnused);

    /**
     * 根据key，type，seq删除空闲序号
     */
    boolean delete(SequencesUnused sequencesUnused);

    /**
     * 保存空闲序号
     */
    boolean save(SequencesUnused sequencesUnused);

    /**
     * 批量保存空闲序号
     */
    boolean saveBatch(List<SequencesUnused> sequencesUnusedList);

    void createTable();
}
