package de.rebelmetal.schockenwebapp.repository;

import de.rebelmetal.schockenwebapp.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

}
