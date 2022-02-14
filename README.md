# seq——基于mysql+spring-jdbc的自增序号生成器

---

用于生成全局自增序号，跳过的序号可以回收使用。

---

使用方法：

+ 在项目中放置jar包的地方把seq-1.4.1.jar、seq-1.4.1-sources.jar、seq-1.4.1-pom.xml复制过去
+ 在pom.xml中增加以下内容，然后执行maven命令：mvn clean

```xml

<project>
    <dependencies>
        <dependency>
            <groupId>com.yanghuanglin</groupId>
            <artifactId>seq</artifactId>
            <version>1.4.1</version>
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

    <build>
        <plugins>
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
                            <file>${project.basedir}/lib/seq-1.4.1.jar</file>
                            <pomFile>${pom.basedir}/lib/seq-1.4.1-pom.xml</pomFile>
                            <sources>${project.basedir}/lib/seq-1.4.1-sources.jar</sources>
                            <repositoryLayout>default</repositoryLayout>
                            <groupId>com.yanghuanglin</groupId>
                            <artifactId>seq</artifactId>
                            <version>1.4.1</version>
                            <packaging>jar</packaging>
                            <generatePom>true</generatePom>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

+ springboot中配置方式一（优先）：直接注入已有jdbcTemplate和transactionTemplate

```java
package com.yanghuanglin.springseq.baseConfig;

import com.yanghuanglin.seq.baseConfig.GeneratorConfig;
import com.yanghuanglin.seq.baseConfig.TableConfig;
import com.yanghuanglin.seq.generator.Generator;
import com.yanghuanglin.seq.generator.impl.SequencesGenerator;
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
        tableConfig.setTable("sequences");
        tableConfig.setKeyColumn("SEQUENCE_KEY");
        tableConfig.setTypeColumn("SEQUENCE_TYPE");
        tableConfig.setSeqColumn("NEXT_ID");
        tableConfig.setCreateTimeColumn("CREATE_TIME");
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
package com.yanghuanglin.springseq.baseConfig;

import com.yanghuanglin.seq.baseConfig.GeneratorConfig;
import com.yanghuanglin.seq.baseConfig.TableConfig;
import com.yanghuanglin.seq.generator.Generator;
import com.yanghuanglin.seq.generator.impl.SequencesGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        tableConfig.setTable("sequences");
        tableConfig.setKeyColumn("SEQUENCE_KEY");
        tableConfig.setTypeColumn("SEQUENCE_TYPE");
        tableConfig.setSeqColumn("NEXT_ID");
        tableConfig.setCreateTimeColumn("CREATE_TIME");
        return tableConfig;
    }

    /**
     * 序号生成器配置类
     * @param tableConfig 序号表配置类
     */
    @DependsOn("tableConfig")
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
    @DependsOn("generatorConfig")
    @Bean
    public Generator generator(GeneratorConfig generatorConfig) {
        return new SequencesGenerator(generatorConfig);
    }
}
```

+ 使用：

```java
package com.yanghuanglin.springseq.baseConfig;

import com.yanghuanglin.seq.generator.Generator;
import com.yanghuanglin.seq.po.Sequences;
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

| 配置项                 | 类型                                                               | 默认值              | 说明               |
|---------------------|------------------------------------------------------------------|------------------|------------------|
| dataSource          | javax.sql.DataSource                                             | null             | 数据源              |
| jdbcTemplate        | org.springframework.jdbc.core.JdbcTemplate                       | null             | 数据库操作模板          |
| transactionTemplate | org.springframework.jdbc.core.JdbcTemplate                       | null             | 事务操作模板           |
| transactionManager  | org.springframework.jdbc.datasource.DataSourceTransactionManager | null             | 事务管理器            |
| autoCreate          | Boolean                                                          | true             | 开启自动建表           |
| step                | Integer                                                          | 1                | 序号增加时的步长         |
| type                | String                                                           | DEFAULT          | 默认序号类型           |
| minLength           | Integer                                                          | 1                | 默认序号格式化后长度，不足的补零 |
| tableConfig         | com.yanghuanglin.seq.baseConfig.TableConfig                      | TableConfig的默认配置 | 表配置              |

以上配置中，jdbcTemplate和transactionTemplate优先级最高，如果jdbcTemplate、transactionTemplate、dataSource、transactionManager同时配置，则dataSource和transactionManager无效；
可进行这几种组合：dataSource+autoCreate+step+minLength+tableConfig，jdbcTemplate+transactionTemplate+autoCreate+step+minLength+tableConfig，jdbcTemplate+transactionManager+autoCreate+step+minLength+tableConfig

---
Generator方法如下：

```java
package com.yanghuanglin.seq.generator;

import com.yanghuanglin.seq.config.BaseConfig;
import com.yanghuanglin.seq.config.GeneratorConfig;
import com.yanghuanglin.seq.enums.FormatPlaceholder;
import com.yanghuanglin.seq.po.Sequences;
import com.yanghuanglin.seq.po.SequencesUnlock;
import com.yanghuanglin.seq.po.SequencesUnused;

import java.util.Date;

public interface Generator {
    /**
     * 根据传入的key和type生成可用的序号对象。
     * <p/>
     * 如果根据key和默认的{@link GeneratorConfig#getType()}在{@link Sequences}中找不到记录，说明该组合的序号对象还未初次生成，返回的是seq为step的序号对象，该对象数据会写入到{@link SequencesUnlock}中。
     * <p/>
     * 如果根据key和默认的{@link GeneratorConfig#getType()}在{@link Sequences}中找到了记录，且在{@link SequencesUnused}也找到了记录，说明该组合生成的序号有部分未使用，返回的是{@link SequencesUnused}中找到的seq最小的序号对象。同时会将{@link SequencesUnused}中找到的seq最小的记录删除，然后写入到{@link SequencesUnlock}中。
     * <p/>
     *
     * @param key 数据字典中的编码
     * @return 可用的序号对象
     */
    Sequences generate(String key);

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
     * 将{@link #generate(String, String)}得到的序号对象格式化为补零后的序号字符串，其最小长度通过{@link BaseConfig#getMinLength()}设定。实际上只会用到{@link Sequences#getSeq()}属性
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR}(当前年份)、{@link FormatPlaceholder#MONTH}}(当前月份)、{@link FormatPlaceholder#DAY}}(当前日期)、{@link FormatPlaceholder#SEQ}}(生成的字符串序号)几个枚举值通过{@link FormatPlaceholder#getPlaceholder()}得到的字符串
     *
     * @param sequences 生成的序号对象
     * @param pattern   格式
     * @return 格式化后的字符串
     */
    String format(Sequences sequences, String pattern);

    /**
     * 将{@link #generate(String, String)}得到的序号对象格式化为补零后的序号字符串。实际上只会用到{@link Sequences#getSeq()}属性
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR}(当前年份)、{@link FormatPlaceholder#MONTH}}(当前月份)、{@link FormatPlaceholder#DAY}}(当前日期)、{@link FormatPlaceholder#SEQ}}(生成的字符串序号)几个枚举值通过{@link FormatPlaceholder#getPlaceholder()}得到的字符串
     *
     * @param sequences 生成的序号对象
     * @param minLength 序号数字最小长度
     * @param pattern   格式
     * @return 格式化后的字符串
     */
    String format(Sequences sequences, Integer minLength, String pattern);

    /**
     * 将生成的序号对象格式化为指定格式，格式化后字符串最小长度为{@link BaseConfig#getMinLength()}，不足则补零
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR#getPlaceholder()}(当前年份)、{@link FormatPlaceholder#MONTH#getPlaceholder()}(当前月份)、{@link FormatPlaceholder#DAY#getPlaceholder()}(当前日期)、{@link FormatPlaceholder#SEQ#getPlaceholder()}(生成的字符串序号)四个变量
     * <p/>
     * seq为1，pattern为#year##month##day#6#seq#，则会格式化为2022013061。此序号含义如下：
     * <p/>
     * 序号格式：[年][月][日][固定6开头][序号1，最小位数为{@link BaseConfig#getMinLength()}设置，默认为1，不足则补零]
     *
     * @param seq     需要格式化的序号
     * @param pattern 格式
     * @return 格式化后的序号字符串
     */
    String format(Long seq, String pattern);

    /**
     * 将生成的序号对象格式化为指定格式
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR}}(当前年份)、{@link FormatPlaceholder#MONTH}}(当前月份)、{@link FormatPlaceholder#DAY}}(当前日期)、{@link FormatPlaceholder#SEQ}}(生成的字符串序号)几个枚举值通过{@link FormatPlaceholder#getPlaceholder()}得到的字符串
     * <p/>
     * seq为1，minLength为4，pattern为#year##month##day#6#seq#，则会格式化为2022013060001。此序号含义如下：
     * <p/>
     * 序号格式：[年][月][日][固定6开头][序号1，最小位数为4位，不足4位则补零]
     *
     * @param seq       需要格式化的序号
     * @param minLength 序号最小长度，不足的会补零
     * @param pattern   格式
     * @return 格式化后的序号字符串
     */
    String format(Long seq, Integer minLength, String pattern);

    /**
     * 将生成的序号对象格式化为指定格式，格式化后字符串最小长度为{@link BaseConfig#getMinLength()}，不足则补零
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR}(当前年份)、{@link FormatPlaceholder#MONTH}}(当前月份)、{@link FormatPlaceholder#DAY}}(当前日期)、{@link FormatPlaceholder#SEQ}}(生成的字符串序号)几个枚举值通过{@link FormatPlaceholder#getPlaceholder()}得到的字符串
     * <p/>
     * seq为1,start为6，minLength为4，pattern为#year##month##day##seq#，则会格式化为2022013061。此序号含义如下：
     * <p/>
     * 序号格式：[年][月][日][固定6开头][序号1，最小位数为{@link BaseConfig#getMinLength()}设置，默认为1，不足则补零]
     *
     * @param seq     需要格式化的序号
     * @param start   序号格式化后以什么字符串开头
     * @param pattern 序号格式
     * @return 格式化后的序号字符串
     */
    String format(Long seq, String start, String pattern);

    /**
     * 将生成的序号对象格式化为指定格式
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR}(当前年份)、{@link FormatPlaceholder#MONTH}}(当前月份)、{@link FormatPlaceholder#DAY}}(当前日期)、{@link FormatPlaceholder#SEQ}}(生成的字符串序号)几个枚举值通过{@link FormatPlaceholder#getPlaceholder()}得到的字符串
     * <p/>
     * seq为1,start为6，minLength为4，pattern为#year##month##day##seq#，则会格式化为2022013060001。此序号含义如下：
     * <p/>
     * 序号格式：[年][月][日][固定6开头][序号1，最小位数为4位，不足4位则补零]
     *
     * @param seq       需要格式化的序号
     * @param start     序号格式化后以什么字符串开头
     * @param minLength 序号最小长度，不足的会补零
     * @param pattern   序号格式
     * @return 格式化后的序号字符串
     */
    String format(Long seq, String start, Integer minLength, String pattern);

    /**
     * 将已格式化的序号解析为序号对象
     * <p/>
     * pattern支持：{@link FormatPlaceholder#YEAR}(当前年份)、{@link FormatPlaceholder#MONTH}}(当前月份)、{@link FormatPlaceholder#DAY}}(当前日期)、{@link FormatPlaceholder#SEQ}}(生成的字符串序号)几个枚举值通过{@link FormatPlaceholder#getPlaceholder()}得到的字符串
     * <p/>
     * 返回的序号对象{@link Sequences#getKey()}为null，{@link Sequences#getType()}为{@link GeneratorConfig#getType()}的默认值，但是临时字段{@link Sequences#getYear()}、{@link Sequences#getMonth()}、{@link Sequences#getDay()}可能有值
     * <p/>
     * 如果生成序号时，序号的key在年、月、日上有关联（如每年每月的序号要从1开始），则需要自行用序号字符串与{@link Sequences#getYear()}、{@link Sequences#getMonth()}、{@link Sequences#getDay()}进行组合，进而得到key
     * <p/>
     * 例如：SNT序号每年都从1开始，则key应该是类似SNT2021、SNT2022这种格式，而在配置中，该序号的代码只是SNT，但是由于每年都要从1开始，所有应该每年有一个key，这个key就为SNT+年份，而这个年份就是此处解析后返回的对象中的{@link Sequences#getYear()}
     * <p/>
     * 注意：序号格式和格式化后的字符串占位一定要匹配。如：处〔#year#〕#month#10801第#seq#号 对应 处〔2022〕0210801第10001号，而不能对应 处〔2022〕021110801第10001号
     *
     * @param formatted 格式化后的序号字符串
     * @param pattern   序号格式
     * @return 包含了序号字符串对应年（如果有）、月（如果有）、日（如果有）、序号的序号对象，其key需要根据情况手动设置，type为{@link GeneratorConfig#getType()}的默认值
     */
    Sequences parse(String formatted, String pattern);

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

```