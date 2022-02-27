package com.example.parksmart;

public class PaymentsObject {
    private int image;
    private String cost, timetotal, parkedSlot, date;

    public PaymentsObject(int image, String cost, String timetotal, String parkedSlot, String date) {
        this.image = image;
        this.cost = cost;
        this.timetotal = timetotal;
        this.parkedSlot = parkedSlot;
        this.date = date;
    }

    public int getImage() {
        return image;
    }

    public String getCost() {
        return cost;
    }

    public String getTimetotal() {
        return timetotal;
    }

    public String getParkedSlot() {
        return parkedSlot;
    }

    public String getDate() {
        return date;
    }
}
