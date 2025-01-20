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
import roomit.main.domain.member.repository.MemberRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final ApplicationEventPublisher eventPublisher;

    // 세션 상태를 관리할 ConcurrentMap 사용
    private final ConcurrentMap<String, String> connectedSessions = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        log.info("accessor: " + accessor);
        log.info("command: " + command);

        if (StompCommand.CONNECT.equals(command)) {
            String chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
            String nickName = accessor.getFirstNativeHeader("nickName");
            String senderType = accessor.getFirstNativeHeader("senderType");

            if (connectedSessions.containsKey(sessionId)) {
                log.info("Session already connected, skipping CONNECT event for sessionId: " + sessionId);
                return message; // 이미 연결된 세션이면 처리하지 않음
            }

            connectedSessions.put(sessionId, chatRoomId);
            log.info("chatRoomId: " + chatRoomId);
            log.info("sessionId: " + sessionId);
            log.info("memberName: " + nickName);
            log.info("senderType: " + senderType);

            eventPublisher.publishEvent(new ChatRoomConnectEvent(
                    nickName, senderType, Long.valueOf(chatRoomId), sessionId
            ));
        } else if (StompCommand.DISCONNECT.equals(command)) {
            log.info("Disconnect event triggered for sessionId: " + sessionId);

            if (connectedSessions.containsKey(sessionId)) {
                connectedSessions.remove(sessionId);
                log.info("Session disconnected, sessionId removed: " + sessionId);

                eventPublisher.publishEvent(new ChatRoomDisConnectEvent(sessionId));
            } else {
                log.info("Session not found for DISCONNECT event, skipping: " + sessionId);
            }
        }

        return message;
    }
}
