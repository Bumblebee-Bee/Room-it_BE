package roomit.main.domain.chat.chatroom.dto;

import java.time.LocalDateTime;

public interface ChatRoomResponse {
    Long roomId();
    Long id();
    LocalDateTime updatedAt();
}
