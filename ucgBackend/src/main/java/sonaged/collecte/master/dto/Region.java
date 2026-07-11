package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.model.DepartmentEntity;
import sonaged.collecte.master.model.GeometryEntity;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Region {

    Long regionId;

    String name;

    String code;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    List<DepartmentEntity> departments;

    Geometry geometry;
}
