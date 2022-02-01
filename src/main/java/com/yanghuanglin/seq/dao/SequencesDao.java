package com.yanghuanglin.seq.dao;

import com.yanghuanglin.seq.po.Sequences;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
public interface SequencesDao {
    /**
     * 查找最后被使用的序号
     */
    Sequences find(Sequences sequences);

    /**
     * 保存新生成的序号
     */
    boolean save(Sequences sequences);

    /**
     * 更新被使用的序号
     */
    boolean update(Sequences sequences);

    void createTable();
}
