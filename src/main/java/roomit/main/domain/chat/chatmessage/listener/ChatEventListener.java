package roomit.main.domain.chat.chatmessage.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import roomit.main.domain.chat.chatmessage.event.ChatSaveEvent;
import roomit.main.domain.chat.chatroom.service.RedisService;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;

    @Async("testExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendChatAndDebounceNotification(ChatSaveEvent event) throws InterruptedException {
        messagingTemplate.convertAndSend("/sub/chat/" + event.chatMessageSaveRequest().roomId(), event.chatMessageSaveRequest());

        log.info("receiver" + event.receiverId());
        log.info("sender" + event.senderId());

        if(redisService.isNotExistsKey("debouncing_" + event.receiverId())) // 수신자 알림 디바운싱
            redisService.setData("debouncing_" + event.receiverId(),"", 2L, TimeUnit.SECONDS);
        if(redisService.isNotExistsKey("debouncing_" + event.senderId())) // 발신자 알림 디바운싱
            redisService.setData("debouncing_" + event.senderId(),"", 2L,TimeUnit.SECONDS);
    }
}
