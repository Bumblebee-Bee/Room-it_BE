package roomit.main.domain.chat.chatroom.event;

public record ChatRoomDisConnectEvent(
        Long chatRoomId,
        String sessionId
) {
    public ChatRoomDisConnectEvent(String sessionId) {
        this(null, sessionId);  // chatRoomId를 null로 설정하고 sessionId만 전달
    }
}