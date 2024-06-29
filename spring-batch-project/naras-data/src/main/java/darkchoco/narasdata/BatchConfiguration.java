package darkchoco.narasdata;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Value("${file.input}")
    private String fileInput;

    @Bean
    public JsonItemReader<Country> reader() {
        return new JsonItemReaderBuilder<Country>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(Country.class))
                .resource(new FileSystemResource(fileInput))
//                .resource(new ClassPathResource(fileInput))
                .name("countryItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<Country, Country> processor() {
        return country -> country;
    }

    @Bean
    public ItemWriter<Country> writer(CountryRepository repository) {
        return repository::saveAll;
    }

    @Bean
    public Job importCountryJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step1) {
        return new JobBuilder("importCountryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      ItemWriter<Country> writer) {
        return new StepBuilder("step1", jobRepository)
                .<Country, Country>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }
}
