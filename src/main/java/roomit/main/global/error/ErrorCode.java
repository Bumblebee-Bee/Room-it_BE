package roomit.main.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import roomit.main.global.exception.CommonException;

@Getter
public enum ErrorCode {
    /*Login*/
    BUSINESS_NOT_REGISTER(HttpStatus.BAD_REQUEST, "L006", "사업자 등록에 실패했습니다."),

    /*Member*/
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "M003-1", "존재 하지 않는 회원입니다."),
    MEMBER_UPDATE_EXCEPTION(HttpStatus.BAD_REQUEST, "M001", "잘못된 회원 수정 입니다."),

    /*Reservation*/
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST,"R001","존재하지 않는 예약입니다."),

    /*StudyRoom*/
    STUDYROOM_NOT_FOUND(HttpStatus.BAD_REQUEST,"S001","존재하지 않는 스터디룸입니다."),
    STYDYROOM_NOT_REGISTERD(HttpStatus.UNAUTHORIZED, "S002","스터디름 등록에 실패하였습니다."),

    /*Business*/
    BUSINESS_NOT_FOUND(HttpStatus.BAD_REQUEST, "B003", "존재 하지 않는 사업자입니다."),
    BUSINESS_NOT_MODIFY(HttpStatus.BAD_REQUEST, "B001", "사업자 수정에 실패했습니다."),
    BUSINESS_NOT_DELETE(HttpStatus.BAD_REQUEST, "B002", "사업자 탈퇴에 실패했습니다."),
    BUSINESS_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "B003", "권한이 없습니다."),

    /*WorkPlace*/
    WORKPLACE_NOT_REGISTERED(HttpStatus.UNAUTHORIZED, "W001","사업장 등록에 실패하였습니다."),
    WORKPLACE_NOT_MODIFIED(HttpStatus.FORBIDDEN,"W002","사업장 수정에 실패하였습니다."),
    WORKPLACE_NOT_DELETE(HttpStatus.FORBIDDEN,"W003","사업장 삭제에 실패하였습니다."),
    WORKPLACE_NOT_FOUND(HttpStatus.NOT_FOUND,"W004","존재하지 않는 사업장 입니다"),
    WORKPLACE_INVALID_REQUEST(HttpStatus.BAD_REQUEST,"W001-2","잘못된 입력입니다."),
    WORKPLACE_INVALID_ADDRESS(HttpStatus.BAD_REQUEST,"W001-3","잘못된 주소입니다."),

    /*Review*/
    REVIEW_NOT_FOUND(HttpStatus.BAD_REQUEST,"V003","존재 하지 않는 리뷰입니다."),
    REVIEW_UPDATE_EXCEPTION(HttpStatus.BAD_REQUEST,"V002","잘못 수정 입니다."),

    /*Payments*/
    PAYMENTS_NOT_FOUND(HttpStatus.NOT_FOUND,"P001","존재 하지 않는 결제 내역 입니다."),
    PAYMENTS_INVALID_AMOUNT(HttpStatus.BAD_REQUEST,"P002","잘못된 결제 금액입니다."),
    PAYMENTS_ALREADY_APPROVED(HttpStatus.BAD_REQUEST,"P003","이미 승인된 결제입니다"),
    PAYMENTS_AMOUNT_EXP(HttpStatus.BAD_REQUEST,"P004","결제 금액이 일치하지 않습니다");

    /*Reservation*/
    RESERVATIN_NOT_FOUND(HttpStatus.BAD_REQUEST, "R001", "존재 하지 않는 예약입니다.");
  
    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(HttpStatus status,String code, String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
    public CommonException commonException() {
        return new CommonException(this);
    }
}
