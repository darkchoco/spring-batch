package darkchoco.bankstatement.batch;

import darkchoco.bankstatement.domain.CustomerUpdate;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

@Component
public class CustomerItemValidator implements Validator<CustomerUpdate> {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    protected static final String FIND_CUSTOMER =
            "SELECT COUNT(*) FROM customer WHERE customer_id = :id";

    // 테스트 용도로 사용되는 생성자. 외부에서 jdbcTemplate을 직접 주입받아 초기화할 수 있다.
    protected CustomerItemValidator(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Spring이 사용하는 생성자. DataSource를 주입받아 NamedParameterJdbcTemplate을 생성.
    // 이와 같이 보통 public 생성자에 @Autowired 를 사용한다.
    @Autowired
    public CustomerItemValidator(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void validate(CustomerUpdate customer) throws ValidationException {
        // Collections.singletonMap 의 사용 이유
        // - 간단한 맵 초기화: 단일 키-값 쌍을 가지는 맵을 생성할 때 매우 편리.
        // - 불변성: 생성된 맵은 불변(immutable). 즉, 한 번 생성된 후에는 수정할 수 없다. 이는 데이터가 변경되지 않도록
        //   보장하는 데 유용하다.
        // - 코드 간결성: 새로운 HashMap을 생성하고 값을 추가하는 것보다 코드가 더 간결하고 읽기 쉽다.
        Map<String, Long> parameterMap = Collections.singletonMap("id", customer.getCustomerId());

        Long count = jdbcTemplate.queryForObject(FIND_CUSTOMER, parameterMap, Long.class);

        if (count != null && count == 0) {
            throw new ValidationException(
                    String.format("Customer id %s was not able to be found", customer.getCustomerId()));
        }
    }
}
