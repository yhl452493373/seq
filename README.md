# seq——基于mysql+spring-jdbc的自增序号生成器

---

用于生成全局自增序号，跳过的序号可以回收使用。

---

使用方法：

+ 在项目中放置jar包的地方把seq-1.0.0.jar、seq-1.0.0-sources.jar、seq-1.0.0-pom.xml复制过去
+ 在pom.xml中增加以下内容，然后执行maven命令：mvn clean

```xml

<build>
    <dependencies>
        <dependency>
            <groupId>com.cdhncy</groupId>
            <artifactId>seq</artifactId>
            <version>1.0.0</version>
            <exclusions>
                <!-- 如若你项目中有引用spring-jdbc，则需要排除seq的jdbc依赖 -->
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-jdbc</artifactId>
                </exclusion>
                <!-- 如若你项目中有引用mysql驱动，则需要排除seq的mysql依赖 -->
                <exclusion>
                    <groupId>mysql</groupId>
                    <artifactId>mysql-connector-java</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <!-- 每次执行mvn clean时，自动安装指定的jar包 -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-install-plugin</artifactId>
        <version>2.5</version>
        <executions>
            <execution>
                <id>install-external</id>
                <phase>clean</phase>
                <goals>
                    <goal>install-file</goal>
                </goals>
                <configuration>
                    <!-- ${project.basedir}表示当前项目的根目录 -->
                    <file>${project.basedir}/lib/seq-1.0.0.jar</file>
                    <pomFile>${pom.basedir}/lib/seq-1.0.0-pom.xml</pomFile>
                    <sources>${project.basedir}/lib/seq-1.0.0-sources.jar</sources>
                    <repositoryLayout>default</repositoryLayout>
                    <groupId>com.cdhncy</groupId>
                    <artifactId>seq</artifactId>
                    <version>1.0.0</version>
                    <packaging>jar</packaging>
                    <generatePom>true</generatePom>
                </configuration>
            </execution>
        </executions>
    </plugin>
</build>
```

+ springboot中配置方式一（优先）：直接注入已有jdbcTemplate和transactionTemplate

```java
package com.yang.springseq.config;

import com.cdhncy.seq.config.GeneratorConfig;
import com.cdhncy.seq.config.TableConfig;
import com.cdhncy.seq.generator.Generator;
import com.cdhncy.seq.generator.impl.SequencesGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
 * 基于已有的jdbcTemplate和transactionTemplate，一般如果引用了spring的数据库操作，如jpa、mybatis，都可以直接注入
 */
@Configuration
public class SeqGeneratorConfig {
    /**
     * 注入已有的数据库操作模板
     */
    @Resource
    private JdbcTemplate jdbcTemplate;
    /**
     * 注入已有的事务操作模板
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 序号表配置类
     */
    @Bean
    public TableConfig tableConfig() {
        TableConfig tableConfig = new TableConfig();
        //自定义表名、字段名
        //tableConfig.setTable("sequences");
        //tableConfig.setKeyColumn("SEQUENCE_KEY");
        //tableConfig.setTypeColumn("SEQUENCE_TYPE");
        //tableConfig.setSeqColumn("SEQUENCE_NEXT_ID");
        //tableConfig.setCreateTimeColumn("CREATE_TIME");
        return tableConfig;
    }

    /**
     * 序号生成器配置类
     * @param tableConfig 序号表配置类
     */
    @Bean
    public GeneratorConfig generatorConfig(TableConfig tableConfig) {
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setJdbcTemplate(jdbcTemplate);
        generatorConfig.setTransactionTemplate(transactionTemplate);
        generatorConfig.setTableConfig(tableConfig);
        return generatorConfig;
    }

    /**
     * 注册序号生成器类
     * @param generatorConfig 序号生成器配置类
     */
    @Bean
    public Generator generator(GeneratorConfig generatorConfig) {
        return new SequencesGenerator(generatorConfig);
    }
}
```

+ springboot中配置方式二：注入已有的dataSource或自行构建dataSource，通过dataSource自动生成jdbcTemplate和transactionTemplate

```java
package com.yang.springseq.config;

import com.cdhncy.seq.config.GeneratorConfig;
import com.cdhncy.seq.config.TableConfig;
import com.cdhncy.seq.generator.Generator;
import com.cdhncy.seq.generator.impl.SequencesGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 注入已有的dataSource或自行构建dataSource，通过dataSource自动生成jdbcTemplate和transactionTemplate
 */
@Configuration
public class SeqGeneratorConfig {
    /**
     * 注入已有的数据源，果没有，也可以自行构建
     */
    @Resource
    private DataSource dataSource;

    /**
     * 序号表配置类
     */
    @Bean
    public TableConfig tableConfig() {
        TableConfig tableConfig = new TableConfig();
        //自定义表名、字段名
        //tableConfig.setTable("sequences");
        //tableConfig.setKeyColumn("SEQUENCE_KEY");
        //tableConfig.setTypeColumn("SEQUENCE_TYPE");
        //tableConfig.setSeqColumn("SEQUENCE_NEXT_ID");
        //tableConfig.setCreateTimeColumn("CREATE_TIME");
        return tableConfig;
    }

    /**
     * 序号生成器配置类
     * @param tableConfig 序号表配置类
     */
    @Bean
    public GeneratorConfig generatorConfig(TableConfig tableConfig) {
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setDataSource(dataSource);
        return generatorConfig;
    }

    /**
     * 注册序号生成器类
     * @param generatorConfig 序号生成器配置类
     */
    @Bean
    public Generator generator(GeneratorConfig generatorConfig) {
        return new SequencesGenerator(generatorConfig);
    }
}
```

+ 使用：

```java
package com.yang.springseq.config;

import com.cdhncy.seq.generator.Generator;
import com.cdhncy.seq.po.Sequences;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class SeqTestService {
    @Resource
    private Generator generator;

    public void test() {
        //释放未锁定序号，此处测试，因此每次生成前都全部释放，实际使用时，建议通过定时任务，隔天释放。
        generator.release();

        //释放指定时间范围内的序号。建议使用定时任务，当天释放前天时间段的序号
        //generator.release(beginDate,endDate);

        Set<String> set = new HashSet<>();
        //开启多线程进行测试，实际使用时，每个业务中只需要execute()->{}里面的方法，这里是测试多个用户同时使用时是否会重复
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));
        for (int i = 0; i < 5; i++) {
            threadPoolExecutor.execute(() -> {
                Sequences sequences = generator.generate("SNT", "MISSION");
                String formattedSeq = generator.format(sequences.getSeq(), 5, "处〔#year#〕10801#seq#");
                generator.lock(sequences);
                set.add(formattedSeq);
                //打印生成的序号
                System.out.println(formattedSeq);
            });
        }
        threadPoolExecutor.shutdown();
        while (true) {
            if (threadPoolExecutor.isTerminated())
                break;
        }
        System.out.println("共生成了:" + set.size() + "个");
    }
}

```

---

TableConfig配置项，通过set方法设置（一般不用改，如果已有相同结构的表，则可以通过这些配置将原数据利用起来）：

| 配置项              | 类型     | 默认值         | 说明                 |
|------------------|--------|-------------|--------------------|
| table            | String | sequences   | 序号表名称              |
| keyColumn        | String | key         | 序号名称列名，和序号类型组成唯一主键 |
| typeColumn       | String | type        | 序号类型列名，和序号名称组成唯一主键 |
| seqColumn        | String | seq         | 序号值列名，和序号名称组成唯一主键  |
| createTimeColumn | String | create_time | 创建时间列名，用于未锁定表排序    |

---

GeneratorConfig配置项，通过set方法设置

| 配置项                 | 类型                                                               | 默认值              | 说明       |
|---------------------|------------------------------------------------------------------|------------------|----------|
| dataSource          | javax.sql.DataSource                                             | null             | 数据源      |
| jdbcTemplate        | org.springframework.jdbc.core.JdbcTemplate                       | null             | 数据库操作模板  |
| transactionTemplate | org.springframework.jdbc.core.JdbcTemplate                       | null             | 事务操作模板   |
| transactionManager  | org.springframework.jdbc.datasource.DataSourceTransactionManager | null             | 事务管理器    |
| autoCreate          | Boolean                                                          | true             | 开启自动建表   |
| step                | Integer                                                          | 1                | 序号增加时的步长 |
| tableConfig         | com.cdhncy.seq.config.TableConfig                                | TableConfig的默认配置 | 表配置      |

以上配置中，jdbcTemplate和transactionTemplate优先级最高，如果jdbcTemplate、transactionTemplate、dataSource、transactionManager同时配置，则dataSource和transactionManager无效；
进行这几种组合：dataSource+autoCreate+step+tableConfig，jdbcTemplate+transactionTemplate+autoCreate+step+tableConfig，jdbcTemplate+transactionManager+autoCreate+step+tableConfig

---
Generator方法如下：

```java
package com.cdhncy.seq.generator;

import com.cdhncy.seq.po.Sequences;
import com.cdhncy.seq.po.SequencesUnlock;
import com.cdhncy.seq.po.SequencesUnused;

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
}

```