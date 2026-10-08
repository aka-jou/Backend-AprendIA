package com.aprendia.backend.feature.student.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.common.util.DateFormats;
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
import com.aprendia.backend.feature.user.entities.Student;
import com.aprendia.backend.feature.user.entities.StudentRelative;
import com.aprendia.backend.feature.user.entities.User;
import com.aprendia.backend.feature.user.repository.PersonRepository;
import com.aprendia.backend.feature.user.repository.StudentRepository;
import com.aprendia.backend.feature.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        // Cuentas de usuario y nombres de perfil de toda la página en 2 consultas (evita N+1)
        Set<Long> personIds = page.getContent().stream().map(s -> s.getPerson().getId()).collect(Collectors.toSet());
        Map<Long, User> usersByPerson = new HashMap<>();
        if (!personIds.isEmpty()) {
            userRepository.findByPersonIdIn(personIds).forEach(u -> usersByPerson.putIfAbsent(u.getPerson().getId(), u));
        }
        Set<Long> profileIds = page.getContent().stream()
                .map(Student::getProfileId).filter(Objects::nonNull).map(Integer::longValue).collect(Collectors.toSet());
        Map<Long, String> profileNames = new HashMap<>();
        profileRepository.findAllById(profileIds).forEach(p -> profileNames.put(p.getId(), p.getName()));

        return PagedResponse.fromPage(page.map(s -> toResponse(s,
                usersByPerson.get(s.getPerson().getId()),
                s.getProfileId() != null ? profileNames.get(s.getProfileId().longValue()) : null)));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDTO getStudentById(Long id) {
        Student student = findOrThrow(id);
        return toResponse(student, findAccount(student), profileName(student));
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
        validateAddress(request.address());
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

        return toResponse(student, account, profile.getName());
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
        validateAddress(request.address());

        applyPerson(person, request.person(), curp);
        personRepository.save(person);

        student.setProfileId(Math.toIntExact(profile.getId()));
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

        return toResponse(student, account, profile.getName());
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
        person.setPhone(blankToNull(dto.phone()));
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
            student.getRelatives().add(StudentRelative.builder()
                    .student(student)
                    .name(dto.name().trim())
                    .relationship(dto.relationship().trim())
                    .phone(blankToNull(dto.phone()))
                    .build());
        }
    }

    private StudentResponseDTO toResponse(Student student, User account, String profileName) {
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

        List<StudentRelativeDTO> relatives = student.getRelatives().stream()
                .map(r -> StudentRelativeDTO.builder().name(r.getName()).relationship(r.getRelationship()).phone(r.getPhone()).build())
                .toList();

        return StudentResponseDTO.builder()
                .id(student.getId())
                .person(person)
                .address(address)
                .relatives(relatives)
                .profile(student.getProfileId() != null ? student.getProfileId().longValue() : null)
                .profileName(profileName)
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
