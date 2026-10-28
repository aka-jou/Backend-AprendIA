package com.aprendia.backend.feature.curriculum.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Auditoría y baja lógica comunes a cuadernillos, unidades, temas y recursos.
 *
 * {@code deletedAt} distinto de null significa que el registro fue eliminado con DELETE: deja de
 * existir para la API, pero se conserva en la base (reversible por SQL y auditable).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class CurriculumAuditFields {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50, nullable = false)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    public void markCreated(String username, LocalDateTime now) {
        this.createdAt = now;
        this.createdBy = username;
        this.updatedAt = now;
        this.updatedBy = username;
    }

    public void markUpdated(String username, LocalDateTime now) {
        this.updatedAt = now;
        this.updatedBy = username;
    }

    public void markDeleted(String username, LocalDateTime now) {
        this.deletedAt = now;
        this.deletedBy = username;
        markUpdated(username, now);
    }
}
