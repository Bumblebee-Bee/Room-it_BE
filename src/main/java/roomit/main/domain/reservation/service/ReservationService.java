package roomit.main.domain.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomit.main.domain.business.entity.Business;
import roomit.main.domain.member.entity.Member;
import roomit.main.domain.member.repository.MemberRepository;
import roomit.main.domain.notification.dto.ResponseNotificationDto;
import roomit.main.domain.notification.entity.Notification;
import roomit.main.domain.notification.entity.NotificationType;
import roomit.main.domain.notification.repository.NotificationRepository;
import roomit.main.domain.notification.service.NotificationService;
import roomit.main.domain.reservation.dto.request.CreateReservationRequest;
import roomit.main.domain.reservation.dto.request.UpdateReservationRequest;
import roomit.main.domain.reservation.dto.response.MyWorkPlaceReservationResponse;
import roomit.main.domain.reservation.dto.response.ReservationResponse;
import roomit.main.domain.reservation.entity.Reservation;
import roomit.main.domain.reservation.entity.ReservationState;
import roomit.main.domain.reservation.repository.ReservationRepository;
import roomit.main.domain.studyroom.entity.StudyRoom;
import roomit.main.domain.studyroom.repository.StudyRoomRepository;
import roomit.main.domain.workplace.entity.Workplace;
import roomit.main.domain.workplace.entity.value.WorkplaceName;
import roomit.main.domain.workplace.repository.WorkplaceRepository;
import roomit.main.global.error.ErrorCode;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final WorkplaceRepository workplaceRepository;

    // 예약 만드는 메서드
    @Transactional
    public Long createReservation(Long memberId,Long studyRoomId,CreateReservationRequest request) {
        if(!validateReservation(request.startTime(),request.endTime())){
            throw ErrorCode.START_TIME_NOT_AFTER_END_TIME.commonException();
        }

//        if(isDuplicateReservation(studyRoomId,request.startTime(),request.endTime())){
//            throw ErrorCode.DUPLICATE_RESERVATION.commonException();
//        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(ErrorCode.BUSINESS_NOT_FOUND::commonException);


        StudyRoom studyRoom = studyRoomRepository.findByIdWithWorkplace(studyRoomId)
                .orElseThrow(ErrorCode.STUDYROOM_NOT_FOUND::commonException);

        Reservation reservation = request.toEntity(member,studyRoom);


        // 예약 저장
        Long reservationId = reservationRepository.save(reservation).getReservationId();

        // 알림 처리
        StudyRoom studyRoom1 = reservation.getStudyRoom();
        Workplace workPlace = studyRoom1.getWorkPlace();


        alrim(workPlace);

        return reservationId;
    }

    public void alrim(Workplace workplace){
        Business business = workplace.getBusiness();

        Notification notification = Notification.builder()
                .business(business)
                .notificationType(NotificationType.RESERVATION_CONFIRMED)
                .content("예약이 등록되었습니다.")
                .build();

        ResponseNotificationDto responseNotificationDto = ResponseNotificationDto
                .builder()
                .notification(notification)
                .workplaceId(workplace.getWorkplaceId())
                .build();

        notificationService.notify(
                business.getBusinessId(),
                responseNotificationDto
        );
    }



    // validation startTime & endTime(startTime < endTime = True)
    @Transactional(readOnly = true)
    public boolean validateReservation(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime.isBefore(endTime);
    }



//    // 중복 예약 방지 로직
//    private boolean isDuplicateReservation(Long studyRoomId,LocalDateTime startTime,LocalDateTime endTime){
//        return reservationRepository.existsByStudyRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(studyRoomId,startTime,endTime);
//    }

    // x를 눌러 예약을 삭제하는 메서드
    @Transactional
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(ErrorCode.RESERVATION_NOT_FOUND::commonException);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationTime = reservation.getStartTime();

        if(now.isBefore(reservationTime.minusDays(2))){
            // 100프로 환불 메서드
            reservation.changeReservationState(ReservationState.CANCELLED);
        }else if(now.isBefore(reservationTime.minusDays(1))){
            // 50프로 환불 메서드
            reservation.changeReservationState(ReservationState.CANCELLED);
        }else{
            throw ErrorCode.RESERVATION_CANNOT_CANCEL.commonException();
        }
    }

    // 예약을 수정하는 메서드
    @Transactional
    public void updateReservation(Long reservationId,Long memberId ,UpdateReservationRequest request) {
        validateReservationOwner(reservationId,memberId);

        Reservation existingReservation = reservationRepository.findById(reservationId)
            .orElseThrow(ErrorCode.RESERVATION_NOT_FOUND::commonException);
        try {
            existingReservation.updateReservationDetails(
                request.reservationName(),
                request.reservationPhoneNumber(),
                request.startTime(),
                request.endTime()
            );
        }catch (Exception e){
            throw ErrorCode.RESERVATION_NOT_MODIFIED.commonException();
        }
    }

    @Transactional(readOnly = true)
    public void validateReservationOwner(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(ErrorCode.RESERVATION_NOT_FOUND::commonException);

        // 예약을 만든 사람과 수정 요청자가 동일한지 확인
        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw ErrorCode.RESERVATION_NOT_MODIFIED.commonException();  // 수정 권한이 없을 경우 예외 처리
        }
    }


    // memberId를 이용하여 나의 최근 예약 조회
    @Transactional(readOnly = true)
    public ReservationResponse findByMemberId(Long memberId) {
        List<Reservation> recentReservation = reservationRepository.findRecentReservationByMemberId(memberId);

        if (recentReservation.isEmpty())
        {
            return null;
        }else {
            Reservation recentReservation1 = recentReservation.get(0);
            StudyRoom studyRoom = recentReservation1.getStudyRoom();
            Workplace workplace = studyRoom.getWorkPlace();

            return ReservationResponse.from(studyRoom,recentReservation1,workplace);
        }
    }

    // memberId를 이용하여 나의 예약 전체 조회
    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservationsByMemberId(Long memberId){
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(memberId);

        if(reservations.isEmpty()){
            return null;
        } {
            return reservations.stream()
                .map(reservation -> ReservationResponse.from(
                    reservation.getStudyRoom(),
                    reservation,
                    reservation.getStudyRoom().getWorkPlace()
                ))
                .toList();
        }
    }



    // 내 사업장의 예약자 보기 (예약자 확인 페이지)
    @Transactional(readOnly = true)
    public List<MyWorkPlaceReservationResponse> findReservationByWorkplaceId(Long businessId) {
        List<Reservation> reservations = reservationRepository.findMyAllReservations(businessId);

        if(reservations.isEmpty()){
            return null;
        } else {
            return reservations.stream()
                .map(reservation -> MyWorkPlaceReservationResponse.from(
                    reservation.getStudyRoom(),
                    reservation,
                    reservation.getStudyRoom().getWorkPlace()
                ))
                .toList();
        }
    }
}