package com.yanghuanglin.seq.po;


/**
 * 当前序号
 *
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class Sequences {
    /**
     * 需要生成序号的key，和type组成唯一主键
     */
    protected String key;

    /**
     * 需要生成序号的类型，和key组成唯一主键
     */
    protected String type;

    /**
     * 默认序号
     */
    protected Long seq = 0L;

    public Sequences() {
    }

    public Sequences(Long seq) {
        this.seq = seq;
    }

    public Sequences(String key, String type) {
        this.key = key;
        this.type = type;
    }

    public Sequences(String key, String type, Long seq) {
        this.key = key;
        this.type = type;
        this.seq = seq;
    }

    public Sequences(SequencesUnused sequencesUnused) {
        this.key = sequencesUnused.getKey();
        this.type = sequencesUnused.getType();
        this.seq = sequencesUnused.getSeq();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    /**
     * 将序号增加指定步长
     *
     * @param step 步长
     */
    public void increase(Integer step) {
        this.seq += step;
    }

    /**
     * 序号补零
     *
     * @param minLength 最小长度，低于此长度，会填充零
     * @return 补零后的序号
     */
    public String format(Integer minLength) {
        if (minLength != null)
            return String.format("%0" + minLength + "d", this.seq);
        return String.valueOf(this.seq);
    }
}
