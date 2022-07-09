import com.mysql.cj.jdbc.MysqlDataSource;
import com.yanghuanglin.seq.config.BaseConfig;
import com.yanghuanglin.seq.config.GeneratorConfig;
import com.yanghuanglin.seq.config.TableConfig;
import com.yanghuanglin.seq.generator.Generator;
import com.yanghuanglin.seq.generator.impl.SequencesGenerator;
import com.yanghuanglin.seq.po.Sequences;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yanghuanglin
 * @since 2022/1/28
 */
public class SeqTest {
    private static final MysqlDataSource dataSource = new MysqlDataSource();
    private static final Generator generator;

    static {
        dataSource.setURL("jdbc:mysql://127.0.0.1:3306/sequence");
        dataSource.setUser("root");
        dataSource.setPassword("root");

        GeneratorConfig generatorConfig = new GeneratorConfig(dataSource);

        TableConfig tableConfig = new TableConfig();
        tableConfig.setTable("sequences");
        tableConfig.setKeyColumn("SEQUENCE_KEY");
        tableConfig.setTypeColumn("SEQUENCE_TYPE");
        tableConfig.setSeqColumn("CURRENT");
        generatorConfig.setTableConfig(tableConfig);
        generatorConfig.setDayZeroFilling(false);
        generatorConfig.setMonthZeroFilling(false);

        generator = new SequencesGenerator(generatorConfig);
    }

    @Test
    public void generateTest() {
        //释放未锁定序列号
        generator.release();

        Set<String> set = new HashSet<>();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));
        for (int i = 0; i < 5; i++) {
            threadPoolExecutor.execute(() -> {
                Sequences sequences = generator.generate("SNT", "MISSION");
                String formattedSeq = generator.format(sequences.getSeq(), "处〔#year#〕10801#seq#");
                generator.lock(sequences);
                set.add(formattedSeq);
                System.out.println(formattedSeq);
            });
        }
        threadPoolExecutor.shutdown();
        while (true) {
            if (threadPoolExecutor.isTerminated())
                break;
        }
        System.out.println(set.size());
    }

    @Test
    public void releaseTest() {
        generator.release();
    }

    @Test
    public void formatTest() {
        String s = "select * from sequences where `%s`=? and `%s`=?";
        System.out.println(String.format(s, "key", "value"));
    }

    @Test
    public void parseTest() {
        String seqPattern = "ZZF#year##month##seq#";
        String formatted = "ZZF20220200008";

        Sequences sequences = generator.parse(formatted, seqPattern);

        String key = "zzfCode" + sequences.getYear();

        sequences.setKey(key);
        sequences.setType("MISSION");
        System.out.println(sequences);
    }
}
