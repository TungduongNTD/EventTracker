package client.view;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import client.ClientMain;
import common.model.Event;
import client.view.AddEventDialog;
import common.model.Response;

public class MainFrame extends JFrame {
    private final ClientMain client;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton refreshButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton uploadButton;
    private JButton viewMediaButton;
    private JButton shareButton;

    public MainFrame(ClientMain client) {
        this.client = client;
        initializeUI();
        loadEvents();
    }

    private void initializeUI() {
        setTitle("Event Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create table model
        String[] columns = {"ID", "Title", "Description", "Start Time", "End Time", "Location"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        eventTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(eventTable);

        // Create buttons
        addButton = new JButton("Add Event");
        refreshButton = new JButton("Refresh");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        uploadButton = new JButton("Upload Media");
        viewMediaButton = new JButton("View Media");
        shareButton = new JButton("Share");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(viewMediaButton);
        buttonPanel.add(shareButton);

        // Add components to frame
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add event listeners
        addButton.addActionListener(e -> showAddEventDialog());
        refreshButton.addActionListener(e -> loadEvents());
        editButton.addActionListener(e -> showEditEventDialog());
        deleteButton.addActionListener(e -> deleteSelectedEvent());
        uploadButton.addActionListener(e -> uploadMediaForSelectedEvent());
        viewMediaButton.addActionListener(e -> showMediaListForSelectedEvent());
        shareButton.addActionListener(e -> shareSelectedEvent());

        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // TODO: Implement cleanup if needed
            }
        });
    }

    private void loadEvents() {
        List<Event> events = client.getAllEvents();
        if (events != null) {
            tableModel.setRowCount(0);
            for (Event event : events) {
                Object[] row = {
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getStartTime(),
                        event.getEndTime(),
                        event.getLocation()
                };
                tableModel.addRow(row);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to load events",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog(this, true);
        dialog.setVisible(true);
        if (dialog.isSubmitted()) {
            Event newEvent = dialog.getEvent();
            Event addedEvent = client.addNewEvent(newEvent);
            if (addedEvent != null) {
                loadEvents();
                JOptionPane.showMessageDialog(this, "Event added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add event.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditEventDialog() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long eventId = (Long) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        String description = (String) tableModel.getValueAt(selectedRow, 2);
        java.time.LocalDateTime startTime = (java.time.LocalDateTime) tableModel.getValueAt(selectedRow, 3);
        java.time.LocalDateTime endTime = (java.time.LocalDateTime) tableModel.getValueAt(selectedRow, 4);
        String location = (String) tableModel.getValueAt(selectedRow, 5);

        Event event = new Event();
        event.setId(eventId);
        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setLocation(location);

        AddEventDialog dialog = new AddEventDialog(this, true, event);
        dialog.setVisible(true);
        if (dialog.isSubmitted()) {
            Event updatedEvent = dialog.getEvent();
            client.sendRequest(new common.model.Request("UPDATE_EVENT", updatedEvent));
            loadEvents();
        }
    }

    private void deleteSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long eventId = (Long) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this event?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            client.sendRequest(new common.model.Request("DELETE_EVENT", eventId));
            loadEvents();
        }
    }

    private void uploadMediaForSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to upload media.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long eventId = (Long) tableModel.getValueAt(selectedRow, 0);
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String type = getFileType(file);
            String description = JOptionPane.showInputDialog(this, "Enter media description:");
            try {
                byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
                common.model.EventMedia media = new common.model.EventMedia();
                media.setId(eventId); // id ở đây là eventId
                media.setMediaType(type);
                media.setDescription(description);
                media.setFileName(file.getName());
                common.model.Request request = new common.model.Request("UPLOAD_MEDIA", new Object[]{media, fileBytes});
                common.model.Response response = client.sendRequest(request);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Upload thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Upload thất bại: " + response.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi đọc file: " + ex.getMessage());
            }
        }
    }

    private String getFileType(java.io.File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif")) {
            return "IMAGE";
        } else if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mov")) {
            return "VIDEO";
        } else {
            return "OTHER";
        }
    }

    private void showMediaListForSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to view media.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long eventId = (Long) tableModel.getValueAt(selectedRow, 0);
        Response response = client.sendRequest(new common.model.Request("GET_EVENT_MEDIA", eventId));
        if (response.isSuccess()) {
            java.util.List<common.model.EventMedia> mediaList = (java.util.List<common.model.EventMedia>) response.getData();
            if (mediaList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No media found for this event.");
                return;
            }
            // Hiển thị danh sách media
            String[] mediaNames = mediaList.stream().map(m -> m.getFileName()).toArray(String[]::new);
            String selected = (String) JOptionPane.showInputDialog(this, "Select media to open:", "Media List",
                    JOptionPane.PLAIN_MESSAGE, null, mediaNames, mediaNames[0]);
            if (selected != null) {
                // Mở file bằng chương trình mặc định của hệ điều hành
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(selected));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Không thể mở file: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi tải media: " + response.getMessage());
        }
    }

    private void shareSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event to share.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long eventId = (Long) tableModel.getValueAt(selectedRow, 0);
        String email = JOptionPane.showInputDialog(this, "Enter recipient's email:");
        if (email == null || email.trim().isEmpty()) return;
        Event event = new Event();
        event.setId(eventId);
        event.setTitle((String) tableModel.getValueAt(selectedRow, 1));
        event.setDescription((String) tableModel.getValueAt(selectedRow, 2));
        event.setStartTime((java.time.LocalDateTime) tableModel.getValueAt(selectedRow, 3));
        event.setEndTime((java.time.LocalDateTime) tableModel.getValueAt(selectedRow, 4));
        event.setLocation((String) tableModel.getValueAt(selectedRow, 5));
        Response response = client.sendRequest(new common.model.Request("SHARE_EVENT", new Object[]{event, email}));
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Đã gửi email thành công!");
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi gửi email: " + response.getMessage());
        }
    }
} 