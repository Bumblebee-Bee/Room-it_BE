package roomit.main.domain.chat.chatmessage.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomit.main.domain.chat.chatmessage.dto.request.ChatMessageSaveRequest;
import roomit.main.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import roomit.main.domain.chat.chatmessage.entity.SenderType;

public record ChatSaveEvent (
        Long receiverId,
        Long senderId,
        ChatMessageSaveRequest chatMessageSaveRequest
){
}
