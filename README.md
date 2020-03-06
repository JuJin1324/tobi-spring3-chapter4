# 4장 예외 
토비의 스프링 3.1 Vol.1 스프링 이해와 원리

## 기초 셋팅
[3장 탬플릿](https://github.com/JuJin1324/tobi-spring3-chapter3#%EA%B8%B0%EC%B4%88-%EC%85%8B%ED%8C%85)의 기초 셋팅을 모두 가져옴(Maven 포함)

## 4.1 사라진 SQLException
jdbcContext 의 쿼리 메서드에서 <b>jdbcTemplate</b> 쿼리 메서드를 사용하면서 <b>throws SQLException</b> 제거하였음.

### 4.1.1 초난감 예외처리
```java
try {
    ...
} catch(SQLException e) {}  // catch 블럭 생성 후 아무것도 하지 않고 넘어간다.
```
위와 같이 예외 발생을 무시해버리고 정상적인 상황인 것처럼 다음 라인으로 넘어가겠다는 분명한 의도가 있는게 아니라면 연습 중에도 절대 만들어서는 안 되는 코드이다.

어디선가 오류가 있어서 예외가 발생했는데 그것을 무시하고 계속 진행해버리게되면 발생한 예외로 인해 어떤 기능이 비정상적으로 동작하거나, 
메모리나 리소스가 소진되어 예상치 못한 다른 문제를 야기한다.

더 큰 문제는 그 시스템 오류나 이상한 결과의 원인이 무엇인지 찾아내기가 매우 힘들어진다.

```java
} catch(SQLException e) {
    System.out.println(e);
}

} catch(SQLException e) {
    e.printStackTrace();
} 
```
다음과 같이 에러를 출력만 하는 것도 문제이다. 콘솔 로그를 누군가가 계속 모니터링 하지 않는 이상 예외 코드는 심각한 폭탄으로 남이 있게 된다.
예외를 출력하는 것만으로는 예외를 처리했다고 할 수 없다.

모든 예외는 적절하게 복구되든지 아니면 작업을 중단시키고 운영자 또는 개발자에게 분명하게 통보돼야 한다.
  
굳이 예외를 잡아서 뭔가 조치를 취할 방법이 없다면 잡지말아야 한다. 메서드에 throws XXXException(해당 exception)을 선언해서 
메서드 밖으로 던지고 자신을 호출한 코드에 예외처리 책임을 전가해라.

<b>무의미하고 무책임한 throws</b>   
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

발생하는 예외를 일일이 catch 하기도 귀찮고 매번 정확하게 예외 이름을 적어서 선언하기도 귀찮으니 아예 `throws Exception`이라는, 
모든 예외를 무조건 던져버리는 선언을 모든 메서드에 기계적으로 넣었다.

초난감 예외처리에서 나온 catch 블록 내에 아무것도 처리하지 않은 예외보다는 낫지만 이런 무책임한 throws 선언도 심각한 문제점이 있다.

자신이 사용하려는 메서드에 `throws Exception`이 선언되어 있다고 생각해보자.  
정말 무엇인가 실행 중에 예외적인 상황이 발생할 수 있다는 것인지, 아니면 그냥 습관적으로 복사해서 붙여놓은 것인지 알 수가 없다. 
결국 이런 메서드를 사용하는 메서드에서도 역시 `throws Exception`을 따라서 붙이는 수밖에 없다. 
결과적으로 적절한 처리를 통해 복구될 수 있는 예외상황도 재대로 다룰 수 있는 기회를 박탈당한다.

### 4.1.2 예외의 종류와 특징
자바에서 throw를 통해 발생시킬 수 있는 예외는 크게 세 가지가 있다.

* Error   
첫번째는 <b>java.lang.Error</b> 클래스의 서브클래스들이다. 
에러는 시스템에 뭔가 비정상적인 상황이 발생했을 경우에 사용된다. 
주로 자바 VM에서 발생시키는 것이기 때문에 애플리케이션 코드에서 잡으려고 하면 안 된다.
<b>OutOfMemoryError</b>나 <b>ThreadDeath</b> 같은 에러는 catch 블록으로 잡아봤자 아무런 대응 방법이 없기 때문이다.
시스템 레벨에서 특별한 작업을 하는 게 아니라면 애플리케이션에서는 이런 에러에 대한 처리는 신경 쓰지 않는다.

* Exception과 체크 예외   
<b>java.lang.Exception</b> 클래스와 그 서브클래스로 정의되는 예외들은 에러와 달리 개발자들이 만든 애플리케이션 코드의 작업 중에 예외상황이 발생했을 경우에 사용된다.
Exception 클래스는 <b>체크 예외(checked exception)</b>과 <b>언체크 예외(unchecked exception)</b>로 구분된다. 
체크 예외는 말 그래도 개발자들이 catch 로 체크하여 처리 해줘야하는 예외들이고 언체크 예외는 <b>RuntimeException</b>를 상속받은 예외이며 
주로 개발자의 코드 오류로 발생하는 경우임으로 catch 나 throws 를 하지 않는다.(밑에 자세히 설명)
 
* 체크 예외(checked exception)   
<b>RuntimeException</b> 클래스를 상속하지 <b>않은</b> 것들.   
체크 예외가 발생할 수 있는 메서드를 사용할 경우 반드시 예외를 처리하는 코드를 함께 작성해야 한다. 그렇지 않으면 컴파일 에러가 발생한다.

* 언체크 예외(unchecked exception)   
<b>RuntimeException</b> 클래스를 <b>상속한</b> 클래스들.   
명시적인 예외처리를 강제하지 않기 때문에 <b>언체크 예외</b>라고 불린다. 또는 대표 클래스 이름을 따서 <b>런타임 예외</b>라고도 불린다.
Error 와 마찬가지로 catch 문으로 잡거나 throws로 선언하지 않아도 된다. 물론 해도 상관없다.
<b>런타임 예외</b>는 주로 프로그램의 오류가 있을 때 발생하도록 의도된 것들이며, 대표적으로 오브젝트를 할당하지 않은 레퍼런스 변수를 사용하려고 시도할 때 발생하는 
<b>NullPointerException</b>이나, 허용되지 않는 값을 사용해서 메서드를 호출할 때 발생하는 <b>IllegealArgumentException</b>등이 있다.
피할 수 있지만 개발자가 부주의해서 발생할 수 있는 경우기 때문에 예상치 못했던 예외상황에서 발생되는 것이 아니기 때문에 굳이 catch 나 throws 를 사용하지 않아도 되도록 만든 것이다.

* 체크 예외가 예외처리를 강제하는 것 때문에 무책임한 throws 같은 코드가 남발됐다는 생각이 많기 때문에 최근 새로 등장하는 자바 표준 스펙의 API들은 예상 가능한 예외상황을
다루는 예외를 체크 예외로 만들지 않는 경향이 있기도 하다.

### 4.1.3 예외처리 방법
<b>예외 복구</b>   
첫 번째 예외처리 방법은 예외상황을 파악하고 문제를 해결해서 정상 상태로 돌려놓는 것이다.   
예를 들어 사용자가 요청한 파일을 읽으려고 시도했는데 해당 파일이 없거나 다른 문제로 IOException이 발생했다면 사용자에게 상황을 알려주고 다른 파일을 
용하도록 안내해서 예외상황을 해결할 수 있다.  
이런 경우 예외상황은 다시 정상으로 돌아오고 <b>예외를 복구</b>했다고 할 수 있다.  
단, IOException 에러 메시지가 사용자에게 그냥 던져지는 것은 예외 복구라고 볼 수 없다. 예외가 처리됐으면 비록 기능적으로는 사용자에게 예외상황으로 비쳐지더라도
애플리케이션에서는 정상적으로 설계된 흐름을 따라 진행돼야 한다.

예외처리 코드를 강제하는 체크 예외들은 이렇게 `예외를 어떤 식으로든 복구할 가능성이 있는 경우`에 사용한다.   
다음은 통제 불가능한 외부 요인으로 인해 예외가 발생하면 MAX_RETRY만큼 재시도를 하는 예이다.
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

<b>예외 처리 회피</b>   
두 번째 방법은 예외처리를 자신이 담당하지 않고 자신을 호출한 쪽으로 던져버리는 것이다.   
catch 문에서 로그만 남기고 다시 예외를 던지는 방식

* 예외처리 회피 1 : 그냥 예외 발생시 바로 호출한 쪽으로 던지기.
```java
public void add() throws SQLException {
    // JDBC API
}
```

* 예외처리 회피 2 : 로그 출력 후 호출한 쪽으로 던지기
```java
public void add() throws SQLException {
    try {
        // JDBC API
    } catch (SQLException) {
        // 로그 출력 후 호출한 쪽으로 던지기.
        throw e;
    }
}
```

JdbcContext 나 JdbcTemplate 이 사용하는 콜백 오브젝트는 ResultSet 이나 PreparedStatement 등을 이용해서 작업 중에 발생하는 SQLException 을
자신이 처리하지 않고 템플릿으로 던진다. SQLException 을 처리하는 일은 콜백 오브젝트의 역할이 아니라고 보기 때문이다.

콜백과 템플릿 처럼 긴밀하게 역할을 분담하고 있는 관계가 아니라면 자신의 코드에서 발생하는 예외를 그냥 던져버려서는 안된다.
예외를 회피하는 것은 예외를 복구하는 것처럼 의도가 분명해야 한다.

<b>예외 전환</b>   
마지막으로 예외를 처리하는 방법은 <b>예외 전환(exception translation)</b>을 하는 것이다.

예외 회피와 비슷하며 예외를 복구해서 정상적인 상태로 만들 수 없을 때 예외를 메서드 밖으로 던진다. 
하지만 예외 회피와는 달리, 발생한 예외를 그대로 넘기는 것이 아니라 <b>적절한 예외로 전환해서</b> 던진다.

예외 전환은 보통 두 가지 목적으로 사용된다.

첫번째로 내부에서 발생한 예외를 그대로 던지는 것이 그 예외상황에 대한 적절한 의미를 부여해주지 못하는 경우에, <b>의미를 분명하게 해줄 수 있는 예외</b>로 바꿔주기 위해서이다.   
예를 들어 새로운 사용자를 등록하려고 시도했을 때 아이디가 같은 사용자가 있어서 DB에러 발생시에 JDBC API는 SQLException을 발생시킨다. 
이 경우 DAO 메서드가 SQLException을 그대로 밖으로 던지면, DAO를 이용해 사용자를 추가하려한 서비스 계층에서는 왜 SQLException이 발생했는지 쉽게 알 방법이 없다. 
로그인 아이디 중복 같은 경우는 충분히 예상 가능하고 복구 가능한 예외상황이다. 이럴 땐 DAO에서 SQLException의 정보를 해석해서 
<b>DuplicateUserIdException</b> 같은 예외로 바꿔서 던져주는 것이 좋다.

의미가 분명한 예외가 던져지면 서비스 계층 오브젝트에서는 적절한 복구 작업을 시도할 수가 있다. 서비스 계층 오브젝트에서 SQLException 의 원인을 해석해서
대응하는 것도 불가능하지는 않지만, 특정 기술의 정보를 해석하는 코드를 비지니스 로직을 담은 서비스 계층에 두는 건 매우 어색하다.
```java
class UserDao {

    public void add(User user) throws DuplicateKeyException, SQLException {
        try {
            // JDBC를 이용해 user 정보를 DB에 추가하는 코드
        } catch (SQLException e) {
            // ErrorCode가 MySQL의 "Duplicatge Entry(1062)"이면 예외 전환
            if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
                throw DuplicateKeyException();
            else
                throw e;
        }
    }
}
```
보통 전환하는 예외에 원래 발생한 예외를 담아서 중첩 예외로 만드는 것이 좋다. 중첩 예외는 <b>getCause()</b> 메서드를 이용해서 처음 발생한 예외가 무엇인지 확인할 수 있다.   

중첩 예외 1)
```java
    catch(SQLException e) {
        ...
        throw DuplicateKeyException(e);
    }
``` 
중첩 예외 2)
```java
    catch(SQLException e) {
        ...
        throw DuplicateKeyException().initCause(e);
    }
``` 

두 번째 전환 방법은 예외를 처리하기 쉽고 단순하게 만들기 위해 포장하는 것이다.  
의미를 명확하게 하려고 다른 예외로 전환하는 것이 아닌 예외처리를 강제하는 체크 예외를 언체크 예외인 런타임 예외에 원인이 되는 예외를 내부로 담아서 바꾸는 경우에 사용한다.  
=> 어짜피 처리 불가능한 예외를 계속 외부로 던지게 하는 것이 아닌 런타임 예외로 바꿔서 시스템으로 던져버린다.

### 4.1.4 예외처리 전략
<b>런타임 예외의 보편화</b>   
체크 예외는 복구할 가능성이 조금이라도 있는, 말 그대로 예외적인 상황이기 때문에 자바는 이를 처리하는 catch 블록이나 throws 선언을 강제하고 있다.   
이렇게 예외처리를 강제하는 것은 예외가 발생할 가능성이 있는 API 메서드를 사용하는 개발자의 실수를 방지하기 위한 배려라고 볼 수도 있겠지만, 실제로는 예외를 
재대로 다루고 싶지 않을 만큼 짜증나게 만드는 원인이 되기도 한다.

자바가 처음 만들어질 때 애플릿이나 AWT, 스윙을 사용한 독립형 애플리케이션에서는 통제 불가능한 예외라고 할지라도 애플리케이션의 작업이 중단되지 않게 해주고 상황을 복구해야 했다.  
하지만 자바 엔터프라이즈 서버환경은 다르다. 수많은 사용자가 동시에 요청을 보내고 각 요청이 독립적인 작업으로 취급된다. 
하나의 요청을 처리하는 중에 예외가 발생하면 해당 작업만 중단시키면 그만이다. 독립형 애플리케이션과 달리 서버의 특정 계층에서 예외가 발생했을 때 작업을 일시 중지하고 사용자와 바로 
커뮤니케이션하면서 예외상황을 복구할 수 있는 방법이 없다. 차라리 예외상황을 미리 파악하고, 예외가 발생하지 않도록 차단하는 게 좋다.
또는 프로그램의 오류나 외부 환경으로 인해 예외가 발생하는 경우 빨리 해당 요청의 작업을 취소하고 서버 관리자나 개발자에게 통보해주는 편이 낫다.  

자바의 환경이 서버로 이동하면서 체크 예외의 활용도와 가치는 점점 떨어지고 있다. 자칫하면 `throws Exception`으로 점철된 아무런 의미도 없는 메서드들을 낳을 뿐이다. 
그래서 대응이 불가능한 체크 예외라면 빨리 런타임 예외로 전환해서 던지는 게 낫다.

<b>add() 메서드의 예외처리</b>   
add() 메서드는 DuplicateKeyException과 SQLException 두 가지의 체크 예외를 던지게 되어 있다. JDBC 코드에서 발생할 수 있는 SQLException 에서,
예외 발생 원인이 ID 중복이면 좀 더 의미 있는 예외인 DuplicateKeyException으로 전환해주고, 아리면 SQLException 을 그대로 던지게 했다.  
<b>DuplicateKeyException</b>는 충분히 복구 가능한 예외이므로 add() 메서드를 사용하는 쪽에서 잡아서 대응할 수 있다.  

하지만 <b>SQLException</b>은 대부분 복구 불가능한 예외이므로 잡아봤자 처리할 것도 없고, 결국 throws를 타고 계속 앞으로 전달되다가 애플리케이션 밖으로 던져질 것이다.   
그럴 바에는 그냥 런타임 예외로 포장해 던져버려서 그 밖의 메서드들이 신경 쓰지 않게 해주는 편이 낫다.

add() 메서드에 명시적으로 DuplicateUserIdException 를 던진다고 선언하여 해당 메서드를 사용하는 개발자에게 의미 있는 정보를 전달해줄 수 있다.
다음은 런타임 예외(언체크 예외) 클래스인 RuntimeException을 상속한 DuplicateUserIdException 클래스를 만든 코드이다.
```java
public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(Throwable cause) {
        super(cause);
    }
}
```
필요하면 언제든 잡아서 처리할 수 있도록 별도의 예외로 정의하기는 하지만, 필요 없다면 신경 쓰지 않아도 되도록 `RuntimeException`을 상속한 런타임 예외로 만든다.

```java
public class ExceptionHandleUserDao {
    
    public void add(User user) throws DuplicateUserIdException {
        try {
                Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?, ?, ?)");
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());
                
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
                    throw new DuplicateUserIdException(e);      /* 예외 전환 */
                else
                    throw new RuntimeException(e);              /* 예외 포장 */
            }
        }
    }
}
```
이제 add() 메서드를 사용하는 오브젝트는 SQLException을 처리하기 위해 불필요한 throws 선언을 할 필요가 없어졌으며 처리해야할 예외가 상당히 구체적으로
변했다.

<b>애플리케이션 예외</b>
런타임 예외 중심의 전략
* 낙관적인 예외처리 기법
* 일단 복구할 수 있는 예외는 없다고 가정
* 꼭 필요한 경우는 런타임 예외라도 잡아서 복구하거나 대응해줄 수 있음으로 문제 없음

비관적인 접근 방법
* 직접 처리할 수 없는 예외가 대부분이라고 하더라도 혹시 놓치는 예외가 있을 수 있으니, 일단 잡고 보도록 강제

애플리케이션 예외
* 시스템 또는 외부의 예외상황이 원인이 아니라 애플리케이션 자체의 로직에 의해 의도적으로 발생시키고, 반드시 catch 해서 무엇인가 조치를 취하도록 요구하는 예외.

예시 : 은행 계좌에서 출금하는 기능에서 현재 잔고보다 출금하는 금액이 더 큰 경우 예외 처리 방법은 다음 2가지가 있다.

정상 처리와 잔고 부족 처럼 예외 처리 시에 리턴하는 값을 다르게 하는 경우(정상은 0, 비정상은 -1 과 같이)   
* 해당 메서드 사용시에 예외상황에 대한 리턴 값을 명확하게 코드화하고 잘 관리하지 않으면 혼란이 생길 수 있다.  
정상적인 처리가 안 됐을 때 전달하는 값의 표준 같은 것은 없다. 어떤 개발자는 0을 생각할 수도 있고, -1이나 -999를 돌려주는 개발자도 있다.  
재대로된 상수화 처리가 되지 않으면 개발자 사이의 의사소통 문제로인해 재대로 동작하지 않을 수 있다.
```java
public int deposit(int money, Account account) {
    // 파라미터로 받은 계정과 돈으로 입금 처리
    if ()   // 예외발생 가능한 사항 조건문 처리
        return -1;
    
    return 0;   // 정상인 경우
}
```
* 또 한가지 문제는 결과 값을 확인하는 조건문이 자주 등장한다는 점이다.  
```java
public void main(String[] args) {
    int res = deposit(1000, user1);
    if (res == -1) {
        // 예외 처리 1
    } else if (res == -2) {
        // 예외 처리 2
    }
}
```
* 이런 식으로 결과를 돌려주는 메서드를 연이어 사용하는 경우 if 블록이 범벅된 코드가 이어질 수 있다.

정상적인 흐름을 따르는 코드는 그대로 두고, 잔고 부족과 같은 예외 상황에서는 예외를 던지도록 만드는 것, 이 때 던지는 예외는 체크 예외로 만들어서 예외 처리를 
사용자가 하도록 강제한다.
```java
try {
    BigDecimal balance = account.withdraw(amount);
    ...
    /* 정상적인 처리 결과를 출력하도록 진행 */
} catch (InsufficientBalanceException e) {      /* 체크 예외 */
    BigDecimal availFunds = e.getAvailFunds();
    ...
    /* 잔고 부족 안내 메시지를 준비하고 이를 출력하도록 진행 */
}
```

### 4.1.5 SQLException은 어떻게 됐나?
`SQLException`은 복구 가능한 예외가 아니다. 
SQL의 문법이 틀렸거나, 제약조건 위반, DB 서버의 다운, 네트워크 불안정, DB커넥션 풀이 모두 차서 커넥션을 가져올 수 없는 경우 
애플리케이션 레벨에서 복구할 방법이 없다. 그럼으로 개발자나 관리자에게 예외 발생 사실을 알려지도록 전달하는 방법 밖에 없다. 

DAO 밖에서 SQLException을 다룰 수 있는 가능성도 거의 없음으로 필요도 없는 기계적인 `throws`선언이 등장하도록 방치하지 말고 
가능한 빨리 언체크/런타임 예외로 전환해줘야 한다.

스프링의 `JdbcTemplate`은 바로 이 예외처리 전략을 잘 따르며, JdbcTemplate 템플릿과 콜백 안에서 발생하는 모든 SQLException을 
런타임 예외인 DataAccessException으로 포장해서 던져준다. 
따라서 throws로 선언되어 있긴 하지만 DataAccessException이 런타임 예외이므로 해당 메서드를 사용하는 메서드에서 이를 잡거나 다시 던질 의무는 없다.

그 밖에도 스프링의 API 메서드에 정의되어 있는 대부분의 예외는 런타임 예외다. 따라서 발생가능한 예외가 있다고 하더라도 이를 처리하도록 강제하지 않는다.

## 4.2 예외 전환
예외 전환의 목적 2가지
* 런타임 예외로 포장해서 굳이 필요하지 않은 `catch/throws`를 줄이기
* 로우레벨의 예외를 좀 더 의미 있고 추상화된 예외로 바꿔서 던져주기 위함.

<b>DataAccessException</b>
* DB 마다 에러 코드가 제각각이다. 예를 들어 키 값이 중복돼서 중복 오류 발생시에 MySQL은 1062, 오라클은 1, DB2는 803이다.
* 스프링은 DB별 에러 코드를 분류해서 스프링이 정의한 예외 클래스와 매핑해놓았다.
* `DataAccessException`을 이용해서 중복 키가 발생한 경우에 외부 처리가 필요하면 
`throws DuplicatedKeyException`을 이용해서 외부에서 처리할 수 있도록 할 수 있다.

### 4.2.1 JDBC의 한계
JDBC는 자바를 이용해 DB에 접근하는 방법을 추상화된 API 형태로 정의해놓고, 각 DB 업체가 JDBC 표준을 따라 만들어진 드라이버를 제공하게 해준다.

표준화된 JDBC API가 DB 프로그램 개발 방법을 학습하는 부담은 확실히 줄여주지만 DB를 자유롭게 변경해서 사용할 수 있는 유연한 코드를 보장해주지는 못한다.
현실적으로 DB를 자유롭게 바꾸어 사용할 수 있는 DB 프로그램을 작성하는 데는 두 가지 걸림돌이 존재한다.

<b>비표준 SQL</b>   
SQL은 어느정도 표준화된 언어이지만 대부분의 DB는 표준을 따르지 않는 비표준 문법과 기능도 제공한다. 
해당 DB의 특별한 기능을 사용하거나 최적화된 SQL을 만들 때 유용하기 때문이다.

이렇게 작성된 비표준 SQL은 결국 DAO 코드에 들어가고, 해당 DAO는 특정 DB에 종속적인 코드가 되고 만다. 
보통은 DB가 자주 변경되지도 않고, 사용하는 DB에 최적화하는 것이 중요하므로 비표준 SQL을 거리낌없이 사용한다. 
하지만 DB의 변경 가능성을 고려해서 유연하게 만들어야 한다면 SQL은 제법 큰 걸림돌이 된다.

<b>호환성 없는 SQLException의 DB 에러정보</b>
DB를 사용하다 발생할 수 있는 예외의 원인은 다양하다. SQL 문법 오류도 있고, DB 커넥션을 가져오지 못했을 수도 있으며, 테이블이나 필드가 존재하지 않거나,
키가 중복되거나 다양한 제약조건을 위배하는 시도를 한 경우, 데드락에 걸렸거나 락을 얻지 못했을 경우 등 수백여 가지에 이른다.

문제는 DB마다 SQL만 다른 것이 아니라 에러의 종류와 원인도 제각각이라는 점이다. 그래서 JDBC는 데이터 처리 중에 발생하는 다양한 예외를 그냥 SQLException 
하나에 모두 담아버린다. JDBC API는 이 SQLException 한 가지만 던지도록 설계되어 있다. 예외가 발생한 원인은 SQLException 안에 담긴 에로 코드와 SQL 상태정보를
참조해봐야 한다.

그런데 SQLException의 getErrorCode() 로 가져올 수 있는 DB 에러 코드는 DB별로 모두 다르다. 
ExceptionHandleUserDao 클래스의 add() 메서드에서 키 중복 예외가 발생하는 경우를 확인하기 위해 다음과 같은 방법을 사용했었다.
```java
if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
``` 
그런데 여기서 사용한 에러 코드는 MySQL 전용 코드일 뿐이다. DB가 오라클이나 SQLServer로 바뀐다면 에러 코드도 달라지므로 이 코드는 기대한 대로 
동작하지 않는다.

그래서 SQLException은 예외가 발생했을 때의 DB 상태를 담은 SQL 상태정보를 부가적으로 제공한다. getSQLState() 메서드로 예외상황에 대한 상태정보를 
가져올 수 있다. 이 상태정보는 DB 별로 달라지는 에러 코드를 대신할 수 있도록 DB 독립적인 표준 상태 코드가 정의되어 있다.

SQLException이 이러한 상태 코드를 제공하는 이유는 DB에 독립적인 에러정보를 얻기 위해서이다. 하지만 DB의 JDBC 드라이버에서 SQLException을 담을 상태 코드를
정확하게 만들어주지 않기 때문에 이 SQL 상태 코드를 믿고 결과를 파악하도록 코드를 작성하는 것은 위험하다.

결국 SQLException 만으로 DB에 독립적인 유연한 코드를 작성하는 건 불가능하다.

### 4.2.2 DB 에러 코드 매핑을 통한 전환
스프링은 DataAccessException 이라는 SQLException을 대체할 수 있는 런타임 예외를 정의하고 있을 뿐 아니라 DataAccessException 의 서브클래스로
세분화된 예외 클래스들을 정의하고 있다.
* 문법 오류 -> BadSqlGrammarException
* DB 커넥션 오류 -> DataAccessResourceFailureException
등등

스프링은 각 DB 별 에러 코드를 분류해서 스프링이 정의한 예외 클래스와 매핑해놓은 에러 코드 매핑정보 테이블을 만들어두고 이를 이용한다.
JdbcTemplate 은 SQLException을 단지 런타임 예외인 DataAccessException 으로 포장하는 것이 아니라 드라이버나 DB 메타정보를 참조해서
DB 종류를 확인하고 DB별로 미리 준비된 매핑정보를 참조해서 DB 에러 코드를 DataAccessException의 적절한 서브클래스로 포장한다.

따라서 JdbcTemplate 사용시에 예외 포장을 위한 코드가 따로 필요 없다.
add() 메서드를 사용하는 쪽에서 중복 키 상황에 대한 대응이 필요한 경우에 참고할 수 있도록 DuplicateKeyException 을 메서드 선언에 넣어주자.
```java
public void add(final User user) throws DuplicateKeyException {
    jdbcTemplate.update("insert into users(id, name, password) values (?, ?, ?)",
            user.getId(), user.getName(), user.getPassword());
}
```

JdbcTemplate을 이용한다면 JDBC에서 발생하는 DB 관련 예외는 거의 신경 쓰지 않아도 된다.

그런데 모종의 이유로 중복키 에러가 발생했을 때 직접 정의한 체크 예외를 발생시키고 싶다면 다음과 같지 예외를 전환하는 코드를 넣어준다.
```java
public void add(final User user) throws DuplicateUserIdException {  /* 체크 예외 */
    try {
        jdbcTemplate.update("insert into users(id, name, password) values (?, ?, ?)",
                user.getId(), user.getName(), user.getPassword());
    } catch (DuplicateKeyException e) {
        /* 로그를 남기는 등의 필요한 작업 */
        throw new DuplicateUserIdException(e);     /* 예외 전환 시에는 원인이 되는 예외를 중첩하는 것이 좋다. */
    }
}
```

JDK 1.6 에 포함된 JDBC 4.0 부터는 SQLException을 좀 더 세분화했다고하며 현재 Java 8 ~ Java 12 부터는 어느정도 신뢰도가 생기지 않았을까 추측해본다.

### 4.2.3 DAO 인터페이스와 DataAccessException 계층구조
