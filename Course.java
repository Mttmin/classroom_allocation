import java.util.ArrayList;
import java.util.List;

class Course {
    private String name;
    private int cohortSize;
    private List<RoomType> typePreferences;
    private String assignedRoom;
    private int choicenumber; // Added choice number for the room allocation

    public Course(String name, int cohortSize) {
        this.name = name;
        this.cohortSize = cohortSize;
        this.typePreferences = new ArrayList<>();
        this.assignedRoom = null;
        this.choicenumber = 0;
    }

    public void setTypePreferences(List<RoomType> preferences) {
        this.typePreferences = new ArrayList<>(preferences);
    }

    public String getName() { return name; }
    public int getCohortSize() { return cohortSize; }
    public List<RoomType> getTypePreferences() { return typePreferences; }
    public String getAssignedRoom() { return assignedRoom; }
    public void setAssignedRoom(String room) { this.assignedRoom = room; }
    public int getChoiceNumber() { return choicenumber; }
    public void incrementChoiceNumber() { this.choicenumber++; }

    @Override
    public String toString() {
        return name + " (Size: " + cohortSize + ")" ;
    }
}

