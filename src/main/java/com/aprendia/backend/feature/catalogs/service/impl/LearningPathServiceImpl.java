package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.LearningPathRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.LearningPathResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.LearningPath;
import com.aprendia.backend.feature.catalogs.entities.Metodology;
import com.aprendia.backend.feature.catalogs.entities.Profile;
import com.aprendia.backend.feature.catalogs.repository.LearningPathRepository;
import com.aprendia.backend.feature.catalogs.repository.MetodologyRepository;
import com.aprendia.backend.feature.catalogs.repository.ProfileRepository;
import com.aprendia.backend.feature.catalogs.service.LearningPathService;
import com.aprendia.backend.feature.curriculum.repository.WorkbookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LearningPathServiceImpl implements LearningPathService {

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private MetodologyRepository metodologyRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private WorkbookRepository workbookRepository;

    @Override
    @Transactional
    public LearningPathResponseDTO createLearningPath(LearningPathRequestDTO request) {
        LearningPath learningPath = LearningPath.builder()
                .name(request.nombre().trim())
                .metodology(findMetodology(request.idMetodologia()))
                .status(request.status())
                .profile(request.idPerfil() == null ? null : findProfile(request.idPerfil()))
                .imageUrl(blankToNull(request.urlImagen()))
                .createdAt(LocalDateTime.now())
                .createdBy(CurrentUser.username())
                .build();
        return mapToResponse(learningPathRepository.save(learningPath));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponseDTO> getAllLearningPaths() {
        return learningPathRepository.findAllByOrderByIdAsc().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponseDTO getLearningPathById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public LearningPathResponseDTO updateLearningPath(Long id, LearningPathRequestDTO request) {
        LearningPath learningPath = findOrThrow(id);
        learningPath.setName(request.nombre().trim());
        learningPath.setMetodology(findMetodology(request.idMetodologia()));
        learningPath.setStatus(request.status());
        // Campos opcionales: null = conservar el valor actual (el PUT de la sección 5.2 no los envía)
        if (request.idPerfil() != null) {
            learningPath.setProfile(findProfile(request.idPerfil()));
        }
        if (request.urlImagen() != null) {
            learningPath.setImageUrl(blankToNull(request.urlImagen()));
        }
        return mapToResponse(learningPathRepository.save(learningPath));
    }

    @Override
    @Transactional
    public void deleteLearningPath(Long id) {
        findOrThrow(id);
        // Cuenta también los cuadernillos dados de baja lógica: siguen referenciando la ruta por FK
        if (workbookRepository.existsByLearningPathId(id)) {
            throw new ConflictException("No se puede eliminar la ruta de aprendizaje porque tiene cuadernillos asociados. Desactívela con status=false.");
        }
        learningPathRepository.deleteById(id);
    }

    private LearningPath findOrThrow(Long id) {
        return learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruta de aprendizaje no encontrada con ID: " + id));
    }

    // Referencia inválida dentro del body -> 400 (la ruta solicitada sí existe, el dato enviado no)
    private Metodology findMetodology(Long idMetodologia) {
        return metodologyRepository.findById(idMetodologia)
                .orElseThrow(() -> new BadRequestException("La metodología con ID " + idMetodologia + " no existe en el catálogo."));
    }

    private Profile findProfile(Long idPerfil) {
        return profileRepository.findById(idPerfil)
                .orElseThrow(() -> new BadRequestException("El perfil con ID " + idPerfil + " no existe en el catálogo."));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LearningPathResponseDTO mapToResponse(LearningPath learningPath) {
        Metodology metodology = learningPath.getMetodology();
        Profile profile = learningPath.getProfile();
        return LearningPathResponseDTO.builder()
                .id(learningPath.getId())
                .nombre(learningPath.getName())
                .idMetodologia(metodology.getId())
                .nombreMetodologia(metodology.getName())
                .status(learningPath.getStatus())
                .idPerfil(profile == null ? null : profile.getId())
                .nombrePerfil(profile == null ? null : profile.getName())
                .urlImagen(learningPath.getImageUrl())
                .build();
    }
}
