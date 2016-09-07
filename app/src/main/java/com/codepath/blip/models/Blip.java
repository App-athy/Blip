package com.codepath.blip.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class Blip extends ParseObject implements ClusterItem, Serializable {
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
    
    public rx.Observable<Blip> upvoteBlip() {
        final Blip self = this;
        return Observable.create(new Observable.OnSubscribe<Blip>() {
            @Override
            public void call(Subscriber<? super Blip> subscriber) {
                increment(UPVOTE);
                try {
                    save();
                    subscriber.onNext(self);
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public rx.Observable<Blip> downvoteBlip() {
        final Blip self = this;
        return Observable.create(new Observable.OnSubscribe<Blip>() {
            @Override
            public void call(Subscriber<? super Blip> subscriber) {
                increment(DOWNVOTE);
                try {
                    save();
                    subscriber.onNext(self);
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Given a list of known object ids, returns a list of Blips from cache.
     * @param objectIds List of known ids to be hydrated
     * @return List of Blips matching th ids passed in
     */
    public static rx.Observable<List<Blip>> fromIdList(final List<String> objectIds) {
        return Observable.create(new Observable.OnSubscribe<List<Blip>>() {
            @Override
            public void call(Subscriber<? super List<Blip>> subscriber) {
                List<Blip> blips = new ArrayList<>();
                for (String objectId : objectIds) {
                    ParseQuery<Blip> query = ParseQuery.getQuery(Blip.class);
                    query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                    try {
                        blips.add(query.get(objectId));
                    } catch (ParseException e) {
                        // Failed to retrieve a blip for some reason.
                        Log.e("Blip Retrieval Failure", "Failed to get a blip which should have been cached", e);
                    }
                }
                subscriber.onNext(blips);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static rx.Observable<Blip> fromId(final String objectId) {
        return Observable.create(new Observable.OnSubscribe<Blip>() {
            @Override
            public void call(Subscriber<? super Blip> subscriber) {
                ParseQuery<Blip> query = ParseQuery.getQuery(Blip.class);
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                try {
                    subscriber.onNext(query.get(objectId));
                } catch (ParseException e) {
                    // Failed to retrieve a blip for some reason.
                    Log.e("Blip Retrieval Failure", "Failed to get a blip which should have been cached", e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
