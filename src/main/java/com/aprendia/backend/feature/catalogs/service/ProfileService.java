package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.feature.catalogs.dto.ProfileRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.ProfileResponseDTO;

import java.util.List;

public interface ProfileService {
    ProfileResponseDTO createProfile(ProfileRequestDTO request);
    List<ProfileResponseDTO> getAllProfiles(boolean incluirInactivos);
    ProfileResponseDTO getProfileById(Long id);
    ProfileResponseDTO updateProfile(Long id, ProfileRequestDTO request);
    ProfileResponseDTO updateStatus(Long id, boolean activo);
    void deleteProfile(Long id);
}
