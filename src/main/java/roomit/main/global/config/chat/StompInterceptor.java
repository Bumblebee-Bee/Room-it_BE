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

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final ApplicationEventPublisher eventPublisher;

    // 세션 상태를 추적할 맵 추가 (세션 ID로 관리)
    private final Set<String> connectedSessions = new HashSet<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        String sessionId = accessor.getSessionId();

        log.info("accessor" + accessor);
        log.info("command : " + command);
        /**
         * @Description
         * 1. 모든 메시지 읽음 처리
         * 2. Redis에 채팅방 참여 정보 저장
         */
        if (StompCommand.CONNECT.equals(command)) {
            String chatRoomId = accessor.getFirstNativeHeader("chatRoomId");
            String nickName = accessor.getFirstNativeHeader("nickName");
            String senderType = accessor.getFirstNativeHeader("senderType");

            // 이미 연결된 세션인 경우 처리하지 않음
            if (connectedSessions.contains(sessionId)) {
                log.info("Session already connected, skipping CONNECT event for sessionId: " + sessionId);
                return message;
            }

            // 세션을 연결된 상태로 마킹
            connectedSessions.add(sessionId);

            log.info("chatRoomId: " + chatRoomId);
            log.info("sessionId: " + sessionId);
            log.info("memberName: " + nickName);
            log.info("senderType: " + senderType);

            eventPublisher.publishEvent(new ChatRoomConnectEvent(
                    nickName, senderType, Long.valueOf(chatRoomId), sessionId
            ));
        } else if (StompCommand.DISCONNECT.equals(command)) {
            log.info("disconnect");
            if (connectedSessions.contains(sessionId)) {
                connectedSessions.remove(sessionId);  // 세션 연결 상태에서 제거

                // 연결 종료 이벤트 발행
                eventPublisher.publishEvent(new ChatRoomDisConnectEvent(
                        sessionId
                ));
            } else {
                log.info("Session not found for DISCONNECT event, skipping: " + sessionId);
            }
        }


        return message;
    }
}