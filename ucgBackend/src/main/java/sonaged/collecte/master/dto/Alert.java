package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import sonaged.collecte.master.enums.AlertCode;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Alert implements Serializable {
    Long alertId;

    String object;

    String message;

    String address;

    AlertCode code;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    MultipartFile file;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Image image;

    Coordinate coordinate;

}