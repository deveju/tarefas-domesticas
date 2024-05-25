package com.example.dometask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private String roomId;
    private String roomName;
    private String creatorId;
    private List<Task> tasks;

    public Room() {
        // Construtor vazio pra inicialização do Firebase
    }

    public Room(String roomId, String roomName, String creatorId, List<Task> tasks) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.creatorId = creatorId;
        this.tasks = tasks;
    }

    // Getters e Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
