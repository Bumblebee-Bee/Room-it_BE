package roomit.main.domain.chat.chatroom.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record ChatRoomDisConnectEvent(
        Long chatRoomId,
        String sessionId
) {
}