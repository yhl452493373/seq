package com.yanghuanglin.seq.generator.impl;

import com.yanghuanglin.seq.config.GeneratorConfig;
import com.yanghuanglin.seq.dao.SequencesDao;
import com.yanghuanglin.seq.dao.SequencesUnlockDao;
import com.yanghuanglin.seq.dao.SequencesUnusedDao;
import com.yanghuanglin.seq.dao.impl.SequencesDaoImpl;
import com.yanghuanglin.seq.dao.impl.SequencesUnlockDaoImpl;
import com.yanghuanglin.seq.dao.impl.SequencesUnusedDaoImpl;
import com.yanghuanglin.seq.generator.Generator;
import com.yanghuanglin.seq.po.Sequences;
import com.yanghuanglin.seq.po.SequencesUnlock;
import com.yanghuanglin.seq.po.SequencesUnused;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequencesGenerator implements Generator {
    private final TransactionTemplate transactionTemplate;
    private final SequencesDao sequencesDao;
    private final SequencesUnusedDao sequencesUnusedDao;
    private final SequencesUnlockDao sequencesUnlockDao;
    private final Integer step;

    public SequencesGenerator(GeneratorConfig generatorConfig) {
        //数据库操作模板
        JdbcTemplate jdbcTemplate = generatorConfig.getJdbcTemplate();

        if (jdbcTemplate == null) {
            //数据源
            DataSource dataSource = generatorConfig.getDataSource();
            if (dataSource == null)
                //若数据库操作模板为空，也没有配置数据源，则抛出异常
                throw new NullPointerException("数据源不能为空");
            //否则以数据源创建数据库操作模板
            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        if (generatorConfig.getTransactionTemplate() == null) {
            //若没有配置事务操作模板，则从配置中取事务管理器
            DataSourceTransactionManager transactionManager = generatorConfig.getTransactionManager();
            if (transactionManager == null) {
                //若未配置事务管理器，则通过数据源新建
                DataSource dataSource = jdbcTemplate.getDataSource();
                if (dataSource == null)
                    throw new NullPointerException("数据源不能为空");
                transactionManager = new DataSourceTransactionManager(dataSource);
            }
            //通过事务管理器创建事务操作模板
            transactionTemplate = new TransactionTemplate(transactionManager);
        } else {
            //获取事务操作模板
            transactionTemplate = generatorConfig.getTransactionTemplate();
        }

        this.sequencesDao = new SequencesDaoImpl(jdbcTemplate, generatorConfig.getTableConfig());
        this.sequencesUnusedDao = new SequencesUnusedDaoImpl(jdbcTemplate, generatorConfig.getTableConfig());
        this.sequencesUnlockDao = new SequencesUnlockDaoImpl(jdbcTemplate, generatorConfig.getTableConfig());
        this.step = generatorConfig.getStep();

        autoCreateTable(generatorConfig.getAutoCreate());
    }

    /**
     * 自动创建需要的表
     */
    private void autoCreateTable(Boolean autoCreate) {
        if (!autoCreate)
            return;
        this.sequencesDao.createTable();
        this.sequencesUnusedDao.createTable();
        this.sequencesUnlockDao.createTable();
    }

    @Override
    public synchronized Sequences generate(String key, String type) {
        return transactionTemplate.execute(status -> {
            try {
                //根据传入的key和type新生成查询条件对象
                Sequences condition = new Sequences(key, type);

                //找到正在使用的最大序号
                Sequences sequences = sequencesDao.find(condition);
                if (sequences == null) {
                    //不存在，说明还没生成，将新生成的入库，此时序号为默认的0
                    sequences = condition;
                    sequencesDao.save(sequences);
                }

                //根据传入的key和type查找空闲编号最小的一个
                SequencesUnused conditionIdle = new SequencesUnused(condition);
                SequencesUnused sequencesUnused = sequencesUnusedDao.findMinSeq(conditionIdle);

                if (sequencesUnused == null) {
                    //空闲编号不存在，说明是未生成过，序号需要增加后直接使用，同时将新生成的写入到使用中表
                    sequences.increase(step);
                    SequencesUnlock sequencesUnlock = new SequencesUnlock(sequences);
                    sequencesUnlock.setCreateTime(new Date());

                    sequencesDao.update(sequences);
                    sequencesUnlockDao.save(sequencesUnlock);
                } else {
                    //空闲编号存在，说明已经生成过，序号不需要增加，直接使用。同时将该空闲编号移动到使用中表
                    sequences = new Sequences(sequencesUnused);
                    SequencesUnlock sequencesUnlock = new SequencesUnlock(sequencesUnused);
                    sequencesUnlock.setCreateTime(new Date());

                    sequencesUnlockDao.save(sequencesUnlock);
                    sequencesUnusedDao.delete(sequencesUnused);
                }
                return sequences;
            } catch (Exception e) {
                e.printStackTrace();
                status.setRollbackOnly();
                return null;
            }
        });
    }

    @Override
    public synchronized String generate(String key, String type, Integer minLength) {
        Sequences sequences = this.generate(key, type);
        if (sequences == null)
            return null;
        return sequences.format(minLength);
    }

    @Override
    public String format(Sequences sequences, Integer minLength, String pattern) {
        return format(sequences.getSeq(), minLength, pattern);
    }

    @Override
    public String format(Long seq, Integer minLength, String pattern) {
        return format(seq, null, minLength, pattern);
    }

    @Override
    public String format(Long seq, String start, Integer minLength, String pattern) {
        if (start == null)
            start = "";
        String seqString = start + new Sequences(seq).format(minLength);
        Calendar calendar = Calendar.getInstance();
        pattern = pattern.replaceAll(Generator.YEAR, String.valueOf(calendar.get(Calendar.YEAR)));
        pattern = pattern.replaceAll(Generator.MONTH, String.format("%02d", calendar.get(Calendar.MONTH) + 1));
        pattern = pattern.replaceAll(Generator.DAY, String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        pattern = pattern.replaceAll(Generator.SEQ, seqString);
        return pattern;
    }

    @Override
    public Sequences parse(String formatted, String pattern) {
        //年、月、日、序号分隔特殊符号正则规则
        String splitRegString = "(" + YEAR + "|" + MONTH + "|" + DAY + "|" + SEQ + ")";
        //根据年、月、日、序号的特殊符号，对格式进行分隔，得到排除特殊符号后的字符串数组
        String[] split = pattern.split(splitRegString);

        for (String splitString : split) {
            //用排除特殊符号后的字符串依次替换序号格式字符串和格式化后的序号字符串，得到纯净的序号字符串和序号格式对应关系
            pattern = pattern.replace(splitString, "");
            formatted = formatted.replace(splitString, "");
        }

        //年、月、日的数字匹配正则规则
        String yearRegStr = "\\d{4}";
        String monthRegStr = "\\d{2}";
        String dayRegStr = "\\d{2}";

        //将序号格式分隔特殊符号字符串转为正则匹配规则
        Pattern seqPattern = Pattern.compile(splitRegString);
        //对序号格式进行匹配
        Matcher matcher = seqPattern.matcher(pattern);

        //将年、月、日匹配规则字符串转为正则匹配规则
        Pattern yearPattern = Pattern.compile(yearRegStr);
        Pattern monthPattern = Pattern.compile(monthRegStr);
        Pattern dayPattern = Pattern.compile(dayRegStr);

        //默认的年、月、日均为空字符串
        String year = "", month = "", day = "", seq = "";
        //根据序号匹配规则字符串查找字符串分组
        while (matcher.find()) {
            String group = matcher.group();
            switch (group) {
                case Generator.YEAR:
                    //若分组为年份分组，则将年份正则匹配到的字符串赋值给year，同时把格式化后的序号字符串中，对应年的字符串替换为空字符串
                    Matcher yearMatcher = yearPattern.matcher(formatted);
                    if (yearMatcher.find()) {
                        year = yearMatcher.group();
                    }
                    formatted = formatted.replaceFirst(yearRegStr, "");
                    break;
                case Generator.MONTH:
                    //若分组为月份分组，则将月份正则匹配到的字符串赋值给month，同时把格式化后的序号字符串中，对应月的字符串替换为空字符串
                    Matcher monthMatcher = monthPattern.matcher(formatted);
                    if (monthMatcher.find()) {
                        month = monthMatcher.group();
                    }
                    formatted = formatted.replaceFirst(monthRegStr, "");
                    break;
                case Generator.DAY:
                    //若分组为日期分组，则将日期正则匹配到的字符串赋值给day，同时把格式化后的序号字符串中，对应日期的字符串替换为空字符串
                    Matcher dayMatcher = dayPattern.matcher(formatted);
                    if (dayMatcher.find()) {
                        day = dayMatcher.group();
                    }
                    formatted = formatted.replaceFirst(dayRegStr, "");
                    break;
            }
        }
        //经过以上替换后，最后剩下的为序号，这个序号可能是补零了的，需要调用Long.parseLong来去零
        seq = formatted;

        //构建一个新的序号对象
        Sequences sequences = new Sequences();
        //用获取到的年、月、日、序号给新构建的序号对象设置对应的值
        sequences.setYear(StringUtils.hasLength(year) ? Integer.valueOf(year) : null);
        sequences.setMonth(StringUtils.hasLength(month) ? Integer.valueOf(month) : null);
        sequences.setDay(StringUtils.hasLength(day) ? Integer.valueOf(day) : null);
        sequences.setSeq(StringUtils.hasLength(seq) ? Long.parseLong(seq) : 0L);

        return sequences;
    }

    @Override
    public synchronized boolean lock(Sequences sequences) {
        SequencesUnlock condition = new SequencesUnlock(sequences);
        //将使用中表的对应数据删除，空闲表中数据在生成时会删除，因此这里不需要处理该表
        return sequencesUnlockDao.delete(condition);
    }

    @Override
    public void release() {
        //列出所有使用中表存在的序号
        List<SequencesUnlock> sequencesUnlockList = sequencesUnlockDao.listAll();

        List<SequencesUnused> sequencesUnusedList = new ArrayList<>();
        for (SequencesUnlock sequencesUnlock : sequencesUnlockList) {
            sequencesUnusedList.add(new SequencesUnused(sequencesUnlock));
        }

        //将使用中表的序号放到空闲表中
        sequencesUnusedDao.saveBatch(sequencesUnusedList);
        //删除所有使用中表的数据
        sequencesUnlockDao.deleteAll();
    }

    @Override
    public void release(Date begin, Date end) {
        //列出指定时间段内使用中表存在的序号
        List<SequencesUnlock> sequencesUnlockList = sequencesUnlockDao.listByDate(begin, end);

        List<SequencesUnused> sequencesUnusedList = new ArrayList<>();
        for (SequencesUnlock sequencesUnlock : sequencesUnlockList) {
            sequencesUnusedList.add(new SequencesUnused(sequencesUnlock));
        }

        //将指定时间段内使用中表的序号放到空闲表中
        sequencesUnusedDao.saveBatch(sequencesUnusedList);
        //删除指定时间段内使用中表的数据
        sequencesUnlockDao.deleteByDate(begin, end);
    }

    @Override
    public void release(Sequences sequences) {
        sequencesUnlockDao.delete(new SequencesUnlock(sequences));
        sequencesUnusedDao.save(new SequencesUnused(sequences));
    }
}
