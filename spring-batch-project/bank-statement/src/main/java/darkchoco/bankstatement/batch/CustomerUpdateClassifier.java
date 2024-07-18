package darkchoco.bankstatement.batch;

import darkchoco.bankstatement.domain.CustomerAddressUpdate;
import darkchoco.bankstatement.domain.CustomerUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.classify.Classifier;

@RequiredArgsConstructor
public class CustomerUpdateClassifier implements Classifier<CustomerUpdate, ItemWriter<? super CustomerUpdate>> {

    private final JdbcBatchItemWriter<CustomerUpdate> recordType1ItemWriter;
    private final JdbcBatchItemWriter<CustomerUpdate> recordType2ItemWriter;
    private final JdbcBatchItemWriter<CustomerUpdate> recordType3ItemWriter;

    @Override
    public ItemWriter<? super CustomerUpdate> classify(CustomerUpdate classifiable) {
        if (classifiable == null) {
            throw new IllegalArgumentException("classifiable is null");
        }

        return switch (classifiable.getClass().getSimpleName()) {
            case "CustomerNameUpdate" -> recordType1ItemWriter;
            case "CustomerAddressUpdate" -> recordType2ItemWriter;
            case "CustomerContactUpdate" -> recordType3ItemWriter;
            default -> throw new IllegalArgumentException("Invalid type: " + classifiable.getClass().getCanonicalName());
        };
    }
}
