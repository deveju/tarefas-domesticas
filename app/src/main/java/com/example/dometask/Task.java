package com.example.dometask;
public class Task {
    private String title;
    private String description;
    private int id;
    private boolean status;

    // Construtor vazio (para o FireBase)
    public Task() {
    }

    public Task(String title, String description, int id, boolean status) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
    }

    // Getters e Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean getStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
}
