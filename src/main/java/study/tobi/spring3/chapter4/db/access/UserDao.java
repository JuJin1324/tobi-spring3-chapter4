package study.tobi.spring3.chapter4.db.access;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import study.tobi.spring3.chapter4.db.entity.User;
import study.tobi.spring3.chapter4.exception.DuplicateUserIdException;

import java.util.List;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 08/09/2019
 */

@NoArgsConstructor
@Setter
public class UserDao {

    private JdbcTemplate    jdbcTemplate;
    private RowMapper<User> userRowMapper;

    public User get(String id) {
        return jdbcTemplate.queryForObject("select * from users where id = ?",
                new Object[]{id},
                userRowMapper);
    }

    /* 클라이언트 : 전략 인터페이스인 StatementStrategy의 구현체를 컨텍스트로 주입 */
    public void deleteAll() {
        jdbcTemplate.update("delete from users");
    }

    public void add(final User user) throws DuplicateKeyException {
        jdbcTemplate.update("insert into users(id, name, password) values (?, ?, ?)",
                user.getId(), user.getName(), user.getPassword());
    }

//    public void add(final User user) throws DuplicateUserIdException {
//        try {
//            jdbcTemplate.update("insert into users(id, name, password) values (?, ?, ?)",
//                    user.getId(), user.getName(), user.getPassword());
//        } catch (DuplicateKeyException e) {
//            throw new DuplicateUserIdException(e);
//        }
//    }

    public int getCount() {
        return jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
    }

    public List<User> getAll() {
        return jdbcTemplate.query("select * from users order by id",
                userRowMapper);
    }
}
