package cassdemo.backend;

public class Room {

    public Room() {
    }

    public Room(int roomId, String user, int size) {
        this.roomId = roomId;
        this.user = user;
        this.size = size;
    }

    private int roomId;
    private String user;
    private int size;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}