import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.sql.*;

public class SQLManager {

    private DataSource dataSource;

    public SQLManager(String host, int port, String database, String username, String password) {
        MysqlDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
        mysqlDataSource.setServerName(host);
        mysqlDataSource.setPort(port);
        mysqlDataSource.setDatabaseName(database);
        mysqlDataSource.setUser(username);
        mysqlDataSource.setPassword(password);
        this.dataSource = mysqlDataSource;
        try {
            connect();
        } catch (SQLException e) {
            throw new RuntimeException("Could not establish database connection.", e);
        }
    }

    private void connect() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void createTable(String tableName, String columnsDefinition) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnsDefinition + ")";
            stmt.executeUpdate(sql);
        }
    }

    public void insertData(String tableName, String columnNames, String values) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + values + ")";
            stmt.executeUpdate(sql);
        }
    }

    public void updateData(String tableName, String setClause, String condition) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "UPDATE `" + tableName + "` SET " + setClause + " WHERE " + condition;
            stmt.executeUpdate(sql);
        }
    }

    public ResultSet getData(String tableName, String columns, String condition) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            Bukkit.getLogger().info("SELECT " + columns + " FROM " + tableName + " WHERE " + condition);
            String sql = "SELECT " + columns + " FROM " + tableName + " WHERE " + condition;
            return stmt.executeQuery(sql);
        }
    }

    public void deleteData(String tableName, String condition) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "DELETE FROM " + tableName + " WHERE " + condition;
            stmt.executeUpdate(sql);
        }
    }

    public ResultSet getAllData(String tableName) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM " + tableName;
            return stmt.executeQuery(sql);
        }
    }

    public boolean playerExists(String uuid) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM players WHERE uuid = ?")) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    public boolean playerSettingsExists(String uuid) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM psettings WHERE uuid = ?")) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    public ResultSet getDataWithPreparedStatement(String query, String... parameters) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setString(i + 1, parameters[i]);
            }
            return statement.executeQuery();
        }
    }

    public void updateDataWithPreparedStatement(String query, String... parameters) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setString(i + 1, parameters[i]);
            }
            statement.executeUpdate();
        }
    }
}
