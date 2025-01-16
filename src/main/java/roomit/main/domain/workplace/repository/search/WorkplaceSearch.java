package roomit.main.domain.workplace.repository.search;

import java.time.LocalDateTime;
import java.util.List;

import roomit.main.domain.workplace.dto.request.WorkplaceRequest;
import roomit.main.domain.workplace.dto.response.DistanceWorkplaceResponse;

public interface WorkplaceSearch {
  List<DistanceWorkplaceResponse> findNearbyWorkplaces(Double longitude, Double latitude, Double maxDistance, LocalDateTime endTime);

  void updateWorkplace(WorkplaceRequest workplaceRequest, Long workplaceId);
}
