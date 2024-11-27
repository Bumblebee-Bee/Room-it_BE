package roomit.main.domain.workplace.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomit.main.domain.workplace.entity.Workplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkplaceGetResponse(
        Long workplaceId,
        String workplaceName,
        String workplacePhoneNumber,
        String workplaceDescription,
        String workplaceAddress,
        String imageUrl,
        @JsonFormat(pattern = "HH:mm") LocalDateTime workplaceStartTime,
        @JsonFormat(pattern = "HH:mm") LocalDateTime workplaceEndTime,
        LocalDateTime createdAt,
        BigDecimal longitude,
        BigDecimal latitude,
        Double distance
) {}

