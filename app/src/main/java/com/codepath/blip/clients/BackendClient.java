package com.codepath.blip.clients;

import android.content.Context;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.interceptors.ParseLogInterceptor;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Author: soroushmehraein
 * Project: Blip
 * Date: 8/17/16
 */
public class BackendClient {
    public BackendClient(String parseUrl, String parseAppId, Context context) {
        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId(parseAppId)
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server(parseUrl).build());
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
}
