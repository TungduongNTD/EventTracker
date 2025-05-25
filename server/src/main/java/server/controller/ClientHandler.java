package server.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import common.model.Event;
import common.model.EventMedia;
import common.model.EventNote;
import common.model.Request;
import common.model.Response;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Connection dbConnection;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.dbConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/event_tracker", 
                "root", 
                "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Request request = (Request) input.readObject();
                Response response = handleRequest(request);
                output.writeObject(response);
                output.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (clientSocket != null) clientSocket.close();
                if (dbConnection != null) dbConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Response handleRequest(Request request) {
        switch (request.getAction()) {
            case "ADD_EVENT":
                return addEvent(request);
            case "GET_EVENTS":
                return getEvents();
            case "ADD_EVENT_MEDIA":
                return addEventMedia(request);
            case "ADD_EVENT_NOTE":
                return addEventNote(request);
            case "GET_UPCOMING_EVENTS":
                return getUpcomingEvents();
            case "UPDATE_EVENT":
                return updateEvent(request);
            case "DELETE_EVENT":
                return deleteEvent(request);
            case "UPLOAD_MEDIA":
                return uploadMedia(request);
            case "GET_EVENT_MEDIA":
                return getEventMedia(request);
            case "SHARE_EVENT":
                return shareEvent(request);
            default:
                return new Response(false, "Invalid action", null);
        }
    }

    private Response addEvent(Request request) {
        Event event = (Event) request.getData();
        String sql = "INSERT INTO events (title, description, start_time, end_time, location) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setObject(3, event.getStartTime());
            stmt.setObject(4, event.getEndTime());
            stmt.setString(5, event.getLocation());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return new Response(false, "Failed to add event", null);
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getLong(1));
                    return new Response(true, "Event added successfully", event);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response getEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY start_time DESC";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getLong("id"));
                event.setTitle(rs.getString("title"));
                event.setDescription(rs.getString("description"));
                event.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                event.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                event.setLocation(rs.getString("location"));
                events.add(event);
            }
            return new Response(true, "Events retrieved successfully", events);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response addEventMedia(Request request) {
        EventMedia media = (EventMedia) request.getData();
        String sql = "INSERT INTO event_media (event_id, type, url, description) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, media.getId());
            stmt.setString(2, media.getMediaType());
            stmt.setString(3, media.getFileName());
            stmt.setString(4, media.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return new Response(false, "Failed to add media", null);
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    media.setId(generatedKeys.getLong(1));
                    return new Response(true, "Media added successfully", media);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response addEventNote(Request request) {
        EventNote note = (EventNote) request.getData();
        String sql = "INSERT INTO event_notes (event_id, content) VALUES (?, ?)";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, note.getId());
            stmt.setString(2, note.getContent());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return new Response(false, "Failed to add note", null);
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    note.setId(generatedKeys.getLong(1));
                    return new Response(true, "Note added successfully", note);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response getUpcomingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE start_time > NOW() ORDER BY start_time ASC";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getLong("id"));
                event.setTitle(rs.getString("title"));
                event.setDescription(rs.getString("description"));
                event.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                event.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                event.setLocation(rs.getString("location"));
                events.add(event);
            }
            return new Response(true, "Upcoming events retrieved successfully", events);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response updateEvent(Request request) {
        Event event = (Event) request.getData();
        String sql = "UPDATE events SET title=?, description=?, start_time=?, end_time=?, location=? WHERE id=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDescription());
            stmt.setObject(3, event.getStartTime());
            stmt.setObject(4, event.getEndTime());
            stmt.setString(5, event.getLocation());
            stmt.setLong(6, event.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return new Response(true, "Event updated successfully", event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response deleteEvent(Request request) {
        Long eventId = (Long) request.getData();
        String sql = "DELETE FROM events WHERE id=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setLong(1, eventId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return new Response(true, "Event deleted successfully", eventId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "Database error", null);
    }

    private Response uploadMedia(Request request) {
        Object[] data = (Object[]) request.getData();
        EventMedia media = (EventMedia) data[0];
        byte[] fileBytes = (byte[]) data[1];
        String uploadsDir = "server/resources/uploads/";
        java.io.File dir = new java.io.File(uploadsDir);
        if (!dir.exists()) dir.mkdirs();
        String filePath = uploadsDir + media.getFileName();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
            fos.write(fileBytes);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return new Response(false, "Lỗi lưu file", null);
        }
        String sql = "INSERT INTO event_media (event_id, type, url, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, media.getId());
            stmt.setString(2, media.getMediaType());
            stmt.setString(3, filePath);
            stmt.setString(4, media.getDescription());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return new Response(false, "Failed to add media", null);
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    media.setId(generatedKeys.getLong(1));
                    return new Response(true, "Media uploaded successfully", media);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(false, "Database error", null);
        }
        return new Response(false, "Unknown error", null);
    }

    private Response getEventMedia(Request request) {
        Long eventId = (Long) request.getData();
        List<EventMedia> mediaList = new ArrayList<>();
        String sql = "SELECT * FROM event_media WHERE event_id=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setLong(1, eventId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EventMedia media = new EventMedia();
                media.setId(rs.getLong("id"));
                media.setMediaType(rs.getString("type"));
                media.setFileName(rs.getString("url"));
                media.setDescription(rs.getString("description"));
                mediaList.add(media);
            }
            return new Response(true, "Media list loaded", mediaList);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(false, "Database error", null);
        }
    }

    private Response shareEvent(Request request) {
        Object[] data = (Object[]) request.getData();
        Event event = (Event) data[0];
        String email = (String) data[1];
        // Cấu hình email server (ví dụ dùng Gmail SMTP)
        String host = "smtp.gmail.com";
        String from = "tungduongtn2003@gmail.com";
        String pass = "jfev airt boog gytv";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(from, pass);
            }
        });
        try {
            jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(session);
            message.setFrom(new jakarta.mail.internet.InternetAddress(from));
            message.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(email));
            message.setSubject("Chia sẻ sự kiện: " + event.getTitle());
            String content = "Tên sự kiện: " + event.getTitle() + "\n"
                    + "Thời gian: " + event.getStartTime() + " - " + event.getEndTime() + "\n"
                    + "Địa điểm: " + event.getLocation() + "\n"
                    + "Mô tả: " + event.getDescription();
            message.setText(content);
            jakarta.mail.Transport.send(message);
            return new Response(true, "Đã gửi email thành công!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi gửi email: " + e.getMessage(), null);
        }
    }
} 