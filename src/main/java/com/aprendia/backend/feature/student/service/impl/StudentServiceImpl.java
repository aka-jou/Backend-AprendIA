package com.aprendia.backend.feature.student.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.common.util.PhoneNumbers;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.entities.Municipality;
import com.aprendia.backend.feature.catalogs.entities.Profile;
import com.aprendia.backend.feature.catalogs.entities.Role;
import com.aprendia.backend.feature.catalogs.repository.MunicipalityRepository;
import com.aprendia.backend.feature.catalogs.repository.ProfileRepository;
import com.aprendia.backend.feature.catalogs.repository.RoleRepository;
import com.aprendia.backend.feature.catalogs.repository.StateRepository;
import com.aprendia.backend.feature.student.dto.StudentAddressDTO;
import com.aprendia.backend.feature.student.dto.StudentPersonDTO;
import com.aprendia.backend.feature.student.dto.StudentRelativeDTO;
import com.aprendia.backend.feature.student.dto.StudentRequestDTO;
import com.aprendia.backend.feature.student.dto.StudentResponseDTO;
import com.aprendia.backend.feature.student.service.StudentService;
import com.aprendia.backend.feature.user.entities.Address;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.RelativeAddress;
import com.aprendia.backend.feature.user.entities.Student;
import com.aprendia.backend.feature.user.entities.StudentRelative;
import com.aprendia.backend.feature.user.entities.User;
import com.aprendia.backend.feature.user.repository.PersonRepository;
import com.aprendia.backend.feature.user.repository.StudentRepository;
import com.aprendia.backend.feature.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------ consultas

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponseDTO> getStudents(String query, Pageable pageable) {
        Page<Student> page;
        if (StringUtils.hasText(query)) {
            String term = query.trim();
            page = studentRepository.search("%" + term.toLowerCase(Locale.ROOT) + "%", term.toUpperCase(Locale.ROOT), pageable);
        } else {
            page = studentRepository.findAll(pageable);
        }
        return toPagedResponse(page);
    }

    /**
     * Alumnos asignados a un docente. ADMINISTRADOR puede consultar cualquier docente;
     * DOCENTE_AULA solo puede consultar su propio id (otro id responde 403).
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponseDTO> getStudentsByTeacher(Long teacherId, String query, Pageable pageable) {
        User teacher = userRepository.findById(teacherId)
                .filter(this::isTeacher)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con ID: " + teacherId));
        assertCanViewTeacher(teacher);

        Page<Student> page;
        if (StringUtils.hasText(query)) {
            String term = query.trim();
            page = studentRepository.searchByTeacher(teacherId, "%" + term.toLowerCase(Locale.ROOT) + "%",
                    term.toUpperCase(Locale.ROOT), pageable);
        } else {
            page = studentRepository.findByTeacherId(teacherId, pageable);
        }
        return toPagedResponse(page);
    }

    /** Cuentas, nombres de perfil y nombres de docente de toda la página en 3 consultas (evita N+1). */
    private PagedResponse<StudentResponseDTO> toPagedResponse(Page<Student> page) {
        Set<Long> personIds = page.getContent().stream().map(s -> s.getPerson().getId()).collect(Collectors.toSet());
        Map<Long, User> usersByPerson = new HashMap<>();
        if (!personIds.isEmpty()) {
            userRepository.findByPersonIdIn(personIds).forEach(u -> usersByPerson.putIfAbsent(u.getPerson().getId(), u));
        }
        Set<Long> profileIds = page.getContent().stream()
                .map(Student::getProfileId).filter(Objects::nonNull).map(Integer::longValue).collect(Collectors.toSet());
        Map<Long, String> profileNames = new HashMap<>();
        profileRepository.findAllById(profileIds).forEach(p -> profileNames.put(p.getId(), p.getName()));

        Set<Long> teacherIds = page.getContent().stream()
                .map(Student::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> teacherNames = new HashMap<>();
        userRepository.findAllById(teacherIds).forEach(t -> teacherNames.put(t.getId(), displayName(t)));

        return PagedResponse.fromPage(page.map(s -> toResponse(s,
                usersByPerson.get(s.getPerson().getId()),
                s.getProfileId() != null ? profileNames.get(s.getProfileId().longValue()) : null,
                s.getTeacherId() != null ? teacherNames.get(s.getTeacherId()) : null)));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        Student student = findOrThrow(id);
        return toResponse(student, findAccount(student), profileName(student), teacherName(student.getTeacherId()));
    }

    // ------------------------------------------------------------------ alta

    @Override
    @Transactional
    public StudentResponseDTO createStudent(StudentRequestDTO request) {
        String curp = normalizeCurp(request.person().curp());
        String email = normalizeEmail(request.email());
        String username = curp.toLowerCase(Locale.ROOT);

        if (personRepository.existsByCurp(curp)) {
            throw new BadRequestException("La CURP proporcionada ya está registrada en el sistema.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("Ya existe una cuenta asociada a esta CURP.");
        }

        Profile profile = resolveProfile(request.profile());
        User teacher = resolveTeacher(request.teacherId());
        validateAddress(request.address());
        validateRelativeAddresses(request.relatives());
        Role studentRole = roleRepository.findByName(RoleNames.STUDENT)
                .orElseThrow(() -> new IllegalStateException("El rol " + RoleNames.STUDENT + " no está registrado en la base de datos."));

        Person person = new Person();
        applyPerson(person, request.person(), curp);
        person = personRepository.save(person);

        // El mismo identificador sirve como código QR de inicio de sesión del alumno (POST /auth/student)
        String qrCode = UUID.randomUUID().toString();

        Student student = Student.builder()
                .person(person)
                .profileId(Math.toIntExact(profile.getId()))
                .teacherId(teacher != null ? teacher.getId() : null)
                .qrUrl(qrCode)
                .build();
        Address address = new Address();
        address.setStudent(student);
        applyAddress(address, request.address());
        student.setAddress(address);
        replaceRelatives(student, request.relatives());
        student = studentRepository.save(student);

        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        User account = User.builder()
                .username(username)
                .email(email)
                // El alumno entra por OTP (correo) o por QR: la contraseña es aleatoria y nunca se comunica
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .isActive(true)
                .person(person)
                .qrCode(qrCode)
                .roles(roles)
                .build();
        account = userRepository.save(account);

        return toResponse(student, account, profile.getName(), teacher != null ? displayName(teacher) : null);
    }

    // ------------------------------------------------------------------ actualización

    @Override
    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO request) {
        Student student = findOrThrow(id);
        Person person = student.getPerson();
        User account = findAccount(student);

        String curp = normalizeCurp(request.person().curp());
        String email = normalizeEmail(request.email());

        personRepository.findByCurp(curp)
                .filter(other -> !other.getId().equals(person.getId()))
                .ifPresent(other -> { throw new BadRequestException("La CURP proporcionada ya está registrada en el sistema."); });
        userRepository.findByEmailIgnoreCase(email)
                .filter(other -> account == null || !other.getId().equals(account.getId()))
                .ifPresent(other -> { throw new BadRequestException("El correo electrónico ya está registrado."); });

        Profile profile = resolveProfile(request.profile());
        User teacher = resolveTeacher(request.teacherId());
        validateAddress(request.address());
        validateRelativeAddresses(request.relatives());

        applyPerson(person, request.person(), curp);
        personRepository.save(person);

        student.setProfileId(Math.toIntExact(profile.getId()));
        student.setTeacherId(teacher != null ? teacher.getId() : null);
        Address address = student.getAddress();
        if (address == null) {
            address = new Address();
            address.setStudent(student);
        }
        applyAddress(address, request.address());
        student.setAddress(address);
        replaceRelatives(student, request.relatives());
        student = studentRepository.save(student);

        if (account != null) {
            account.setEmail(email);
            // El usuario del alumno es su CURP en minúsculas: se mantiene sincronizado si la CURP se corrige
            String username = curp.toLowerCase(Locale.ROOT);
            if (!username.equalsIgnoreCase(account.getUsername())) {
                if (userRepository.existsByUsernameIgnoreCase(username)) {
                    throw new BadRequestException("Ya existe una cuenta asociada a esta CURP.");
                }
                account.setUsername(username);
            }
            userRepository.save(account);
        }

        return toResponse(student, account, profile.getName(), teacher != null ? displayName(teacher) : null);
    }

    // ------------------------------------------------------------------ baja

    @Override
    @Transactional
    public void deactivateStudent(Long id) {
        Student student = findOrThrow(id);
        User account = findAccount(student);
        // Baja lógica: el expediente se conserva; la cuenta deja de poder autenticarse (OTP, QR y JWT vigentes)
        if (account != null && account.isActive()) {
            account.setActive(false);
            userRepository.save(account);
        }
    }

    // ------------------------------------------------------------------ apoyo

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + id));
    }

    private User findAccount(Student student) {
        return userRepository.findByPersonId(student.getPerson().getId()).orElse(null);
    }

    private String profileName(Student student) {
        if (student.getProfileId() == null) {
            return null;
        }
        return profileRepository.findById(student.getProfileId().longValue()).map(Profile::getName).orElse(null);
    }

    private Profile resolveProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BadRequestException("El perfil con ID " + profileId + " no existe en el catálogo."));
        if (Boolean.FALSE.equals(profile.getStatus())) {
            throw new BadRequestException("El perfil con ID " + profileId + " está inactivo.");
        }
        return profile;
    }

    /** teacherId opcional; si viene, debe ser un usuario activo con rol DOCENTE_AULA. */
    private User resolveTeacher(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new BadRequestException("El docente con ID " + teacherId + " no existe."));
        if (!isTeacher(teacher)) {
            throw new BadRequestException("El usuario con ID " + teacherId + " no tiene el rol DOCENTE_AULA.");
        }
        if (!teacher.isActive()) {
            throw new BadRequestException("El docente con ID " + teacherId + " está inactivo.");
        }
        return teacher;
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream().anyMatch(r -> RoleNames.DOCENTE_AULA.equals(r.getName()));
    }

    private void assertCanViewTeacher(User teacher) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Sesión no autenticada.");
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> RoleNames.ADMINISTRADOR.equals(a.getAuthority()));
        if (!isAdmin && !teacher.getUsername().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Un docente solo puede consultar sus propios alumnos.");
        }
    }

    private String teacherName(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        return userRepository.findById(teacherId).map(this::displayName).orElse(null);
    }

    /** Nombre completo de la persona vinculada al usuario; si no tiene persona, su username. */
    private String displayName(User user) {
        Person p = user.getPerson();
        if (p == null) {
            return user.getUsername();
        }
        return fullName(p.getFirstName(), p.getMiddleName(), p.getLastName(), p.getSecondLastName());
    }

    private String fullName(String... parts) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                joiner.add(part.trim());
            }
        }
        return joiner.toString();
    }

    private void validateRelativeAddresses(List<StudentRelativeDTO> relatives) {
        for (StudentRelativeDTO relative : relatives) {
            if (relative.address() != null) {
                validateAddress(relative.address());
            }
        }
    }

    /** El estado debe existir y el municipio debe pertenecer a ese estado. */
    private void validateAddress(StudentAddressDTO address) {
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

    private void applyPerson(Person person, StudentPersonDTO dto, String curp) {
        person.setFirstName(dto.firstName().trim());
        person.setMiddleName(blankToNull(dto.middleName()));
        person.setLastName(dto.lastName().trim());
        person.setSecondLastName(blankToNull(dto.secondLastName()));
        person.setCurp(curp);
        person.setBirthDate(DateFormats.startOfDay(dto.birthDate()));
        person.setGender(dto.gender().toUpperCase(Locale.ROOT));
        person.setPhone(PhoneNumbers.normalize(dto.phone()));
        person.setImageUrl(blankToNull(dto.imageUrl()));
    }

    private void applyAddress(Address address, StudentAddressDTO dto) {
        address.setStreet(dto.street().trim());
        address.setExteriorNumber(dto.exteriorNumber().trim());
        address.setSettlementType(blankToNull(dto.settlementType()));
        address.setSettlement(dto.settlement().trim());
        address.setMunicipalityId(Math.toIntExact(dto.municipalityId()));
        address.setStateId(Math.toIntExact(dto.stateId()));
        address.setZipCode(dto.zipCode());
    }

    /** Reemplazo completo: orphanRemoval elimina los familiares que ya no vienen en la petición. */
    private void replaceRelatives(Student student, List<StudentRelativeDTO> relatives) {
        student.getRelatives().clear();
        for (StudentRelativeDTO dto : relatives) {
            String firstName = dto.firstName().trim();
            String middleName = blankToNull(dto.middleName());
            String lastName = dto.lastName().trim();
            String secondLastName = blankToNull(dto.secondLastName());
            String curp = blankToNull(dto.curp());
            String gender = blankToNull(dto.gender());
            String email = blankToNull(dto.email());

            student.getRelatives().add(StudentRelative.builder()
                    .student(student)
                    .name(fullName(firstName, middleName, lastName, secondLastName))
                    .firstName(firstName)
                    .middleName(middleName)
                    .lastName(lastName)
                    .secondLastName(secondLastName)
                    .curp(curp != null ? curp.toUpperCase(Locale.ROOT) : null)
                    .birthDate(dto.birthDate())
                    .gender(gender != null ? gender.toUpperCase(Locale.ROOT) : null)
                    .phone(PhoneNumbers.normalize(dto.phone()))
                    .email(email != null ? email.toLowerCase(Locale.ROOT) : null)
                    .relationship(dto.relationship().trim())
                    .address(toRelativeAddress(dto.address()))
                    .build());
        }
    }

    private RelativeAddress toRelativeAddress(StudentAddressDTO dto) {
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

    private StudentRelativeDTO toRelativeDTO(StudentRelative r) {
        RelativeAddress a = r.getAddress();
        StudentAddressDTO address = a == null ? null : StudentAddressDTO.builder()
                .street(a.getStreet())
                .exteriorNumber(a.getExteriorNumber())
                .settlementType(a.getSettlementType())
                .settlement(a.getSettlement())
                .municipalityId(a.getMunicipalityId() != null ? a.getMunicipalityId().longValue() : null)
                .stateId(a.getStateId() != null ? a.getStateId().longValue() : null)
                .zipCode(a.getZipCode())
                .build();
        return StudentRelativeDTO.builder()
                .curp(r.getCurp())
                .firstName(r.getFirstName())
                .middleName(r.getMiddleName())
                .lastName(r.getLastName())
                .secondLastName(r.getSecondLastName())
                .name(r.getName())
                .birthDate(r.getBirthDate())
                .gender(r.getGender())
                .phone(r.getPhone())
                .email(r.getEmail())
                .relationship(r.getRelationship())
                .address(address)
                .build();
    }

    private StudentResponseDTO toResponse(Student student, User account, String profileName, String teacherName) {
        Person p = student.getPerson();
        Address a = student.getAddress();

        StudentResponseDTO.Person person = StudentResponseDTO.Person.builder()
                .firstName(p.getFirstName())
                .middleName(p.getMiddleName())
                .lastName(p.getLastName())
                .secondLastName(p.getSecondLastName())
                .curp(p.getCurp())
                .birthDate(DateFormats.isoDate(p.getBirthDate()))
                .gender(p.getGender())
                .phone(p.getPhone())
                .imageUrl(p.getImageUrl())
                .build();

        StudentResponseDTO.Address address = a == null ? null : StudentResponseDTO.Address.builder()
                .street(a.getStreet())
                .exteriorNumber(a.getExteriorNumber())
                .settlementType(a.getSettlementType())
                .settlement(a.getSettlement())
                .municipalityId(a.getMunicipalityId() != null ? a.getMunicipalityId().longValue() : null)
                .stateId(a.getStateId() != null ? a.getStateId().longValue() : null)
                .zipCode(a.getZipCode())
                .build();

        List<StudentRelativeDTO> relatives = student.getRelatives().stream().map(this::toRelativeDTO).toList();

        return StudentResponseDTO.builder()
                .id(student.getId())
                .person(person)
                .address(address)
                .relatives(relatives)
                .profile(student.getProfileId() != null ? student.getProfileId().longValue() : null)
                .profileName(profileName)
                .teacherId(student.getTeacherId())
                .teacherName(teacherName)
                .email(account != null ? account.getEmail() : null)
                .isActive(account != null ? account.isActive() : null)
                .qrCode(student.getQrUrl())
                .createdAt(account != null ? DateFormats.isoInstant(account.getCreatedAt()) : null)
                .build();
    }

    private String normalizeCurp(String curp) {
        return curp.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
