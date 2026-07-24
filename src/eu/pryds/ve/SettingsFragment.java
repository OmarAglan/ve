package eu.pryds.ve;

import android.app.backup.BackupManager;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
	
	@Override
    public void onStop() {
        super.onStop();
        
        // Request backup, data might have changed.
        BackupManager bm = new BackupManager(getActivity());
        bm.dataChanged();
    }
}
