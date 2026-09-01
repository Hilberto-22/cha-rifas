package br.com.charifa.repository;

import br.com.charifa.domain.RaffleNumber;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RaffleNumberRepository extends JpaRepository<RaffleNumber, UUID> {
    @Query("select n from RaffleNumber n left join fetch n.reservation where n.raffle.id = :raffleId order by n.number")
    List<RaffleNumber> findAllWithReservation(@Param("raffleId") UUID raffleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from RaffleNumber n where n.raffle.id = :raffleId and n.number in :numbers order by n.number")
    List<RaffleNumber> findAllForUpdate(@Param("raffleId") UUID raffleId, @Param("numbers") Collection<Integer> numbers);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from RaffleNumber n where n.status = br.com.charifa.domain.RaffleNumberStatus.RESERVED")
    List<RaffleNumber> findReservedForUpdate();

    List<RaffleNumber> findByReservationIdOrderByNumber(UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from RaffleNumber n where n.reservation.id = :reservationId order by n.number")
    List<RaffleNumber> findByReservationIdForUpdate(@Param("reservationId") UUID reservationId);
}
