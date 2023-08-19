***
# JDBC 등장
DB 마다 커넥션 방법, SQL전달 방법, 결과를 받는 방법이 다르다.

때문에 DB를 변경하게 되면 애플리케이션의 코드도 같이 변경해야한다.

이 문제를 해결하기 위해 JDBC라는 자바 표준이 등장했다.
***
# JDBC 표준인터페이스
대표적으로 다음 3가 기능을 표준 인터페이로 정해서 제공한다.
> 연결 - java.sql.Connection
>
> 질의 - java.sql.Statement
>
> 응답 - java.sql.ResultSet

각각의 DB벤더에서 자신의 DB에 맞게 JDBC 인터페이스를 구현한 라이브러리 제공한다.

이를 JDBC Driver라고 한다.
***
