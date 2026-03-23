package de.rebelmetal.schockenwebapp.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        if (attribute == null) return null;
        return attribute.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return Arrays.stream(dbData.split(","))
                .map(Integer::parseInt)
                .toList();
    }
}
