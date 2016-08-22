package com.codepath.blip.clients;

import android.content.Context;

import com.codepath.blip.R;
import com.codepath.blip.models.Blip;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.interceptors.ParseLogInterceptor;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
public class BackendClient {
    private BehaviorSubject<List<Blip>> mNearbyBlipsSubject;
    private BehaviorSubject<List<Blip>> mDistantBlipsSubject;

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
        mDistantBlipsSubject = BehaviorSubject.create();
    }

    /**
     * Returns the BehaviorSubject for nearby Blips. Treat this as you would a regular observable.
     * Note that this is a HOT observable, and will immediately return the last published value on subscribe.
     * @return BehaviorSubject which publishes nearby Blips.
     */
    public BehaviorSubject<List<Blip>> getNearbyBlipsSubject() {
        return mNearbyBlipsSubject;
    }

    /**
     * Returns the BehaviorSubject for distant Blips. Treat this as you would a regular observable.
     * Note that this is a HOT observable, and will immediately return the last published value on subscribe.
     * @return BehaviorSubject which publishes distant Blips.
     */
    public BehaviorSubject<List<Blip>> getDistantBlipsSubject() {
        return mDistantBlipsSubject;
    }

    /**
     * Posts an object to Parse with three simple fields. Execution is handled on a background io thread.
     * Note that this a cold observable and won't execute unless you subscribe to it.
     * If you're going to do anything to the UI with the returned value, don't forget to observe on the main thread.
     * @param firstName Random string
     * @param lastName Random string
     * @param gender Another random string
     * @return An Observable which eventually returns a single, fully-formed (with id) Parse object, then completes.
     */
    public rx.Observable<ParseObject> postTestObjectToParse(String firstName, String lastName, String gender) {
        final ParseObject testObject = new ParseObject("Test");
        testObject.put("First_Name", firstName);
        testObject.put("Last_Name", lastName);
        testObject.put("Gender", gender);
        return rx.Observable.create(new Observable.OnSubscribe<ParseObject>() {
            @Override
            public void call(Subscriber<? super ParseObject> subscriber) {
                try {
                    testObject.save();
                    subscriber.onNext(testObject);
                } catch (ParseException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Use to determine whether or not a user is logged into the app. Log-ins are cached onto disk.
     * @return Boolean indicating if a user is logged in.
     */
    public boolean isUserLoggedIn() {
        return (ParseUser.getCurrentUser() != null);
    }

    /**
     * Sign up a new user. Requires a UNIQUE email and username. Caches sign-in to disk.
     * Will throw a ParseException if either username or email is already taken.
     * Be prepared to catch this in your subscriber and respond accordingly.
     * Note that this a cold observable and won't execute unless you subscribe to it.
     * @param username A unique string for a publicly visibly username.
     * @param password A password - the Parse SDK will hash this before sending it off.
     * @param email A unique email.
     * @return An observable that will either return the newly registered user, or throw a ParseException if unable to register the user.
     */
    public rx.Observable<ParseUser> registerNewUser(String username, String password, String email) {
        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
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
        }).subscribeOn(Schedulers.io());
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
        }).subscribeOn(Schedulers.io());
    }

    /**
     * TEMPORARILY MOCKED OUT
     * Queries Parse for Blips and updates BOTH behavior subjects.
     * Use this whenever you want to refresh the Blips.
     */
    public void updateBlips() {
        // Update nearby Blips
        List<Blip> fakeNearbyBlips = new ArrayList<>();
        fakeNearbyBlips.add(new Blip());
        mNearbyBlipsSubject.onNext(fakeNearbyBlips);

        // Update distant Blips
        List<Blip> fakeDistantBlips = new ArrayList<>();
        fakeDistantBlips.add(new Blip());
        mDistantBlipsSubject.onNext(fakeDistantBlips);
    }

}
