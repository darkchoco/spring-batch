package darkchoco.bankstatement.batch;

import darkchoco.bankstatement.domain.CustomerAddressUpdate;
import darkchoco.bankstatement.domain.CustomerUpdate;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerAddressUpdatePreparedStatementSetter implements ItemPreparedStatementSetter<CustomerUpdate> {
    @Override
    public void setValues(@NonNull CustomerUpdate item, @NonNull PreparedStatement ps) throws SQLException {
        // Pattern variable을 사용하여 코드를 보다 simple하게 만들었다.
        if (item instanceof CustomerAddressUpdate customerAddressUpdate) {
            ps.setString(1, customerAddressUpdate.getAddress1());
            ps.setString(2, customerAddressUpdate.getAddress2());
            ps.setString(3, customerAddressUpdate.getCity());
            ps.setString(4, customerAddressUpdate.getState());
            ps.setString(5, customerAddressUpdate.getPostalCode());
            ps.setLong(6, customerAddressUpdate.getCustomerId());
        }
        else {
            throw new IllegalArgumentException(
                    "Invalid type for CustomerAddressUpdatePreparedStatementSetter: " + item.getClass().getName());
        }
    }
}
