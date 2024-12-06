package roomit.main.domain.reservation.service;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomit.main.domain.reservation.entity.Reservation;
import roomit.main.domain.reservation.entity.ReservationState;
import roomit.main.domain.reservation.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;


    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateReservationStatus() {
        LocalDateTime currentTime = LocalDateTime.now();

        List<Reservation> reservations = reservationRepository.findByReservationState(
                ReservationState.ACTIVE);

        for (Reservation reservation : reservations) {
            reservation.changeReservationState(ReservationState.COMPLETED);
            reservationRepository.save(reservation);
        }
    }
}
