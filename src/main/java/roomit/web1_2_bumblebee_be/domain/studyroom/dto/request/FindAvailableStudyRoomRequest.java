package roomit.web1_2_bumblebee_be.domain.studyroom.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FindAvailableStudyRoomRequest(
    @NotBlank String workplaceAddress,
    @NotBlank String startTime,
    @NotBlank String endTime,
    @NotBlank Integer capacity
){
}