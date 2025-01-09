package roomit.main.domain.chat.chatroom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomRedisService {
    private final RedisService redisService;

    public void connectChatRoom(Long chatRoomId, String sessionId) {
        redisService.addToSet("chatRoom_" + chatRoomId, sessionId);
        redisService.setData(sessionId, chatRoomId);
    }

    public void disConnectChatRoom(String sessionId) {
        Long chatRoomId = Long.valueOf((String) redisService.getData(sessionId));
        if (redisService.getSetSize("chatRoom_" + chatRoomId) == 1) {
            redisService.deleteData("chatRoom_" + chatRoomId);
        } else {
            redisService.deleteToSet("chatRoom_" + chatRoomId, sessionId);
        }
    }

    public Long getConnectionMemberSize(String key) {
        return redisService.getSetSize(key);
    }
}
