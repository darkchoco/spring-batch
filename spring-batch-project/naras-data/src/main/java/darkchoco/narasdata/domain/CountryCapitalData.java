package darkchoco.narasdata.domain;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Setter
@Getter
public class CountryCapitalData {

    private String id;
    private String capital;

    @ManyToOne
    @JoinColumn(name = "country_code", referencedColumnName = "code")  // referencedColumnName: 외래키가 참조할 상대 테이블의 칼럼명을 명시.
    @ToString.Exclude
    private CountryData countryData;
}
