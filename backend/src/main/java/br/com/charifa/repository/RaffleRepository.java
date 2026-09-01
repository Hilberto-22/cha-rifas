package br.com.charifa.repository;

import br.com.charifa.domain.Raffle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RaffleRepository extends JpaRepository<Raffle, UUID> {
    Optional<Raffle> findFirstByActiveTrue();
}
