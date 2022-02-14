package com.yanghuanglin.seq.config;

import com.yanghuanglin.seq.dao.SequencesDao;
import com.yanghuanglin.seq.dao.SequencesUnlockDao;
import com.yanghuanglin.seq.dao.SequencesUnusedDao;
import com.yanghuanglin.seq.dao.impl.SequencesDaoImpl;
import com.yanghuanglin.seq.dao.impl.SequencesUnlockDaoImpl;
import com.yanghuanglin.seq.dao.impl.SequencesUnusedDaoImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 生成器最后的基础配置信息，单例模式，便于其他地方调用
 *
 * @author yanghuanglin
 * @since 2022/2/14
 */
public class BaseConfig {
    private static volatile BaseConfig instance;

    private TransactionTemplate transactionTemplate;
    private SequencesDao sequencesDao;
    private SequencesUnusedDao sequencesUnusedDao;
    private SequencesUnlockDao sequencesUnlockDao;
    private Integer step;
    private String type;
    private Integer minLength;

    private BaseConfig() {
    }

    public static BaseConfig getInstance() {
        if (instance == null) {
            synchronized (BaseConfig.class) {
                if (instance == null) {
                    instance = new BaseConfig();
                }
            }
        }
        return instance;
    }

    public static BaseConfig getInstance(GeneratorConfig generatorConfig) {
        if (instance == null) {
            synchronized (BaseConfig.class) {
                if (instance == null) {
                    instance = new BaseConfig();
                }
            }
        }
        instance.init(generatorConfig);
        return instance;
    }

    public static void setInstance(BaseConfig instance) {
        BaseConfig.instance = instance;
    }

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public SequencesDao getSequencesDao() {
        if (sequencesDao == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return sequencesDao;
    }

    public void setSequencesDao(SequencesDao sequencesDao) {
        this.sequencesDao = sequencesDao;
    }

    public SequencesUnusedDao getSequencesUnusedDao() {
        if (sequencesUnusedDao == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return sequencesUnusedDao;
    }

    public void setSequencesUnusedDao(SequencesUnusedDao sequencesUnusedDao) {
        this.sequencesUnusedDao = sequencesUnusedDao;
    }

    public SequencesUnlockDao getSequencesUnlockDao() {
        if (sequencesUnlockDao == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return sequencesUnlockDao;
    }

    public void setSequencesUnlockDao(SequencesUnlockDao sequencesUnlockDao) {
        this.sequencesUnlockDao = sequencesUnlockDao;
    }

    public Integer getStep() {
        if (step == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getType() {
        if (type == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMinLength() {
        if (minLength == null)
            throw new NullPointerException("请先初始化BaseConfig");
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    private void init(GeneratorConfig generatorConfig) {
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
        this.type = generatorConfig.getType();
        this.minLength = generatorConfig.getMinLength();
    }
}
