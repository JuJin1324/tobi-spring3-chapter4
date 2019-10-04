# 4장 예외 
토비의 스프링 3.1 Vol.1 스프링 이해와 원리

## 기초 셋팅
* [1장 오브젝트와 의존관계](https://github.com/JuJin1324/tobi-spring3-chapter1)의
기초 셋팅을 모두 가져옴(Maven 포함)

## 예외 처리
### 초난감 예외처리
```java
try {
    ...
} catch(SQLException e) {}  // catch 블럭 생성 후 아무것도 하지 않고 넘어간다.
```
* 위와 같이 예외 발생을 무시해버리고 정상적인 상황인 것처럼 다음 라인으로 넘어가겠다는 분명한 의도가 있는게 아니라면  
연습 중에도 절대 만들어서는 안 되는 코드이다.

* 어디선가 오류가 있어서 예외가 발생했는데 그것을 무시하고 계속 진행해버리게되면  
발생한 예외로 인해 어떤 기능이 비정상적으로 동작하거나, 메모리나 리소스가 소진되어 예상치 못한 다른 문제를 야기한다.

* 더 큰 문제는 그 시스템 오류나 이상한 결과의 원인이 무엇인지 찾아내기가 매우 힘들어진다.

* 예외를 처리할 때 반드시 지켜야 할 핵심 원칙은 한 가지이다.
    * 모든 예외는 적절하게 복구되든지 아니면 작업을 중단시키고 운영자 또는 개발자에게 분명하게 통보돼야 한다.
  
* 굳이 예외를 잡아서 뭔가 조치를 취할 방법이 없다면 잡지말아야 한다. 메서드에 throws XXXException(해당 exception)을 선언해서  
메서드 밖으로 던지고 자신을 호출한 코드에 예외처리 책임을 전가해라.

### 무의미하고 무책임한 throws
```java
public void method1() throws Exception {
    method2();      // ↓ 호출
}
/* ↑ 예외 던지기 */
public void method2() throws Exception {
    method3();      // ↓ 호출 
}
/* ↑ 예외 던지기 */
public void method3() throws Exception {
    ...
}
```

* 발생하는 예외를 일일이 catch하기도 귀찮고 매번 정확하게 예외 이름을 적어서 선언하기도 귀찮으니 아예  
`throws Exception`이라는, 모든 예외를 무조건 던져버리는 선언을 모든 메서드에 기계적으로 넣었다.

* 초난감 예외처리에서 나온 catch 블록 내에 아무것도 처리하지 않은 예외보다는 낫지만 이런 무책임한 throws  
선언도 심각한 문제점이 있다.

* 자신이 사용하려는 메서드에 `throws Exception`이 선언되어 있다고 생각해보자.  
정말 무엇인가 실행 중에 예외적인 상황이 발생할 수 있다는 것인지, 아니면 그냥 습관적으로 복사해서 붙여놓은 것인지  
알 수가 없다. 결국 이런 메서드를 사용하는 메서드에서도 역시 `throws Exception`을 따라서 붙이는 수밖에 없다.  
결과적으로 적절한 처리를 통해 복구될 수 있는 예외상황도 재대로 다룰 수 있는 기회를 박탈당한다.

## 예외의 종류와 특징
* 자바에서 throw를 통해 발생시킬 수 있는 예외는 크게 세 가지가 있다.

### Error
* 첫번째는 `java.lang.Error` 클래스의 서브클래스들이다.
* 에러는 시스템에 뭔가 비정상적인 상황이 발생했을 경우에 사용된다.
* 주로 자바 VM에서 발생시키는 것이기 때문에 애플리케이션 코드에서 잡으려고 하면 안 된다.
* `OutOfMemoryError`나 `ThreadDeath` 같은 에러는 catch 블록으로 잡아봤자 아무런 대응 방법이 없기 때문이다.
* 시스템 레벨에서 특별한 작업을 하는 게 아니라면 애플리케이션에서는 이런 에러에 대한 처리는 신경 쓰지 않는다.

### Exception과 체크 예외
* `java.lang.Exception` 클래스와 그 서브클래스로 정의되는 예외들은 에러와 달리 개발자들이 만든 애플리케이션  
코드의 작업 중에 예외상황이 발생했을 경우에 사용된다.
* Exception 클래스는 `체크 예외(checked exception)`과 `언체크 예외(unchecked exception)`로 구분된다.

### 체크 예외(checked exception)
* `RuntimeException` 클래스를 상속하지 <b>않은</b> 것들.
* 체크 예외가 발생할 수 있는 메서드를 사용할 경우 반드시 예외를 처리하는 코드를 함께 작성해야 한다.  
그렇지 않으면 컴파일 에러가 발생한다.

    
### 언체크 예외(unchecked exception)
* `RuntimeException` 클래스를 <b>상속한</b> 클래스들.

* 명시적인 예외처리를 강제하지 않기 때문에 `언체크 예외`라고 불린다. 또는 대표 클래스 이름을 따서 `런타임 예외`라고도 불린다.

* `Error`와 마찬가지로 catch 문으로 잡거나 throws로 선언하지 않아도 된다. 물론 해도 상관없다.

* `런타임 예외`는 주로 프로그램의 오류가 있을 때 발생하도록 의도된 것들이며,  
대표적으로 오브젝트를 할당하지 않은 레퍼런스 변수를 사용하려고 시도할 때 발생하는 `NullPointerException`이나,  
허용되지 않는 값을 사용해서 메서드를 호출할 때 발생하는 `IllegealArgumentException`등이 있다.

* 피할 수 있지만 개발자가 부주의해서 발생할 수 있는 경우기 때문에 예상치 못했던 예외상황에서 발생되는 것이 아니기 때문에  
굳이 catch 나 throws 를 사용하지 않아도 되도록 만든 것이다.

## 예외처리 방법
### 예외 복구
* 첫 번째 예외처리 방법은 예외상황을 파악하고 문제를 해결해서 정상 상태로 돌려놓는 것이다.
* 예를 들어 사용자가 요청한 파일을 읽으려고 시도했는데 해당 파일이 없거나 다른 문제로 IOException이 발생했다면  
사용자에게 상황을 알려주고 다른 파일을 이용하도록 안내해서 예외상황을 해결할 수 있다.  
이런 경우 예외상황은 다시 정상으로 돌아오고 `예외를 복구`했다고 할 수 있다.  
단, IOException 에러 메시지가 사용자에게 그냥 던져지는 것은 예외 복구라고 볼 수 없다.

* 예외처리 코드를 강제하는 체크 예외들은 이렇게 `예외를 어떤 식으로든 복구할 가능성이 있는 경우`에 사용한다.
* 다음은 통제 불가능한 외부 요인으로 인해 예외가 발생하면 MAX_RETRY만큼 재시도를 하는 예이다.
```java
int maxRetry = MAX_RETRY;
while (maxRetry-- > 0) {
    try {
        ...         // 예외가 발생할 가능성이 있는 시도
        return;     // 작업 성공
    } catch (SomeException) {
        // 로그 출력. 정해진 시간만큼 대기 
    } finally {
        // 리소스 반납. 정리 작업
    }
}
throw new RetryFailedException();   // 최대 재시도 횟수를 넘기면 직접 예외 발생
```

### 예외 회피
* 두 번째 방법은 예외처리를 자신이 담당하지 않고 자신을 호출한 쪽으로 던져버리는 것이다.
* 예외처리 회피 1
```java
public void add() throws SQLException {
    // JDBC API
}
```

* 예외처리 회피 2
```java
public void add() throws SQLException {
    try {
        // JDBC API
    } catch (SQLException) {
        // 로그 출력
        throw e;
    }
}
```

### 예외 전환
* 마지막으로 예외를 처리하는 방법은 `예외 전환(exception translation)`을 하는 것이다.

* 예외 회피와 비슷하며 예외를 복구해서 정상적인 상태로 만들 수 없을 때 예외를 메서드 밖으로 던진다.  
하지만 예외 회피와는 달리, 발생한 예외를 그대로 넘기는 것이 아니라 적절한 예외로 전환해서 던진다.

* 예외 전환은 보통 두 가지 목적으로 사용된다.
    * 첫번째로 내부에서 발생한 예외를 그대로 던지는 것이 그 예외상황에 대한 적절한 의미를 부여해주지 못하는 경우에,  
    의미를 분명하게 해줄 수 있는 예외로 바꿔주기 위해서이다.
    * 예를 들어 새로운 사용자를 등록하려고 시도했을 때 아이디가 같은 사용자가 있어서 DB에러 발생시에 JDBC API는  
    SQLException을 발생시킨다. 이 경우 DAO 메서드가 SQLException을 그대로 밖으로 던지면, DAO를 이용해 사용자를  
    추가하려한 서비스 계층에서는 왜 SQLException이 발생했는지 쉽게 알 방법이 없다.  
    로그인 아이디 중복 같은 경우는 충분히 예상 가능하고 복구 가능한 예외상황이다. 이럴 땐 DAO에서 SQLException의 정보를  
    해석해서 `DuplicateUserIdException` 같은 예외로 바꿔서 던져주는 것이 좋다.
```java
public void add(User user) throws DuplicateUserIdException, SQLException {
    try {
        // JDBC를 이용해 user 정보를 DB에 추가하는 코드
    } catch (SQLException) {
        // ErrorCode가 MySQL의 "Duplicatge Entry(1062)"이면 예외 전환
        if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
            throw DuplicateUserIdException();
        else
            throw e;
    }
}
```

* 보통 전환하는 예외에 원래 발생한 예외를 담아서 중첩 예외로 만드는 것이 좋다.  
중첩 예외는 `getCause()` 메서드를 이용해서 처음 발생한 예외가 무엇인지 확인할 수 있다.
예 1)
```java
    catch(SQLException e) {
        ...
        throw DuplicateUserIdException(e);
    }
``` 
예 2)
```java
    catch(SQLException e) {
        ...
        throw DuplicateUserIdException().initCause(e);
    }
``` 

* 두 번째 전환 방법은 예외를 처리하기 쉽고 단순하게 만들기 위해 포장하는 것이다.  
의미를 명확하게 하려고 다른 예외로 전환하는 것이 아닌 예외처리를 강제하는 체크 예외를 언체크 예외인 런타임 예외로 바꾸는 경우에 사용한다.
=> 어짜피 처리 불가능한 예외를 계속 외부로 던지게 하는 것이 아닌 런타임 예외로 바꿔서 시스템으로 던져버린다.

## 예외처리 전략
* 자바가 처음 만들어질 때 애플릿이나 AWT, 스윙을 사용한 독립형 애플리케이션에서는 통제 불가능한 예외라고 할지라도 애플리케이션의  
작업이 중단되지 않게 해주고 상황을 복구해야 했다.  
* 하지만 자바 엔터프라이즈 서버환경은 다르다. 수많은 사용자가 동시에 요청을 보내고 각 요청이 독립적인 작업으로 취급된다.  
하나의 요청을 처리하는 중에 예외가 발생하면 해당 작업만 중단시키면 그만이다.
* 프로그램의 오류나 외부 환경으로 인해 예외가 발생하는 경우 빨리 해당 요청의 작업을 취고하고 서버 관리자나 개발자에게 통보해주는 편이 낫다.  
* 자바의 환경이 서버로 이동하면서 체크 예외의 활용도와 가치는 점점 떨어지고 있다.   
자칫하면 `throws Exception`으로 점철된 아무런 의미도 없는 메소드들을 낳을 뿐이다. 
그래서 대응이 불가능한 제크 예외라면 빨리 런타임 예외로 전환해서 던지는 게 낫다.

### add() 메서드의 예외처리
* `DuplicateUserIdException`는 충분히 복구 가능한 예외이므로 add() 메서드를 사용하는 쪽에서 잡아서 대응할 수 있다.  
* 하지만 `SQLException`은 대부분 복구 불가능한 예외이므로 잡아봤자 처리할 것도 없고, 결국 throws를 타고 계속  
앞으로 전달되다가 애플리케이션 밖으로 던져질 것이다. 그럴 바에는 그냥 런타임 예외로 포장해 던져버려서 그 밖의 메서드들이   
신경 쓰지 않게 해주는 편이 낫다.

* DuplicateUserIdException 클래스
```java
public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(Throwable cause) {
        super(cause);
    }
}
```
필요하면 언제든 잡아서 처리할 수 있도록 별도의 예외로 정의하기는 하지만, 필요 없다면 신경 쓰지 않아도 되도록 `RuntimeException`을  
상속한 런타임 예외로 만든다.

## 애플리케이션 예외
* 시스템 또는 외부의 예외상황이 원인이 아니라 애플리케이션 자체의 로직에 의해 의도적으로 발생시키고, 반드시 catch 해서  
무엇인가 조치를 취하도록 요구하는 예외.

### 상황 예시
* 처리 결과를 리턴값으로 반환하는 메서드를 생각해보자. 해당 메서드 사용시에 예외상황에 대한 리턴 값을 명확하게 코드화하고 잘 관리하지 않으면  
혼란이 생길 수 있다. 정상적인 처리가 안 됐을 때 전달하는 값의 표준 같은 것은 없다. 어떤 개발자는 0을 생각할 수도 있고, -1이나 -999를 돌려주는 개발자도 있다.  
재대로된 상수화 처리가 되지 않으면 개발자 사이의 의사소통 문제로인해 재대로 동작하지 않을 수 있다.

* 또 한가지 문제는 결과 값을 확인하는 조건문이 자주 등장한다는 점이다. 이런 식으로 결과를 돌려주는 메서드를 연이어 사용하는 경우  
if 블록이 범벅된 코드가 이어질 수 있다.

* 다른 방법으로 정상적인 흐름을 따르는 코드는 그대로 두고, 잔고 부족과 같은 예외상황에서는 비즈니스적인 의미를 띤 예외를 던지도록 만든다.(애플리케이션 예외 활용)

## SQLException 처리
* `SQLException`은 복구 가능한 예외가 아니다. SQL의 문법이 틀렸거나, 제약조건 위반, DB 서버의 다운, 네트워크 불안정, DB커넥션 풀이 모두 차서  
커넥션을 가져올 수 없는 경우 애플리케이션 레벨에서 복구할 방법이 없다. 그럼으로 개발자나 관리자에게 예외 발생 사실을 알려지도록 전달하는 방법 밖에 없다. 

* DAO 밖에서 SQLException을 다룰 수 있는 가능성도 거의 없음으로 필요도 없는 기계적인 `throws`선언이 등장하도록 방치하지 말고  
가능한 빨리 언체크/런타임 예외로 전환해줘야 한다.

* 스프링의 `JdbcTemplate`은 바로 이 예외처리 전략을 잘 따르며, JdbcTemplate 템플릿과 콜백 안에서 발생하는 모든 SQLException을  
런타임 예외인 DataAccessException으로 포장해서 던져준다. 

* 따라서 throws로 선언되어 있긴 하지만 DataAccessException이 런타임 예외이므로 해당 메서드를 사용하는 메서드에서  
이를 잡거나 다시 던질 의무는 없다.

* 그 밖에도 스프링의 API 메서드에 정의되어 있는 대부분의 예외는 런타임 예외다. 따라서 발생가능한 예외가 있다고 하더라도 이를 처리하도록 강제하지 않는다.

## 예외 전환
* 예외 전환의 목적 2가지
    * 런타임 예외로 포장해서 굳이 필요하지 않은 `catch/throws`를 줄이기
    * 로우레벨의 예외를 좀 더 의미 있고 추상화된 예외로 바꿔서 던져주기 위함.

## DataAccessException
* DB 마다 에러 코드가 제각각이다. 예를 들어 키 값이 중복돼서 중복 오류 발생시에 MySQL은 1062, 오라클은 1, DB2는 803이다.
* 스프링은 DB별 에러 코드를 분류해서 스플잉 정의한 예외 클래스와 매핑해놓았다.
* `DataAccessException`을 이용해서 중복 키 발생한 경우에 외부 처리가 필요하면 `throws DuplicatedKeyException`을 이용해서  
외부에서 처리할 수 있도록 할 수 있다.


