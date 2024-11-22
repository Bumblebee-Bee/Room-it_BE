package roomit.web1_2_bumblebee_be.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomit.web1_2_bumblebee_be.domain.member.entity.Age;
import roomit.web1_2_bumblebee_be.domain.member.entity.Member;
import roomit.web1_2_bumblebee_be.domain.member.entity.Role;
import roomit.web1_2_bumblebee_be.domain.member.entity.Sex;
import roomit.web1_2_bumblebee_be.domain.member.exception.MemberNotFound;
import roomit.web1_2_bumblebee_be.domain.member.repository.MemberRepository;
import roomit.web1_2_bumblebee_be.domain.member.dto.request.MemberRegisterRequest;
import roomit.web1_2_bumblebee_be.domain.member.dto.request.MemberUpdateRequest;
import roomit.web1_2_bumblebee_be.domain.member.dto.response.MemberResponse;
import roomit.web1_2_bumblebee_be.domain.review.repository.ReviewRepository;
import roomit.web1_2_bumblebee_be.domain.workplace.repository.WorkplaceRepository;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest

class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private  BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private WorkplaceRepository workplaceRepository;
    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        reviewRepository.deleteAll();
        workplaceRepository.deleteAll();
    }

    @Test
    @DisplayName("멤버 등록")
    void test1(){
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        MemberRegisterRequest memberRequest = MemberRegisterRequest.builder()
                .age(Age.TEN)
                .sex(Sex.FEMALE)
                .pwd("Business1!")
                .email("sdsd@naver.com")
                .role(Role.ROLE_ADMIN)
                .phoneNumber("010-3323-2323")
                .nickName("치킨유저")
                .build();

        memberService.signupMember(memberRequest);

        Member member = memberRepository.findByMemberEmail("sdsd@naver.com")
                .orElseThrow(NoSuchElementException::new);



        assertTrue(bCryptPasswordEncoder.matches("Business1!",member.getMemberPwd()));
        assertEquals("치킨유저",member.getMemberNickName());

    }

    @Test
    @DisplayName("정보 조회")
    void test2(){
        Member member = Member.builder()
                .memberAge(Age.TEN)
                .memberSex(Sex.FEMALE)
                .memberPwd("Business1!")
                .memberEmail("sdsd@naver.com")
                .memberPhoneNumber("010-3323-2323")
                .memberNickName("치킨유저")
                .passwordEncoder(bCryptPasswordEncoder)
                .build();

        memberRepository.save(member);

        MemberResponse myDate = memberService.read(member.getMemberId());

        assertEquals("10대",member.getMemberAge().getDescription());
        assertTrue(bCryptPasswordEncoder.matches("Business1!",myDate.getPwd()));
        assertEquals("치킨유저",myDate.getNickName());
    }

    @Test
    @DisplayName("없는 정보 조회")
    void test3(){
        Member member = Member.builder()
                .memberAge(Age.TEN)
                .memberSex(Sex.FEMALE)
                .memberPwd("Business1!")
                .memberEmail("sdsd@naver.com")
                .memberPhoneNumber("010-3323-2323")
                .memberNickName("치킨유저")
                .passwordEncoder(bCryptPasswordEncoder)
                .build();

        memberRepository.save(member);
        assertThrows( MemberNotFound.class, () -> memberService.read(member.getMemberId() + 1));


    }

    @Test
    @DisplayName(" 정보 수정")
    void test4(){
        Member member = Member.builder()
                .memberAge(Age.TEN)
                .memberSex(Sex.FEMALE)
                .memberPwd("Business1!")
                .memberEmail("sdsd@naver.com")
                .memberPhoneNumber("010-3323-2323")
                .memberNickName("치킨유저")
                .passwordEncoder(bCryptPasswordEncoder)
                .build();

        memberRepository.save(member);

        MemberUpdateRequest memberRequest = MemberUpdateRequest.builder()
                .pwd("Business2!")
                .email("sdsd@naver.com")
                .phoneNumber("010-3323-2323")
                .memberNickName("이이")
                .build();



        MemberResponse myDate = memberService.update(member.getMemberId(), memberRequest);

        assertTrue(bCryptPasswordEncoder.matches("Business2!", myDate.getPwd()));

    }

    @Test
    @DisplayName(" 정보 삭제")
    void test5(){
        Member member = Member.builder()
                .memberAge(Age.TEN)
                .memberSex(Sex.FEMALE)
                .memberPwd("Business1!")
                .memberEmail("sdsd@naver.com")
                .memberPhoneNumber("010-3323-2323")
                .memberNickName("치킨유저")
                .passwordEncoder(bCryptPasswordEncoder)
                .build();

        memberRepository.save(member);

        memberService.delete(member.getMemberId());

        assertEquals(0, memberRepository.count());
    }

    @Test
    @DisplayName(" 없는 정보 삭제")
    void test6(){
        Member member = Member.builder()
                .memberAge(Age.TEN)
                .memberSex(Sex.FEMALE)
                .memberPwd("Business1!")
                .memberEmail("sdsd@naver.com")
                .memberPhoneNumber("010-3323-2323")
                .memberNickName("치킨유저")
                .passwordEncoder(bCryptPasswordEncoder)
                .build();

        memberRepository.save(member);

        assertThrows(MemberNotFound.class, () -> memberService.delete(member.getMemberId() + 1));

    }
}