package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLExceptio 제거
 * MemberRepository Interface 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;

    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void ApoTest() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());

        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();

    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemeberId(), memberB.getMemeberId(), 2000);
        log.info("END TX");
        // then
        Member findmemberA = memberRepository.findById(memberA.getMemeberId());
        Member findmemberB = memberRepository.findById(memberB.getMemeberId());
        assertThat(findmemberA.getMoney()).isEqualTo(8000);
        assertThat(findmemberB.getMoney()).isEqualTo(12000);

    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemeberId(), memberEx.getMemeberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findmemberA = memberRepository.findById(memberA.getMemeberId());
        Member findmemberB = memberRepository.findById(memberEx.getMemeberId());
        assertThat(findmemberA.getMoney()).isEqualTo(10000);
        assertThat(findmemberB.getMoney()).isEqualTo(10000);

    }
}