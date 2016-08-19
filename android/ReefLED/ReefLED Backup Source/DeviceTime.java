package org.nerdysouth.empty;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import at.abraxas.amarino.Amarino;

public class DeviceTime extends Activity {
	private static final String DEVICE_ADDRESS = "00:18:E4:25:C2:31";
	private SharedPreferences preferences;
	
	private String deviceID;
	private Number currentDeviceTimeHours;
	private Number currentDeviceTimeMinutes;
	private Number currentDeviceTimeSeconds;			
	private Number currentDeviceDateMonth;			
	private Number currentDeviceDateDa;			
	private Number currentDeviceDateYear;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    	
    	setContentView(R.layout.time);

        // Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		deviceID = preferences.getString("deviceID", DEVICE_ADDRESS);
		currentDeviceTimeHours = preferences.getInt("uiCurrentTimeHours", 13);
		currentDeviceTimeMinutes = preferences.getInt("uiCurrentTimeMinutes", 4);
		currentDeviceTimeSeconds = preferences.getInt("uiCurrentTimeSeconds", 42);
		currentDeviceDateMonth = preferences.getInt("uiCurrentDateMonth", 3);
		currentDeviceDateDa = preferences.getInt("uiCurrentDateDay", 25);
		currentDeviceDateYear = preferences.getInt("uiCurrentDateYear", 2011);
		
		EditText e1 = (EditText)findViewById(R.id.currentTimeHoursEditText);
		e1.setText(currentDeviceTimeHours.toString());
		
		e1 = (EditText)findViewById(R.id.currentTimeMinutesEditText);
		e1.setText(currentDeviceTimeMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.currentTimeSecondsEditText);
		e1.setText(currentDeviceTimeSeconds.toString());
		
		e1 = (EditText)findViewById(R.id.currentDateMonthsEditText);
		e1.setText(currentDeviceDateMonth.toString());
		
		e1 = (EditText)findViewById(R.id.currentDateDaysEditText);			
		e1.setText(currentDeviceDateDa.toString());
		
		e1 = (EditText)findViewById(R.id.currentDateYearEditText);
		e1.setText(currentDeviceDateYear.toString());		
    }

    @Override
    protected void onStart()
    {
    	super.onStart();
		Amarino.connect(this, deviceID);
    }


    @Override
    public void onStop()
    {
    	super.onStop();
    	//Amarino.disconnect(this, deviceID);    
    }    
    
    public boolean compareInputToCurrent(int id, Number current, String pref, Editor e)
    {
    	boolean userMadeUpdate = false;
		EditText e1 = (EditText)findViewById(id);
		int i1 = Integer.parseInt(e1.getText().toString());
		if ( i1 != current.intValue())
		{
			userMadeUpdate = true;
			current = i1;
			e.putInt(pref, i1);
		}
		
		return userMadeUpdate;
    }
    
    public void myClickHandler(View view) {
    	switch (view.getId()) {
	    	case(R.id.deviceTimeSaveButton):
	    		Editor e = preferences.edit();
		    	boolean userMadeUpdate = false;
		    	
		    	userMadeUpdate = compareInputToCurrent(R.id.currentTimeHoursEditText, currentDeviceTimeHours, "uiCurrentTimeHours", e);	    	
		    	userMadeUpdate |= compareInputToCurrent(R.id.currentTimeMinutesEditText, currentDeviceTimeMinutes, "uiCurrentTimeMinutes", e);
		    	userMadeUpdate |= compareInputToCurrent(R.id.currentTimeSecondsEditText, currentDeviceTimeSeconds, "uiCurrentTimeSeconds", e);		    	
	    		if (userMadeUpdate)
	    		{
	    			// Save the new time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'g', currentDeviceTimeHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'h', currentDeviceTimeMinutes.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'i', currentDeviceTimeSeconds.intValue());
	    			Amarino.sendDataToArduino(this, deviceID, 'g', new int[]{currentDeviceTimeHours.intValue(), currentDeviceTimeMinutes.intValue(), currentDeviceTimeSeconds.intValue()});
	    			e.commit();
	    		}
	    		
	    		userMadeUpdate = false;    		
		    	userMadeUpdate = compareInputToCurrent(R.id.currentDateDaysEditText, currentDeviceDateDa, "uiCurrentDateMonth", e);	    	
		    	userMadeUpdate |= compareInputToCurrent(R.id.currentDateMonthsEditText, currentDeviceDateMonth, "uiCurrentDateDay", e);
		    	userMadeUpdate |= compareInputToCurrent(R.id.currentDateYearEditText, currentDeviceDateYear, "uiCurrentDateYear", e);
	    		if (userMadeUpdate)
	    		{
	    			// Save the new date to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'j', currentDeviceDateMonth.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'k', currentDeviceDateDa.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'l', currentDeviceDateYear.intValue());
	    			Amarino.sendDataToArduino(this, deviceID, 'h', new int[]{currentDeviceDateMonth.intValue(), currentDeviceDateDa.intValue(), currentDeviceDateYear.intValue()});
	    			e.commit();
	    		}	
	
	    		break;
	    	case(R.id.deviceTimeCancelButton):
	    		break;
    	}
		finish();
    }    
}
