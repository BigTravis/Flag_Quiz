package edu.orangecoastcollege.cs273.tmorrissey1.flagquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    // Creates preferences GUI from preferences.xml file in res/xml
    public SettingsActivityFragment() {
    }

    /**
     * Perform initialization of all fragments and loaders.
     * @param bundle If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState. Note: Otherwise it is null.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
    }


}
