package itmo.ivank.soa.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "organization")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    @Builder.Default
    private LocalDate creationDate = LocalDate.now();

    @Column
    @Positive
    private Float annualTurnover;

    @Column
    private String fullName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "coordinates_x")),
            @AttributeOverride(name = "y", column = @Column(name = "coordinates_y"))
    })
    private Coordinates coordinates;

    @Column
    @Enumerated(EnumType.STRING)
    private OrganizationType type;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "official_address_street")),
            @AttributeOverride(name = "town.x", column = @Column(name = "official_address_town_x")),
            @AttributeOverride(name = "town.y", column = @Column(name = "official_address_town_y")),
            @AttributeOverride(name = "town.name", column = @Column(name = "official_address_town_name")),
    })
    private Address officialAddress;

}
