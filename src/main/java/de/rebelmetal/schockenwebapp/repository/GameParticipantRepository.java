package de.rebelmetal.schockenwebapp.repository;

import de.rebelmetal.schockenwebapp.model.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameParticipantRepository extends JpaRepository<GameParticipant, UUID> {

}
