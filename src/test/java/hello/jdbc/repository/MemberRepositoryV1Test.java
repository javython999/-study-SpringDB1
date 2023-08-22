package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach() {
        // 기본 DriverManager - 항상 새로운 커넥션 획득
        //DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, USERPASSWORD);

        // 커넥션 풀
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(USERPASSWORD);
        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException, InterruptedException {
        Member member = new Member("memberV3", 10000);
        repository.save(member);

        Member findMember = repository.findById(member.getMemeberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);

        repository.update(member.getMemeberId(), 20000);
        Member updatedMember = repository.findById(member.getMemeberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        repository.delete(member.getMemeberId());
        assertThatThrownBy(()->repository.findById(member.getMemeberId()))
                .isInstanceOf(NoSuchElementException.class);


        Thread.sleep(1000);
    }
}