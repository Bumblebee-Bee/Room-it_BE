package roomit.main.domain.chat.chatroom.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import roomit.main.domain.business.repository.BusinessRepository;
import roomit.main.domain.chat.chatmessage.dto.response.ChatConnectResponse;
import roomit.main.domain.chat.chatmessage.entity.SenderType;
import roomit.main.domain.chat.chatmessage.service.ChatService;
import roomit.main.domain.chat.chatroom.event.ChatRoomConnectEvent;
import roomit.main.domain.chat.chatroom.event.ChatRoomDisConnectEvent;
import roomit.main.domain.chat.chatroom.service.ChatRoomRedisService;
import roomit.main.domain.member.repository.MemberRepository;
import roomit.main.global.error.ErrorCode;

@RequiredArgsConstructor
@Component
@Slf4j
public class ChatRoomEventListener {
    private final ChatRoomRedisService chatRoomRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MemberRepository memberRepository;
    private final BusinessRepository businessRepository;
//    private final SseService sseService;

    /**
     * @Description
     * 1. 채팅방에 연결됐음을 저장
     * 2. 연결된 사람의 채팅방에 안읽었던 메시지를 모두 읽음 처리
     * 3. 채팅방에 상대방이 연결되었음을 알리는 알림
     */
    @EventListener
    public void readAllChatAndSaveConnectMember(ChatRoomConnectEvent event){
        log.info("Received ChatRoomConnectEvent: " + event);

        Long userId = null;
        SenderType senderType = null;
        if(event.senderType().equalsIgnoreCase("member")){
            userId = memberRepository.findByMemberNickName(event.connectMemberName()).getMemberId();
            senderType = SenderType.MEMBER;
        } else if (event.senderType().equalsIgnoreCase("business")) {
            userId = businessRepository.findByBusinessName(event.connectMemberName()).getBusinessId();
            senderType = SenderType.BUSINESS;
        } else {
            throw ErrorCode.MEMBER_NOT_FOUND.commonException();
        }

        log.info("userId"+ userId);

        chatRoomRedisService.connectChatRoom(event.chatRoomId(), event.sessionId());
        if(chatRoomRedisService.getConnectionMemberSize("chatRoom_" + event.chatRoomId())==2)
            messagingTemplate.convertAndSend("/sub/chat/connect/" + event.chatRoomId(), new ChatConnectResponse(event.connectMemberName()));
        chatService.readAllMyNotReadChatList(event.chatRoomId(), event.connectMemberName(), senderType);

//        messagingTemplate.convertAndSend("/sub/chat/" + event.chatRoomId(), new ChatConnectResponse(userId));
//        sseService.sendToClient("CHATROOM_UPDATE", event.getConnectMemberId(), "채팅방 목록을 업데이트 해주세요.");
    }

    @EventListener
    public void deleteConnectMember(ChatRoomDisConnectEvent event){
        chatRoomRedisService.disConnectChatRoom(event.sessionId());
    }
}