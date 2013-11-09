package com.lidroid.xutils.view;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

/**
 * Author: wyouflf
 * Date: 13-9-9
 * Time: 下午12:29
 */
public class ViewFinder {

    private View view;
    private Activity activity;
    private PreferenceActivity preferenceActivity;

    public ViewFinder(View view) {
        this.view = view;
    }

    public ViewFinder(Activity activity) {
        this.activity = activity;
    }

    public ViewFinder(PreferenceActivity preferenceActivity) {
        this.preferenceActivity = preferenceActivity;
        this.activity = preferenceActivity;
    }

    public View findViewById(int id) {
        return activity == null ? view.findViewById(id) : activity.findViewById(id);
    }

    @SuppressWarnings("deprecation")
    public Preference findPreference(CharSequence key) {
        return preferenceActivity.findPreference(key);
    }

    public Context getContext() {
        if (view != null) return view.getContext();
        if (activity != null) return activity;
        if (preferenceActivity != null) return preferenceActivity;
        return null;
    }
}
