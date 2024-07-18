package darkchoco.bankstatement.configuration;

import darkchoco.bankstatement.batch.*;
import darkchoco.bankstatement.domain.CustomerAddressUpdate;
import darkchoco.bankstatement.domain.CustomerContactUpdate;
import darkchoco.bankstatement.domain.CustomerNameUpdate;
import darkchoco.bankstatement.domain.CustomerUpdate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BatchConfiguation {

    @Value("${file.input}")
    private String fileInput;

    private final DataSource dataSource;

    public BatchConfiguation(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public Job bankStatementJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step) {
        return new JobBuilder("bankStatementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }

    @Bean
    public Step importCustomerUpdates(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("importCustomerUpdates", jobRepository)
                .<CustomerUpdate, CustomerUpdate>chunk(50, transactionManager)
                .reader(customerUpdateItemReader())
                .processor(customerValidatingItemProcessor())
                .writer(customerUpdateItemWriter())
                .listener(new ChunkLoggingListener())
                .build();
    }

    @Bean
    public FlatFileItemReader<CustomerUpdate> customerUpdateItemReader() throws Exception {
        return new FlatFileItemReaderBuilder<CustomerUpdate>()
//                .resource(new FileSystemResource(fileInput))
                .resource(new ClassPathResource(fileInput))
                .name("customerUpdateItemReader")
                .lineTokenizer(customerUpdatesLineTokenizer())
                .fieldSetMapper(customerUpdateFieldSetMapper())
                .build();
    }

    @Bean
    public LineTokenizer customerUpdatesLineTokenizer() throws Exception {
        DelimitedLineTokenizer recordType1 = new DelimitedLineTokenizer();

        recordType1.setNames("recordId", "customerId", "firstName", "middleName", "lastName");

        recordType1.afterPropertiesSet();

        DelimitedLineTokenizer recordType2 = new DelimitedLineTokenizer();

        recordType2.setNames("recordId", "customerId", "address1", "address2", "city", "state", "postalCode");

        recordType2.afterPropertiesSet();

        DelimitedLineTokenizer recordType3 = new DelimitedLineTokenizer();

        recordType3.setNames("recordId", "customerId", "emailAddress", "homePhone", "cellPhone", "workPhone", "notificationPreference");

        recordType3.afterPropertiesSet();

        // The String in each Map entry defines a pattern the record must match in order to use
        // that LineTokenzier.
        Map<String, LineTokenizer> tokenizers = new HashMap<>(3);
        tokenizers.put("1*", recordType1);
        tokenizers.put("2*", recordType2);
        tokenizers.put("3*", recordType3);

        PatternMatchingCompositeLineTokenizer lineTokenizer = new PatternMatchingCompositeLineTokenizer();

        lineTokenizer.setTokenizers(tokenizers);

        return lineTokenizer;
    }

    @Bean
    public FieldSetMapper<CustomerUpdate> customerUpdateFieldSetMapper() {
        return fieldSet -> {
            switch (fieldSet.readInt("recordId")) {
                case 1:
                    return new CustomerNameUpdate(
                            fieldSet.readLong("customerId"),
                            fieldSet.readString("firstName"),
                            fieldSet.readString("middleName"),
                            fieldSet.readString("lastName"));
                case 2:
                    return new CustomerAddressUpdate(
                            fieldSet.readLong("customerId"),
                            fieldSet.readString("address1"),
                            fieldSet.readString("address2"),
                            fieldSet.readString("city"),
                            fieldSet.readString("state"),
                            fieldSet.readString("postalCode"));
                case 3:
                    String rawPreference = fieldSet.readString("notificationPreference");

                    Integer notificationPreference = null;

                    if (StringUtils.hasText(rawPreference)) {
                        notificationPreference = Integer.parseInt(rawPreference);
                    }

                    return new CustomerContactUpdate(
                            fieldSet.readLong("customerId"),
                            fieldSet.readString("emailAddress"),
                            fieldSet.readString("homePhone"),
                            fieldSet.readString("cellPhone"),
                            fieldSet.readString("workPhone"),
                            notificationPreference);
                default:
                    throw new IllegalArgumentException(
                            "Invalid record type was found:" + fieldSet.readInt("recordId"));
            }
        };
    }

    @Bean
    public ValidatingItemProcessor<CustomerUpdate> customerValidatingItemProcessor() {
        ValidatingItemProcessor<CustomerUpdate> customerValidatingItemProcessor =
                new ValidatingItemProcessor<>();

        customerValidatingItemProcessor.setValidator(new CustomerItemValidator(dataSource));
        customerValidatingItemProcessor.setFilter(true);

        return customerValidatingItemProcessor;
    }

    @Bean
    public JdbcBatchItemWriter<CustomerUpdate> customerNameUpdateItemWriter() {
        return new JdbcBatchItemWriterBuilder<CustomerUpdate>()
                .beanMapped()
                .sql("""
                        UPDATE CUSTOMER
                        SET    FIRST_NAME = COALESCE(:firstName, FIRST_NAME),
                               MIDDLE_NAME = COALESCE(:middleName, MIDDLE_NAME),
                               LAST_NAME = COALESCE(:lastName, LAST_NAME)
                        WHERE CUSTOMER_ID = :customerId
                        """)
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CustomerUpdate> customerAddressUpdateItemWriter() {
        return new JdbcBatchItemWriterBuilder<CustomerUpdate>()
                .beanMapped()
                .sql("""
                        UPDATE CUSTOMER
                        SET    ADDRESS1 = COALESCE(:address1, ADDRESS1),
                               ADDRESS2 = COALESCE(:address2, ADDRESS2),
                               CITY = COALESCE(:city, CITY),
                               STATE = COALESCE(:state, STATE),
                               POSTAL_CODE = COALESCE(:postalCode, POSTAL_CODE)
                        WHERE CUSTOMER_ID = :customerId
                        """)
                .itemPreparedStatementSetter(new CustomerAddressUpdatePreparedStatementSetter())
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CustomerUpdate> customerContactUpdateItemWriter() {
        return new JdbcBatchItemWriterBuilder<CustomerUpdate>()
                .beanMapped()
                .sql("""
                        UPDATE CUSTOMER
                        SET    EMAIL_ADDRESS = COALESCE(:emailAddress, EMAIL_ADDRESS),
                               HOME_PHONE = COALESCE(:homePhone, HOME_PHONE),
                               CELL_PHONE = COALESCE(:cellPhone, CELL_PHONE),
                               WORK_PHONE = COALESCE(:workPhone, WORK_PHONE),
                               NOTIFICATION_PREF = COALESCE(CAST(:notificationPreferences AS CHAR), NOTIFICATION_PREF)
                        WHERE CUSTOMER_ID = :customerId
                        """)
                .itemPreparedStatementSetter(new CustomerContactUpdatePreparedStatementSetter())
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ClassifierCompositeItemWriter<CustomerUpdate> customerUpdateItemWriter() {
        CustomerUpdateClassifier classifier =
                new CustomerUpdateClassifier(
                        customerNameUpdateItemWriter(),
                        customerAddressUpdateItemWriter(),
                        customerContactUpdateItemWriter());

        ClassifierCompositeItemWriter<CustomerUpdate> compositeItemWriter =
                new ClassifierCompositeItemWriter<>();

        compositeItemWriter.setClassifier(classifier);

        return compositeItemWriter;
    }
}
