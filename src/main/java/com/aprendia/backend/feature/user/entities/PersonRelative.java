package com.aprendia.backend.feature.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_relatives", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"person_id", "relative_person_id", "relative_role_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRelative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relative_person_id", nullable = false)
    private Person relativePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relative_role_id", nullable = false)
    private RelativeRole relativeRole;
}
