package roomit.main.domain.chat.chatroom.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomit.main.domain.chat.chatmessage.entity.SenderType;

public record ChatRoomConnectEvent(
        Long connectMemberId,
        SenderType senderType,
        Long chatRoomId,
        String sessionId
) {
}