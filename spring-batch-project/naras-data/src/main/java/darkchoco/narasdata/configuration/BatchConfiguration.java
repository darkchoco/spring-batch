package darkchoco.narasdata.configuration;

import darkchoco.narasdata.batch.ChunkLoggingListener;
import darkchoco.narasdata.batch.CountryCapitalProcessor;
import darkchoco.narasdata.batch.JobCompletionNotificationListener;
import darkchoco.narasdata.domain.CountryCapitalData;
import darkchoco.narasdata.domain.CountryData;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        // countryCapitalDelegatingItemWriter는 내부에서 countryCapitalItemWriter를 호출한다
        return new CompositeItemWriterBuilder<CountryData>()
                .delegates(countryItemWriter(), countryCapitalDelegatingItemWriter(countryCapitalItemWriter()))
                .build();
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
        JdbcBatchItemWriter<CountryCapitalData> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("INSERT INTO country_capital (capital, country_code) VALUES (?, ?)");
        
        writer.setItemPreparedStatementSetter((countryCapitalData, ps) -> {
            String countryCode = countryCapitalData.getCountryData() != null ? countryCapitalData.getCountryData().getCode() : "UNKNOWN";

            ps.setString(1, countryCapitalData.getCapital());
            ps.setString(2, countryCode);
        });
        
        writer.setAssertUpdates(false);
        
        // Verify the configuration
        try {
            writer.afterPropertiesSet();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JdbcBatchItemWriter", e);
        }
        
        return writer;
    }

    @Bean
    public ItemWriter<CountryData> countryCapitalDelegatingItemWriter(JdbcBatchItemWriter<CountryCapitalData> countryCapitalItemWriter) {
        return chunk -> {
            List<CountryCapitalData> allCapitals = new ArrayList<>();
            log.info("Processing chunk with {} items", chunk.size());
            
            for (CountryData item : chunk) {
                log.info("Processing country: {} - {}", item.getCode(), item.getCommonName());
                
                // Process capital list from JSON
                if (item.getCapital() != null && !item.getCapital().isEmpty()) {
                    log.info("  - Found {} capitals in JSON", item.getCapital().size());
                    for (String capital : item.getCapital()) {
                        log.info("    - Processing capital: {}", capital);
                        CountryCapitalData capitalData = new CountryCapitalData();
                        capitalData.setCapital(capital);
                        capitalData.setCountryData(item);
                        allCapitals.add(capitalData);
                    }
                } else {
                    log.info("  - No capitals found in JSON");
                }
            }
            
            // Write all new capital data
            log.info("Total CountryCapitalData to write: {}", allCapitals.size());
            if (!allCapitals.isEmpty()) {
                try {
                    countryCapitalItemWriter.write(new Chunk<>(allCapitals));
                    log.info("Successfully wrote {} CountryCapitalData records", allCapitals.size());
                } catch (Exception e) {
                    log.error("Error writing CountryCapitalData: {}", e.getMessage());
                    throw e; // Re-throw to fail the step if there's an error
                }
            } else {
                log.info("No capital data to write");
            }
        };
    }
}
