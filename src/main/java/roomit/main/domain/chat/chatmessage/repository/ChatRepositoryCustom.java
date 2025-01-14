package roomit.main.domain.chat.chatmessage.repository;

import roomit.main.domain.chat.chatmessage.entity.ChatMessage;
import roomit.main.domain.chat.chatroom.entity.ChatRoom;
import roomit.main.domain.member.entity.Member;

import java.util.List;

public interface ChatRepositoryCustom<T> {
    void readAllMyNotReadChatList(ChatRoom chatRoom, T member);
}
