package com.cdhncy.seq.generator.impl;

import com.cdhncy.seq.config.GeneratorConfig;
import com.cdhncy.seq.dao.SequencesDao;
import com.cdhncy.seq.dao.SequencesUnusedDao;
import com.cdhncy.seq.dao.SequencesUnlockDao;
import com.cdhncy.seq.dao.impl.SequencesDaoImpl;
import com.cdhncy.seq.dao.impl.SequencesUnusedDaoImpl;
import com.cdhncy.seq.dao.impl.SequencesUnlockDaoImpl;
import com.cdhncy.seq.po.Sequences;
import com.cdhncy.seq.po.SequencesUnused;
import com.cdhncy.seq.po.SequencesUnlock;
import com.cdhncy.seq.generator.Generator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;

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
                transactionManager = new JdbcTransactionManager(dataSource);
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
}
