package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.ProfileRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.ProfileResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Profile;
import com.aprendia.backend.feature.catalogs.repository.ProfileRepository;
import com.aprendia.backend.feature.catalogs.service.ProfileService;
import com.aprendia.backend.feature.user.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    @Transactional
    public ProfileResponseDTO createProfile(ProfileRequestDTO request) {
        Profile profile = Profile.builder()
                .name(request.nombre().trim())
                .description(blankToNull(request.descripcion()))
                .status(request.status())
                .createdAt(LocalDateTime.now())
                .createdBy(CurrentUser.username())
                .build();
        return mapToResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponseDTO> getAllProfiles() {
        return profileRepository.findAllByOrderByIdAsc().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfileById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfile(Long id, ProfileRequestDTO request) {
        Profile profile = findOrThrow(id);
        profile.setName(request.nombre().trim());
        profile.setDescription(blankToNull(request.descripcion()));
        profile.setStatus(request.status());
        return mapToResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteProfile(Long id) {
        findOrThrow(id);
        if (studentRepository.existsByProfileId(Math.toIntExact(id))) {
            throw new ConflictException("No se puede eliminar el perfil porque hay estudiantes asignados a él. Desactívelo con status=false.");
        }
        profileRepository.deleteById(id);
    }

    private Profile findOrThrow(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con ID: " + id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ProfileResponseDTO mapToResponse(Profile profile) {
        return ProfileResponseDTO.builder()
                .id(profile.getId())
                .nombre(profile.getName())
                .descripcion(profile.getDescription())
                .status(profile.getStatus())
                .build();
    }
}
