package com.example.parksmart;

public class Slots {
    private boolean Available;
    private int ID;
    private boolean Reverse;

    public boolean isAvailable() {
        return Available;
    }

    public void setAvailable(boolean available) {
        Available = available;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public boolean isReverse() {
        return Reverse;
    }

    public void setReverse(boolean reverse) {
        Reverse = reverse;
    }
}
