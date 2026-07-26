package sonaged.ucg.supervision;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.AccessLevel;

import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentState {

    long totalPRN;
    long totalBacs;
    long totalPP;
    long totalCP;
    long totalHabitants;
    long totalCircuits;
    long totalBennes;
    long totalDepotoirs;
    long totalCommunes;
}
