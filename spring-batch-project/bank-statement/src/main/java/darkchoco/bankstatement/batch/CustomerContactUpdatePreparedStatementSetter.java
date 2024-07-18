package darkchoco.bankstatement.batch;

import darkchoco.bankstatement.domain.CustomerContactUpdate;
import darkchoco.bankstatement.domain.CustomerUpdate;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerContactUpdatePreparedStatementSetter implements ItemPreparedStatementSetter<CustomerUpdate> {
    @Override
    public void setValues(@NonNull CustomerUpdate item, @NonNull PreparedStatement ps) throws SQLException {
        // Pattern variable을 사용하여 코드를 보다 simple하게 만들었다.
        if (item instanceof CustomerContactUpdate customerContactUpdate) {
            ps.setString(1, customerContactUpdate.getEmailAddress());
            ps.setString(2, customerContactUpdate.getHomePhone());
            ps.setString(3, customerContactUpdate.getCellPhone());
            ps.setString(4, customerContactUpdate.getWorkPhone());
            // CHAR 타입은 정수형과 혼동될 수 있기 때문에 String.valueOf() 로 확실하게 지정해주어야 한다.
            ps.setString(5, String.valueOf(customerContactUpdate.getNotificationPreferences()));
            ps.setLong(6, customerContactUpdate.getCustomerId());
        }
        else {
            throw new IllegalArgumentException(
                    "Invalid type for CustomerAddressUpdatePreparedStatementSetter: " + item.getClass().getName());
        }
    }
}
