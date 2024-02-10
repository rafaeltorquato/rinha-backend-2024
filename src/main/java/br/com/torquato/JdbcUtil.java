package br.com.torquato;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbcUtil {
    public static void safeClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static void safeClose(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static void safeClose(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
