package ru.coolone.travelquest;

import android.support.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * @author coolone
 * @since 19.06.18
 */
public class Application extends MultiDexApplication {
    @Override
    public void onCreate() {
        Fresco.initialize(this);
        super.onCreate();
    }
}
