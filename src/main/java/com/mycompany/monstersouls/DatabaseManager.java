package com.mycompany.monstersouls;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:derby://localhost:1527/MonsterSoulsDB";
    private static final String USER = "DB"; // DATABASE USERNAME
    private static final String PASSWORD = "DB"; // DATABASE PASSWORD
    private Connection connection;

    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public Player loadPlayerData() throws SQLException {
        String query = "SELECT health, x, y, username FROM Player WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, 1);//ID 1

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int health = rs.getInt("health");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                String username = rs.getString("username");

                Player player = new Player(x, y, health);
                player.setUsername(username);

                return player;
            } else {
                return new Player(0, 0, 100); //DEFAULT VALUES IN CASE NO PLAYER DATA FOUND
            }
        }
    }

    public void savePlayerData(Player player) throws SQLException { //SAVING PLAYER DATA
        String checkSql = "SELECT COUNT(*) FROM PLAYERDATA WHERE PLAYER_USERNAME = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, player.getUsername());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            int count = rs.getInt(1);
            rs.close();

            if (count > 0) {
                String updateSql = "UPDATE PLAYERDATA SET X_POSITION = ?, Y_POSITION = ?, HEALTH = ? WHERE PLAYER_USERNAME = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, player.getX());
                    updateStmt.setInt(2, player.getY());
                    updateStmt.setInt(3, player.getHealth());
                    updateStmt.setString(4, player.getUsername());
                    updateStmt.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO PLAYERDATA (PLAYER_USERNAME, X_POSITION, Y_POSITION, HEALTH) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setString(1, player.getUsername());
                    insertStmt.setInt(2, player.getX());
                    insertStmt.setInt(3, player.getY());
                    insertStmt.setInt(4, player.getHealth());
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public void backupEnemies(List<Enemy> enemies) throws SQLException {//SAVING ENEMIES TO DATABASE
    String updateEnemyQuery = "UPDATE ENEMY SET HEALTH = ?, SPEED = ? WHERE ID = ?";
    String insertEnemyQuery = "INSERT INTO ENEMY (TYPE, HEALTH, SPEED) VALUES (?, ?, ?)";

    try (PreparedStatement updateStmt = connection.prepareStatement(updateEnemyQuery);
         PreparedStatement insertStmt = connection.prepareStatement(insertEnemyQuery)) {

        for (Enemy enemy : enemies) {
            // Attempt to update existing enemy
            updateStmt.setInt(1, enemy.getHealth());
            updateStmt.setInt(2, enemy.getSpeed());
            updateStmt.setInt(3, enemy.getId());
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected == 0) { 
                // If no rows were updated, it means this enemy does not exist in the DB
                // Insert new enemy, do not include ID, let the DB handle it
                insertStmt.setString(1, enemy.getType());
                insertStmt.setInt(2, enemy.getHealth());
                insertStmt.setInt(3, enemy.getSpeed());
                insertStmt.executeUpdate();
            }
        }
    }
}



    public static Connection getConnection() throws SQLException { //GET CONNECTION
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
    
    public static Connection createConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, USER, PASSWORD);
}
    
    public Player checkAndLoadPlayer(String username) throws SQLException {
    String query = "SELECT HEALTH, X_POSITION, Y_POSITION FROM PLAYERDATA WHERE PLAYER_USERNAME = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            int health = rs.getInt("HEALTH");
            int x = rs.getInt("X_POSITION");
            int y = rs.getInt("Y_POSITION");

            Player player = new Player(x, y, health);
            player.setUsername(username); // Set the username separately
            return player;
        } else {
            // Insert new player data with specified starting coordinates if the username doesn't exist
            String insertQuery = "INSERT INTO PLAYERDATA (PLAYER_USERNAME, HEALTH, X_POSITION, Y_POSITION) VALUES (?, 100, 400, 300)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.executeUpdate();
            }

            // Return player with starting coordinates
            Player newPlayer = new Player(400, 300, 100);
            newPlayer.setUsername(username);
            return newPlayer;
        }
    }
}


}
