package com.cs595.uwm.chatbylocation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.cs595.uwm.chatbylocation.R;
import com.cs595.uwm.chatbylocation.objModel.UserIcon;
import com.cs595.uwm.chatbylocation.service.Database;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 42;
    public static final int IMAGE_QUALITY = 100;

    private static boolean inFragment = false;
    private static String caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        if (caller == null) {
            System.out.println("Setting caller");
            caller = getIntent().getStringExtra("caller");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (inFragment) {
                    startActivity(new Intent(this, SettingsActivity.class));
                    inFragment = false;
                    break;
                }

                System.out.println("Caller = " + caller);
                if (caller == null) break;

                try {
                    Class cls = Class.forName(caller);
                    startActivity(new Intent(this, cls));
                    caller = null;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            inFragment = true;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final ListPreference iconPref = (ListPreference) findPreference("user_icon");
            ColorPickerPreference colorPref = (ColorPickerPreference) findPreference("color1");

            // Set 'Display Name' summary to Username
            EditTextPreference namePref = (EditTextPreference) findPreference("example_text");
            namePref.setSummary(Database.getUserUsername());

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("example_list"));

            // User icon setting
            // Nathan TODO: Set pref icon to user icon saved in DB
            String icon = Database.getCurrentUserIcon();//prefs.getString("user_icon", UserIcon.NONE);
            int iconRes = UserIcon.getIconResource(icon);
            if (iconRes == 0) {
                trace("Set 'user icon' setting icon as custom photo");
                Bitmap image = Database.getCurrentUserImage();
                if (image != null) {
                    iconPref.setIcon(new BitmapDrawable(getResources(), image));
                } else {
                    iconPref.setIcon(UserIcon.NONE_RESOURCE);
                }
            } else {
                trace("Set 'user icon' setting icon");
                iconPref.setIcon(iconRes);
            }

            iconPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String icon = String.valueOf(newValue);
                    Database.setIcon(icon);

                    if (UserIcon.PHOTO.equals(icon)) {
                        Bitmap image = Database.getUserImage(Database.getUserId());
                        iconPref.setIcon(new BitmapDrawable(getResources(), image));

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }

                    int iconRes = UserIcon.getIconResource(icon);
                    if (iconRes != 0) {
                        iconPref.setIcon(iconRes);
                    }

                    return true;
                }
            });

            // Message color setting
            int color = prefs.getInt("color1", Color.BLACK);
            colorPref.setSummary(ColorPickerPreference.convertToRGB(color));
            colorPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String color = ColorPickerPreference.convertToRGB(Integer.valueOf((Integer) newValue));
                    preference.setSummary(color);

                    return true;
                }
            });

            // Text size setting
            /*
            ListPreference textSize = (ListPreference) findPreference("msg_font_size");
            textSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String sizeString = String.valueOf(newValue);
                    int sizeInt = Integer.valueOf(String.valueOf(newValue));
                    preference.setSummary(sizeString);

                    return true;
                }
            });
            //*/
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

                // Get bitmap from camera result
                Bitmap image = (Bitmap) data.getExtras().get("data");
                if (image != null) {

                    // Crop image to square proportions
                    Bitmap squareImage;
                    int w = image.getWidth();
                    int h = image.getHeight();
                    if (w < h) {
                        squareImage = Bitmap.createBitmap(image, 0, h/2 - w/2, w, w);
                    } else {
                        squareImage = Bitmap.createBitmap(image, w/2 - h/2, 0, h, h);
                    }

                    // Set preference icon to new image
                    final ListPreference iconPref = (ListPreference) findPreference("user_icon");
                    iconPref.setIcon(new BitmapDrawable(getResources(), squareImage));

                    // Get image as bytes array for upload
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    squareImage.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, baos);
                    byte[] imageBytes = baos.toByteArray();

                    final String userId = Database.getUserId();
                    if (userId != null) {

                        // Upload image bytes to Firebase Storage
                        Database.getImageStorageReference(userId)
                        .putBytes(imageBytes)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                trace("Upload success");

                                // When image is successfully uploaded, update local list of images
                                Database.updateUserImage(userId);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            inFragment = true;

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            inFragment = true;

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }

    private static void trace(String message) {
        System.out.println("Settings >> " + message);
    }
}
