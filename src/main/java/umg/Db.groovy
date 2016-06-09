package umg

import groovy.sql.Sql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/**
 * Created by richard on 25/05/2016.
 */
class Db {
    static Sql sql;
    Connection conn

    public void init() {
        try {
//            Map db = [url:'jdbc:hsqldb:/Users/richard/worksapce\\workspace_spring/spring_boot_enterprise_services/db/tracking;hsqldb.write_delay=false',
//                      user:'', password:'', driver:'org.hsqldb.jdbc.JDBCDriver']
//            sql = Sql.newInstance(db.url, db.user, db.password, db.driver)

            Connection conn = null;
            Properties connectionProps = new Properties();
            connectionProps.put("user", "");
            connectionProps.put("password", "");

            conn = DriverManager.getConnection(
                    'jdbc:hsqldb:/Users/richard/worksapce\\workspace_spring/spring_boot_enterprise_services/db/tracking;hsqldb.write_delay=false',
                    connectionProps);

            sql = new Sql(conn)

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void dropTable() {
    }

    public void getTracking(String resourceId) {
        rs = stmt.executeQuery("select * from word");
        while (rs.next()) {
            System.out.println("word id: " + rs.getLong(1) +
                    " spelling: " + rs.getString(2) +
                    " part of speech: " + rs.getString(3));
        }
    }
}
