package de.rebelmetal.schockenwebapp.dto;

import java.util.List;
import java.util.UUID;

public record RoundEvaluationRequest(List<UUID> participantIds) {
}
