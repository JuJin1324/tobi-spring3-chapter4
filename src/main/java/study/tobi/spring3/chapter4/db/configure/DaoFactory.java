package study.tobi.spring3.chapter4.db.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import study.tobi.spring3.chapter4.db.access.AccountDao;
import study.tobi.spring3.chapter4.db.access.ExceptionHandleUserDao;
import study.tobi.spring3.chapter4.db.access.MessageDao;
import study.tobi.spring3.chapter4.db.access.UserDao;
import study.tobi.spring3.chapter4.db.entity.User;

import javax.sql.DataSource;
import javax.swing.plaf.PanelUI;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-12
 */

@Configuration
public class DaoFactory {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/spring3?useSSL=false";

    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setJdbcTemplate(jdbcTemplate());
        userDao.setUserRowMapper(userRowMapper());

        return userDao;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public ExceptionHandleUserDao exceptionHandleUserDao() {
        ExceptionHandleUserDao exceptionHandleUserDao = new ExceptionHandleUserDao();
        exceptionHandleUserDao.setDataSource(dataSource());

        return exceptionHandleUserDao;
    }

    @Bean
    public RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
            return user;
        };
    }

    @Bean
    public AccountDao accountDao() {
        AccountDao accountDao = new AccountDao();
        accountDao.setDataSource(dataSource());

        return accountDao;
    }

    @Bean
    public MessageDao messageDao() {
        MessageDao messageDao = new MessageDao();
        messageDao.setDataSource(dataSource());

        return messageDao;
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

        dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        dataSource.setUrl(MYSQL_URL);
        dataSource.setUsername("scott");
        dataSource.setPassword("tiger");

        return dataSource;
    }
}
