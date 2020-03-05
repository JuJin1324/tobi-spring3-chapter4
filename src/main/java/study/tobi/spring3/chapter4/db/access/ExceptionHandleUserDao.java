package study.tobi.spring3.chapter4.db.access;

import com.mysql.jdbc.MysqlErrorNumbers;
import lombok.Cleanup;
import lombok.Setter;
import org.springframework.dao.EmptyResultDataAccessException;
import study.tobi.spring3.chapter4.db.entity.User;
import study.tobi.spring3.chapter4.exception.DuplicateUserIdException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yoo Ju Jin(jujin1324@daum.net)
 * Created Date : 2020/02/25
 */

@Setter
public class ExceptionHandleUserDao {
    /* setter를 통한 DI */
    private DataSource dataSource;

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

    public User get(String id) throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("select id, name, password from users where id = ?");
        ps.setString(1, id);

        @Cleanup
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        if (user == null) throw new EmptyResultDataAccessException(1);

        return user;
    }

    public void deleteAll() throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("delete from users");
        ps.executeUpdate();
    }

    public int getCount() throws SQLException {
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("select count(*) from users");

        @Cleanup
        ResultSet rs = ps.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        return count;
    }

    public List<User> getAll() throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        @Cleanup
        Connection c = dataSource.getConnection();

        @Cleanup
        PreparedStatement ps = c.prepareStatement("select id, name, password from users");

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));

            users.add(user);
        }

        return users;
    }
}
