package com.codepath.blip.clients;

import android.content.Context;

import com.codepath.blip.R;
import com.codepath.blip.models.Blip;
import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.interceptors.ParseLogInterceptor;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
public class BackendClient {
    private BehaviorSubject<List<Blip>> mNearbyBlipsSubject;

    /**
     * Initialize Parse and the Blips BehaviorSubjects.
     * @param applicationContext Context passed in during app initialization.
     */
    public BackendClient(Context applicationContext) {
        ParseObject.registerSubclass(Blip.class);
        Parse.initialize(new Parse.Configuration.Builder(applicationContext)
                .applicationId(applicationContext.getResources().getString(R.string.parse_app_id))
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server(applicationContext.getResources().getString(R.string.parse_url)).build());
        mNearbyBlipsSubject = BehaviorSubject.create();
    }

    /**
     * Returns the Observable for nearby Blips, configured to observe on the Main Thread.
     * Note that this is a HOT observable, and will immediately return the last published value on subscribe.
     * @return Observable which publishes lists of nearby Blips.
     */
    public rx.Observable<List<Blip>> getNearbyBlipsSubject() {
        return mNearbyBlipsSubject.asObservable().observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Use to determine whether or not a user is logged into the app. Log-ins are cached onto disk.
     * @return Boolean indicating if a user is logged in.
     */
    public boolean isUserLoggedIn() {
        return (ParseUser.getCurrentUser() != null);
    }

    /**
     * Sign up a new user. Requires a UNIQUE username. Caches sign-in to disk.
     * Will throw a ParseException if either username or email is already taken.
     * Be prepared to catch this in your subscriber and respond accordingly.
     * Note that this a cold observable and won't execute unless you subscribe to it.
     * @param username A unique string for a publicly visibly username.
     * @param password A password - the Parse SDK will hash this before sending it off.
     * @return An observable that will either return the newly registered user, or throw a ParseException if unable to register the user.
     */
    public rx.Observable<ParseUser> registerNewUser(String username, String password) {
        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        return rx.Observable.create(new Observable.OnSubscribe<ParseUser>() {
            @Override
            public void call(Subscriber<? super ParseUser> subscriber) {
                try {
                    user.signUp();
                    subscriber.onNext(user);
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Log in an existing user. Sign-in is cached to disk.
     * Will throw a ParseException if unable to sign in.
     * Be prepared to catch this in your subscriber and respond accordingly.
     * Note that this a cold observable and won't execute unless you subscribe to it.
     * @param username A string representing an existing username
     * @param password A string representing the user's password - the Parse SDK will hash this
     * @return An observable that will either return the logged-in user, or throw a ParseException if unable to sign-in.
     */
    public rx.Observable<ParseUser> logInUser(final String username, final String password) {
        return rx.Observable.create(new Observable.OnSubscribe<ParseUser>() {
            @Override
            public void call(Subscriber<? super ParseUser> subscriber) {
                try {
                    subscriber.onNext(ParseUser.logIn(username, password));
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Refreshes the nearby and distant Blips. Updates the NearbyBlipsSubject. Returns ALL blips.
     * This does NOT return the new Blips.
     * @return An observable which returns a Boolean value indicating if Blips were successfully fetched.
     */
    public rx.Observable<Boolean> updateBlips() {
        return rx.Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                ParseQuery<Blip> query = ParseQuery.getQuery(Blip.class);
                try {
                    List<Blip> blips = query.find();
                    mNearbyBlipsSubject.onNext(blips);
                    subscriber.onNext(true);
                } catch (ParseException e) {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Refreshes the nearby and distant Blips. Updates the NearbyBlipsSubject based on the location and range provided.
     * Fetches Blips withing a SQUARE based on the values provided.
     * This does NOT return the new Blips.
     * @param location A LatLng representing the CENTER of the area to query.
     * @param range The lat/lng range to query on either side of the center. Ex: Providing a Lat of 120.00 and a
     * range of 0.10 will result in a query encompassing Lat values of 119.90 - 120.10.
     * @return An observable which returns a Boolean value indicating if Blips were successfully fetched.
     */
    public rx.Observable<Boolean> updateBlips(final LatLng location, final double range) {
        return rx.Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                ParseQuery<Blip> query = ParseQuery.getQuery(Blip.class);
                ParseGeoPoint swCorner = new ParseGeoPoint(location.latitude - range, location.longitude - range);
                ParseGeoPoint neCorner = new ParseGeoPoint(location.latitude + range, location.longitude + range);
                query.whereWithinGeoBox(Blip.LOCATION, swCorner, neCorner);
                try {
                    List<Blip> blips = query.find();
                    mNearbyBlipsSubject.onNext(blips);
                    subscriber.onNext(true);
                } catch (ParseException e) {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Retrieves all of the Blips posted by the provided user.
     * @param user User for which to retrieve history of Blips.
     * @return An observable which returns the user's list of Blips, in descending chronological order, then exits.
     */
    public rx.Observable<List<Blip>> getBlipsForUser(final ParseUser user) {
        return rx.Observable.create(new Observable.OnSubscribe<List<Blip>>() {
            @Override
            public void call(Subscriber<? super List<Blip>> subscriber) {
                ParseQuery<Blip> query = ParseQuery.getQuery(Blip.class);
                query.whereEqualTo(Blip.USER, user);
                query.orderByDescending("_created_at");
                try {
                    List<Blip> blips = query.find();
                    subscriber.onNext(blips);
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Retrieves all of the Blips posted by the currently logged-in user.
     * If no user is logged in, this will throw an IllegalStateException.
     * @return An observable which returns the user's list of Blips, in descending chronological order, then exits.
     */
    public rx.Observable<List<Blip>> getBlipsForUser() {
        if (ParseUser.getCurrentUser() == null) {
            throw new IllegalStateException("No currently logged in user");
        }
        return getBlipsForUser(ParseUser.getCurrentUser());
    }

}
