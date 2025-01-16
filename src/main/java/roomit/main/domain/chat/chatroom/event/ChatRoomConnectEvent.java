package roomit.main.domain.chat.chatroom.event;

public record ChatRoomConnectEvent(
        String connectMemberName,
        String senderType,
        Long chatRoomId,
        String sessionId
) {
}