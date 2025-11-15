package itmo.ivank.soa.entity;

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
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private LocalDate creationDate = LocalDate.now();

    @Column
    @Positive
    private Float annualTurnover;

    @Column
    private String fullName;

    @Column
    @Embedded
    private Coordinates coordinates;

    @Column
    @Enumerated(EnumType.STRING)
    private OrganizationType type;

    @Column
    @Embedded
    private Address officialAddress;

}
