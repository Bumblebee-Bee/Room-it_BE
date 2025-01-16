package roomit.main.domain.chat.chatmessage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomit.main.domain.chat.chatmessage.entity.ChatMessage;
import roomit.main.domain.chat.chatroom.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatRepositoryCustom {
    @Query("""
    SELECT cm 
    FROM ChatMessage cm 
    WHERE cm.room.roomId = :roomId AND cm.timestamp < :cursor 
    ORDER BY cm.timestamp DESC
""")
    List<ChatMessage> findByRoomId(Long roomId, LocalDateTime cursor, Pageable pageable);
    void deleteByTimestampBefore(LocalDateTime cutoffDate);
}