package roomit.main.domain.chat.chatroom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import roomit.main.domain.business.dto.CustomBusinessDetails;
import roomit.main.domain.chat.chatroom.dto.request.ChatRoomRequest;
import roomit.main.domain.chat.chatroom.dto.response.ChatRoomResponse;
import roomit.main.domain.chat.chatroom.service.ChatRoomService;
import roomit.main.domain.member.dto.CustomMemberDetails;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createRoom(@RequestBody ChatRoomRequest request,
            @AuthenticationPrincipal CustomMemberDetails memberDetails){
        return chatRoomService.create(memberDetails.getId(), request.workplaceId());
    }


    @GetMapping("/room")
    @ResponseStatus(HttpStatus.OK)
    public List<? extends ChatRoomResponse> list(@AuthenticationPrincipal CustomMemberDetails memberDetails,
                                                 @AuthenticationPrincipal CustomBusinessDetails businessDetails) {
        return chatRoomService.getChatRooms(memberDetails, businessDetails);
    }

//    @GetMapping("/room/detail/{roomId}")
//    @ResponseStatus(HttpStatus.OK)
//    public List<? extends ChatRoomResponse> list(@AuthenticationPrincipal CustomMemberDetails memberDetails,
//                                                 @AuthenticationPrincipal CustomBusinessDetails businessDetails,
//                                                 @RequestParam() LocalDateTime cursor) {  // 커서만 받기
//        return chatRoomService.getChatRooms(memberDetails, businessDetails, cursor);
//    }
}
