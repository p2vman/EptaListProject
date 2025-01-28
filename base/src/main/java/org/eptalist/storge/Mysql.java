package org.eptalist.storge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Mysql implements Data<String> {
    public Connection connection;
    public Map<String, Object> data;
    public Mysql(Map<String, Object> data) {
        this.data = data;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection((String) this.data.get("file"));
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (username VARCHAR(255) PRIMARY KEY)");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeUser(String name) {
        if (!is(name)) {
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE username = ?");
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeUser(String name, List<String> info) {
        if (!is(name)) {
            info.add("&r" + name + "is not in the whitelist");
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE username = ?");
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            info.add("&rThere was an error in the database");
            return false;
        }
    }

    @Override
    public boolean is(String name, List<String> info) {
        try {
            if (connection.isClosed()) {
                connection = DriverManager.getConnection(String.format((String) this.data.get("file")));
                info.add("&6The database has been reconnected");
            }
            PreparedStatement statement = connection.prepareStatement("SELECT user_name FROM users WHERE username = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            info.add("&rThere was an error in the database");
            return false;
        }
    }

    @Override
    public boolean is(String name) {
        try {
            if (connection.isClosed()) {
                connection = DriverManager.getConnection((String) this.data.get("file"));
            }
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM users WHERE username = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addUser(String name, List<String> info) {
        if (is(name)) {
            info.add("&r" + name + "is already on the whitelist");
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username) VALUES (?)");
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            info.add("&rThere was an error in the database");
            return false;
        }
    }

    @Override
    public boolean addUser(String name) {
        if (is(name)) {
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (username) VALUES (?)");
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null) {
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> toList() {

        List<String> players = new ArrayList<>();
        try {
            if (connection.isClosed()) {
                connection = DriverManager.getConnection((String) this.data.get("file"));
            }
            String sql = "SELECT username FROM users";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                players.add(rs.getString("username"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
}
