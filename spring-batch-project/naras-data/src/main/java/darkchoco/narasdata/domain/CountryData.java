package darkchoco.narasdata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@Setter
@Getter
public class CountryData {
    private String code;
    private String commonName;
    private String officialName;
    private String flagEmoji;
    private String flagImg;
    private List<String> capital;
    private String region;
    private int population;
    @JsonProperty("googleMapURL")
    private String googleMapUrl;
}
