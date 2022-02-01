package com.yanghuanglin.seq.po;


import java.util.Date;

/**
 * 未锁定序号
 *
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class SequencesUnlock extends Sequences {
    private Date createTime;

    public SequencesUnlock() {
    }

    public SequencesUnlock(Sequences sequences) {
        this.key = sequences.getKey();
        this.type = sequences.getType();
        this.seq = sequences.getSeq();
    }

    public SequencesUnlock(SequencesUnused sequencesUnused) {
        this.key = sequencesUnused.getKey();
        this.type = sequencesUnused.getType();
        this.seq = sequencesUnused.getSeq();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
