package darkchoco.narasdata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.FetchType;
import jakarta.persistence.Transient;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
public class CountryData {
    private String code;
    private String commonName;
    private String officialName;
    private String flagEmoji;
    private String flagImg;
    private String region;
    private int population;

    @JsonProperty("googleMapURL")
    private String googleMapUrl;

    @JsonProperty("capital")
    @Transient  // JPA는 이 필드를 무시. 결국 capital 필드는 JSON에서 데이터 받아오는 용도로만 사용된다.
    private List<String> capital;  // JSON의 capital 배열을 저장할 필드

    @OneToMany(mappedBy = "countryData", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CountryCapitalData> capitals;
}
