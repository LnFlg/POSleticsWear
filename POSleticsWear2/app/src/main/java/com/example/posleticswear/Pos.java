package com.example.posleticswear;

import android.location.Location;

import java.util.ArrayList;

public class Pos {




    Location loc= new Location("");
    private int id;
    private int upvotes;
    private ArrayList<String> highestHashtags=null;


    //warum wird die highest hashtag List nicht mit im Konstruktor gesetzt ?
    public Pos (double lat, double lng, int id, int upvotes){
        super();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        this.id=id;
        this.upvotes =upvotes;
    }
    public Pos (Location l, int id){
        this.loc=l;
        this.id=id;

        this.upvotes = 0;
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

    public void setUpvotes(){
        //aus Network Singelton ?
    }
    public int getUpvotes(){return upvotes;}





    public ArrayList<String> getHighestHashtags() {
        return highestHashtags;
    }

    public void setHighestHashtags(ArrayList<String> highestHashtags) {
        this.highestHashtags = highestHashtags;
    }

}