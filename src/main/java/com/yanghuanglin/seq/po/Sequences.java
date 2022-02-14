package com.yanghuanglin.seq.po;

import com.yanghuanglin.seq.config.BaseConfig;

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

    /**
     * 临时字段，序号对应的年份（如2022年）（如果能获取到的话）。
     * 该字段仅用于解析序号字符串时，解析出对应年份，用于合成key（如：序号对应的key为SNT+年份，返回的为其年份）
     */
    private transient Integer year;

    /**
     * 临时字段，序号对应的月份（如2月）（如果能获取到的话）。
     * 该字段仅用于解析序号字符串时，解析出对应月份，用于合成key（如：序号对应的key为SNT+年份+月份，返回的为其月份）
     */
    private transient Integer month;

    /**
     * 临时字段，序号对应的日期（如1日）（如果能获取到的话）。
     * 该字段仅用于解析序号字符串时，解析出对应日期，用于合成key（如：序号对应的key为SNT+年份+月份+日期，返回的为其日期）
     */
    private transient Integer day;

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
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
     * @return 补零后的序号，若未单独设置序号的长度，则最小长度为{@link BaseConfig#getMinLength()}长度；否则为修改后的长度，不足部分补零
     */
    public String format() {
        BaseConfig baseConfig = BaseConfig.getInstance();
        if (baseConfig.getMinLength() != null)
            return String.format("%0" + baseConfig.getMinLength() + "d", this.seq);
        return String.valueOf(this.seq);
    }

    /**
     * 序号补零
     *
     * @param minLength 最小长度，低于此长度，会补零
     * @return 补零后的序号
     */
    public String format(Integer minLength) {
        if (minLength != null)
            return String.format("%0" + minLength + "d", this.seq);
        return String.valueOf(this.seq);
    }

    @Override
    public String toString() {
        return "Sequences{" +
                "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", seq=" + seq +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}
