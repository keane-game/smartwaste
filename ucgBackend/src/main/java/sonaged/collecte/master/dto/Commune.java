package sonaged.collecte.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import sonaged.collecte.master.model.GeometryEntity;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Commune {

    Long communeId;

    String name;

    String code;

    String total;

    String women;

    String men;

    String length;

    String area;

   /* @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    List<Quartier> quartiers;*/

    // P1-7 / ADR-0012 : `List<DepotoirEntity> depotoirs` retiré.
    // (1) Il exposait une ENTITÉ JPA dans un DTO ; (2) il créait un cycle
    // « Référentiel territorial » -> « Points de collecte » entre deux bounded contexts,
    // que Spring Modulith rejette. Les dépotoirs d'une commune se lisent via l'API du
    // contexte propriétaire (`GET /v1/depotoirss`, filtrable sur `communeId`).

    // `DepartmentEntity department` -> identifiant. Department appartient au MÊME contexte
    // (Référentiel territorial), l'association JPA reste donc légitime côté entité ;
    // c'est son exposition en DTO qui ne l'était pas.
    Long departmentId;

    Geometry geometry;


}
