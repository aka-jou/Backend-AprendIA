package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.ProfileRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.ProfileResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Profile;
import com.aprendia.backend.feature.catalogs.repository.ProfileRepository;
import com.aprendia.backend.feature.catalogs.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    @Transactional
    public ProfileResponseDTO createProfile(ProfileRequestDTO request) {
        Profile profile = Profile.builder()
                .name(request.nombre())
                .accessLevel(request.nivelAcceso())
                .build();

        return mapToResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileResponseDTO> getAllProfiles() {
        return profileRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfile(Long id, ProfileRequestDTO request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con ID: " + id));

        profile.setName(request.nombre());
        profile.setAccessLevel(request.nivelAcceso());

        return mapToResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Perfil no encontrado con ID: " + id);
        }
        profileRepository.deleteById(id);
    }

    private ProfileResponseDTO mapToResponse(Profile profile) {
        return ProfileResponseDTO.builder()
                .id(profile.getId())
                .nombre(profile.getName())
                .nivelAcceso(profile.getAccessLevel())
                .build();
    }
}
