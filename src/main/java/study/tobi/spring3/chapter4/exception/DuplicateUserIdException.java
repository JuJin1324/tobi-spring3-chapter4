package study.tobi.spring3.chapter4.exception;

/**
 * Created by Yoo Ju Jin(jujin1324@daum.net)
 * Created Date : 2020/03/05
 */
public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(Throwable cause) {
        super(cause);
    }
}
