package com.codepath.blip.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/20/16
 */

@ParseClassName("Blip")
public class Blip extends ParseObject implements ClusterItem {
    // Keys for object attributes
    public static final String LOCATION = "LOCATION";
    public static final String USER = "USER";
    private static final String IMAGE_FILE = "IMAGE_FILE";
    private static final String CAPTION = "CAPTION";
    private static final String UPVOTE = "UPVOTE";
    private static final String DOWNVOTE = "DOWNVOTE";

    /**
     * Default constructor for Parse - Do not remove or modify
     */
    public Blip() {
        super();
    }

    private Blip(String caption, ParseFile imageFile, ParseGeoPoint geoPoint) {
        put(IMAGE_FILE, imageFile);
        put(LOCATION, geoPoint);
        put(CAPTION, caption);
        put(UPVOTE, 0);
        put(DOWNVOTE, 0);
        if (ParseUser.getCurrentUser() != null) {
            put(USER, ParseUser.getCurrentUser());
        }
    }

    /**
     * Create and save a Blip on Parse. Returns the newly created Blip through an observable.
     * In the future, it may be the case that non-users cannot save a Blip. In that case, the Observable will error out.
     * @param caption A String caption
     * @param location A LatLng representing where the Blip was made.
     * @param image A string of bytes
     * @return An observable which will return a Blip if it was saved, or a ParseException if something went wrong.
     */
    public static rx.Observable<Blip> createBlip(final String caption, @NonNull final LatLng location, @NonNull final
    Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        final byte[] imageBytes = stream.toByteArray();
        return rx.Observable.create(new Observable.OnSubscribe<Blip>() {
            @Override
            public void call(Subscriber<? super Blip> subscriber) {
                try {
                    // Store image on Parse
                    ParseFile imageFile = new ParseFile("image.jpg", imageBytes);
                    imageFile.save();

                    // Create geopoint
                    ParseGeoPoint geoPoint = new ParseGeoPoint(location.latitude, location.longitude);

                    // Create Blip
                    Blip blip = new Blip(caption, imageFile, geoPoint);
                    blip.save();

                    // Return saved blip
                    subscriber.onNext(blip);
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public String getImageUri() {
        ParseFile imageFile = (ParseFile) get(IMAGE_FILE);
        return imageFile.getUrl();
    }

    public String getCaption() {
        return getString(CAPTION);
    }

    @Override
    public LatLng getPosition() {
        ParseGeoPoint geoPoint = (ParseGeoPoint) get(LOCATION);
        return new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public String getUuid() {
        return getObjectId();
    }

    public int getScore() {
        return getInt(UPVOTE) - getInt(DOWNVOTE);
    }

    public int getUpVotes() {
        return getInt(UPVOTE);
    }

    public int getDownVotes() {
        return getInt(DOWNVOTE);
    }

    public void upVoteBlip() {
        increment(UPVOTE);
        saveInBackground();
    }

    public void downVoteBlip() {
        increment(DOWNVOTE);
        saveInBackground();
    }

}
