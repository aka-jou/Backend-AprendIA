package com.aprendia.backend.feature.curriculum.entities;

import java.util.List;

/** Estructura guardada en {@code resources.content}: { "slides": [ ... ] }. */
public record ResourceContent(List<ResourceSlide> slides) {

    public record ResourceSlide(Integer order, String title, String description, String imageUrl) {
    }
}
