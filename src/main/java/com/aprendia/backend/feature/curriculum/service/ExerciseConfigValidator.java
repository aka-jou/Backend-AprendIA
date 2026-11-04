package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.feature.curriculum.entities.Layout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Valida el config (contexto) de un ejercicio según su plantilla (spec, sección 5.3).
 *
 * Las cantidades salen de los atributos del layout (optionsCount, pairsCount, minSlides, maxSlides);
 * si un layout no las define se usan las de la spec. Un layout creado con un código distinto de los
 * cinco conocidos solo exige que config sea un objeto JSON. Los mensajes indican la ruta del campo
 * (ej. "options[2].text") para que el frontend ubique el error.
 */
public final class ExerciseConfigValidator {

    public static final String IDENTIFY_FAMILY = "identify_family";
    public static final String SPELLING_NAME = "spelling_name";
    public static final String MATCHING_7X7 = "matching_7x7";
    public static final String IMAGE_LINKING_5X5 = "image_linking_5x5";
    public static final String CAROUSEL_CARDS = "carousel_cards";

    private static final Pattern HTTP_URL = Pattern.compile("^https?://\\S+$");

    private ExerciseConfigValidator() {
    }

    /** @param position número del ejercicio en el mensaje (1, 2...) o null si es uno solo. */
    public static void validate(Layout layout, Map<String, Object> config, Integer position) {
        String prefix = position == null ? "config" : "exercises[" + position + "].config";
        Map<String, Object> attributes = layout.getAttributes() == null ? Map.of() : layout.getAttributes();
        switch (layout.getCode()) {
            case IDENTIFY_FAMILY -> validateIdentifyFamily(config, attributes, prefix);
            case SPELLING_NAME -> validateSpelling(config, prefix);
            case MATCHING_7X7 -> validateMatching(config, attributes, prefix);
            case IMAGE_LINKING_5X5 -> validateImageLinking(config, attributes, prefix);
            case CAROUSEL_CARDS -> validateCarousel(config, attributes, prefix);
            default -> {
                // Plantilla sin esquema conocido: basta con que sea un objeto (lo garantiza el tipo Map)
            }
        }
    }

    // ---------- 1) identify_family ----------

    private static void validateIdentifyFamily(Map<String, Object> config, Map<String, Object> attributes, String prefix) {
        requireUrl(config, "mainImageUrl", prefix);
        int expected = intAttribute(attributes, "optionsCount", 4);
        List<Object> options = requireList(config, "options", prefix);
        if (options.size() != expected) {
            throw bad(prefix + ".options debe tener exactamente " + expected + " opciones.");
        }
        Set<String> ids = new HashSet<>();
        int correct = 0;
        for (int i = 0; i < options.size(); i++) {
            String path = prefix + ".options[" + i + "]";
            Map<String, Object> option = asObject(options.get(i), path);
            requireUniqueId(option, path, ids);
            requireText(option, "text", path);
            if (!(option.get("isCorrect") instanceof Boolean isCorrect)) {
                throw bad(path + ".isCorrect es obligatorio (true o false).");
            }
            if (isCorrect) {
                correct++;
            }
        }
        if (correct != 1) {
            throw bad(prefix + ".options debe tener exactamente una opción con isCorrect: true.");
        }
        optionalText(config, "feedbackExplanation", prefix);
    }

    // ---------- 2) spelling_name ----------

    private static void validateSpelling(Map<String, Object> config, String prefix) {
        String targetWord = requireText(config, "targetWord", prefix);
        List<Object> letters = requireList(config, "availableLetters", prefix);
        if (letters.isEmpty()) {
            throw bad(prefix + ".availableLetters debe tener al menos una letra.");
        }
        boolean caseSensitive = optionalBoolean(config, "caseSensitive", prefix);
        Map<String, Integer> available = new HashMap<>();
        for (int i = 0; i < letters.size(); i++) {
            if (!(letters.get(i) instanceof String letter) || letter.isBlank()) {
                throw bad(prefix + ".availableLetters[" + i + "] debe ser una letra (texto no vacío).");
            }
            available.merge(normalizeLetter(letter.trim(), caseSensitive), 1, Integer::sum);
        }
        // Las letras disponibles (correctas + distractoras) deben alcanzar para formar la palabra
        for (int offset = 0; offset < targetWord.length(); ) {
            int codePoint = targetWord.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint)) {
                continue;
            }
            String letter = normalizeLetter(new String(Character.toChars(codePoint)), caseSensitive);
            int left = available.getOrDefault(letter, 0);
            if (left == 0) {
                throw bad(prefix + ".availableLetters no alcanza para formar \"" + targetWord + "\": falta la letra \""
                        + letter + "\".");
            }
            available.put(letter, left - 1);
        }
        optionalUrl(config, "referenceImageUrl", prefix);
    }

    // ---------- 3) matching_7x7 ----------

    private static void validateMatching(Map<String, Object> config, Map<String, Object> attributes, String prefix) {
        List<Object> pairs = requirePairs(config, attributes, 7, prefix);
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < pairs.size(); i++) {
            String path = prefix + ".pairs[" + i + "]";
            Map<String, Object> pair = asObject(pairs.get(i), path);
            requireUniqueId(pair, path, ids);
            for (String side : List.of("leftItem", "rightItem")) {
                Map<String, Object> item = requireObject(pair, side, path);
                requireText(item, "id", path + "." + side);
                requireText(item, "label", path + "." + side);
                optionalUrl(item, "imageUrl", path + "." + side);
            }
        }
    }

    // ---------- 4) image_linking_5x5 ----------

    private static void validateImageLinking(Map<String, Object> config, Map<String, Object> attributes, String prefix) {
        List<Object> pairs = requirePairs(config, attributes, 5, prefix);
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < pairs.size(); i++) {
            String path = prefix + ".pairs[" + i + "]";
            Map<String, Object> pair = asObject(pairs.get(i), path);
            requireUniqueId(pair, path, ids);
            Map<String, Object> image = requireObject(pair, "image", path);
            requireText(image, "id", path + ".image");
            requireUrl(image, "url", path + ".image");
            optionalText(image, "alt", path + ".image");
            Map<String, Object> answer = requireObject(pair, "answer", path);
            requireText(answer, "id", path + ".answer");
            requireText(answer, "text", path + ".answer");
            optionalUrl(answer, "videoLsmUrl", path + ".answer");
        }
    }

    // ---------- 5) carousel_cards ----------

    private static void validateCarousel(Map<String, Object> config, Map<String, Object> attributes, String prefix) {
        int min = intAttribute(attributes, "minSlides", 2);
        int max = intAttribute(attributes, "maxSlides", 10);
        List<Object> slides = requireList(config, "slides", prefix);
        if (slides.size() < min || slides.size() > max) {
            throw bad(prefix + ".slides debe tener entre " + min + " y " + max + " tarjetas.");
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < slides.size(); i++) {
            String path = prefix + ".slides[" + i + "]";
            Map<String, Object> slide = asObject(slides.get(i), path);
            requireUniqueId(slide, path, ids);
            if (!(slide.get("order") instanceof Integer order) || order < 1) {
                throw bad(path + ".order es obligatorio y debe ser un entero mayor o igual a 1.");
            }
            requireText(slide, "title", path);
            requireText(slide, "description", path);
            requireUrl(slide, "imageUrl", path);
            optionalText(slide, "lsmSignNotes", path);
            optionalUrl(slide, "videoLsmUrl", path);
        }
        optionalBoolean(config, "autoPlay", prefix);
        optionalBoolean(config, "allowAudioSupport", prefix);
    }

    // ---------- Utilidades ----------

    private static List<Object> requirePairs(Map<String, Object> config, Map<String, Object> attributes, int defaultCount,
                                             String prefix) {
        int expected = intAttribute(attributes, "pairsCount", defaultCount);
        List<Object> pairs = requireList(config, "pairs", prefix);
        if (pairs.size() != expected) {
            throw bad(prefix + ".pairs debe tener exactamente " + expected + " pares.");
        }
        return pairs;
    }

    private static int intAttribute(Map<String, Object> attributes, String name, int defaultValue) {
        Object value = attributes.get(name);
        return value instanceof Number number && number.intValue() > 0 ? number.intValue() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> requireList(Map<String, Object> source, String field, String path) {
        Object value = source.get(field);
        if (!(value instanceof List<?> list)) {
            throw bad(path + "." + field + " es obligatorio y debe ser un arreglo.");
        }
        return (List<Object>) list;
    }

    private static Map<String, Object> requireObject(Map<String, Object> source, String field, String path) {
        return asObject(source.get(field), path + "." + field);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value, String path) {
        if (!(value instanceof Map<?, ?> map)) {
            throw bad(path + " es obligatorio y debe ser un objeto.");
        }
        return (Map<String, Object>) map;
    }

    private static String requireText(Map<String, Object> source, String field, String path) {
        Object value = source.get(field);
        if (!(value instanceof String text) || text.isBlank()) {
            throw bad(path + "." + field + " es obligatorio (texto no vacío).");
        }
        return text.trim();
    }

    private static void optionalText(Map<String, Object> source, String field, String path) {
        Object value = source.get(field);
        if (value != null && !(value instanceof String)) {
            throw bad(path + "." + field + " debe ser texto.");
        }
    }

    private static void requireUrl(Map<String, Object> source, String field, String path) {
        String value = requireText(source, field, path);
        if (!HTTP_URL.matcher(value).matches()) {
            throw bad(path + "." + field + " debe ser una URL http(s) válida.");
        }
    }

    private static void optionalUrl(Map<String, Object> source, String field, String path) {
        Object value = source.get(field);
        if (value == null || (value instanceof String text && text.isBlank())) {
            return;
        }
        if (!(value instanceof String text) || !HTTP_URL.matcher(text.trim()).matches()) {
            throw bad(path + "." + field + " debe ser una URL http(s) válida.");
        }
    }

    private static boolean optionalBoolean(Map<String, Object> source, String field, String path) {
        Object value = source.get(field);
        if (value == null) {
            return false;
        }
        if (!(value instanceof Boolean flag)) {
            throw bad(path + "." + field + " debe ser true o false.");
        }
        return flag;
    }

    private static void requireUniqueId(Map<String, Object> item, String path, Set<String> seen) {
        String id = requireText(item, "id", path);
        if (!seen.add(id)) {
            throw bad(path + ".id \"" + id + "\" está repetido.");
        }
    }

    private static String normalizeLetter(String letter, boolean caseSensitive) {
        return caseSensitive ? letter : letter.toUpperCase(Locale.ROOT);
    }

    private static BadRequestException bad(String message) {
        return new BadRequestException(message);
    }
}
