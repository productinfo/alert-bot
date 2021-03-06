package com.wire.bots.alert;

import com.wire.bots.alert.model.Config;
import com.wire.bots.sdk.Configuration;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Database {
    private final Config conf;

    Database(Config conf) {
        this.conf = conf;
    }

    boolean insertSubscriber(String botId, String convId) throws Exception {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("INSERT INTO Alert (botId, conversationId, serviceId) VALUES (?, ?, ?)");
            stmt.setObject(1, UUID.fromString(botId));
            stmt.setObject(2, UUID.fromString(convId));
            stmt.setObject(3, conf.getServiceId());
            return stmt.executeUpdate() == 1;
        }
    }

    ArrayList<String> getSubscribers() throws Exception {
        ArrayList<String> ret = new ArrayList<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT botId FROM Alert");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                ret.add(resultSet.getString("botId"));
            }
        }
        return ret;
    }

    ArrayList<String> getMySubscribers() throws Exception {
        ArrayList<String> ret = new ArrayList<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT botId FROM Alert WHERE serviceId = ?");
            stmt.setObject(1, conf.getServiceId());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                ret.add(resultSet.getString("botId"));
            }
        }
        return ret;
    }

    boolean unsubscribe(String botId) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM Alert WHERE botId = ?");
            stmt.setObject(1, UUID.fromString(botId));
            return stmt.executeUpdate() == 1;
        }
    }

    String getConversationId(String botId) throws Exception {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT conversationId FROM Alert WHERE botId = ?");
            stmt.setObject(1, UUID.fromString(botId));
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("conversationId");
            }
        }
        return null;
    }

    boolean insertAnnotation(String botId, String key, String value) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("INSERT INTO Annotations (botId, key, value) VALUES (?, ?, ?)");
            stmt.setObject(1, UUID.fromString(botId));
            stmt.setString(2, key);
            stmt.setString(3, value);
            return stmt.executeUpdate() == 1;
        }
    }

    Map<String, String> getAnnotations(String botId) throws Exception {
        HashMap<String, String> ret = new HashMap<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT key, value FROM Annotations WHERE botId = ?");
            stmt.setObject(1, UUID.fromString(botId));
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String value = resultSet.getString("value");
                String key = resultSet.getString("key");
                ret.put(key, value);
            }
        }
        return ret;
    }

    private Connection newConnection() throws SQLException {
        Configuration.DB postgres = conf.getPostgres();
        String url = String.format("jdbc:%s://%s:%d/%s", postgres.driver, postgres.host, postgres.port, postgres.database);
        return DriverManager.getConnection(url, postgres.user, postgres.password);
    }

    boolean removeAnnotation(String botId, String key, String value) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM Annotations WHERE botId = ? AND key = ? AND value = ?");
            stmt.setObject(1, UUID.fromString(botId));
            stmt.setString(2, key);
            stmt.setString(3, value);
            return stmt.executeUpdate() == 1;
        }
    }
}
