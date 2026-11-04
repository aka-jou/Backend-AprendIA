package com.aprendia.backend.feature.student.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.common.validation.ValidationPatterns;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.student.dto.PersonLookupDTO;
import com.aprendia.backend.feature.student.dto.StudentRelativeDTO;
import com.aprendia.backend.feature.student.service.StudentRelativeService;
import com.aprendia.backend.feature.student.service.StudentRelativeSupport;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.Student;
import com.aprendia.backend.feature.user.entities.StudentRelative;
import com.aprendia.backend.feature.user.repository.PersonRepository;
import com.aprendia.backend.feature.user.repository.StudentRepository;
import com.aprendia.backend.feature.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Búsqueda de persona por CURP y CRUD independiente de familiares (spec de búsqueda por CURP,
 * secciones 2 y 4). Desvincular un familiar no borra su persona: puede seguir vinculado a otros alumnos.
 */
@Service
public class StudentRelativeServiceImpl implements StudentRelativeService {

    private static final Set<String> STAFF_WITH_FULL_ACCESS = Set.of(RoleNames.ADMINISTRADOR, RoleNames.ASESOR_PEDAGOGICO);

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRelativeSupport support;

    @Override
    @Transactional(readOnly = true)
    public PersonLookupDTO findPersonByCurp(String curp) {
        String value = curp == null ? "" : curp.trim();
        if (!value.matches(ValidationPatterns.CURP)) {
            throw new BadRequestException("La CURP debe tener exactamente 18 caracteres con formato válido.");
        }
        return personRepository.findByCurp(StudentRelativeSupport.normalizeCurp(value))
                .map(support::toLookup)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una persona registrada con la CURP proporcionada."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentRelativeDTO> getRelatives(Long studentId) {
        Student student = findStudent(studentId);
        assertCanRead(student);
        return student.getRelatives().stream().map(support::toDto).toList();
    }

    @Override
    @Transactional
    public StudentRelativeDTO addRelative(Long studentId, StudentRelativeDTO request) {
        Student student = findStudent(studentId);
        Person person = support.resolvePerson(request, student.getPerson(), "relative");
        boolean linked = student.getRelatives().stream().anyMatch(r -> r.getPerson().getId().equals(person.getId()));
        if (linked) {
            throw new ConflictException("El familiar (personId " + person.getId() + ") ya está vinculado a este estudiante.");
        }
        StudentRelative relative = StudentRelative.builder()
                .student(student)
                .person(person)
                .relationship(request.relationship().trim())
                .build();
        student.getRelatives().add(relative);
        studentRepository.saveAndFlush(student);
        return support.toDto(relative);
    }

    /**
     * Actualiza parentesco y datos del familiar. Los cambios se ven en todos los alumnos vinculados a esa
     * persona. Si la persona es además un estudiante o un usuario del sistema, sus datos se administran
     * desde su expediente o su cuenta: aquí solo cambia el parentesco.
     */
    @Override
    @Transactional
    public StudentRelativeDTO updateRelative(Long studentId, Long relativeId, StudentRelativeDTO request) {
        Student student = findStudent(studentId);
        StudentRelative relative = findRelative(student, relativeId);
        Person person = relative.getPerson();
        boolean managedElsewhere = studentRepository.findByPersonId(person.getId()).isPresent()
                || userRepository.findByPersonId(person.getId()).isPresent();
        if (!managedElsewhere) {
            support.applyPersonData(person, request, "");
            personRepository.save(person);
        }
        relative.setRelationship(request.relationship().trim());
        studentRepository.saveAndFlush(student);
        return support.toDto(relative);
    }

    @Override
    @Transactional
    public void removeRelative(Long studentId, Long relativeId) {
        Student student = findStudent(studentId);
        StudentRelative relative = findRelative(student, relativeId);
        // orphanRemoval borra solo el vínculo; la fila de persons se conserva
        student.getRelatives().remove(relative);
        studentRepository.save(student);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + id));
    }

    private static StudentRelative findRelative(Student student, Long relativeId) {
        return student.getRelatives().stream()
                .filter(r -> r.getId().equals(relativeId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Familiar no encontrado con ID: " + relativeId
                        + " para el estudiante con ID: " + student.getId()));
    }

    /** Un DOCENTE_AULA solo consulta los familiares de sus propios alumnos. */
    private void assertCanRead(Student student) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean fullAccess = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> STAFF_WITH_FULL_ACCESS.contains(a.getAuthority()));
        if (fullAccess) {
            return;
        }
        Long currentUserId = userRepository.findByUsernameIgnoreCase(CurrentUser.username()).map(u -> u.getId()).orElse(null);
        if (currentUserId == null || !currentUserId.equals(student.getTeacherId())) {
            throw new AccessDeniedException("Un docente solo puede consultar los familiares de sus propios alumnos.");
        }
    }
}
