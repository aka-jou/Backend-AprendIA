package com.aprendia.backend.feature.student.service;

import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.common.util.PhoneNumbers;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.feature.catalogs.entities.Municipality;
import com.aprendia.backend.feature.catalogs.repository.MunicipalityRepository;
import com.aprendia.backend.feature.catalogs.repository.StateRepository;
import com.aprendia.backend.feature.student.dto.PersonLookupDTO;
import com.aprendia.backend.feature.student.dto.StudentAddressDTO;
import com.aprendia.backend.feature.student.dto.StudentRelativeDTO;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.RelativeAddress;
import com.aprendia.backend.feature.user.entities.StudentRelative;
import com.aprendia.backend.feature.user.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Reglas de familiares compartidas por /students (relatives[]) y /students/{id}/relatives:
 * resolver qué persona se vincula sin duplicarla, aplicar sus datos y armar las respuestas.
 */
@Component
public class StudentRelativeSupport {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    /**
     * Persona que se vincula al alumno (spec, sección 1, paso 2):
     * con personId, esa persona; sin personId, la de la misma CURP si ya existe; si no, una nueva.
     *
     * @param field prefijo de los mensajes de error, ej. "relatives[0]".
     */
    public Person resolvePerson(StudentRelativeDTO dto, Person studentPerson, String field) {
        Person person;
        if (dto.personId() != null) {
            person = personRepository.findById(dto.personId())
                    .orElseThrow(() -> new BadRequestException(field + ".personId: la persona con ID " + dto.personId() + " no existe."));
        } else {
            String curp = normalizeCurp(dto.curp());
            Optional<Person> existing = curp == null ? Optional.empty() : personRepository.findByCurp(curp);
            if (existing.isPresent()) {
                person = existing.get();
            } else {
                person = new Person();
                applyPersonData(person, dto, field);
                person = personRepository.save(person);
            }
        }
        if (studentPerson != null && studentPerson.getId() != null && studentPerson.getId().equals(person.getId())) {
            throw new BadRequestException(field + ": el familiar no puede ser el propio estudiante.");
        }
        return person;
    }

    /**
     * Copia los datos del familiar a la persona (alta, o PUT /students/{id}/relatives/{relativeId}).
     * La CURP, si viene, no puede ser de otra persona.
     */
    public void applyPersonData(Person person, StudentRelativeDTO dto, String field) {
        String prefix = field.isEmpty() ? "" : field + ": ";
        String firstName = blankToNull(dto.firstName());
        String lastName = blankToNull(dto.lastName());
        if (firstName == null) {
            throw new BadRequestException(prefix + "El primer nombre del familiar es obligatorio.");
        }
        if (lastName == null) {
            throw new BadRequestException(prefix + "El primer apellido del familiar es obligatorio.");
        }
        String curp = normalizeCurp(dto.curp());
        if (curp != null) {
            personRepository.findByCurp(curp)
                    .filter(other -> !other.getId().equals(person.getId()))
                    .ifPresent(other -> {
                        throw new BadRequestException(prefix + "La CURP del familiar ya está registrada en el sistema (personId "
                                + other.getId() + ").");
                    });
        }
        if (dto.address() != null) {
            validateAddress(dto.address());
        }
        String gender = blankToNull(dto.gender());
        String email = blankToNull(dto.email());

        person.setFirstName(firstName);
        person.setMiddleName(blankToNull(dto.middleName()));
        person.setLastName(lastName);
        person.setSecondLastName(blankToNull(dto.secondLastName()));
        person.setCurp(curp);
        person.setBirthDate(DateFormats.startOfDay(dto.birthDate()));
        person.setGender(gender != null ? gender.toUpperCase(Locale.ROOT) : null);
        person.setPhone(PhoneNumbers.normalize(dto.phone()));
        person.setEmail(email != null ? email.toLowerCase(Locale.ROOT) : null);
        person.setAddress(toAddress(dto.address()));
    }

    /** El estado debe existir y el municipio debe pertenecer a ese estado. */
    public void validateAddress(StudentAddressDTO address) {
        if (!stateRepository.existsById(address.stateId())) {
            throw new BadRequestException("El estado con ID " + address.stateId() + " no existe en el catálogo.");
        }
        Municipality municipality = municipalityRepository.findById(address.municipalityId())
                .orElseThrow(() -> new BadRequestException("El municipio con ID " + address.municipalityId() + " no existe en el catálogo."));
        if (municipality.getState() == null || !address.stateId().equals(municipality.getState().getId())) {
            throw new BadRequestException("El municipio con ID " + address.municipalityId()
                    + " no pertenece al estado con ID " + address.stateId() + ".");
        }
    }

    public StudentRelativeDTO toDto(StudentRelative relative) {
        Person p = relative.getPerson();
        return StudentRelativeDTO.builder()
                .relativeId(relative.getId())
                .personId(p.getId())
                .curp(p.getCurp())
                .firstName(p.getFirstName())
                .middleName(p.getMiddleName())
                .lastName(p.getLastName())
                .secondLastName(p.getSecondLastName())
                .name(fullName(p))
                .birthDate(p.getBirthDate() != null ? p.getBirthDate().toLocalDate() : null)
                .gender(p.getGender())
                .phone(p.getPhone())
                .email(p.getEmail())
                .relationship(relative.getRelationship())
                .address(toAddressDto(p.getAddress()))
                .build();
    }

    public PersonLookupDTO toLookup(Person p) {
        return PersonLookupDTO.builder()
                .id(p.getId())
                .personId(p.getId())
                .curp(p.getCurp())
                .firstName(p.getFirstName())
                .middleName(p.getMiddleName())
                .lastName(p.getLastName())
                .secondLastName(p.getSecondLastName())
                .fullName(fullName(p))
                .birthDate(DateFormats.isoDate(p.getBirthDate()))
                .gender(p.getGender())
                .phone(p.getPhone())
                .email(p.getEmail())
                .address(toAddressDto(p.getAddress()))
                .build();
    }

    public static String normalizeCurp(String curp) {
        String value = blankToNull(curp);
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    public static String fullName(Person p) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String part : new String[] {p.getFirstName(), p.getMiddleName(), p.getLastName(), p.getSecondLastName()}) {
            if (part != null && !part.isBlank()) {
                joiner.add(part.trim());
            }
        }
        return joiner.toString();
    }

    private static RelativeAddress toAddress(StudentAddressDTO dto) {
        if (dto == null) {
            return null;
        }
        return RelativeAddress.builder()
                .street(dto.street().trim())
                .exteriorNumber(dto.exteriorNumber().trim())
                .settlementType(blankToNull(dto.settlementType()))
                .settlement(dto.settlement().trim())
                .municipalityId(Math.toIntExact(dto.municipalityId()))
                .stateId(Math.toIntExact(dto.stateId()))
                .zipCode(dto.zipCode())
                .build();
    }

    private static StudentAddressDTO toAddressDto(RelativeAddress a) {
        if (a == null) {
            return null;
        }
        return StudentAddressDTO.builder()
                .street(a.getStreet())
                .exteriorNumber(a.getExteriorNumber())
                .settlementType(a.getSettlementType())
                .settlement(a.getSettlement())
                .municipalityId(a.getMunicipalityId() != null ? a.getMunicipalityId().longValue() : null)
                .stateId(a.getStateId() != null ? a.getStateId().longValue() : null)
                .zipCode(a.getZipCode())
                .build();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
