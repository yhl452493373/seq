package com.cdhncy.seq.po;


/**
 * 闲置序号
 *
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class SequencesUnused extends Sequences {
    public SequencesUnused() {
    }

    public SequencesUnused(Sequences sequences) {
        this.key = sequences.getKey();
        this.type = sequences.getType();
        this.seq = sequences.getSeq();
    }
}
