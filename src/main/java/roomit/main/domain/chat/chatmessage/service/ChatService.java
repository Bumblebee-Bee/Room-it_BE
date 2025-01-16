package roomit.main.domain.chat.chatmessage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import roomit.main.domain.business.dto.CustomBusinessDetails;
import roomit.main.domain.business.entity.Business;
import roomit.main.domain.business.repository.BusinessRepository;
import roomit.main.domain.chat.chatmessage.dto.request.ChatMessageRequest;
import roomit.main.domain.chat.chatmessage.dto.request.ChatMessageSaveRequest;
import roomit.main.domain.chat.chatmessage.dto.response.ChatMessageResponse;
import roomit.main.domain.chat.chatmessage.entity.ChatMessage;
import roomit.main.domain.chat.chatmessage.entity.SenderType;
import roomit.main.domain.chat.chatmessage.event.ChatSaveEvent;
import roomit.main.domain.chat.chatmessage.repository.ChatMessageRepository;
import roomit.main.domain.chat.chatroom.dto.response.ChatRoomResponse;
import roomit.main.domain.chat.chatroom.entity.ChatRoom;
import roomit.main.domain.chat.chatroom.repository.ChatRoomRepository;
import roomit.main.domain.chat.chatroom.service.ChatRoomRedisService;
import roomit.main.domain.chat.notification.event.NotReadChatEvent;
//import roomit.main.domain.chat.redis.service.RedisPublisher;
import roomit.main.domain.member.dto.CustomMemberDetails;
import roomit.main.domain.member.entity.Member;
import roomit.main.domain.member.repository.MemberRepository;
import roomit.main.global.error.ErrorCode;
import roomit.main.global.util.BumblebeeStringUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private static final String REDIS_MESSAGE_KEY_FORMAT = "chat:room:{}:messages";

    private final ApplicationEventPublisher eventPublisher;

//    private final RedisPublisher redisPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ChatRoomRedisService chatRoomRedisService;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BusinessRepository businessRepository;
    private final MemberRepository memberRepository;

    private final ObjectMapper objectMapper;

    @Value("${redis.message.ttl:3600}") // 메시지 TTL 설정
    private int messageTtl;

    @Transactional
    public void sendMessage(ChatMessageRequest request) {
        ChatRoom roomDetails = chatRoomRepository.findRoomDetailsById(request.roomId())
                .orElseThrow(ErrorCode.CHATROOM_NOT_FOUND::commonException);

        validateSender(request, roomDetails);

        Long connectMemberSize = chatRoomRedisService.getConnectionMemberSize("chatRoom_" + request.roomId());
        System.out.println(connectMemberSize);
        SenderType senderType = request.getSenderTypeEnum();
        ChatMessageSaveRequest saveRequest = new ChatMessageSaveRequest
                (request.roomId(), request.sender(), request.content(), LocalDateTime.now(), senderType, connectMemberSize == 2);
        if (request.timestamp() == null) {
            saveRequest.withTimestamp(LocalDateTime.now());
        }

        ChatMessage message = new ChatMessage(roomDetails, saveRequest);
        chatMessageRepository.save(message);

        if (connectMemberSize == 1) {
            eventPublisher.publishEvent(new NotReadChatEvent());
        }


        if (request.senderType().equalsIgnoreCase("MEMBER")) {
            Long receiverId = roomDetails.getBusiness().getBusinessId();
            Long senderId = roomDetails.getMember().getMemberId();
            eventPublisher.publishEvent(new ChatSaveEvent(receiverId, senderId, saveRequest));
        } else if (request.senderType().equalsIgnoreCase("BUSINESS")) {
            Long senderId = roomDetails.getBusiness().getBusinessId();
            Long receiverId = roomDetails.getMember().getMemberId();
            eventPublisher.publishEvent(new ChatSaveEvent(receiverId, senderId, saveRequest));
        }else{
            throw ErrorCode.MEMBER_NOT_FOUND.commonException();
        }




//        // Redis Pub/Sub 발행
//        String topic = "/sub/chat/" + request.roomId();
//        redisPublisher.publish(topic, saveRequest);
//
//        // Redis에 저장
//        saveMessageToRedis(new ChatMessageSaveRequest(request));
    }

    private void validateSender(ChatMessageRequest request, ChatRoom roomDetails) {
        if (!roomDetails.isSenderValid(request.getSenderTypeEnum(), request.sender())) {
            throw ErrorCode.CHAT_NOT_AUTHORIZED.commonException();
        }
    }

    //메시지 조회(Mysql만 사용)
    @Transactional(readOnly = true)
    public List<ChatMessageResponse>  getChats(Long roomId, CustomMemberDetails memberDetails, CustomBusinessDetails businessDetails, LocalDateTime cursor) {
        var senderInformation = extractSenderInformation(memberDetails, businessDetails);

        String senderName = senderInformation.getLeft();
        SenderType senderType = senderInformation.getRight();

        ChatRoom room = chatRoomRepository.findRoomDetailsById(roomId)
                .orElseThrow(ErrorCode.CHATROOM_NOT_FOUND::commonException);
        log.info("room " + room.toString());

        validateAuthorization(senderType, room, senderName);

        log.info("cursor"+cursor);

        List<ChatMessageResponse> chatMessages = chatMessageRepository
                .findByRoomId(roomId, cursor, PageRequest.of(0, 20))
                .stream()
                .map(ChatMessageResponse::new)
                .sorted(Comparator.comparing(ChatMessageResponse::timestamp)) // 시간순 정렬
                .toList();

        return chatMessages;
    }

    private void validateAuthorization(SenderType senderType, ChatRoom room, String senderName) {
        if (!room.isSenderValid(senderType, senderName)) {
            throw new IllegalArgumentException("Sender is not authorized to view messages in this room");
        }
    }

    private Pair<String, SenderType> extractSenderInformation(CustomMemberDetails memberDetails, CustomBusinessDetails businessDetails) {
        if (memberDetails != null) {
            return Pair.of(memberDetails.getName(), SenderType.MEMBER);
        } else if (businessDetails != null) {
            return Pair.of(businessDetails.getName(), SenderType.BUSINESS);
        }
        throw ErrorCode.CHAT_NOT_AUTHORIZED.commonException();
    }

    @Transactional
    public void readAllMyNotReadChatList(Long chatRoomId, String username, SenderType userType) {
        ChatRoom chatRoom = chatRoomRepository.findRoomDetailsById(chatRoomId)
                .orElseThrow(ErrorCode.CHATROOM_NOT_FOUND::commonException);
        if (userType.equals(SenderType.MEMBER)) {
            Member member = memberRepository.findByMemberNickName(username);
            chatMessageRepository.readAllMyNotReadChatList(chatRoom, member);
        } else if (userType.equals(SenderType.BUSINESS)) {
            Business business = businessRepository.findByBusinessName(username);
            chatMessageRepository.readAllMyNotReadChatList(chatRoom, business);
        }
    }
//
//    private void saveMessageToRedis(ChatMessageSaveRequest message) {
//        String redisKey = BumblebeeStringUtil.format(REDIS_MESSAGE_KEY_FORMAT, message.roomId());
//
//        try {
//            ChatMessageSaveRequest messageWithFormattedTimestamp = message.withFormattedTimestamp();
//
//            // Redis에 ZSet으로 저장 (timestamp를 score로 사용)
//            redisTemplate.opsForZSet().add(redisKey, messageWithFormattedTimestamp, message.timestamp().toEpochSecond(ZoneOffset.UTC));
//            redisTemplate.expire(redisKey, Duration.ofSeconds(messageTtl)); // TTL 설정
//
//            log.debug("Saved message to Redis: key={}, message={}", redisKey, messageWithFormattedTimestamp);
//        } catch (Exception e) {
//            log.error("Failed to save message to Redis: key={}, message={}, error={}", redisKey, message, e.getMessage(), e);
//            throw new RuntimeException("Failed to save message to Redis", e);
//        }
//    }
//
//    public void flushMessagesToDatabase(Long roomId) {
//        String redisKey = BumblebeeStringUtil.format(REDIS_MESSAGE_KEY_FORMAT, roomId);
//        log.debug("Flushing messages to database: key={}", redisKey);
//
//        Set<Object> sortedMessages = redisTemplate.opsForZSet().range(redisKey, 0, -1);
//        if (sortedMessages == null || sortedMessages.isEmpty()) {
//            log.debug("No messages to flush: key={}", redisKey);
//            return;
//        }
//
//        List<ChatMessageSaveRequest> messages = sortedMessages.stream()
//                .map(value -> objectMapper.convertValue(value, ChatMessageSaveRequest.class))
//                .toList();
//
//        saveMessagesToDatabase(messages);
//
//        redisTemplate.delete(redisKey);
//        log.debug("Deleted ZSet data from Redis: key={}", redisKey);
//    }
//
//
//    private void saveMessagesToDatabase(List<ChatMessageSaveRequest> requests) {
//        requests.forEach(request -> {
//                    // 메시지 읽음 상태 업데이트 (Redis의 읽음 상태 반영)
//                    ChatRoom chatRoom = chatRoomRepository.findById(request.roomId())
//                            .orElseThrow(ErrorCode.CHATROOM_NOT_FOUND::commonException);
//                    ChatMessage message = new ChatMessage(chatRoom, request);
//                    chatMessageRepository.save(message);
//                }
//        );
//    }
//
//    public List<ChatMessageResponse> getMessagesByRoomId(Long roomId, CustomMemberDetails memberDetails, CustomBusinessDetails businessDetails) {
//        var senderInformation = extractSenderInformation(memberDetails, businessDetails);
//
//        String senderName = senderInformation.getLeft();
//        SenderType senderType = senderInformation.getRight();
//
//        ChatRoom room = chatRoomRepository.findRoomDetailsById(roomId)
//                .orElseThrow(ErrorCode.CHATROOM_NOT_FOUND::commonException);
//
//        validateAuthorization(senderType, room, senderName);
//
//        String redisKey = BumblebeeStringUtil.format(REDIS_MESSAGE_KEY_FORMAT, roomId);
//
//        CopyOnWriteArrayList<ChatMessageResponse> combinedList = new CopyOnWriteArrayList<>();
//
//        redisChatMessagesAdd(roomId, redisKey, room, combinedList);
//
//        mysqlChatMessagesAdd(roomId, senderName, combinedList);
//
//        // 시간순으로 정렬 (최신 메시지가 마지막으로)
//        combinedList.sort(Comparator.comparing(ChatMessageResponse::timestamp));
//
//        // Combined 결과 반환
//        return combinedList;
//    }
//
//    private void mysqlChatMessagesAdd(Long roomId, String senderName, CopyOnWriteArrayList<ChatMessageResponse> combinedList) {
//        // MySQL에서 데이터 조회
//        List<ChatMessageResponse> mysqlMessages = chatMessageRepository.findByRoomId(roomId).stream()
//                .peek(message -> {
//                    if (!message.getIsRead()&&!message.getSender().equals(senderName)) {
//                        // 메시지를 읽음으로 설정
//                        message.read();
//                        chatMessageRepository.save(message); // 변경 사항 저장
//                    }
//                })
//                .map(ChatMessageResponse::new) // 읽음 상태를 true로 설정)
//                .toList();
//
//        // MySQL 메시지를 CopyOnWriteArrayList에 추가
//        combinedList.addAll(mysqlMessages);
//        log.debug("Fetched messages from MySQL for roomId {}: {}", roomId, mysqlMessages);
//    }
//
//    private void redisChatMessagesAdd(Long roomId, String redisKey, ChatRoom room, CopyOnWriteArrayList<ChatMessageResponse> combinedList) {
//        Set<Object> sortedMessages = redisTemplate.opsForZSet().range(redisKey, 0, -1);
//        if (sortedMessages != null) {
//            List<ChatMessageResponse> redisMessages = sortedMessages.stream()
//                    .map(value -> objectMapper.convertValue(value, ChatMessageSaveRequest.class)) // ChatMessageSaveRequest로 역직렬화
//                    .map(request -> Pair.of(room.getRoomId(), request))
//                    .map(pair -> new ChatMessageResponse(pair.getLeft(), pair.getRight(), false))
//                    .toList();
//
//            combinedList.addAll(redisMessages);
//
//            log.debug("Fetched messages from Redis: key={}, messages={}", redisKey, redisMessages);
//        }
//    }
//

//
    public void deleteOldMessages() {
        chatMessageRepository.deleteByTimestampBefore(LocalDateTime.now().minusDays(30));
    }

    public void removeExpiredMessages(Long roomId) {
        String redisKey = REDIS_MESSAGE_KEY_FORMAT + roomId + ":messages"; // 변경된 Key
        double cutoffTime = LocalDateTime.now().minusSeconds(messageTtl).toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, cutoffTime);
        log.debug("Expired messages removed for key={}, roomId={}", redisKey, roomId);
    }

    public void removeExpiredMessagesForAllRooms() {
        Set<String> roomKeys = redisTemplate.keys(REDIS_MESSAGE_KEY_FORMAT + "*");
        if (roomKeys == null || roomKeys.isEmpty()) {
            return;
        }
        for (String roomKey : roomKeys) {
            String roomIdStr = roomKey.replace(REDIS_MESSAGE_KEY_FORMAT, "").split(":")[0];
            try {
                Long roomId = Long.valueOf(roomIdStr);
                removeExpiredMessages(roomId);
            } catch (NumberFormatException e) {
                log.warn("Invalid room key format: {}", roomKey, e);
            }
        }
    }

    @Scheduled(fixedRateString = "${cleanupInterval:40000}") // Flush 이후 실행
    public void scheduleRemoveExpiredMessages() {
        removeExpiredMessagesForAllRooms();
    }
}