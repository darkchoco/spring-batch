package darkchoco.narasdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Entity
@Table(name = "country")
public class Country {

    @Id
    private String code;
    private String commonName;
    private String officialName;
    private String flagEmoji;
    private String flagImg;
    @ElementCollection
    private List<String> capital;
    private String region;
    private int population;
    @JsonProperty("googleMapURL")
    private String googleMapUrl;
}
