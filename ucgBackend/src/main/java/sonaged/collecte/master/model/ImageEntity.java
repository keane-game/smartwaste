package sonaged.collecte.master.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "IMAGE")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageId")
    Long imageId;

    @Lob
    @Column(name = "Data")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    byte[] data;

    @Column(name = "Name")
    String name;

    @Column(name = "Type")
    String type;

}