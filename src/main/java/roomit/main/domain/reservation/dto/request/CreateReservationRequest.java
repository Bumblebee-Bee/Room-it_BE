package roomit.main.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import roomit.main.domain.member.entity.Member;
import roomit.main.domain.reservation.entity.Reservation;
import roomit.main.domain.reservation.entity.ReservationState;
import roomit.main.domain.reservation.entity.value.ReservationNum;
import roomit.main.domain.studyroom.entity.StudyRoom;

import java.time.LocalDateTime;

public record CreateReservationRequest (

        @NotBlank String reservationName,
        @Pattern(regexp = ReservationNum.REGEX, message = ReservationNum.ERR_MSG) String reservationPhoneNumber,
        @NotNull Integer capacity,
        @NotNull Integer price,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
    public Reservation toEntity(Member member, StudyRoom studyRoom) {
        return Reservation.builder()
                .reservationName(this.reservationName)
                .reservationPhoneNumber(this.reservationPhoneNumber)
                .reservationState(ReservationState.COMPLETED)
                .reservationCapacity(this.capacity)
                .reservationPrice(this.price)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .memberId(member)
                .studyRoomId(studyRoom)
                .build();
    }
}