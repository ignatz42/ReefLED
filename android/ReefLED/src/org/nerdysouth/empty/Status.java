package org.nerdysouth.empty;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

import java.lang.Exception;
import java.util.zip.DataFormatException;

public class Status extends Activity implements OnSeekBarChangeListener {
	private static final String DEVICE_ADDRESS = "00:18:E4:25:C2:31";

	/*
	private static final int BLUE_ON_HOUR = 0;
	private static final int BLUE_ON_MINUTE = 1;
	private static final int BLUE_RS_HOUR = 2;
	private static final int BLUE_RS_MINUTE = 3;
	private static final int BLUE_OFF_HOUR = 4;
	private static final int BLUE_OFF_MINUTE = 5;
	private static final int BLUE_CHASE_PCT = 6;
	private static final int BLUE_LED_MAX = 7;
	private static final int WHITE_ON_HOUR = 8;
	private static final int WHITE_ON_MINUTE = 9;
	private static final int WHITE_RS_HOUR = 10;
	private static final int WHITE_RS_MINUTE = 11;
	private static final int WHITE_OFF_HOUR = 12;
	private static final int WHITE_OFF_MINUTE = 13;
	private static final int WHITE_CHASE_PCT = 14;
	private static final int WHITE_LED_MAX = 15;
	private static final int WARN_TEMP_HI = 16;
	private static final int MAX_TEMP_HI = 17;
	private static final int WARN_TEMP_LOW = 18;
	private static final int MAX_TEMP_LOW = 19;
	private static final int LED0 = 20;
	private static final int LED1 = 21;
	private static final int LED2 = 22;
	private static final int LED3 = 23;
	private static final int CURRENT_TIME_HOUR = 24;
	private static final int CURRENT_TIME_MINUTE = 25;
	private static final int CURRENT_TIME_SECOND = 26;
	private static final int CURRENT_DATE_MONTH = 27;
	private static final int CURRENT_DATE_DAY = 28;
	private static final int CURRENT_DATE_YEAR = 29;
	private static final int CURRENT_TEMP_YEAR = 30;
	private static final int CURRENT_TEMP_YEAR = 31;
	private static final int EOT = 32;
*/
	
	private static final String variableNames[] = 
	{ 
		"uiBlueOnHours",
		"uiBlueOnMinutes",
		"uiBlueRsHours",
		"uiBlueRsMinutes",
		"uiBlueOffHours",
		"uiBlueOffMinutes",
		"uiBlueChasePct",
		"uiBlueMaxPct",
		"uiWhiteOnHours",
		"uiWhiteOnMinutes",
		"uiWhiteRsHours",
		"uiWhiteRsMinutes",
		"uiWhiteOffHours",
		"uiWhiteOffMinutes",
		"uiWhiteChasePct",
		"uiWhiteMaxPct",
		"uiWarnWholeTemp",
		"uiMaxWholeTemp",
		"uiWarnFracTemp",
		"uiMaxFracTemp",
		"uiCurrentLED0",
		"uiCurrentLED1",
		"uiCurrentLED2",
		"uiCurrentLED3",
		"uiCurrentTimeHours",
		"uiCurrentTimeMinutes",
		"uiCurrentTimeSeconds",
		"uiCurrentDateMonth",
		"uiCurrentDateDay",
		"uiCurrentDateYear",
		"uiCurrentTempWhole",
		"uiCurrentTempFrac"
	};
	
	// Local variables.
	private Number currentBlueOnHours;
	private Number currentBlueOnMinutes;
	private Number currentBlueRsHours;
	private Number currentBlueRsMinutes;
	private Number currentBlueOffHours;
	private Number currentBlueOffMinutes;
	private Number currentBlueChasePct;
	private Number currentBlueMaxPct;
	
	private Number currentWhiteOnHours;
	private Number currentWhiteOnMinutes;
	private Number currentWhiteRsHours;
	private Number currentWhiteRsMinutes;
	private Number currentWhiteOffHours;
	private Number currentWhiteOffMinutes;
	private Number currentWhiteChasePct;
	private Number currentWhiteMaxPct;
	
	private Number currentWarnTempWhole;
	private Number currentWarnTempFrac;
	private Number currentMaxTempWhole;
	private Number currentMaxTempFrac;	
	
	private Number currentDeviceTimeHours;
	private Number currentDeviceTimeMinutes;
	private Number currentDeviceTimeSeconds;			
	private Number currentDeviceDateMonth;			
	private Number currentDeviceDateDa;			
	private Number currentDeviceDateYear;
	
	private Number currentLED01;
	private Number currentLED02;
	private Number currentLED03;
	private Number currentLED04;
	
	private Number currentDeviceTempWhole;
	private Number currentDeviceTempFrac;
	
	private SharedPreferences preferences;
	private String deviceID;
	String[] addresses; // connected devices
	
	private long deviceDelay;
	private long lastChange;
	private boolean dataInitReceived;
	
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        dataInitReceived = false;
        
        // Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		deviceID = preferences.getString("deviceIDText", DEVICE_ADDRESS);		
		try
		{
			deviceDelay = Long.getLong(preferences.getString("deviceDelayText", "150")).longValue();
		}
		catch (Exception e)
		{
			deviceDelay = 150;
		}
		
		/*
		// Write the defaults to prefs.
		Editor e = preferences.edit();
		e.putString("deviceIDText", deviceID);
		e.putString("deviceDelayText", Long.toString(deviceDelay));
		e.commit();
		*/
		
		RefreshUIElements();
		
		// listen for device state changes
		IntentFilter intentFilter = new IntentFilter(AmarinoIntent.ACTION_CONNECTED_DEVICES);
		//intentFilter.addAction(AmarinoIntent.ACTION_CONNECTED);
	    //intentFilter.addAction(AmarinoIntent.ACTION_DISCONNECTED);
	    //intentFilter.addAction(AmarinoIntent.ACTION_CONNECTION_FAILED);
	    //intentFilter.addAction(AmarinoIntent.ACTION_PAIRING_REQUESTED);
		intentFilter.addAction(AmarinoIntent.ACTION_RECEIVED);
	    registerReceiver(arduinoReceiver, intentFilter);
	    // registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
	    
	    // request state of devices
	    Intent intent = new Intent(this, AmarinoIntent.class);
		intent.setAction(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES);
		startService(intent);		
    }
    
    @Override
	protected void onStart()
    {
		super.onStart();    			
		// Amarino.connect(this, deviceID);  
    }
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	//Amarino.disconnect(this, deviceID);
		unregisterReceiver(arduinoReceiver);      	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent i = null;
		
		switch (item.getItemId()) {
		// We have only one menu option
		case R.id.menuPreferences:
			// Launch Preference activity
			i = new Intent(Status.this, Preferences.class);
			break;
		case R.id.menuRefresh:
			DoRefresh();
			break;
		case R.id.menuWriteE:
			DoWriteEEPROM();
			break;
		}
		
		if (i != null)
		{
			startActivity(i);
		}
		
		return true;
	}
	    
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - lastChange > deviceDelay ){
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		lastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {
		char operation = '0'; // Not defined.
		int progress = seekBar.getProgress();
		
		switch (seekBar.getId()){
			case R.id.blueSeekBar:
				operation = 'c';
				currentBlueMaxPct = progress;
				break;
			case R.id.whiteSeekBar:
				operation = 'd';
				currentWhiteMaxPct = progress;
				break;
		}
		
		//Amarino.sendDataToArduino(this, deviceID, operation, progress);
		DoSendArmainoData(operation, new int[] {progress});
	}    
 
	public void myClickHandler(View view)
    {
    	switch (view.getId()) 
    	{
    		case(R.id.dateTimeLinearLayout):
    			ShowDateTimeDialog();
    			break;
    		case(R.id.tempTableLayout):
    			ShowTempDialog();
    			break;
    		case(R.id.ledsTableLayout):
    			ShowSunChangesDialog();
    			break;
        	case(R.id.enableAutoModeCheckBox):
        		CheckBox cb = (CheckBox)findViewById(R.id.enableAutoModeCheckBox);
        	    if (cb.isChecked())
        	    {
        	      //Amarino.sendDataToArduino(this, deviceID, 'a', new int[] {0});
        	      DoSendArmainoData('a', new int[] {0});
        	    }
        	    else
        	    {
        	      DoSendArmainoData('a', new int[] {1});
        	      DoSendArmainoData('b', new int[] {128, 128, 128, 128});
        	      //Amarino.sendDataToArduino(this, deviceID, 'a', new int[] {1});
        	      //Amarino.sendDataToArduino(this, deviceID, 'b', new int[] {128, 128, 128, 128});
        	    }
        	    RefreshUIElements();
        		break;    		
    	}
    }
	

    public boolean compareInputToCurrent(TextView t1, Number current, String pref)
    {
    	boolean userMadeUpdate = false;
    	Editor e = preferences.edit();
    	
		int i1 = Integer.parseInt(t1.getText().toString());
		if ( i1 != current.intValue())
		{
			userMadeUpdate = true;
			current = i1;
			e.putInt(pref, i1);
			e.commit();
		}
		
		return userMadeUpdate;
    }
    

    private void ShowDateTimeDialog() 
    {
		LayoutInflater inflater = LayoutInflater.from(Status.this);
		final View device_time = inflater.inflate(R.layout.time, null);

		final TextView etth = (EditText) device_time.findViewById(R.id.currentTimeHoursEditText);
		etth.setText(currentDeviceTimeHours.toString());
		final TextView ettm = (EditText) device_time.findViewById(R.id.currentTimeMinutesEditText);
		ettm.setText(currentDeviceTimeMinutes.toString());
		final TextView etts = (EditText) device_time.findViewById(R.id.currentTimeSecondsEditText);
		etts.setText(currentDeviceTimeSeconds.toString());
		final TextView etdm = (EditText) device_time.findViewById(R.id.currentDateMonthsEditText);
		etdm.setText(currentDeviceDateMonth.toString());
		final TextView etdd = (EditText) device_time.findViewById(R.id.currentDateDaysEditText);
		etdd.setText(currentDeviceDateDa.toString());
		final TextView etdy = (EditText) device_time.findViewById(R.id.currentDateYearEditText);
		etdy.setText(currentDeviceDateYear.toString());
		
		AlertDialog dialog = new AlertDialog.Builder(Status.this)
	    	.setTitle("Enter the current date time.")
	    	.setView(device_time)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog, int whichButton) 
	    		{
			    	boolean userMadeUpdate = false;
			    	
			    	userMadeUpdate = compareInputToCurrent(etth, currentDeviceTimeHours, "uiCurrentTimeHours");	    	
			    	userMadeUpdate |= compareInputToCurrent(ettm, currentDeviceTimeMinutes, "uiCurrentTimeMinutes");
			    	userMadeUpdate |= compareInputToCurrent(etts, currentDeviceTimeSeconds, "uiCurrentTimeSeconds");		    	
		    		if (userMadeUpdate)
		    		{
		    			// Save the new time to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'g', new int[]{currentDeviceTimeHours.intValue(), currentDeviceTimeMinutes.intValue(), currentDeviceTimeSeconds.intValue()});
		    			DoSendArmainoData('g', new int[]{currentDeviceTimeHours.intValue(), currentDeviceTimeMinutes.intValue(), currentDeviceTimeSeconds.intValue()});
		    		}
		    		
		    		userMadeUpdate = false;    		
			    	userMadeUpdate = compareInputToCurrent(etdm, currentDeviceDateDa, "uiCurrentDateMonth");	    	
			    	userMadeUpdate |= compareInputToCurrent(etdd, currentDeviceDateMonth, "uiCurrentDateDay");
			    	userMadeUpdate |= compareInputToCurrent(etdy, currentDeviceDateYear, "uiCurrentDateYear");
		    		if (userMadeUpdate)
		    		{
		    			// Save the new date to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'h', new int[]{currentDeviceDateMonth.intValue(), currentDeviceDateDa.intValue(), currentDeviceDateYear.intValue()});
		    			DoSendArmainoData('h', new int[]{currentDeviceDateMonth.intValue(), currentDeviceDateDa.intValue(), currentDeviceDateYear.intValue()});
		    		}	    	    	
	    		}
	    	})
	    	.setNegativeButton("Cancel", null).create();
			dialog.show();
	}

	private void ShowTempDialog() 
	{
		LayoutInflater inflater = LayoutInflater.from(Status.this);
		final View deviceTemp = inflater.inflate(R.layout.temp, null);

		final TextView etwwt = (EditText) deviceTemp.findViewById(R.id.warnTempWholeEditText);
		etwwt.setText(currentWarnTempWhole.toString());
		final TextView etwft = (EditText) deviceTemp.findViewById(R.id.warnTempFracEditText);
		etwft.setText(currentWarnTempFrac.toString());
		final TextView etmwt = (EditText) deviceTemp.findViewById(R.id.maxTempWholeEditText);
		etmwt.setText(currentMaxTempWhole.toString());
		final TextView etmft = (EditText) deviceTemp.findViewById(R.id.maxTempFracEditText);
		etmft.setText(currentMaxTempFrac.toString());
		
		AlertDialog dialog = new AlertDialog.Builder(Status.this)
	    	.setTitle("Set temperature limits.")
	    	.setView(deviceTemp)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog, int whichButton) 
	    		{
			    	boolean userMadeUpdate = false;
			    	
			    	userMadeUpdate = compareInputToCurrent(etwwt, currentWarnTempWhole, "uiWarnWholeTemp");	    	
			    	userMadeUpdate |= compareInputToCurrent(etwft, currentWarnTempFrac, "uiWarnFracTemp");
		    		if (userMadeUpdate)
		    		{
		    			// Save the new warning temp to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'e', new int[]{currentWarnTempWhole.intValue(), currentWarnTempFrac.intValue()});
		    			DoSendArmainoData('e', new int[]{currentWarnTempWhole.intValue(), currentWarnTempFrac.intValue()});
		    		}
		    		
		    		userMadeUpdate = false;
			    	userMadeUpdate = compareInputToCurrent(etmwt, currentMaxTempWhole, "uiMaxWholeTemp");	    	
			    	userMadeUpdate |= compareInputToCurrent(etmft, currentMaxTempFrac, "uiMaxFracTemp");
		    		if (userMadeUpdate)
		    		{
		    			// Save the new warning temp to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'f', new int[]{currentMaxTempWhole.intValue(), currentMaxTempFrac.intValue()});
		    			DoSendArmainoData('f', new int[]{currentMaxTempWhole.intValue(), currentMaxTempFrac.intValue()});
		    		}


	    		}
	    	})
	    	.setNegativeButton("Cancel", null).create();
			dialog.show();
	}

	private void ShowSunChangesDialog() 
    {
		LayoutInflater inflater = LayoutInflater.from(Status.this);
		final View sun_changes = inflater.inflate(R.layout.sun_changes, null);

		final TextView etboh = (EditText) sun_changes.findViewById(R.id.blueOnHoursEditText);
		etboh.setText(currentBlueOnHours.toString());
		final TextView etbom = (EditText) sun_changes.findViewById(R.id.blueOnMinutesEditText);
		etbom.setText(currentBlueOnMinutes.toString());
		final TextView etbrh = (EditText) sun_changes.findViewById(R.id.blueRSHoursEditText);
		etbrh.setText(currentBlueRsHours.toString());
		final TextView etbrm = (EditText) sun_changes.findViewById(R.id.blueRSMinutesEditText);
		etbrm.setText(currentBlueRsMinutes.toString());
		final TextView etbfh = (EditText) sun_changes.findViewById(R.id.blueOffHoursEditText);
		etbfh.setText(currentBlueOffHours.toString());
		final TextView etbfm = (EditText) sun_changes.findViewById(R.id.blueOffMinutesEditText);
		etbfm.setText(currentBlueOffMinutes.toString());
		final TextView etbcp = (EditText) sun_changes.findViewById(R.id.blueChasePercentageEditText);
		etbcp.setText(currentBlueChasePct.toString());
		final TextView etwoh = (EditText) sun_changes.findViewById(R.id.whiteOnHoursEditText);
		etwoh.setText(currentWhiteOnHours.toString());
		final TextView etwom = (EditText) sun_changes.findViewById(R.id.whiteOnMinutessEditText);
		etwom.setText(currentWhiteOnMinutes.toString());
		final TextView etwrh = (EditText) sun_changes.findViewById(R.id.whiteRSHoursEditText);
		etwrh.setText(currentWhiteRsHours.toString());
		final TextView etwrm = (EditText) sun_changes.findViewById(R.id.whiteRSMinutesEditText);
		etwrm.setText(currentWhiteRsMinutes.toString());
		final TextView etwfh = (EditText) sun_changes.findViewById(R.id.whiteOffHoursEditText);
		etwfh.setText(currentWhiteOffHours.toString());
		final TextView etwfm = (EditText) sun_changes.findViewById(R.id.whiteOffMinutesEditText);
		etwfm.setText(currentWhiteOffMinutes.toString());
		final TextView etwcp = (EditText) sun_changes.findViewById(R.id.whiteChasePercentageEditText);
		etwcp.setText(currentWhiteChasePct.toString());
		
		AlertDialog dialog = new AlertDialog.Builder(Status.this)
	    	.setTitle("Enter the run times and parameters.")
	    	.setView(sun_changes)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog, int whichButton) 
	    		{	    			
			    	boolean userMadeUpdate = false;
			    	
			    	userMadeUpdate = compareInputToCurrent(etboh, currentBlueOnHours, "uiBlueOnHours");	    	
			    	userMadeUpdate |= compareInputToCurrent(etbom, currentBlueOnMinutes, "uiBlueOnMinutes");
		    		if (userMadeUpdate)
		    		{
		    			// Save the new blue on time to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'i', new int[]{currentBlueOnHours.intValue(), currentBlueOnMinutes.intValue()});
		    			DoSendArmainoData('i', new int[]{currentBlueOnHours.intValue(), currentBlueOnMinutes.intValue()});
		    		}
		    		
		    		userMadeUpdate = false;	    		
		    		userMadeUpdate = compareInputToCurrent(etbrh, currentBlueRsHours, "uiBlueRsHours");
		    		userMadeUpdate |= compareInputToCurrent(etbrm, currentBlueRsMinutes, "uiBlueRsMinutes");	    		
		    		if (userMadeUpdate)
		    		{
		    			// Save the new blue rs time to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'j', new int[]{currentBlueRsHours.intValue(), currentBlueRsMinutes.intValue()});
		    			DoSendArmainoData('j', new int[]{currentBlueRsHours.intValue(), currentBlueRsMinutes.intValue()});
		    		}
		    		
		    		userMadeUpdate = false;	    		
			    	userMadeUpdate = compareInputToCurrent(etbfh, currentBlueRsHours, "uiBlueRsHours");
			    	userMadeUpdate |= compareInputToCurrent(etbfm, currentBlueOffMinutes, "uiBlueOffMinutes");	    		
		    		if (userMadeUpdate)
		    		{
		    			// Save the new blue off time to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'k', new int[]{currentBlueOffHours.intValue(), currentBlueOffMinutes.intValue()});
		    			DoSendArmainoData('k', new int[]{currentBlueOffHours.intValue(), currentBlueOffMinutes.intValue()});
		    		}
		    		
		    		userMadeUpdate = false;	    		
			    	userMadeUpdate = compareInputToCurrent(etbcp, currentBlueChasePct, "uiBlueChasePct");
		    		if (userMadeUpdate)
		    		{
		    			// Save the new blue chase % to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'l', currentBlueChasePct.intValue());
		    			DoSendArmainoData('l', new int[] {currentBlueChasePct.intValue()});
		    		}
		    		
		    		userMadeUpdate = false;
			    	userMadeUpdate = compareInputToCurrent(etwoh, currentWhiteOnHours, "uiWhiteOnHours");
					userMadeUpdate |= compareInputToCurrent(etwom, currentWhiteOnMinutes, "uiWhiteOnMinutes");
					if (userMadeUpdate)
					{
						// Save the new white on time to the Arduino.
						//Amarino.sendDataToArduino(Status.this, deviceID, 'm', new int[]{currentWhiteOnHours.intValue(), currentWhiteOnMinutes.intValue()});
						DoSendArmainoData('m', new int[]{currentWhiteOnHours.intValue(), currentWhiteOnMinutes.intValue()});
					}
					
					userMadeUpdate = false;	    		
					userMadeUpdate = compareInputToCurrent(etwrh, currentWhiteRsHours, "uiWhiteRsHours");
					userMadeUpdate |= compareInputToCurrent(etwrm, currentWhiteRsMinutes, "uiWhiteRsMinutes");	    		
					if (userMadeUpdate)
					{
						// Save the new white rs time to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'n', new int[]{currentWhiteRsHours.intValue(), currentWhiteRsMinutes.intValue()});
		    			DoSendArmainoData('n', new int[]{currentWhiteRsHours.intValue(), currentWhiteRsMinutes.intValue()});
					}
					
					userMadeUpdate = false;	    		
					userMadeUpdate = compareInputToCurrent(etwfh, currentWhiteRsHours, "uiWhiteOffHours");
					userMadeUpdate |= compareInputToCurrent(etwfm, currentWhiteOffMinutes, "uiWhiteOffMinutes");	    		
					if (userMadeUpdate)
					{
						// Save the new white off time to the Arduino.
						//Amarino.sendDataToArduino(Status.this, deviceID, 'o', new int[]{currentWhiteOffHours.intValue(), currentWhiteOffMinutes.intValue()});
						DoSendArmainoData('o', new int[]{currentWhiteOffHours.intValue(), currentWhiteOffMinutes.intValue()});
					}
					
					userMadeUpdate = false;	    		
					userMadeUpdate = compareInputToCurrent(etwcp, currentWhiteChasePct, "uiWhiteChasePct");
					if (userMadeUpdate)
					{
						// Save the new white chase % to the Arduino.
		    			//Amarino.sendDataToArduino(Status.this, deviceID, 'p', new int[] {currentWhiteChasePct.intValue()});
		    			DoSendArmainoData('p', new int[] {currentWhiteChasePct.intValue()});
					}	    			

	    		}
	    	})
	    	.setNegativeButton("Cancel", null).create();
			dialog.show();		
	}

	/**/
	public void RefreshUIElements()
    {
    	// Blue times.
    	currentBlueOnHours       = preferences.getInt("uiBlueOnHours", 8);
    	currentBlueOnMinutes     = preferences.getInt("uiBlueOnMinutes", 0);
    	currentBlueRsHours       = preferences.getInt("uiBlueRsHours", 2);
    	currentBlueRsMinutes     = preferences.getInt("uiBlueRsMinutes", 0);
    	currentBlueOffHours      = preferences.getInt("uiBlueOffHours", 20);
    	currentBlueOffMinutes    = preferences.getInt("uiBlueOffMinutes", 0);
    	currentBlueChasePct      = preferences.getInt("uiBlueChasePct", 50);
    	currentBlueMaxPct        = preferences.getInt("uiBlueMaxPct", 80);
    	
    	// White times.
    	currentWhiteOnHours      = preferences.getInt("uiWhiteOnHours", 10);
    	currentWhiteOnMinutes    = preferences.getInt("uiWhiteOnMinutes", 0);
    	currentWhiteRsHours      = preferences.getInt("uiWhiteRsHours", 2);
    	currentWhiteRsMinutes    = preferences.getInt("uiWhiteRsMinutes", 0);
    	currentWhiteOffHours     = preferences.getInt("uiWhiteOffHours", 18);
    	currentWhiteOffMinutes   = preferences.getInt("uiWhiteOffMinutes", 0);
    	currentWhiteChasePct     = preferences.getInt("uiWhiteChasePct", 50);
    	currentWhiteMaxPct       = preferences.getInt("uiWhiteMaxPct", 80);
    	
    	// Temperatures
    	currentWarnTempWhole     = preferences.getInt("uiWarnWholeTemp", 46);
    	currentWarnTempFrac      = preferences.getInt("uiWarnFracTemp", 0);
    	currentMaxTempWhole      = preferences.getInt("uiMaxWholeTemp", 49);			
    	currentMaxTempFrac       = preferences.getInt("uiMaxFracTemp", 0);
    	
    	// Device Time.
    	currentDeviceTimeHours   = preferences.getInt("uiCurrentTimeHours", 3);
    	currentDeviceTimeMinutes = preferences.getInt("uiCurrentTimeMinutes", 25);
    	currentDeviceTimeSeconds = preferences.getInt("uiCurrentTimeSeconds", 49);			
    	currentDeviceDateMonth   = preferences.getInt("uiCurrentDateMonth", 3);			
    	currentDeviceDateDa      = preferences.getInt("uiCurrentDateDay", 25);			
    	currentDeviceDateYear    = preferences.getInt("uiCurrentDateYear", 2011);

    	// LED Values.
		currentLED01             = preferences.getInt("uiCurrentLED0", 0);
		currentLED02             = preferences.getInt("uiCurrentLED1", 0);
		currentLED03             = preferences.getInt("uiCurrentLED2", 0);
		currentLED04             = preferences.getInt("uiCurrentLED3", 0);
		
		// Device Temp.
		currentDeviceTempWhole = preferences.getInt("uiCurrentTempWhole", 0);
		currentDeviceTempFrac = preferences.getInt("uiCurrentTempFrac", 0);
		
		TextView e1;

		e1 = (TextView)findViewById(R.id.currentDateString);
		e1.setText( currentDeviceDateMonth.toString() + "/" +
				    currentDeviceDateDa.toString() + "/" +
				    currentDeviceDateYear.toString());
		
		e1 = (TextView)findViewById(R.id.currentTimeString);
		e1.setText( currentDeviceTimeHours.toString() + ":" +
				    currentDeviceTimeMinutes.toString() + ":" +
				    currentDeviceTimeSeconds.toString());
		
		e1 = (TextView)findViewById(R.id.currentTempString);
		e1.setText( currentDeviceTempWhole.toString() + "." +
				    currentDeviceTempFrac.toString());
		
		e1 = (TextView)findViewById(R.id.warningTempString);
		e1.setText( currentWarnTempWhole.toString() + "." +
				    currentWarnTempFrac.toString());
		
		e1 = (TextView)findViewById(R.id.maxTempString);
		e1.setText( currentMaxTempWhole.toString() + "." +
				    currentMaxTempFrac.toString());
		
		e1 = (TextView)findViewById(R.id.blueLefttValueEditText);
		e1.setText(currentLED01.toString());
		
		e1 = (TextView)findViewById(R.id.blueRightValueEditText);
		e1.setText(currentLED02.toString());
		
		e1 = (TextView)findViewById(R.id.whiteLefttValueEditText);
		e1.setText(currentLED03.toString());
		
		e1 = (TextView)findViewById(R.id.whiteRightValueEditText);
		e1.setText(currentLED04.toString());
		
		//CheckBox cb = (CheckBox)findViewById(R.id.enableAutoModeCheckBox);
		boolean cbChecked = ((CheckBox)findViewById(R.id.enableAutoModeCheckBox)).isChecked(); 
		SeekBar sb;
		
		sb = (SeekBar)findViewById(R.id.blueSeekBar);
		sb.setProgress(currentBlueMaxPct.intValue());
		sb.setEnabled(!cbChecked);
		sb.refreshDrawableState();
		
		sb = (SeekBar)findViewById(R.id.whiteSeekBar);
		sb.setProgress(currentWhiteMaxPct.intValue());
		sb.setEnabled(!cbChecked);
		sb.refreshDrawableState();
    }


    public void DoRefresh() 
    {
    	//Amarino.sendDataToArduino(this, deviceID, 'r', new int[] {0});
    	DoSendArmainoData('r', new int[] {1});
    }
    

    public void DoWriteEEPROM() 
    {
    	// Amarino.sendDataToArduino(this, deviceID, 'q', new int[] {0});
    	DoSendArmainoData('q', new int[] {0});
    }
    
    public void DoSendArmainoData(char operation, int[] data)
    {
    	// Check for connection. 
		if (addresses == null)
		{
			Toast.makeText(Status.this, "No connected device found!\n\nData not sent.", Toast.LENGTH_SHORT).show();
		}
		else 
		{    	
			for(int i = 0; i < addresses.length; i++)
			{
			  if (addresses[i] == deviceID)
			  {
				// In practice, it seems that 8 - 10 calls are necessary before
				// the Arduion is really really ready to receive requets.
				if (!dataInitReceived && operation != 'r')  
				{
					Toast.makeText(Status.this, "Device not ready!\n\nSending refresh request instead.", Toast.LENGTH_SHORT).show();
					operation = 'r'; // Refresh
					data = new int[] {1};
				}
				Amarino.sendDataToArduino(Status.this, deviceID, operation, data);
			  }
			}
		}
    }
    
	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * AmarinoIntent.ACTION_RECEIVED events.
	 * 
	 * It extracts data from the intent and updates the values in stored prefs.
	 */
	public class ArduinoReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) {

			String data = null;
			String action = intent.getAction();
			
			if (action == null) return;
			
			// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
			// final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			
			if (AmarinoIntent.ACTION_CONNECTED_DEVICES.equals(action))
			{
				addresses = intent.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
				
				// If the list of addresses is empty then request a new connection
				if (addresses == null)
				{
				  Amarino.connect(Status.this, deviceID);
				}
			}
			else if (AmarinoIntent.ACTION_CONNECTED.equals(action))
			{
				// If a connection was received, make sure addresses has a good list.
			}
			else if (AmarinoIntent.ACTION_DISCONNECTED.equals(action))
			{
				// Clear addresses.
			}
			else if (AmarinoIntent.ACTION_CONNECTION_FAILED.equals(action))
			{
				// Clear addresses.
			}
			else if (AmarinoIntent.ACTION_PAIRING_REQUESTED.equals(action))
			{
				// Can I autorespond to this?
			}
			else if (AmarinoIntent.ACTION_RECEIVED.equals(action))
			{
				// the type of data which is added to the intent
				final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
				
				// we only expect String data though, but it is better to check if really string was sent
				// later Amarino will support different data types, so far data comes always as string and
				// you have to parse the data to the type you have sent from Arduino, like it is shown below
				if (dataType == AmarinoIntent.STRING_EXTRA)
				{
					data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
					
					if (data != null)
					{
						try 
						{						
							String[] fields = data.split("=");
							if ((fields.length != 2) || (fields[0] == null) || (fields[1] == null))
							{
								throw new DataFormatException("Bad arugment.");
							}
							
							int varID = Integer.parseInt(fields[0].toString());
							int varVal = Integer.parseInt(fields[1].toString());						
							
							Editor e = preferences.edit();
							e.putInt(variableNames[varID], varVal);
							e.commit();

							RefreshUIElements();
						}
                        catch (DataFormatException de)
                        {
                        	// TODO: Testing shows that for some reason the bluetooth modem's 
                        	// initialization string is echo'd Amarino.  As a workaround, the 
                        	// device should emit some string like "Initialization Finished."
                        	// as a key that it's really really ready to receive requests.
                        	if (data.contains("Finished"))
                        		dataInitReceived = true;
                        }
						catch (Exception e) //(NumberFormatException e) 
						{ /* oh data was not an integer */ }
			
					}
				}			
			}
			else 
			{
				return;			
			}
		}
	}    
}