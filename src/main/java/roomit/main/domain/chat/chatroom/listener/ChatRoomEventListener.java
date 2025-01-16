package roomit.main.domain.chat.chatroom.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import roomit.main.domain.chat.chatmessage.dto.response.ChatConnectResponse;
import roomit.main.domain.chat.chatmessage.service.ChatService;
import roomit.main.domain.chat.chatroom.event.ChatRoomConnectEvent;
import roomit.main.domain.chat.chatroom.event.ChatRoomDisConnectEvent;
import roomit.main.domain.chat.chatroom.service.ChatRoomRedisService;

@RequiredArgsConstructor
@Component
public class ChatRoomEventListener {
    private final ChatRoomRedisService chatRoomRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
//    private final SseService sseService;

    /**
     * @Description
     * 1. 채팅방에 연결됐음을 저장
     * 2. 연결된 사람의 채팅방에 안읽었던 메시지를 모두 읽음 처리
     * 3. 채팅방에 상대방이 연결되었음을 알리는 알림
     */
    @EventListener
    public void readAllChatAndSaveConnectMember(ChatRoomConnectEvent event){
        chatRoomRedisService.connectChatRoom(event.chatRoomId(), event.sessionId());
        chatService.readAllMyNotReadChatList(event.chatRoomId(), event.connectMemberId(), event.senderType());
        messagingTemplate.convertAndSend("/sub/chat/room/" + event.chatRoomId(), new ChatConnectResponse(event.connectMemberId()));
//        sseService.sendToClient("CHATROOM_UPDATE", event.getConnectMemberId(), "채팅방 목록을 업데이트 해주세요.");
    }

    @EventListener
    public void deleteConnectMember(ChatRoomDisConnectEvent event){
        chatRoomRedisService.disConnectChatRoom(event.sessionId());
    }

}