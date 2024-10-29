package com.mycompany.monstersouls;

//IMPORTS
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseManager { //HANDLES DATABASE OPERATIONS
    private static final String DB_URL = "jdbc:derby://localhost:1527/MonsterSoulsDB";
    private static final String USER = "DB"; // DATABASE USERNAME
    private static final String PASSWORD = "DB"; // DATABASE PASSWORD
    private Connection connection;

    
     //CONSTRUCTOR
    public DatabaseManager(Connection connection) {
        this.connection = connection;
        
    }
    

    //PLAYERDATA
    public Player loadPlayerData() throws SQLException { //LOAD PLAYERDATA
        String query = "SELECT health, x, y, username FROM Player WHERE id = ?"; //SQL QUERY
        try (PreparedStatement statement = connection.prepareStatement(query)) { 
            statement.setInt(1, 1);//ID 1

            ResultSet rs = statement.executeQuery();
            if (rs.next()) { //LOADING VARIABLES
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

    public void savePlayerData(Player player) throws SQLException { //SAVE PLAYER DATA
        String checkSql = "SELECT COUNT(*) FROM PLAYERDATA WHERE PLAYER_USERNAME = ?"; //SQL QUERY
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, player.getUsername()); 
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            int count = rs.getInt(1);
            rs.close();

            if (count > 0) { //IF DATA EXISTS, UPDATE:
                String updateSql = "UPDATE PLAYERDATA SET X_POSITION = ?, Y_POSITION = ?, HEALTH = ? WHERE PLAYER_USERNAME = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, player.getX());
                    updateStmt.setInt(2, player.getY());
                    updateStmt.setInt(3, player.getHealth());
                    updateStmt.setString(4, player.getUsername());
                    updateStmt.executeUpdate();
                }
            } else { //IF NO DATA FOUND, INSERT:
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
    
    public Player checkAndLoadPlayer(String username) throws SQLException { //CHECKING IF DATA EXISTS AND LOADING
    String query = "SELECT HEALTH, X_POSITION, Y_POSITION FROM PLAYERDATA WHERE PLAYER_USERNAME = ?"; //SQL QUERY
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int health = rs.getInt("HEALTH");
                int x = rs.getInt("X_POSITION");
                int y = rs.getInt("Y_POSITION");

                Player player = new Player(x, y, health);
                player.setUsername(username); //SETTING USERNAME
                System.out.println("USERNAME SET");
                return player;
            }
            else {
            //INSERT PLAYER DATA IF IT DOESNT EXIST
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
    
    //ENEMIES
    public void backupEnemies(List<Enemy> enemies) throws SQLException {//SAVING ENEMIES TO DATABASE
         
        //SQL QUERIES
        String updateEnemyQuery = "UPDATE ENEMY SET HEALTH = ?, SPEED = ? WHERE ID = ?"; //UPDATE
        String insertEnemyQuery = "INSERT INTO ENEMY (TYPE, HEALTH, SPEED) VALUES (?, ?, ?)"; //INSERT

        try (PreparedStatement updateStmt = connection.prepareStatement(updateEnemyQuery);
            PreparedStatement insertStmt = connection.prepareStatement(insertEnemyQuery)) {

            for (Enemy enemy : enemies) {
                //ATTEMPING TO UPDATE ENEMIES
                updateStmt.setInt(1, enemy.getHealth());
                updateStmt.setInt(2, enemy.getSpeed());
                updateStmt.setInt(3, enemy.getId());
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) { 
                    //IF ENEMY DOESNT EXIST, INSERT ENEMIES:
                    insertStmt.setString(1, enemy.getType());
                    insertStmt.setInt(2, enemy.getHealth());
                    insertStmt.setInt(3, enemy.getSpeed());
                    insertStmt.executeUpdate();
                }
            }
        }
    }
 
    
    //HIGH SCORES
    public void saveHighScore(String username, int score) throws SQLException { //SAVE HIGHSCORE
        String selectQuery = "SELECT SCORE FROM HIGHSCORES WHERE USERNAME = ?"; //SQL QUERY TO CHECK FOR SCORES
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int existingScore = rs.getInt("SCORE"); //GET SCORE
                if (score > existingScore) { //UPDATING IF NEW SCORE IS HIGHER THAN EXISTING HIGHSCORE
                    String updateQuery = "UPDATE HIGHSCORES SET SCORE = ?, TIMESTAMP = CURRENT_TIMESTAMP WHERE USERNAME = ?"; //SQL QUERY TO UPDATE HIGHSCORE
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, score);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                    }
                }
            }
        
        else { //IF USER NAME DOESNT EXIST:
            String insertQuery = "INSERT INTO HIGHSCORES (USERNAME, SCORE, TIMESTAMP) VALUES (?, ?, CURRENT_TIMESTAMP)"; //SQL QUERYT TO INSERT HIGHSCORE
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setInt(2, score);
                insertStmt.executeUpdate();
                }
            }
        }
    }
    
    public int loadHighScore(String username) throws SQLException { //LOAD HIGHSCORES
        String query = "SELECT SCORE FROM HIGHSCORES WHERE USERNAME = ?";//SQL TO LOAD SCORES
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("SCORE"); //RETURN SCORE
            }
            else {
                return 0; //IF HIGHSCORE DOESNT EXIST RETURN 0
            }
        }
    }
    
    
    //GETTER
    public static Connection getConnection() throws SQLException { //GET CONNECTION
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
    //CREATE CONNECTION
    public static Connection createConnection() throws SQLException { //CREATE A CONNECTION
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
    
    public boolean isConnectionValid() {
    try {
        return connection != null && connection.isValid(2);  // Timeout of 2 seconds
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    
    public void reconnect() {
    if (!isConnectionValid()) {
        try {
            connection = createConnection();
            System.out.println("DatabaseManager: Reconnected to the database.");
        } catch (SQLException e) {
            System.out.println("DatabaseManager: Reconnection failed.");
            e.printStackTrace();
        }
    }
}

    
}
