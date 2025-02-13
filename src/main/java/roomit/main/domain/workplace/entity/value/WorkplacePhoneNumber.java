package roomit.main.domain.workplace.entity.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static lombok.AccessLevel.*;

@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = PROTECTED)
public class WorkplacePhoneNumber {
    public static final String REGEX = "^(0507-\\d{4}-\\d{4}|\\d{2,3}-\\d{3,4}-\\d{4})$";
    public static final String ERR_MSG = "전화번호는 형식에 맞게 입력해야 합니다. (예: 0507-1234-5678, 02-123-4567, 031-1234-5678)";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    @Column(name = "workplace_phone_number", nullable = false, length = 15)
    private String value;

    public WorkplacePhoneNumber(final String phoneNumber) {
        if (!PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException(ERR_MSG);
        }
        this.value = phoneNumber;
    }
}
