package com.codepath.blip.models;

import com.parse.ParseGeoPoint;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/20/16
 */
public class Blip {

    private String pictureUrl;
    private String caption;
    private ParseGeoPoint location;
    private int upvotes;

    public Blip() {}

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getCaption() {
        return caption;
    }

    public ParseGeoPoint getLocation() {
        return location;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void upVoteBlip() {
        upvotes++;
        //update upvotes in db
    }

    public void downVoteBlip() {
        upvotes--;
        //update upvotes in db
    }

}
