package roomit.main.domain.chat.chatmessage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import roomit.main.domain.business.entity.Business;
import roomit.main.domain.chat.chatroom.entity.ChatRoom;
import roomit.main.domain.member.entity.Member;

import static roomit.main.domain.chat.chatmessage.entity.QChatMessage.chatMessage;

@RequiredArgsConstructor
public class ChatRepositoryCustomImpl<T> implements ChatRepositoryCustom<T> {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public void readAllMyNotReadChatList(ChatRoom chatroom, T user) {
        if (user instanceof Member) {
            // Member에 대한 처리
            queryFactory.update(chatMessage)
                    .where(chatMessage.room.eq(chatroom), chatMessage.isRead.isFalse(), chatMessage.sender.ne(((Member) user).getMemberNickName()))
                    .set(chatMessage.isRead, true)
                    .execute();
        } else if (user instanceof Business) {
            // Business에 대한 처리
            queryFactory.update(chatMessage)
                    .where(chatMessage.room.eq(chatroom), chatMessage.isRead.isFalse(), chatMessage.sender.ne(((Business) user).getBusinessName()))
                    .set(chatMessage.isRead, true)
                    .execute();
        } else {
            throw new IllegalArgumentException("Unsupported user type");
        }
        em.flush();
        em.clear();
    }
}
