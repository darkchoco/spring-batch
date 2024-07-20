package darkchoco.narasdata.configuration;

import darkchoco.narasdata.batch.ChunkLoggingListener;
import darkchoco.narasdata.batch.CountryCapitalProcessor;
import darkchoco.narasdata.batch.JobCompletionNotificationListener;
import darkchoco.narasdata.domain.CountryCapitalData;
import darkchoco.narasdata.domain.CountryData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class BatchConfiguration {

    @Value("${file.input}")
    private String fileInput;

    private final DataSource dataSource;

    public BatchConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public Job countryDataJob(JobRepository jobRepository,
                              JobCompletionNotificationListener listener,
                              Step step) {
        return new JobBuilder("importCountryDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }

    @Bean
    public Step importCountryData(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("importCountryData", jobRepository)
                .<CountryData, CountryData>chunk(10, transactionManager)
                .reader(reader())
                .processor(new CountryCapitalProcessor())
                .writer(compositeItemWriter())
                .listener(new ChunkLoggingListener())
                .build();
    }

    @Bean
    public JsonItemReader<CountryData> reader() {
        return new JsonItemReaderBuilder<CountryData>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CountryData.class))
//                .resource(new FileSystemResource(fileInput))
                .resource(new ClassPathResource(fileInput))
                .name("countryDataItemReader")
                .build();
    }

//    @Bean
//    public ItemProcessor<CountryData, List<CountryCapitalData>> processor() {
//        return countryData -> {
//            List<CountryCapitalData> capitals = new ArrayList<>();
//            for (String capital : countryData.getCapital()) {
//                capitals.add(new CountryCapitalData(countryData.getCode(), capital));
//            }
//            return capitals;
//        };
//    }

    @Bean
    public CompositeItemWriter<CountryData> compositeItemWriter() {
        return new CompositeItemWriterBuilder<CountryData>()
                .delegates(Arrays.asList(countryItemWriter(), countryCapitalDelegatingItemWriter(countryCapitalItemWriter())))
                .build();
//        List<ItemWriter<? super CountryData>> writers = Arrays.asList(countryItemWriter(), countryCapitalItemWriter());
//        CompositeItemWriter<CountryData> compositeItemWriter = new CompositeItemWriter<>();
//        compositeItemWriter.setDelegates(writers);
//        return compositeItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter<CountryData> countryItemWriter() {
        return new JdbcBatchItemWriterBuilder<CountryData>()
                .beanMapped()
                .sql("""
                        INSERT INTO country
                        (code, common_name, official_name, flag_emoji, flag_img, region, population, google_map_url)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """)
                .itemPreparedStatementSetter((countryData, ps) -> {
                    ps.setString(1, countryData.getCode());
                    ps.setString(2, countryData.getCommonName());
                    ps.setString(3, countryData.getOfficialName());
                    ps.setString(4, countryData.getFlagEmoji());
                    ps.setString(5, countryData.getFlagImg());
                    ps.setString(6, countryData.getRegion());
                    ps.setInt(7, countryData.getPopulation());
                    ps.setString(8, countryData.getGoogleMapUrl());
                })
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CountryCapitalData> countryCapitalItemWriter() {
        return new JdbcBatchItemWriterBuilder<CountryCapitalData>()
                .beanMapped()
                .sql("""
                        INSERT INTO country_capital (code, capital)
                        VALUES (?, ?)
                        """)
                // JdbcBatchItemWriter는 단일 SQL 문을 하나의 항목에 대해 실행하도록 설계되었다. 따라서 for 루프 내에서
                // ps.addBatch()를 호출하면 반복적으로 배치에 항목을 추가하게 되지만, JdbcBatchItemWriter는 이를
                // 올바르게 처리하지 못할 수 있다. 아래 코드는 주석처리 한다.
//                .itemPreparedStatementSetter((countryData, ps) -> {
//                    for (String capital : countryData.getCapital()) {
//                        ps.setString(1, countryData.getCode());
//                        ps.setString(2, capital);
//                        ps.addBatch();  // Add batch for each capital
//                    }
//                })
                .itemPreparedStatementSetter((countryCapitalData, ps) -> {
                    ps.setString(1, countryCapitalData.getCode());
                    ps.setString(2, countryCapitalData.getCapital());
                })
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemWriter<CountryData> countryCapitalDelegatingItemWriter(JdbcBatchItemWriter<CountryCapitalData> countryCapitalItemWriter) {
        return chunk -> {
            for (CountryData item : chunk) {
                for (String capital : item.getCapital()) {
                    CountryCapitalData capitalData = new CountryCapitalData(item.getCode(), capital);
                    countryCapitalItemWriter.write(new Chunk<>(capitalData));
                }
            }
        };
    }
}
