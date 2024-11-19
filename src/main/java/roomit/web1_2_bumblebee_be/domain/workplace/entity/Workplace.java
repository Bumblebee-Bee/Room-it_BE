package roomit.web1_2_bumblebee_be.domain.workplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workplace")
//@EntityListeners()
public class Workplace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workplaceId;

    private String workplaceName;
    private String workplacePhoneNumber;
    private String workplaceDescription;
    private String workplaceAddress;
    private LocalDateTime workplaceStartTime;
    private LocalDateTime workplaceEndTime;
    private Long starSum;

    @Lob  // BLOB 타입으로 처리됨
    private byte[] profileImage;
    private String imageType;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "business_id")
//    private Business business;

//    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
//    private List<Room> rooms = new ArrayList<>();


    public void changeWorkplaceName(String workplaceName) {
        this.workplaceName = workplaceName;
    }

    public void changeWorkplacePhoneNumber(String workplacePhoneNumber) {
        this.workplacePhoneNumber = workplacePhoneNumber;
    }

    public void changeWorkplaceDescription(String workplaceDescription) {
        this.workplaceDescription = workplaceDescription;
    }

    public void changeWorkplaceAddress(String workplaceAddress) {
        this.workplaceAddress = workplaceAddress;
    }

    public void changeWorkplaceStartTime(LocalDateTime workplaceStartTime) {
        this.workplaceStartTime = workplaceStartTime;
    }

    public void changeWorkplaceEndTime(LocalDateTime workplaceEndTime) {
        this.workplaceEndTime = workplaceEndTime;
    }

    public void changeStarSum(Long starSum) {
        this.starSum = starSum;
    }

    public void changeProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    public void changeImageType(String imageType) {
        this.imageType = imageType;
    }
}
