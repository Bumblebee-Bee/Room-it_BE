package roomit.main.domain.chat.chatmessage.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

public record ChatConnectResponse(
        String type,
        Long connectMemberId
) {
    public ChatConnectResponse(Long connectMemberId) {
        this("CONNECT", connectMemberId); // 기본 값 "CONNECT"를 지정
    }

    public ChatConnectResponse() {
        this("CONNECT", null); // 기본 생성자
    }
}