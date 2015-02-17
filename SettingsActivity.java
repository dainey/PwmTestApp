package com.verifone.vmf.testapp;

import static com.verifone.vmf.Constants.VMF_ERROR.VMF_OK;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.verifone.vmf.Log;
import com.verifone.vmf.api.VMF;
import com.verifone.vmf.api.VMF.BarcodeListener;
import com.verifone.vmf.api.VMF.UpdateListener;
import com.verifone.vmf.ijack.Utils;

public class SettingsActivity extends PreferenceActivity
{
  private static final String TAG = "SettingsActivity";

  private static final String KEY_CHECK_UPDATE = "checkUpdate";
  private static final String KEY_BARCODE_EDGE = "edge";
  private static final String KEY_BARCODE_LEVEL = "level";
  private static final String KEY_BARCODE_LEVEL_ONCE = "levelOnce";
  private static final String KEY_BARCODE_FACTORY_DEFAULTS = "factoryDefaults";
  private static final String KEY_BARCODE_ENABLE_1D = "enable1D";
  private static final String KEY_BARCODE_ENABLE_2D = "enable2D";
  private static final String KEY_BARCODE_MESSAGE_FORMAT_0 = "messageFormat0";
  private static final String KEY_BARCODE_MESSAGE_FORMAT_1 = "messageFormat1";
  private static final String KEY_PRINTER = "printer";

  @Override @SuppressWarnings("deprecation")
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    VMF.setUpdateListener(new UpdateHandler());

    Preference.OnPreferenceClickListener preferenceListener = new Preference.OnPreferenceClickListener()
    {

      public boolean onPreferenceClick(Preference preference)
      {
        onPreferenceChanged(preference);
        return true;
      }

    };

    findPreference(KEY_CHECK_UPDATE).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_EDGE).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_LEVEL).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_LEVEL_ONCE).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_FACTORY_DEFAULTS).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_ENABLE_1D).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_ENABLE_2D).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_MESSAGE_FORMAT_0).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_BARCODE_MESSAGE_FORMAT_1).setOnPreferenceClickListener(preferenceListener);
    findPreference(KEY_PRINTER).setOnPreferenceClickListener(preferenceListener);
  }

  private void onPreferenceChanged(Preference preference)
  {
    int retValue = VMF_OK;

    String key = preference.getKey();

    if (key.equals(KEY_CHECK_UPDATE))
    {
      checkForUpdate();

    }
    else if (key.equals(KEY_BARCODE_EDGE))
    {
      retValue = VMF.vmfBarTriggerMode(com.verifone.vmf.Constants.VMF_BAR_TRIGGER_MODE.BCS_BUTTON_EDGE);

    }
    else if (key.equals(KEY_BARCODE_LEVEL))
    {
      retValue = VMF.vmfBarTriggerMode(com.verifone.vmf.Constants.VMF_BAR_TRIGGER_MODE.BCS_BUTTON_LEVEL);

    }
    else if (key.equals(KEY_BARCODE_LEVEL_ONCE))
    {
      retValue = VMF.vmfBarTriggerMode(com.verifone.vmf.Constants.VMF_BAR_TRIGGER_MODE.BCS_BUTTON_LEVEL_ONCE);

    }
    else if (key.equals(KEY_BARCODE_FACTORY_DEFAULTS))
    {
      retValue = VMF.vmfBarResetFactoryDefaults();

    }
    else if (key.equals(KEY_BARCODE_ENABLE_1D))
    {
      retValue = VMF.vmfBarEnable1D2D(0);

    }
    else if (key.equals(KEY_BARCODE_ENABLE_2D))
    {
      retValue = VMF.vmfBarEnable1D2D(1);

    }
    else if (key.equals(KEY_BARCODE_MESSAGE_FORMAT_0))
    {
      retValue = VMF.vmfBarMessageFormat(0);

    }
    else if (key.equals(KEY_BARCODE_MESSAGE_FORMAT_1))
    {
      retValue = VMF.vmfBarMessageFormat(1);

    }
    else if (key.equals(KEY_PRINTER))
    {
      startActivity(new Intent(this, SettingsPrinterActivity.class));
    }

    if (retValue != VMF_OK)
    {
      Toast.makeText(SettingsActivity.this,
                     R.string.barcode_command_failure, Toast.LENGTH_LONG).show();
    }

  }

  @Override
  protected void onResume()
  {
    super.onResume();

    VMF.setBarcodeListener(new BarcodeReceiver());
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    VMF.setBarcodeListener(null);
  }

  private void checkForUpdate()
  {
    int update = VMF.vmfCheckForUpdateVx600(this);

    switch (update)
    {
      case 0:
        showMessage(getString(R.string.lbl_update_no_available));
        break;

      case 1:
        showMessage(getString(R.string.lbl_up_to_date));
        break;

      case 2:
        showMessage(getString(R.string.lbl_update_loading));
        VMF.vmfUploadUpdateToVX600(getApplicationContext());
        break;

      default:
        break;
    }
  }

  private void showMessage(final String message)
  {
    runOnUiThread(new Runnable()
    {

      @Override
      public void run()
      {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

      }
    });
  }

  private class BarcodeReceiver implements BarcodeListener
  {

    @Override
    public void onReceive(final byte[] data)
    {
      Log.d(TAG, "Barcode command result: " + Utils.toHex(data));

      SettingsActivity.this.runOnUiThread(new Runnable()
      {

        @Override
        public void run()
        {

          if (data.length > 0 && data[0] == 0x06)
          {
            Toast.makeText(SettingsActivity.this, R.string.barcode_command_successfull,
                           Toast.LENGTH_LONG).show();
          }
          else
          {
            Toast.makeText(SettingsActivity.this, R.string.barcode_command_failure,
                           Toast.LENGTH_LONG).show();
          }

        }

      });
    }

  }

  private class UpdateHandler implements UpdateListener
  {

    @Override
    public void onUpdateComplete(boolean error)
    {
      if (error)
      {
        showMessage(getString(R.string.lbl_update_error));
      }
      else
      {
        showMessage(getString(R.string.lbl_update_install));
        VMF.vmfInstallVx600Update();
        finish();
      }
    }

    @Override
    public void onUpdateVersionAvailable(String vx600Version, String updateVersion)
    {
      Log.d(TAG, "vx600Version: " + vx600Version + " - updateVersion: " + updateVersion);
      checkForUpdate();
    }

  }
}
