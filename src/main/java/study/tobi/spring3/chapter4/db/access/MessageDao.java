package study.tobi.spring3.chapter4.db.access;

import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.sql.DataSource;

/**
 * Created by Yoo Ju Jin(yjj@hanuritien.com)
 * Created Date : 2019-09-12
 */

@NoArgsConstructor
@Setter
public class MessageDao {

    private DataSource dataSource;
}
