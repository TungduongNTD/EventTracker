package server.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.model.Event;

public class ReminderService {
    private Connection dbConnection;
    private ScheduledExecutorService scheduler;
    private Properties dbProperties;
    
    public ReminderService() {
        try {
            loadDatabaseProperties();
            initializeDatabaseConnection();
            this.scheduler = Executors.newScheduledThreadPool(1);
            startReminderCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadDatabaseProperties() throws IOException {
        dbProperties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new IOException("Unable to find database.properties");
            }
            dbProperties.load(input);
        }
    }
    
    private void initializeDatabaseConnection() throws SQLException, ClassNotFoundException {
        Class.forName(dbProperties.getProperty("db.driver"));
        this.dbConnection = DriverManager.getConnection(
            dbProperties.getProperty("db.url"),
            dbProperties.getProperty("db.username"),
            dbProperties.getProperty("db.password")
        );
    }
    
    private void startReminderCheck() {
        // Check every hour for upcoming events
        scheduler.scheduleAtFixedRate(() -> {
            checkUpcomingEvents();
        }, 0, 1, TimeUnit.HOURS);
    }
    
    private void checkUpcomingEvents() {
        String sql = "SELECT * FROM events WHERE start_time BETWEEN ? AND ?";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(now));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(tomorrow));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getLong("id"));
                event.setTitle(rs.getString("title"));
                event.setDescription(rs.getString("description"));
                event.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                event.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                event.setLocation(rs.getString("location"));
                
                // Notification logic here
                System.out.println("Reminder: " + event.getTitle() + " is coming up on " + event.getStartTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (dbConnection != null) dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 