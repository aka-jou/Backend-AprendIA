package com.aprendia.backend.feature.user.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.common.util.PhoneNumbers;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.entities.Dependency;
import com.aprendia.backend.feature.catalogs.entities.Role;
import com.aprendia.backend.feature.catalogs.repository.DependencyRepository;
import com.aprendia.backend.feature.catalogs.repository.RoleRepository;
import com.aprendia.backend.feature.user.dto.UserAssignmentDTO;
import com.aprendia.backend.feature.user.dto.UserCreateRequestDTO;
import com.aprendia.backend.feature.user.dto.UserCredentialsUpdateDTO;
import com.aprendia.backend.feature.user.dto.UserDetailDTO;
import com.aprendia.backend.feature.user.dto.UserPersonaDTO;
import com.aprendia.backend.feature.user.dto.UserUpdatePersonaDTO;
import com.aprendia.backend.feature.user.dto.UserSummaryDTO;
import com.aprendia.backend.feature.user.dto.UserUpdateRequestDTO;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.User;
import com.aprendia.backend.feature.user.repository.PersonRepository;
import com.aprendia.backend.feature.user.repository.UserRepository;
import com.aprendia.backend.feature.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {

    private static final String STATUS_ACTIVE = "Activo";
    private static final String STATUS_INACTIVE = "Inactivo";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DependencyRepository dependencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------ consultas

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryDTO> getUsers(String query, boolean incluirInactivos, Pageable pageable) {
        Page<User> page;
        if (StringUtils.hasText(query)) {
            String term = query.trim();
            page = userRepository.searchStaff(RoleNames.STUDENT,
                    "%" + term.toLowerCase(Locale.ROOT) + "%",
                    term.toUpperCase(Locale.ROOT),
                    incluirInactivos,
                    pageable);
        } else {
            page = userRepository.findStaff(RoleNames.STUDENT, incluirInactivos, pageable);
        }
        return PagedResponse.fromPage(page.map(this::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailDTO getUserById(Long id) {
        return toDetail(findStaffOrThrow(id));
    }

    // ------------------------------------------------------------------ alta

    @Override
    @Transactional
    public UserDetailDTO createUser(UserCreateRequestDTO request) {
        UserPersonaDTO persona = request.persona();
        String email = normalizeEmail(persona.email());
        String curp = normalizeCurp(persona.curp());
        String username = request.credentials().username().trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("El nombre de usuario ya está en uso.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }
        if (personRepository.existsByCurp(curp)) {
            throw new BadRequestException("La CURP proporcionada ya está registrada en el sistema.");
        }

        Set<Role> roles = resolveRoles(request.assignment().roles(), Set.of());
        Dependency dependency = resolveDependency(request.assignment().dependency(), null);

        Person person = new Person();
        applyPersona(person, persona, curp, request.assignment());
        person = personRepository.save(person);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.credentials().password()))
                .isActive(true)
                .person(person)
                .dependency(dependency)
                .roles(roles)
                .build();

        return toDetail(userRepository.save(user));
    }

    // ------------------------------------------------------------------ actualización

    /**
     * Actualización parcial: cada dato que no viene conserva su valor actual. Acepta el cuerpo del
     * registro (persona, credentials, assignment, status) y el del directorio del frontend
     * (email, roles, idDependencia/entidad, activo); ver {@link UserUpdateRequestDTO}.
     */
    @Override
    @Transactional
    public UserDetailDTO updateUser(Long id, UserUpdateRequestDTO request) {
        User user = findStaffOrThrow(id);
        boolean isSelf = user.getUsername().equals(CurrentUser.username());
        UserUpdatePersonaDTO persona = request.persona();
        UserAssignmentDTO assignment = request.assignment();

        // ---- correo
        String requestedEmail = firstNonBlank(request.email(), persona != null ? persona.email() : null);
        if (requestedEmail != null) {
            String email = normalizeEmail(requestedEmail);
            userRepository.findByEmailIgnoreCase(email)
                    .filter(other -> !other.getId().equals(user.getId()))
                    .ifPresent(other -> { throw new BadRequestException("El correo electrónico ya está registrado."); });
            user.setEmail(email);
        }

        // ---- roles
        List<String> requestedRoles = request.roles() != null ? request.roles()
                : assignment != null ? assignment.roles() : null;
        if (requestedRoles != null) {
            Set<Role> roles = resolveRoles(requestedRoles, user.getRoles());
            if (isSelf && roles.stream().noneMatch(r -> RoleNames.ADMINISTRADOR.equals(r.getName()))) {
                throw new BadRequestException("No puede quitarse a sí mismo el rol ADMINISTRADOR.");
            }
            user.setRoles(roles);
        }

        // ---- dependencia: idDependencia, entidad (id o nombre) o assignment.dependency (nombre)
        Dependency current = user.getDependency();
        if (request.idDependencia() != null) {
            user.setDependency(resolveDependencyById(request.idDependencia(), current));
        } else if (StringUtils.hasText(request.entidad())) {
            String entidad = request.entidad().trim();
            user.setDependency(entidad.chars().allMatch(Character::isDigit)
                    ? resolveDependencyById(Long.valueOf(entidad), current)
                    : resolveDependency(entidad, current));
        } else if (assignment != null && StringUtils.hasText(assignment.dependency())) {
            user.setDependency(resolveDependency(assignment.dependency(), current));
        }

        // ---- estado: activo (booleano) o status ("Activo" / "Inactivo")
        Boolean activo = request.activo() != null ? request.activo()
                : STATUS_INACTIVE.equals(request.status()) ? Boolean.FALSE
                : STATUS_ACTIVE.equals(request.status()) ? Boolean.TRUE : null;
        if (activo != null) {
            applyActive(user, activo, isSelf);
        }

        // ---- credenciales
        UserCredentialsUpdateDTO credentials = request.credentials();
        if (credentials != null) {
            String username = credentials.username().trim();
            if (!username.equalsIgnoreCase(user.getUsername())) {
                if (isSelf) {
                    throw new BadRequestException("No puede cambiar su propio nombre de usuario: invalidaría su sesión actual.");
                }
                if (userRepository.existsByUsernameIgnoreCase(username)) {
                    throw new BadRequestException("El nombre de usuario ya está en uso.");
                }
                user.setUsername(username);
            }
            if (StringUtils.hasText(credentials.password())) {
                user.setPassword(passwordEncoder.encode(credentials.password()));
            }
        }

        // ---- persona
        Person person = user.getPerson() != null ? user.getPerson() : new Person();
        if (persona != null) {
            if (StringUtils.hasText(persona.curp())) {
                String curp = normalizeCurp(persona.curp());
                personRepository.findByCurp(curp)
                        .filter(other -> person.getId() == null || !other.getId().equals(person.getId()))
                        .ifPresent(other -> { throw new BadRequestException("La CURP proporcionada ya está registrada en el sistema."); });
                person.setCurp(curp);
            }
            applyPersonaUpdate(person, persona);
        }
        if (assignment != null && assignment.photoUrl() != null) {
            person.setImageUrl(blankToNull(assignment.photoUrl()));
        }
        requireCompletePerson(person);
        user.setPerson(personRepository.save(person));

        return toDetail(userRepository.save(user));
    }

    // ------------------------------------------------------------------ estado y baja

    @Override
    @Transactional
    public UserDetailDTO updateStatus(Long id, boolean activo) {
        User user = findStaffOrThrow(id);
        applyActive(user, activo, user.getUsername().equals(CurrentUser.username()));
        return toDetail(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = findStaffOrThrow(id);
        // Baja lógica: el registro se conserva para auditoría; sin is_active no puede autenticarse
        applyActive(user, false, user.getUsername().equals(CurrentUser.username()));
        userRepository.save(user);
    }

    private void applyActive(User user, boolean activo, boolean isSelf) {
        if (!activo && isSelf) {
            throw new BadRequestException("No puede desactivar su propia cuenta.");
        }
        user.setActive(activo);
    }

    // ------------------------------------------------------------------ apoyo

    /** El módulo de usuarios solo opera sobre personal; los estudiantes se gestionan en /students. */
    private User findStaffOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRoles().stream().noneMatch(r -> RoleNames.STUDENT.equals(r.getName())))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Roles pedidos -> entidades. Un rol desactivado (catálogo de roles, status = false) no se puede
     * asignar, salvo que el usuario ya lo tenga: así un PUT que reenvía sus roles actuales no falla.
     */
    private Set<Role> resolveRoles(List<String> requested, Set<Role> currentRoles) {
        Set<String> authorities = requested.stream()
                .map(RoleNames::toAuthority)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (authorities.contains(RoleNames.STUDENT)) {
            throw new BadRequestException("El rol STUDENT no se asigna desde /users; registre al alumno en /students.");
        }

        Set<Role> roles = new HashSet<>();
        List<String> unknown = new ArrayList<>();
        for (String authority : authorities) {
            roleRepository.findByName(authority).ifPresentOrElse(roles::add, () -> unknown.add(RoleNames.toApi(authority)));
        }
        if (!unknown.isEmpty()) {
            throw new BadRequestException("Roles no registrados en el catálogo: " + String.join(", ", unknown) + ".");
        }
        List<String> inactive = roles.stream()
                .filter(r -> Boolean.FALSE.equals(r.getStatus()))
                .filter(r -> currentRoles.stream().noneMatch(c -> c.getId().equals(r.getId())))
                .map(r -> RoleNames.toApi(r.getName()))
                .sorted()
                .toList();
        if (!inactive.isEmpty()) {
            throw new BadRequestException("Roles desactivados en el catálogo: " + String.join(", ", inactive) + ".");
        }
        return roles;
    }

    private Dependency resolveDependency(String name, Dependency current) {
        Dependency dependency = dependencyRepository.findFirstByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new BadRequestException(
                        "La dependencia '" + name.trim() + "' no existe en el catálogo de dependencias."));
        return requireActiveDependency(dependency, current);
    }

    private Dependency resolveDependencyById(Long id, Dependency current) {
        Dependency dependency = dependencyRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La dependencia con ID " + id + " no existe."));
        return requireActiveDependency(dependency, current);
    }

    /** Una dependencia desactivada no se asigna, salvo que el usuario ya esté adscrito a ella. */
    private Dependency requireActiveDependency(Dependency dependency, Dependency current) {
        boolean isCurrent = current != null && current.getId().equals(dependency.getId());
        if (Boolean.FALSE.equals(dependency.getStatus()) && !isCurrent) {
            throw new BadRequestException("La dependencia '" + dependency.getName() + "' está desactivada.");
        }
        return dependency;
    }

    private void applyPersona(Person person, UserPersonaDTO persona, String curp, UserAssignmentDTO assignment) {
        person.setCurp(curp);
        person.setFirstName(persona.firstName().trim());
        person.setMiddleName(blankToNull(persona.secondName()));
        person.setLastName(persona.firstSurname().trim());
        person.setSecondLastName(blankToNull(persona.secondSurname()));
        person.setBirthDate(DateFormats.startOfDay(persona.birthDate()));
        person.setGender(persona.gender().toUpperCase(Locale.ROOT));
        person.setPhone(PhoneNumbers.normalize(persona.phone()));
        person.setImageUrl(blankToNull(assignment.photoUrl()));
    }

    /** Solo cambia los datos que vienen; segundo nombre y segundo apellido vacíos se borran. */
    private void applyPersonaUpdate(Person person, UserUpdatePersonaDTO persona) {
        if (StringUtils.hasText(persona.firstName())) {
            person.setFirstName(persona.firstName().trim());
        }
        if (persona.secondName() != null) {
            person.setMiddleName(blankToNull(persona.secondName()));
        }
        if (StringUtils.hasText(persona.firstSurname())) {
            person.setLastName(persona.firstSurname().trim());
        }
        if (persona.secondSurname() != null) {
            person.setSecondLastName(blankToNull(persona.secondSurname()));
        }
        if (persona.birthDate() != null) {
            person.setBirthDate(DateFormats.startOfDay(persona.birthDate()));
        }
        if (StringUtils.hasText(persona.gender())) {
            person.setGender(persona.gender().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(persona.phone())) {
            person.setPhone(PhoneNumbers.normalize(persona.phone()));
        }
    }

    /** Un usuario sin persona previa (datos antiguos) debe quedar con los datos mínimos completos. */
    private void requireCompletePerson(Person person) {
        if (!StringUtils.hasText(person.getCurp()) || !StringUtils.hasText(person.getFirstName())
                || person.getLastName() == null) {
            throw new BadRequestException("El usuario no tiene datos personales registrados: envíe persona con curp, "
                    + "primer nombre y primer apellido.");
        }
    }

    private static String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : StringUtils.hasText(second) ? second : null;
    }

    private UserSummaryDTO toSummary(User user) {
        Person person = user.getPerson();
        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nombreCompleto(fullName(user))
                .email(user.getEmail())
                .role(primaryRole(user))
                .adscripcion(user.getDependency() != null ? user.getDependency().getName() : null)
                .status(user.isActive() ? STATUS_ACTIVE : STATUS_INACTIVE)
                .curp(person != null ? person.getCurp() : null)
                .createdAt(DateFormats.isoInstant(user.getCreatedAt()))
                .build();
    }

    private UserDetailDTO toDetail(User user) {
        Person person = user.getPerson();
        List<String> roles = apiRoles(user);

        UserDetailDTO.Persona personaDto = person == null ? null : UserDetailDTO.Persona.builder()
                .curp(person.getCurp())
                .firstName(person.getFirstName())
                .secondName(person.getMiddleName())
                .firstSurname(person.getLastName())
                .secondSurname(person.getSecondLastName())
                .birthDate(DateFormats.isoDate(person.getBirthDate()))
                .gender(person.getGender())
                .email(user.getEmail())
                .phone(person.getPhone())
                .build();

        UserDetailDTO.Assignment assignmentDto = UserDetailDTO.Assignment.builder()
                .roles(roles)
                .dependency(user.getDependency() != null ? user.getDependency().getName() : null)
                .idDependencia(user.getDependency() != null ? user.getDependency().getId() : null)
                .photoUrl(person != null ? person.getImageUrl() : null)
                .build();

        return UserDetailDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nombreCompleto(fullName(user))
                .email(user.getEmail())
                .status(user.isActive() ? STATUS_ACTIVE : STATUS_INACTIVE)
                .roles(roles)
                .persona(personaDto)
                .assignment(assignmentDto)
                .createdAt(DateFormats.isoInstant(user.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(user.getUpdatedAt()))
                .build();
    }

    private List<String> apiRoles(User user) {
        return user.getRoles().stream().map(r -> RoleNames.toApi(r.getName())).sorted().toList();
    }

    /** La spec lista un solo "role" por usuario: ADMINISTRADOR tiene prioridad, si no el primero alfabético. */
    private String primaryRole(User user) {
        return apiRoles(user).stream()
                .min(Comparator.comparing((String r) -> !"ADMINISTRADOR".equals(r)).thenComparing(r -> r))
                .orElse(null);
    }

    private String fullName(User user) {
        Person p = user.getPerson();
        if (p == null) {
            return user.getUsername();
        }
        return Stream.of(p.getFirstName(), p.getMiddleName(), p.getLastName(), p.getSecondLastName())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCurp(String curp) {
        return curp.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
