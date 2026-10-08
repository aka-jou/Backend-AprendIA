package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.feature.catalogs.dto.ProfileRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.ProfileResponseDTO;

import java.util.List;

public interface ProfileService {
    ProfileResponseDTO createProfile(ProfileRequestDTO request);
    List<ProfileResponseDTO> getAllProfiles();
    ProfileResponseDTO updateProfile(Long id, ProfileRequestDTO request);
    void deleteProfile(Long id);
}
