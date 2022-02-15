package com.yanghuanglin.seq.po;


import java.util.Date;

/**
 * 闲置序号
 *
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class SequencesUnused extends Sequences {
    private Date createTime;

    public SequencesUnused() {
    }

    public SequencesUnused(Sequences sequences) {
        this.key = sequences.getKey();
        this.type = sequences.getType();
        this.seq = sequences.getSeq();
    }

    public SequencesUnused(Sequences sequences, Date createTime) {
        this.key = sequences.getKey();
        this.type = sequences.getType();
        this.seq = sequences.getSeq();
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SequencesUnused{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", seq=" + seq +
                ", createTime=" + createTime +
                '}';
    }
}
