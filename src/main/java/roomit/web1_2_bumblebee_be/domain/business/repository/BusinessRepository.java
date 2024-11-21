package roomit.web1_2_bumblebee_be.domain.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomit.web1_2_bumblebee_be.domain.business.entity.Business;
import roomit.web1_2_bumblebee_be.domain.business.entity.value.BusinessEmail;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    @Query(value = "SELECT b FROM Business b WHERE b.businessEmail.value=:email")
    Optional<Business> findByBusinessEmail(String email);
  
}

