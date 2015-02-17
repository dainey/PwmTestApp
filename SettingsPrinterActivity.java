package com.verifone.vmf.testapp;

import static com.verifone.vmf.Constants.VMF_ERROR.VMF_OK;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.verifone.vmf.Log;
import com.verifone.vmf.Printer;
import com.verifone.vmf.Printer.PrinterProperty;
import com.verifone.vmf.api.VMF;

public class SettingsPrinterActivity extends PreferenceActivity
{
  private static final String TAG = "SettingsPrinterActivity";

  private static final String KEY_SELF_TEST = "selfTest";
  private static final String KEY_RESET = "reset";
  private static final String KEY_PRINT_IMAGE = "printImage";
  private static final String KEY_PRINT_LOGO = "printLogo";
  private static final String KEY_LOAD_LOGO = "loadLogo";
  private static final String KEY_EJECT_LINES = "ejectLines";
  private static final String KEY_BATTERY_CAPACITY = "batteryCapacity";
  private static final String KEY_BATTERY_VOLTAGE = "batteryVoltage";
  private static final String KEY_BATTERY_TEMPERATURE = "batteryTemperature";
  private static final String KEY_FIRMWARE_VERSION = "firmwareVersion";
  private static final String KEY_PRINTER_TYPE = "printerType";
  private static final String KEY_CONTRAST_GET = "getContrast";
  private static final String KEY_CONTRAST_SET = "setContrast";
  private static final String KEY_STATUS = "printerStatus";
  private static final String KEY_BLUETOOTH_FIRMWARE_VERSION = "btFirmwareVersion";
  private static final String KEY_CHARACTERS_PER_LINE = "charactersPerLine";
  private static final String KEY_LINE_HEIGHT = "lineHeight";

  @Override @SuppressWarnings("deprecation")
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.printer_preferences);

    Preference.OnPreferenceClickListener clickListener = new Preference.OnPreferenceClickListener()
    {

      public boolean onPreferenceClick(Preference preference)
      {
        onPreferenceChanged(preference, null);
        return true;
      }

    };

    Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener()
    {

      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue)
      {
        onPreferenceChanged(preference, newValue == null ? null : newValue.toString());
        return true;
      }

    };

    findPreference(KEY_SELF_TEST).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_RESET).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_PRINT_IMAGE).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_BATTERY_CAPACITY).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_BATTERY_VOLTAGE).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_BATTERY_TEMPERATURE).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_FIRMWARE_VERSION).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_STATUS).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_PRINTER_TYPE).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_CONTRAST_GET).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_BLUETOOTH_FIRMWARE_VERSION).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_PRINT_LOGO).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_LOAD_LOGO).setOnPreferenceClickListener(clickListener);
    findPreference(KEY_CHARACTERS_PER_LINE).setOnPreferenceChangeListener(changeListener);
    findPreference(KEY_LINE_HEIGHT).setOnPreferenceChangeListener(changeListener);
    findPreference(KEY_EJECT_LINES).setOnPreferenceChangeListener(changeListener);
    findPreference(KEY_CONTRAST_SET).setOnPreferenceChangeListener(changeListener);
  }

  private void onPreferenceChanged(Preference preference, String newValue)
  {
    int retValue = VMF_OK;
    StringBuffer propertyValue = new StringBuffer("");

    String key = preference.getKey();

    if (key.equals(KEY_SELF_TEST))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtSelfTest();

    }
    else if (key.equals(KEY_RESET))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtReset();

    }
    else if (key.equals(KEY_EJECT_LINES))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtText(String.format(Printer.ESC + "l%s$", newValue));

    }
    else if (key.equals(KEY_LOAD_LOGO))
    {
      try
      {
        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("logo.png"));
        retValue = VMF.vmfGetSelectedPrinter().vmfPrtStoreImage(0, bitmap);
      }
      catch (IOException e)
      {
        Log.e(TAG, "Error while trying to load an image: " + e.getMessage());
      }

    }
    else if (key.equals(KEY_PRINT_LOGO))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtText(Printer.ESC + "*N0$" + Printer.ESC + "L5$");
    }
    else if (key.equals(KEY_PRINT_IMAGE))
    {

      try
      {
        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("world_small.bmp"));
        retValue = VMF.vmfGetSelectedPrinter().vmfPrtImage(bitmap);
      }
      catch (IOException e)
      {
        Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      }

    }
    else if (key.equals(KEY_CHARACTERS_PER_LINE))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CHARACTER_PER_LINE, newValue);

    }
    else if (key.equals(KEY_LINE_HEIGHT))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.LINE_HEIGHT, newValue);

    }
    else if (key.equals(KEY_CONTRAST_SET))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CONTRAST, newValue);

    }
    else if (key.equals(KEY_STATUS))
    {
      int value = VMF.vmfGetSelectedPrinter().vmfPrtGetStatus();

      showMessage("Printer status: " + value);

      //===================================================================================================
      // Begin of TEST area!!!
      //===================================================================================================

      //      // Printing of 7000 bytes directly from the mobile device
      //      String help = "";
      //
      //      for (int i = 0; i < 6998; i++)
      //      {
      //      help = help + "7";
      //      }
      //
      //      help += "\n";
      //
      //      VMF.vmfGetSelectedPrinter().vmfPrtText(help);

      //      int help = 0;

      //      VMF.vmfGetSelectedPrinter().vmfPrtText("1 Hello world\n" +
      //                                           "2 \u001bD$Hello world\u001bd$ Susi\n" +
      //                                           "3 \u001bH$Hello world\u001bh$ Elsa\n" +
      //                                           "4 \u001bR$Hello world\u001br$ Schnulli\n" +
      //                                           "5 \u001bB$Hello world\u001bb$ Miststück\n" +
      //                                           "6 \u001bD$\u001bH$\u001bR$\u001bB$Hello world blablabla\n");
      //      VMF.vmfGetSelectedPrinter().vmfPrtReset();

      //      // FogBugz Case 2092
      //      String property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CONTRAST);
      //      help = 0;

      //      // FogBugz Case 2092
      //      String s = "\\u001ba4HelloWorld\n";
      //
      //      byte[] data = s.getBytes();
      //
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSendRawData(data);

      //    // FogBugz Case 2100
      //    try
      //    {
      //      //Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("logotiff.tif"));
      //        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("TestPicSW.bmp"));
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtImage(bitmap);
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtImage(null);
      //      help = 0;
      //    }
      //    catch (IOException e)
      //    {
      //      Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      //    }

      //      // FogBugz Case 2111
      //      try
      //      {
      //        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("world_small.bmp"));
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtStoreImage(2, bitmap);
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtText("\u001b*N2$\n\n\n\n\n");
      //        help = 0;
      //
      //        try
      //        {
      //          Thread.sleep(500);
      //        }
      //        catch (InterruptedException e)
      //        {
      //          Log.e("vmfPrtText()", "InterruptedException: " + e);
      //        }
      //
      //        Bitmap bitmap2 = BitmapFactory.decodeStream(getAssets().open("TestPicSW.bmp"));
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtStoreImage(2, bitmap2);
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtText("\u001b*N2$\n\n\n\n\n");
      //        help = 0;
      //      }
      //      catch (IOException e)
      //      {
      //        Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      //      }



      //
      //      // FogBugz Case 2101
      //      try
      //      {
      //        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("385x500.png"));
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtImage(bitmap);
      //        help = 0;
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtImage(null);
      //        help = 0;
      //      }
      //      catch (IOException e)
      //      {
      //        Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      //      }

      //      // FogBugz Case 2102
      //      try
      //      {
      //        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("logi.png"));
      //        help = VMF.vmfGetSelectedPrinter().vmfPrtImage(bitmap);
      //        help = 0;
      //      }
      //      catch (IOException e)
      //      {
      //        Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      //      }

      //      // FogBugz Case 2104
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSelectFont(0, 256);
      //      VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\n");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSelectFont(256, 0);
      //      VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\n");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSelectFont(0, -1);
      //      VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\n");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSelectFont(-1, 0);
      //      VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\n");


      //      // FogBugz Case 2104
      //      String value1 = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CONTRAST);
      //      Log.i("TAG", "Output1 = " + value1);

      //      help = VMF.vmfGetSelectedPrinter().vmfPrtReset();
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSelectFont(0, 3);
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtReset();
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("Hello World!!!\u001bL4$\n\u001bF0,3$Hello World!!!\n\u001bl-10$\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtReset();

      //          // FogBugz Case 2113
      //          try
      //          {
      //            Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("Flag2.bmp"));
      //            help = VMF.vmfGetSelectedPrinter().vmfPrtStoreImage(2, bitmap);
      //            help = 0;
      //            help = VMF.vmfGetSelectedPrinter().vmfPrtText("\u001b*N2$\n");
      //            help = 0;
      //          }
      //          catch (IOException e)
      //          {
      //            Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      //          }

      //    // FogBugz Case 2114
      //    try
      //    {
      //      Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("logi.png"));
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtStoreImage(2, bitmap);
      //      help = 0;
      //    }
      //    catch (IOException e)
      //    {
      //      Log.e(TAG, "Error while trying to print image: " + e.getMessage());
      //    }

      //      // FogBugz Case 2127
      //      help = VMF.vmfBarStartScan();
      //      help = VMF.vmfBarEnable1D2D(0);
      //      help = VMF.vmfBarEnable1D2D(1);
      //      help = VMF.vmfBarEnable1D2D(2);
      //      help = VMF.vmfBarEnable1D2D(-1);
      //      help = VMF.vmfBarAbortScan();
      //      help = 0;

      //      // FogBugz Case 2128
      //      help = VMF.vmfBarStartScan();
      //      help = 0;
      //      help = VMF.vmfBarEnable1D2D(0);
      //      help = 0;

      //      // FogBugz Case 2148
      //      help = VMF.vmfBarStartScan();
      //      help = 0;
      //      help = VMF.vmfBarMessageFormat(2);
      //      help = 0;
      //      help = VMF.vmfBarMessageFormat(-1);
      //      help = 0;
      //      help = VMF.vmfBarAbortScan();
      //      help = 0;

      //      // FogBugz Case 2151
      //      String property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CHARACTER_PER_LINE);
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.LINE_HEIGHT);
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CONTRAST);
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("1. Hello World!!!\n12345678901234567890123456789012345678901234567890\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CHARACTER_PER_LINE, "10");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.LINE_HEIGHT, "10");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CONTRAST, "12");
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CHARACTER_PER_LINE);
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.LINE_HEIGHT);
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CONTRAST);
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("2. Hello World!!!\n12345678901234567890123456789012345678901234567890\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CHARACTER_PER_LINE, "30");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.LINE_HEIGHT, "50");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CONTRAST, "6");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("3. Hello World!!!\n12345678901234567890123456789012345678901234567890\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CHARACTER_PER_LINE, "1");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.LINE_HEIGHT, "32");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CONTRAST, "50");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("4. Hello World!!!\n12345678901234567890123456789012345678901234567890\n");
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtReset();
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CHARACTER_PER_LINE);
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.LINE_HEIGHT);
      //      property = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CONTRAST);
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtText("5. Hello World!!!\n12345678901234567890123456789012345678901234567890\n\n\n\n");

      //      // FogBugz Case 2152
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CHARACTER_PER_LINE, "-1");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CHARACTER_PER_LINE, "256");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.LINE_HEIGHT, "-1");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.LINE_HEIGHT, "256");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CONTRAST, "-1");
      //      help = 0;
      //      help = VMF.vmfGetSelectedPrinter().vmfPrtSetProperty(PrinterProperty.CONTRAST, "32");
      //      help = 0;


      //int susi = VMF.vmfGetSelectedPrinter().vmfPrtText("Vikram\n");

      //      int susi = -5;
      //
      //      susi = VMF.vmfCheckForUpdateVx600(this);
      //      susi = VMF.vmfCheckForUpdateVx600(this);
      //
      //      if (susi != -5)
      //        susi = -5;

      //===================================================================================================
      // End of TEST area!!!
      //===================================================================================================
    }
    else if (key.equals(KEY_BATTERY_CAPACITY))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.BATTERY_CAPACITY, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Remaining Battery capacity: " + value + " mAh");
      }
    }
    else if (key.equals(KEY_BATTERY_VOLTAGE))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.BATTERY_VOLTAGE, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Battery voltage: " + value + " mV");
      }
    }
    else if (key.equals(KEY_BATTERY_TEMPERATURE))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.BATTERY_TEMPERATURE, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Battery temperature: " + value + " °C");
      }
    }
    else if (key.equals(KEY_PRINTER_TYPE))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.PRINTER_TYPE, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Printer Type: " + value);
      }
    }
    else if (key.equals(KEY_CONTRAST_GET))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.CONTRAST, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Printer Contrast: " + value);
      }
    }
    else if (key.equals(KEY_FIRMWARE_VERSION))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.FIRMWARE_VERSION_PRINTER, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Printer Firmware Version: " + value);
      }
    }
    else if (key.equals(KEY_BLUETOOTH_FIRMWARE_VERSION))
    {
      retValue = VMF.vmfGetSelectedPrinter().vmfPrtGetProperty(PrinterProperty.FIRMWARE_VERSION_BT_MODULE, propertyValue);

      if (retValue == VMF_OK)
      {
        String value = propertyValue.toString();
        showMessage("Bluetooth module firmware version: " + value);
      }
    }

    if (retValue == VMF_OK)
    {
      showMessage(R.string.printer_command_successfull);
    }
    else
    {
      showMessage(R.string.printer_command_failure);
    }

  }

  @Override
  protected void onResume()
  {
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
  }

  private void showMessage(final int message)
  {
    showMessage(getString(message));
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

}
