package com.yanghuanglin.seq.generator;

import com.yanghuanglin.seq.po.Sequences;
import com.yanghuanglin.seq.po.SequencesUnlock;
import com.yanghuanglin.seq.po.SequencesUnused;

import java.util.Date;

public interface Generator {
    /**
     * 序号格式字符中的年
     */
    String YEAR = "#year#";
    /**
     * 序号格式字符中的月
     */
    String MONTH = "#month#";
    /**
     * 序号格式字符中的日
     */
    String DAY = "#day#";
    /**
     * 序号格式字符中的格式化后的序号
     */
    String SEQ = "#seq#";

    /**
     * 根据传入的key和type生成可用的序号对象。
     * <p/>
     * 如果根据key和type在{@link Sequences}中找不到记录，说明该组合的序号对象还未初次生成，返回的是seq为step的序号对象，该对象数据会写入到{@link SequencesUnlock}中。
     * <p/>
     * 如果根据key和type在{@link Sequences}中找到了记录，且在{@link SequencesUnused}也找到了记录，说明该组合生成的序号有部分未使用，返回的是{@link SequencesUnused}中找到的seq最小的序号对象。同时会将{@link SequencesUnused}中找到的seq最小的记录删除，然后写入到{@link SequencesUnlock}中。
     * <p/>
     *
     * @param key  数据字典中的编码
     * @param type 序号类型
     * @return 可用的序号对象
     */
    Sequences generate(String key, String type);

    /**
     * 返回根据{@link #generate(String, String)}得到的序号对象，补零后的序号字符串
     * <p/>
     * 如生成的为3，而minLength为5，则返回的是00003
     *
     * @param key       数据字典中的编码
     * @param type      序号类型
     * @param minLength 序号数字最小长度
     * @return 补零后的字符串
     */
    String generate(String key, String type, Integer minLength);

    /**
     * 将{@link #generate(String, String)}得到的序号对象格式化为补零后的序号字符串。实际上只会用到{@link Sequences#getSeq()}属性
     *
     * @param sequences 生成的序号对象
     * @param minLength 序号数字最小长度
     * @param pattern   格式
     * @return 格式化后的字符串
     */
    String format(Sequences sequences, Integer minLength, String pattern);

    /**
     * 将生成的序号对象格式化为指定格式
     * <p/>
     * pattern支持：{@link #YEAR}(当前年份)、{@link #MONTH}(当前月份)、{@link #DAY}(当前日期)、{@link #SEQ}(生成的字符串序号)四个变量
     * <p/>
     * seq为1，minLength为4，pattern为#year##month##day#6#seq#，则会格式化为2022013060001。此序号含义如下：
     * <p/>
     * 序号格式：[年][月][日][固定6开头][序号1，最小位数为4位，不足4位则补零]
     *
     * @param seq       需要格式化的序号
     * @param minLength 序号最小长度，不足的会补零
     * @param pattern   格式
     * @return 格式化后的字符串
     */
    String format(Long seq, Integer minLength, String pattern);

    /**
     * 将生成的序号对象格式化为指定格式
     * <p/>
     * pattern支持：{@link #YEAR}(当前年份)、{@link #MONTH}(当前月份)、{@link #DAY}(当前日期)、{@link #SEQ}(生成的字符串序号)四个变量
     * <p/>
     * seq为1,start为6，minLength为4，pattern为#year##month##day##seq#，则会格式化为2022013060001。此序号含义如下：
     * <p/>
     * 序号格式：[年][月][日][固定6开头][序号1，最小位数为4位，不足4位则补零]
     *
     * @param seq       需要格式化的序号
     * @param start     序号格式化后以什么字符串开头
     * @param minLength 序号最小长度，不足的会补零
     * @param pattern   格式
     * @return 格式化后的字符串
     */
    String format(Long seq, String start, Integer minLength, String pattern);

    /**
     * 锁定指定序号，在序号生成后，调用该序号的逻辑完成后需要执行此方法
     * <p/>
     * 如办理案件时，先调用{@link #generate(String, String)}或者{@link #generate(String, String, Integer)}生成了序号，之后对案件进行了入库，如果入库完毕，则将该序号锁定，说明这个序号已被使用
     * <p/>
     * 注意，此处的锁定非数据库中锁定，而是{@link SequencesUnused}和{@link SequencesUnlock}中均不存在key、type、seq相同的记录视为锁定。因此此处实际是把这两个表中的记录均删除了
     *
     * @param sequences 需要锁定的序号
     * @return 锁定结果
     */
    boolean lock(Sequences sequences);

    /**
     * 释放所有未使用的序号
     * <p/>
     * {@link SequencesUnlock}中未通过{@link #lock(Sequences)}方法锁定的序号会一直存在，调用此方法会将里面的所有序号都移动到{@link SequencesUnused}中，下次生成序号时优先从{@link SequencesUnused}获取。
     */
    void release();

    /**
     * 释放指定时间段内未使用的序号
     * <p/>
     * {@link SequencesUnlock}中未通过{@link #lock(Sequences)}方法锁定的序号会一直存在，调用此方法会将里面的所有序号都移动到{@link SequencesUnused}中，下次生成序号时优先从{@link SequencesUnused}获取。
     *
     * @param begin 开始时间
     * @param end   结束时间
     */
    void release(Date begin, Date end);

    /**
     * 释放指定序号。一般用于业务对象删除后，对应序号需要回收使用时。
     *
     * @param sequences 需要释放的序号。一般是一个通过{@link Sequences#setKey(String)}、{@link Sequences#setType(String)}、{@link Sequences#setSeq(Long)}三方法一起手动构建或通过{@link Sequences#Sequences(String, String, Long)}构造方法构建的实例对象
     */
    void release(Sequences sequences);
}
