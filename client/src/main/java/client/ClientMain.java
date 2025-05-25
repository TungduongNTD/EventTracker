package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.io.File;
import java.io.IOException;

import common.model.Event;
import common.model.EventMedia;
import common.model.Request;
import common.model.Response;
import client.view.MainFrame;

public class ClientMain {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    
    public static void main(String[] args) {
        ClientMain client = new ClientMain();
        if (client.connectToServer()) {
            MainFrame mainFrame = new MainFrame(client);
            mainFrame.setVisible(true);
        } else {
            System.err.println("Failed to connect to server");
        }
    }
    
    public boolean connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Response sendRequest(Request request) {
        try {
            output.writeObject(request);
            output.flush();
            return (Response) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Communication error", null);
        }
    }
    
    public List<Event> getAllEvents() {
        Request request = new Request("GET_EVENTS", null);
        Response response = sendRequest(request);
        if (response.isSuccess()) {
            return (List<Event>) response.getData();
        }
        return null;
    }
    
    public Event addNewEvent(Event event) {
        Request request = new Request("ADD_EVENT", event);
        Response response = sendRequest(request);
        if (response.isSuccess()) {
            return (Event) response.getData();
        }
        return null;
    }
    
    public List<Event> getEvents() throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("GET_EVENTS", null));
        if (response.isSuccess()) {
            return (List<Event>) response.getData();
        }
        throw new IOException(response.getMessage());
    }

    public Event getEvent(int eventId) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("GET_EVENT", eventId));
        if (response.isSuccess()) {
            return (Event) response.getData();
        }
        throw new IOException(response.getMessage());
    }

    public void addEvent(Event event) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("ADD_EVENT", event));
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    public void updateEvent(Event event) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("UPDATE_EVENT", event));
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    public void deleteEvent(int eventId) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("DELETE_EVENT", eventId));
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    public void uploadMedia(int eventId, File file) throws IOException, ClassNotFoundException {
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        EventMedia media = new EventMedia();
        media.setEventId(eventId);
        media.setFileName(file.getName());
        media.setMediaType(getFileType(file));
        media.setFileData(fileBytes);
        
        Response response = sendRequest(new Request("UPLOAD_MEDIA", media));
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    public List<EventMedia> getEventMedia(int eventId) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("GET_EVENT_MEDIA", eventId));
        if (response.isSuccess()) {
            return (List<EventMedia>) response.getData();
        }
        throw new IOException(response.getMessage());
    }

    public void viewMedia(int mediaId) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("GET_MEDIA", mediaId));
        if (response.isSuccess()) {
            EventMedia media = (EventMedia) response.getData();
            // Save to temp file and open
            File tempFile = File.createTempFile("media_", media.getFileName());
            java.nio.file.Files.write(tempFile.toPath(), media.getFileData());
            java.awt.Desktop.getDesktop().open(tempFile);
        } else {
            throw new IOException(response.getMessage());
        }
    }

    public void shareEvent(int eventId, String email) throws IOException, ClassNotFoundException {
        Response response = sendRequest(new Request("SHARE_EVENT", new Object[]{eventId, email}));
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    private String getFileType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif")) {
            return "IMAGE";
        } else if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mov")) {
            return "VIDEO";
        } else {
            return "OTHER";
        }
    }

    public void close() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Add other client methods...
}
