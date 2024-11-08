package AndroidAssignment.notespro;

import com.google.firebase.Timestamp;

public class Note {
    String title;
    String content;
    Timestamp timestamp;
    private String dueDate;  // Format: YYYY-MM-DD
    private String dueTime;  // Format: HH:MM

    public Note(String title, String content, String dueDate, String dueTime, Timestamp timestamp) {
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.timestamp = timestamp;
    }

    public Note() { }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
