package com.example.posleticswear;

import android.location.Location;

import java.util.ArrayList;

public class Pos {




    Location loc= new Location("");
    private int id;
    private ArrayList<String> highestHashtags=null;

    public Pos (double lat, double lng, int id){
        super();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        this.id=id;
    }
    public Pos (Location l, int id){
        this.loc=l;
        this.id=id;
    }

    public boolean equals(Pos pos) {
        if(pos.getId()==this.id){return true;}else{return false;}
    }
    public boolean equals(Location l){
        if(
                Math.abs(l.getLatitude()-loc.getLatitude())<=15.0
                && Math.abs(l.getLongitude()-loc.getLongitude())<=15.0)
        {
            return true;
        }else {return false;}
    }

    public double getLat() {
        return loc.getLatitude();
    }

    public void setLat(double lat) {
        loc.setLatitude(lat);
    }

    public double getLng() {
        return loc.getLongitude();
    }

    public void setLng(double lng) {
        loc.setLongitude(lng);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }


    public ArrayList<String> getHighestHashtags() {
        return highestHashtags;
    }

    public void setHighestHashtags(ArrayList<String> highestHashtags) {
        this.highestHashtags = highestHashtags;
    }

}
