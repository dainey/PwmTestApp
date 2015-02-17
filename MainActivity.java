package com.verifone.vmf.testapp;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.verifone.vmf.Constants;
import com.verifone.vmf.api.VMF;
import com.verifone.vmf.api.VMF.AppLinkListener;
import com.verifone.vmf.api.VMF.BarcodeListener;
import com.verifone.vmf.api.VMF.ConnectionListener;
import com.verifone.vmf.api.VMF.UIReqListener;
//import com.verifone.vmf.api.VMF.PrinterDataListener;

public class MainActivity extends FragmentActivity
{

  private static final int REQUEST_ENABLE_BT = 0;

  protected static final String TAG = "MainActivity";

  private Button sendAppLinkBtn;
  private Button sendPayment;
  private Button startTimeServerBtn;
  private Button sendXBytes;

  private EditText receivedData;
  private EditText amount;
  private EditText mEtEcho;
  private CheckBox sslTimeServer;

  private final String PREF_TIME_SERVER_IP = "timeServerIP";
  private final String PREF_TIME_SERVER_PORT = "timeServerPort";

  private final String PREF_TIME_SSL_SERVER_IP = "SSLTimeServerIP";
  private final String PREF_TIME_SSL_SERVER_PORT = "SSLTimeServerPort";

  private DialogFragment mLoadingDialog;
  private ToggleButton toggleBarReader;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    receivedData = (EditText)findViewById(R.id.receivedData);
    amount = (EditText)findViewById(R.id.amount);
    mEtEcho = (EditText)findViewById(R.id.etEcho);
    sslTimeServer = (CheckBox)findViewById(R.id.sslTimeServer);

    sendAppLinkBtn = (Button)findViewById(R.id.sendAppLinkBtn);
    sendAppLinkBtn.setOnClickListener(new View.OnClickListener()
    {

      @Override
      public void onClick(View arg0)
      {
        VMF.vmfAppLinkSend(128, mEtEcho.getText().toString().getBytes(), 5000);
      }
    });

    startTimeServerBtn = (Button)findViewById(R.id.startTimeServerBtn);
    startTimeServerBtn.setOnClickListener(new View.OnClickListener()
    {

      @Override
      public void onClick(View arg0)
      {
        timeServerTest();
      }

    });

    sendPayment = (Button)findViewById(R.id.sendPayment);
    sendPayment.setOnClickListener(new View.OnClickListener()
    {

      @Override
      public void onClick(View arg0)
      {
        sendPayment();
        hideSoftKeyboard();
      }

    });

    toggleBarReader = (ToggleButton)findViewById(R.id.toggleBarcodeReader);
    toggleBarReader.setOnCheckedChangeListener(new OnCheckedChangeListener()
    {

      @Override
      public void onCheckedChanged(CompoundButton arg0, boolean isChecked)
      {

        if (isChecked)
        {
          showProgressDialog(getString(R.string.barcode_reader_off_progress));
          VMF.vmfBarStartScan();
        }
        else
        {
          showProgressDialog(getString(R.string.barcode_reader_on_progress));
          VMF.vmfBarAbortScan();
        }

      }

    });

    sendXBytes = (Button)findViewById(R.id.sendXBytes);
    sendXBytes.setOnClickListener(new View.OnClickListener()
    {

      @Override
      public void onClick(View arg0)
      {
        sendXBytes();
      }
    });

    VMF.setAppLinkListener(new AppLinkReceiver());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem)
  {
    boolean retValue = true;

    if (menuItem.getItemId() == R.id.menu_settings)
    {
      startActivity(new Intent(this, SettingsActivity.class));

    }
    else
    {
      retValue = false;
    }

    return retValue;
  }

  /**
   * Ensures user has turned on Bluetooth on the Android device.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch (requestCode)
    {
      case REQUEST_ENABLE_BT:
        if (resultCode == Activity.RESULT_OK)
        {
          connect();
        }
        else
        {
          finish();
          return;
        }
    }
  }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }

    @Override
  protected void onDestroy()
  {
    super.onDestroy();
    disconnect();
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    String help = VMF.vmfGetVersionLib();
    Log.i(TAG, "libVmf Version : " + help);
    addToLog("libVmf Version: " + help);Log.i(TAG, "libVmf Version : " + help);
    addToLog("libVmf Version: " + help);Log.i(TAG, "libVmf Version : " + help);
    addToLog("libVmf Version: " + help);

    //help = String.valueOf(VMF.vmfBarGetVersion());
    //Log.i(TAG, "BarcodeApp Version: " + help);

    help = VMF.vmfPrtGetVersionLib();
    Log.i(TAG, "libPrt Version: " + help);
    addToLog("libPrt Version: " + help);

    // Ask if they want to activate the BT if it isn't active yet
    if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
    {
      Intent enableIntent = new Intent(
        BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
    else
    {
      connect();
    }

  }

  @Override
  protected void onResume()
  {
    super.onResume();

    // Set required listeners.
    VMF.setUIReqListener(new UIReqReceiver());
    VMF.setBarcodeListener(new BarcodeReceiver());

    //    VMF.setPrinterDataListener(new PrinterReceiver());
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    // Remove listener.
    VMF.setBarcodeListener(null);
  }

  private void connect()
  {
    if (!VMF.isVx600Connected() || !VMF.isBTPrinterConnected())
    {
      showProgressDialog(getString(R.string.connecting));

      new Thread(new Runnable()
      {
        public void run()
        {
          // Try to connect to Vx600.
          //if (!VMF.isVx600Connected()) //TS: Removed to avoid endless loop of progress dialog when the app comes up from the background (case 2049)
          {
            VMF.vmfConnectVx600(MainActivity.this, new Vx600ConnectionListener(), 1);
            //            VMF.vmfConnectVx600(MainActivity.this, new Vx600ConnectionListener(), 61);
          }

          // Now try to connect to the printer.
          //if (!VMF.isBTPrinterConnected()) //TS: Removed to avoid endless loop of progress dialog when the app comes up from the background
          {
            VMF.vmfConnectBTPrinter(MainActivity.this, new PrinterConnectionListener());
          }
        }
      }).start();
    }
  }

  private void showProgressDialog(final String text)
  {
    mLoadingDialog = new DialogFragment()
    {
      @Override
      public Dialog onCreateDialog(Bundle savedInstanceState)
      {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(text);
        return progressDialog;
      }
    };
    mLoadingDialog.setCancelable(false);
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(mLoadingDialog, "progress_dialog");
    transaction.commitAllowingStateLoss();
  }

  private void disconnect()
  {
    VMF.vmfDisconnectVx600();
    VMF.vmfDisconnectBTPrinter();
  }

  private void sendPayment()
  {

    if (amount.length() > 0)
    {
      String command = "PAYMENT:" + amount.getText() + ":";

      int retValue = VMF.vmfAppLinkSend(128, command.getBytes(), 300000);

      if (retValue == Constants.VMF_ERROR.VMF_OK)
      {
        addToLog("CMD: " + command);
      }
    }

  }

  private void timeServerTest()
  {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

    String ipAddr = preferences.getString(PREF_TIME_SERVER_IP, null);
    String port = preferences.getString(PREF_TIME_SERVER_PORT, null);

    String sslIpAddr = preferences.getString(PREF_TIME_SSL_SERVER_IP, null);
    String sslPort = preferences.getString(PREF_TIME_SSL_SERVER_PORT, null);

    String command = null;

    if (sslTimeServer.isChecked())
    {

      if (ipAddr != null && port != null && sslIpAddr != null && sslPort != null)
      {
        command = "GETTIME:" + ipAddr + ":" + port + ":SSL:" + sslIpAddr + ":" + sslPort + ":";
      }
      else
      {
        Toast.makeText(this, R.string.invalid_time_server_conf, Toast.LENGTH_LONG).show();
      }


    }
    else
    {

      if (ipAddr != null && port != null)
      {
        command = "GETTIME:" + ipAddr + ":" + port + ":";
      }
      else
      {
        Toast.makeText(this, R.string.invalid_time_server_conf, Toast.LENGTH_LONG).show();
      }

    }

    if (command != null)
    {

      int retValue = VMF.vmfAppLinkSend(128, command.getBytes(), 300000);

      if (retValue == Constants.VMF_ERROR.VMF_OK)
      {
        addToLog("CMD: " + command);
      }

    }

  }

  private void sendXBytes()
  {

    if (amount.length() > 0)
    {

      int numberOfBytes = 0;

      try
      {
        numberOfBytes = Integer.parseInt(amount.getText().toString().trim());
      }
      catch (Exception e)
      {
        Toast.makeText(this, getString(R.string.invalid_amount_value),
                       Toast.LENGTH_SHORT).show();
      }

      if (numberOfBytes > 0)
      {

        byte[] data = new byte[numberOfBytes];
        Arrays.fill(data, (byte)0x31);

        int retValue = VMF.vmfAppLinkSend(128, data, 300000);

        if (retValue == Constants.VMF_ERROR.VMF_OK)
        {
          addToLog("CMD:sendXBytes:(" + amount.getText() + ")");
        }
        else
        {
          Toast.makeText(this, getString(R.string.app_link_error),
                         Toast.LENGTH_SHORT).show();
        }

      }

    }

  }

  private void addToLog(String value)
  {
    receivedData.append(value + "\n");
  }

  private void addToLog(byte[] value)
  {

    if (value != null)
    {
      receivedData.append(new String(value) + "\n");
    }

  }

  @SuppressLint("ValidFragment")
  private class DeviceDisconnectedDialogFragment extends DialogFragment
  {

    private String deviceName;

    public DeviceDisconnectedDialogFragment(String deviceName)
    {
      this.deviceName = deviceName;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

      builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener()
      {

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          dismiss();
        }

      });

      builder.setPositiveButton(R.string.btn_reconnect, new DialogInterface.OnClickListener()
      {

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          connect();
        }

      });

      builder.setTitle(R.string.connection_lost_title);
      builder.setMessage(getString(R.string.connection_lost_desc, deviceName));

      return builder.create();
    }
  }

  class PrinterConnectionListener implements ConnectionListener
  {

    @Override
    public void onConnectionEstablished()
    {
      Runnable action = new Runnable()
      {

        @Override
        public void run()
        {
          mLoadingDialog.dismissAllowingStateLoss();
          Toast.makeText(MainActivity.this, R.string.printer_connection_stablished,
                         Toast.LENGTH_SHORT).show();
        }

      };

      runOnUiThread(action);
    }

    @Override
    public void onConnectionFailed()
    {
      Runnable action = new Runnable()
      {

        @Override
        public void run()
        {
          Log.e(TAG, "Error while connecting with Printer");

          mLoadingDialog.dismissAllowingStateLoss();

          Toast.makeText(MainActivity.this, getString(R.string.printer_connection_failed_message),
                         Toast.LENGTH_SHORT).show();
        }
      };

      runOnUiThread(action);
    }

    @Override
    public void onDisconnected(String deviceName)
    {
      DeviceDisconnectedDialogFragment reconnectDialog = new DeviceDisconnectedDialogFragment(
        deviceName);
      FragmentManager manager = getSupportFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();
      transaction.add(reconnectDialog, "reconnectDialog");
      transaction.commitAllowingStateLoss();
    }

  }

  class Vx600ConnectionListener implements ConnectionListener
  {

    @Override
    public void onConnectionEstablished()
    {
      Runnable action = new Runnable()
      {

        @Override
        public void run()
        {
          mLoadingDialog.dismissAllowingStateLoss();
          Toast.makeText(MainActivity.this, R.string.vx_connection_stablished, Toast.LENGTH_SHORT).show();
          changeControlsState(true);

        }

      };

      runOnUiThread(action);
    }

    @Override
    public void onConnectionFailed()
    {
      Runnable action = new Runnable()
      {
        @Override
        public void run()
        {
          changeControlsState(false);
          mLoadingDialog.dismissAllowingStateLoss();

          Toast.makeText(MainActivity.this, getString(R.string.vx_connection_failed_message),
                         Toast.LENGTH_SHORT).show();
        }
      };

      runOnUiThread(action);
    }

    @Override
    public void onDisconnected(final String deviceName)
    {

      DeviceDisconnectedDialogFragment reconnectDialog = new DeviceDisconnectedDialogFragment(
        deviceName);
      FragmentManager manager = getSupportFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();
      transaction.add(reconnectDialog, "reconnectDialog");
      transaction.commitAllowingStateLoss();
      changeControlsState(false);
    }

  }

  private void changeControlsState(boolean enable)
  {
    sendAppLinkBtn.setEnabled(enable);
    startTimeServerBtn.setEnabled(enable);
    sendPayment.setEnabled(enable);
    sslTimeServer.setEnabled(enable);
    toggleBarReader.setEnabled(enable);
    sendXBytes.setEnabled(enable);
  }

  public void hideSoftKeyboard()
  {
    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
  }

  private class AppLinkReceiver implements AppLinkListener
  {

    @Override
    public void onResponse(final byte[] recvBuf, final boolean timeOut)
    {

      MainActivity.this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          if (timeOut)
          {
            Toast.makeText(MainActivity.this, R.string.app_link_timeout, Toast.LENGTH_SHORT).show();
          }
          else
          {
            addToLog(recvBuf);
          }
        }
      });

    }

  }

  private class UIReqReceiver implements UIReqListener
  {

    @Override
    public void onReceive(byte[] uiReqData)
    {
      if (uiReqData[0] == 0x3C &&
          uiReqData[1] == 0x21 &&
          uiReqData[2] == 0x44)
      {
        Intent intent = new Intent(MainActivity.this, WebViewDialogActivity.class);

        if (uiReqData != null)
        {
          intent.putExtra("data", new String(uiReqData));
        }

        startActivity(intent);
      }
      else
      {
        Log.i(TAG, "No HTML data received -> send back to sender");

        // Mirror the data back to the Vx600
        VMF.sendUIResponseData(uiReqData);
      }
    }
  }

  private class BarcodeReceiver implements BarcodeListener
  {

    @Override
    public void onReceive(final byte[] recvBuf)
    {
      MainActivity.this.runOnUiThread(new Runnable()
      {

        @Override
        public void run()
        {
          mLoadingDialog.dismissAllowingStateLoss();

          if (recvBuf.length == 2 && recvBuf[0] == 0x1B)
          {
            Toast.makeText(MainActivity.this, R.string.barcode_reader_error, Toast.LENGTH_SHORT).show();
          }
          else if (recvBuf.length > 1)
          {
            addToLog(recvBuf);
          }
        }
      });

    }

  }

  // Printer delegate implementation
  //  private class PrinterReceiver implements PrinterDataListener
  //  {
  //
  //    @Override
  //    public void onReceive(byte[] printData)
  //    {
  //        Log.i(TAG, "print received -> do nothing!");
  //    }
  //  }
}
