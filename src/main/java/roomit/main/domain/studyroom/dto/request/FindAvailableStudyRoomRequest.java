package roomit.main.domain.studyroom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

public record FindAvailableStudyRoomRequest(
    @NotBlank String workplaceAddress,
    @NotNull @DateTimeFormat(pattern = "HH:mm") String startTime,
    @NotNull @DateTimeFormat(pattern = "HH:mm") String endTime,
    @NotBlank Integer capacity
){
    @Builder
    public FindAvailableStudyRoomRequest(String workplaceAddress, String startTime, String endTime, Integer capacity) {
        this.workplaceAddress = workplaceAddress;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
    }


}