package common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private List<EventMedia> media;
    private List<EventNote> notes;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<EventMedia> getMedia() {
        return media;
    }

    public void setMedia(List<EventMedia> media) {
        this.media = media;
    }

    public List<EventNote> getNotes() {
        return notes;
    }

    public void setNotes(List<EventNote> notes) {
        this.notes = notes;
    }
} 