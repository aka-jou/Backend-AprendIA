package com.aprendia.backend.feature.user.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.common.util.DateFormats;
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
    public PagedResponse<UserSummaryDTO> getUsers(String query, Pageable pageable) {
        Page<User> page;
        if (StringUtils.hasText(query)) {
            String term = query.trim();
            page = userRepository.searchStaff(RoleNames.STUDENT,
                    "%" + term.toLowerCase(Locale.ROOT) + "%",
                    term.toUpperCase(Locale.ROOT),
                    pageable);
        } else {
            page = userRepository.findStaff(RoleNames.STUDENT, pageable);
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

        Set<Role> roles = resolveRoles(request.assignment().roles());
        Dependency dependency = resolveDependency(request.assignment().dependency());

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

    @Override
    @Transactional
    public UserDetailDTO updateUser(Long id, UserUpdateRequestDTO request) {
        User user = findStaffOrThrow(id);
        boolean isSelf = user.getUsername().equals(CurrentUser.username());

        UserPersonaDTO persona = request.persona();
        String email = normalizeEmail(persona.email());
        String curp = normalizeCurp(persona.curp());

        userRepository.findByEmailIgnoreCase(email)
                .filter(other -> !other.getId().equals(user.getId()))
                .ifPresent(other -> { throw new BadRequestException("El correo electrónico ya está registrado."); });

        Person person = user.getPerson() != null ? user.getPerson() : new Person();
        personRepository.findByCurp(curp)
                .filter(other -> person.getId() == null || !other.getId().equals(person.getId()))
                .ifPresent(other -> { throw new BadRequestException("La CURP proporcionada ya está registrada en el sistema."); });

        Set<Role> roles = resolveRoles(request.assignment().roles());
        if (isSelf && roles.stream().noneMatch(r -> RoleNames.ADMINISTRADOR.equals(r.getName()))) {
            throw new BadRequestException("No puede quitarse a sí mismo el rol ADMINISTRADOR.");
        }

        if (STATUS_INACTIVE.equals(request.status())) {
            if (isSelf) {
                throw new BadRequestException("No puede desactivar su propia cuenta.");
            }
            user.setActive(false);
        } else if (STATUS_ACTIVE.equals(request.status())) {
            user.setActive(true);
        }

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

        applyPersona(person, persona, curp, request.assignment());
        user.setPerson(personRepository.save(person));
        user.setEmail(email);
        user.setRoles(roles);
        user.setDependency(resolveDependency(request.assignment().dependency()));

        return toDetail(userRepository.save(user));
    }

    // ------------------------------------------------------------------ baja

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = findStaffOrThrow(id);
        if (user.getUsername().equals(CurrentUser.username())) {
            throw new BadRequestException("No puede desactivar su propia cuenta.");
        }
        // Baja lógica: el registro se conserva para auditoría; sin is_active no puede autenticarse
        user.setActive(false);
        userRepository.save(user);
    }

    // ------------------------------------------------------------------ apoyo

    /** El módulo de usuarios solo opera sobre personal; los estudiantes se gestionan en /students. */
    private User findStaffOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRoles().stream().noneMatch(r -> RoleNames.STUDENT.equals(r.getName())))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private Set<Role> resolveRoles(List<String> requested) {
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
        return roles;
    }

    private Dependency resolveDependency(String name) {
        return dependencyRepository.findFirstByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new BadRequestException(
                        "La dependencia '" + name.trim() + "' no existe en el catálogo de dependencias."));
    }

    private void applyPersona(Person person, UserPersonaDTO persona, String curp, UserAssignmentDTO assignment) {
        person.setCurp(curp);
        person.setFirstName(persona.firstName().trim());
        person.setMiddleName(blankToNull(persona.secondName()));
        person.setLastName(persona.firstSurname().trim());
        person.setSecondLastName(blankToNull(persona.secondSurname()));
        person.setBirthDate(DateFormats.startOfDay(persona.birthDate()));
        person.setGender(persona.gender().toUpperCase(Locale.ROOT));
        person.setPhone(persona.phone());
        person.setIneNumber(blankToNull(assignment.ineNumber()));
        person.setImageUrl(blankToNull(assignment.photoUrl()));
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
                .ineNumber(person != null ? person.getIneNumber() : null)
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
