package alv.splash.browser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileSettingsFragment extends Fragment {
    private SwitchMaterial profileModeSwitch;

    public ProfileSettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        profileModeSwitch = view.findViewById(R.id.profile_mode_switch);

        // Set initial state from app preferences
        boolean currentMode = CosmicExplorer.getInstance().isProfileModeEnabled();
        profileModeSwitch.setChecked(currentMode);

        // Handle switch changes
        profileModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CosmicExplorer.getInstance().setProfileModeEnabled(isChecked);

            // Show info about the change
            if (isChecked) {
                Toast.makeText(requireContext(),
                        "Profile mode enabled. Each tab will now have its own profile.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(),
                        "Profile mode disabled. All tabs will share the same profile.",
                        Toast.LENGTH_LONG).show();

                // Confirm restart of browser for changes to fully take effect
                new AlertDialog.Builder(requireContext())
                        .setTitle("Restart Required")
                        .setMessage("For best results, please restart the browser for this change to fully take effect.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        return view;
    }
}