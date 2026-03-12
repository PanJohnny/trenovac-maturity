package me.panjohnny.trenovacmaturity;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagHelper {
    public static final String[] TAGS = {
            "slovní úloha",
            "výrok",
            "konstrukce",
            "geometrie",
            "analytická geometrie",
            "algebra",
            "mnohočleny",
            "rovnice",
            "soustavy rovnic",
            "nerovnice",
            "funkce",
            "posloupnosti",
            "logaritmy",
            "goniometrie",
            "vektory",
            "lineární algebra",
            "komplexní čísla",
            "kombinatorika",
            "pravděpodobnost",
    };

    public static List<String> suggest(String input) {
        // smart suggestion based on weights
        // get last part
        String lastPart = input.toLowerCase().trim();
        if (lastPart.contains(",")) {
            String[] parts = lastPart.split(",");
            lastPart = parts[parts.length - 1].trim();
        }

        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();

        HashMap<String, Integer> suggestions = new HashMap<>();
        for (String tag : TAGS) {
            int distance = levenshteinDistance.apply(lastPart, tag);
            suggestions.put(tag, distance);
        }

        return suggestions.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }
}
