package roomit.main.global.config.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import roomit.main.domain.chat.chatmessage.entity.SenderType;
import roomit.main.domain.chat.chatroom.event.ChatRoomConnectEvent;
import roomit.main.domain.chat.chatroom.event.ChatRoomDisConnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        log.info("accessor" + accessor);
        log.info("command : " + command);
        /**
         * @Description
         * 1. 모든 메시지 읽음 처리
         * 2. Redis에 채팅방 참여 정보 저장
         */
        if (StompCommand.CONNECT.equals(command)) {
            String memberId = accessor.getFirstNativeHeader("memberId");
            String chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
            log.info("memberId: " + memberId);
            log.info("chatRoomId: " + chatRoomId);
            log.info("sessionId: " + accessor.getSessionId());

            eventPublisher.publishEvent(new ChatRoomConnectEvent(
                    Long.valueOf(memberId),
                    SenderType.MEMBER, // 필요한 SenderType 값 설정
                    Long.valueOf(chatRoomId),
                    accessor.getSessionId()
            ));
        } else if (StompCommand.DISCONNECT.equals(command)) {
            String chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
            log.info("chatRoomId: " + chatRoomId);
            log.info("sessionId: " + accessor.getSessionId());

            eventPublisher.publishEvent(new ChatRoomDisConnectEvent(
                    Long.valueOf(chatRoomId),
                    accessor.getSessionId()
            ));
        }


        return message;
    }
}