package com.aprendia.backend.feature.user.service.impl;

import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.entities.Role;
import com.aprendia.backend.feature.catalogs.repository.RoleRepository;
import com.aprendia.backend.feature.catalogs.repository.MunicipalityRepository;
import com.aprendia.backend.feature.catalogs.repository.StateRepository;
import com.aprendia.backend.feature.catalogs.repository.ProfileRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.aprendia.backend.feature.user.repository.PersonRepository;
import com.aprendia.backend.feature.user.repository.StudentRepository;
import com.aprendia.backend.feature.user.repository.PersonRelativeRepository;
import com.aprendia.backend.feature.user.repository.RelativeRoleRepository;
import com.aprendia.backend.feature.user.repository.UserRepository;
import com.aprendia.backend.feature.user.service.UserService;
import com.aprendia.backend.feature.user.mappers.StudentMapper;
//arreglar wildcard
import com.aprendia.backend.feature.user.dto.*;
import com.aprendia.backend.feature.user.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PersonRelativeRepository personRelativeRepository;

    @Autowired
    private RelativeRoleRepository relativeRoleRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private <T> void ensureExists(JpaRepository<T, Long> repository, Long id, String entityName) {
        if (id != null && !repository.existsById(id)) {
            throw new ResourceNotFoundException("El " + entityName + " proporcionado (" + id + ") no existe en el catálogo.");
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
        return mapToUserResponse(user);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<StudentDto> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::mapToStudentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToStudentDto(student);
    }



    @Override
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        return registerUserWithRole(registerRequest, "ROLE_USER");
    }


    @Override
    @Transactional
    public StudentResponse registerStudent(StudentRequest request) {
        String username = request.person().curp().toLowerCase();
        
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Student is already registered (username already exists).");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }

        if (personRepository.existsByCurp(request.person().curp())) {
            throw new BadRequestException("El CURP proporcionado ya está registrado en el sistema.");
        }

        if (request.profile() != null) {
            ensureExists(profileRepository, request.profile().longValue(), "Perfil");
        }
        if (request.address() != null) {
            if (request.address().municipalityId() != null) {
                ensureExists(municipalityRepository, request.address().municipalityId().longValue(), "Municipio");
            }
            if (request.address().stateId() != null) {
                ensureExists(stateRepository, request.address().stateId().longValue(), "Estado");
            }
        }

        Person studentPerson = StudentMapper.buildPersonFromRequest(request.person());
        studentPerson = personRepository.save(studentPerson);

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role 'ROLE_STUDENT' is not registered."));
        
        String generatedQrCode = java.util.UUID.randomUUID().toString();
        
        User user = buildUserForStudent(username, request.email(), studentPerson, studentRole, generatedQrCode);
        User savedUser = userRepository.save(user);

        Student student = StudentMapper.buildStudentFromRequest(request, studentPerson, generatedQrCode);
        studentRepository.save(student);

        saveRelatives(studentPerson, request.relatives());

        return StudentResponse.builder()
                .id(savedUser.getId())
                .qrCode(generatedQrCode)
                .createdAt(savedUser.getCreatedAt() != null ? savedUser.getCreatedAt().toString() : java.time.LocalDateTime.now().toString())
                .createdBy("system_admin") 
                .build();
    }




    @Override
    @Transactional
    public StudentDto updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (request.profile() != null) {
            ensureExists(profileRepository, request.profile().longValue(), "Perfil");
        }
        if (request.address() != null) {
            if (request.address().municipalityId() != null) {
                ensureExists(municipalityRepository, request.address().municipalityId().longValue(), "Municipio");
            }
            if (request.address().stateId() != null) {
                ensureExists(stateRepository, request.address().stateId().longValue(), "Estado");
            }
        }

        Person person = student.getPerson();
        StudentMapper.updatePersonFromRequest(person, request.person());
        personRepository.save(person);

        student.setProfileId(request.profile());

        Address address = student.getAddress();
        if (address == null) {
            address = new Address();
            address.setStudent(student);
        }

        StudentMapper.updateAddressFromRequest(address, request.address());
        student.setAddress(address);

        student = studentRepository.save(student);

        List<PersonRelative> existingRelatives = personRelativeRepository.findByPersonId(person.getId());
        personRelativeRepository.deleteAll(existingRelatives);

        saveRelatives(person, request.relatives());

        return mapToStudentDto(student);
    }



    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BadRequestException("El ID de usuario no puede ser nulo.");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }




    private UserResponse registerUserWithRole(RegisterRequest registerRequest, String roleName) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new BadRequestException("El nombre de usuario ya está en uso.");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new BadRequestException("El correo electrónico ya está registrado.");
        }

        User user = User.builder()
                .username(registerRequest.username())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .isActive(true)
                .build();

        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Error: El Rol base '" + roleName + "' no está registrado en la base de datos."));

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isActive(user.isActive())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    private User buildUserForStudent(String username, String email, Person person, Role role, String qrCode) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                .isActive(true)
                .person(person)
                .qrCode(qrCode)
                .build();
        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        return user;
    }

    private StudentDto mapToStudentDto(Student student) {
        PersonDto personDto = null;
        if (student.getPerson() != null) {
            personDto = PersonDto.builder()
                    .id(student.getPerson().getId())
                    .firstName(student.getPerson().getFirstName())
                    .middleName(student.getPerson().getMiddleName())
                    .lastName(student.getPerson().getLastName())
                    .secondLastName(student.getPerson().getSecondLastName())
                    .curp(student.getPerson().getCurp())
                    .birthDate(student.getPerson().getBirthDate())
                    .gender(student.getPerson().getGender())
                    .phone(student.getPerson().getPhone())
                    .imageUrl(student.getPerson().getImageUrl())
                    .build();
        }

        AddressDto addressDto = null;
        if (student.getAddress() != null) {
            addressDto = AddressDto.builder()
                    .id(student.getAddress().getId())
                    .street(student.getAddress().getStreet())
                    .exteriorNumber(student.getAddress().getExteriorNumber())
                    .settlementType(student.getAddress().getSettlementType())
                    .settlement(student.getAddress().getSettlement())
                    .municipalityId(student.getAddress().getMunicipalityId())
                    .stateId(student.getAddress().getStateId())
                    .zipCode(student.getAddress().getZipCode())
                    .build();
        }

        java.util.List<RelativeDto> relativesDto = new java.util.ArrayList<>();
        if (student.getPerson() != null) {
            relativesDto = personRelativeRepository.findByPersonId(student.getPerson().getId()).stream()
                    .map(pr -> new RelativeDto(
                            PersonDto.builder()
                                    .id(pr.getRelativePerson().getId())
                                    .firstName(pr.getRelativePerson().getFirstName())
                                    .lastName(pr.getRelativePerson().getLastName())
                                    .build(),
                            pr.getRelativeRole().getName()
                    ))
                    .collect(Collectors.toList());
        }

        return StudentDto.builder()
                .id(student.getId())
                .profileId(student.getProfileId())
                .qrUrl(student.getQrUrl())
                .person(personDto)
                .address(addressDto)
                .relatives(relativesDto)
                .build();
    }

    private void saveRelatives(Person studentPerson, List<RelativeRequest> relatives) {
        if (relatives == null || relatives.isEmpty()) {
            return;
        }

        List<PersonRelative> prList = relatives.stream()
            .map(relReq -> {
                RelativeRole role = relativeRoleRepository.findByName(relReq.relationship())
                    .orElseGet(() -> {
                        RelativeRole newRole = new RelativeRole();
                        newRole.setName(relReq.relationship());
                        return relativeRoleRepository.save(newRole);
                    });

                Person relativePerson = Person.builder()
                    .firstName(relReq.name())
                    .lastName("N/A") 
                    .build();
                relativePerson = personRepository.save(relativePerson);

                return PersonRelative.builder()
                    .person(studentPerson)
                    .relativePerson(relativePerson)
                    .relativeRole(role)
                    .build();
            })
            .collect(Collectors.toList());

        personRelativeRepository.saveAll(prList);
    }

}
