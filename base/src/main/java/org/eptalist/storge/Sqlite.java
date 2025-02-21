package org.eptalist.storge;

import io.github.p2vman.lang.Lang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sqlite implements Data<String> {

    public Connection connection;
    public Map<String, Object> data;
    public String teable;
    public String i;
    public Sqlite(Map<String, Object> data) {
        this.data = data;
        final String teable_profix = data.containsKey("prefix") ? "eptalist" : (String) data.get("prefix");
        this.teable = data.containsKey("teable") ? teable_profix+"_users" : teable_profix+data.get("teable");
        this.i = data.containsKey("t") ? "username" : (String) data.get("t");

        try {
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", (String) this.data.get("file")));
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS "+this.teable+" ("+this.i+" VARCHAR(255) PRIMARY KEY)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeUser(String name) {
        if (!is(name)) {
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM "+this.teable+" WHERE "+this.i+" = ?");
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
            info.add(Lang.LANG.format("storge.remove.not.in", name));
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM "+this.teable+" WHERE "+this.i+" = ?");
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            info.add(Lang.LANG.format("err.db"));
            return false;
        }
    }

    @Override
    public boolean is(String name, List<String> info) {
        try {
            if (connection.isClosed()) {
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", (String) this.data.get("file")));
                info.add(Lang.LANG.format("storge.reconnect"));
            }
            PreparedStatement statement = connection.prepareStatement("SELECT "+this.i+" FROM "+this.teable+" WHERE "+this.i+" = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            info.add(Lang.LANG.format("err.db"));
            return false;
        }
    }

    @Override
    public boolean is(String name) {
        try {
            if (connection.isClosed()) {
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", (String) this.data.get("file")));
            }
            PreparedStatement statement = connection.prepareStatement("SELECT "+this.i+" FROM "+this.teable+" WHERE "+this.i+" = ?");
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
            info.add(Lang.LANG.format("storge.add.is.already", name));
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO "+this.teable+" ("+this.i+") VALUES (?)");
            statement.setString(1, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            info.add(Lang.LANG.format("err.db"));
            return false;
        }
    }

    @Override
    public boolean addUser(String name) {
        if (is(name)) {
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO "+this.teable+" ("+this.i+") VALUES (?)");
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
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s.db", (String) this.data.get("file")));
            }
            String sql = "SELECT "+this.i+" FROM "+this.teable;
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                players.add(rs.getString(this.i));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
}