package darkchoco.narasdata.batch;

import darkchoco.narasdata.domain.CountryData;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class CountryCapitalProcessor implements ItemProcessor<CountryData, CountryData> {

    @Override
    public CountryData process(@NonNull CountryData item) {
        return item;
    }
}
