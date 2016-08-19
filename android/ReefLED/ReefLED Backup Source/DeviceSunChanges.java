package org.nerdysouth.empty;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import at.abraxas.amarino.Amarino;

public class DeviceSunChanges extends Activity {
	private static final String DEVICE_ADDRESS = "00:18:E4:25:C2:31";
	private SharedPreferences preferences;
	
	private String deviceID;
	private Number currentBlueOnHours;
	private Number currentBlueOnMinutes;
	private Number currentBlueRsHours;
	private Number currentBlueRsMinutes;
	private Number currentBlueOffHours;
	private Number currentBlueOffMinutes;
	private Number currentBlueChasePct;

	private Number currentWhiteOnHours;
	private Number currentWhiteOnMinutes;
	private Number currentWhiteRsHours;
	private Number currentWhiteRsMinutes;
	private Number currentWhiteOffHours;
	private Number currentWhiteOffMinutes;
	private Number currentWhiteChasePct;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    	
    	setContentView(R.layout.sun_changes);

        // Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		deviceID = preferences.getString("deviceID", DEVICE_ADDRESS);
		currentBlueOnHours = preferences.getInt("uiBlueOnHours", 8);
		currentBlueOnMinutes = preferences.getInt("uiBlueOnMinutes", 0);
		currentBlueRsHours = preferences.getInt("uiBlueRsHours", 2);
		currentBlueRsMinutes = preferences.getInt("uiBlueRsMinutes", 0);
		currentBlueOffHours = preferences.getInt("uiBlueOffHours", 20);
		currentBlueOffMinutes = preferences.getInt("uiBlueOffMinutes", 0);
		currentBlueChasePct = preferences.getInt("uiBlueChasePct", 50);
		currentWhiteOnHours = preferences.getInt("uiWhiteOnHours", 10);
		currentWhiteOnMinutes = preferences.getInt("uiWhiteOnMinutes", 0);
		currentWhiteRsHours = preferences.getInt("uiWhiteRsHours", 2);
		currentWhiteRsMinutes = preferences.getInt("uiWhiteRsMinutes", 0);
		currentWhiteOffHours = preferences.getInt("uiWhiteOffHours", 18);
		currentWhiteOffMinutes = preferences.getInt("uiWhiteOffMinutes", 0);
		currentWhiteChasePct = preferences.getInt("uiWhiteChasePct", 50);
		
		EditText e1 = (EditText)findViewById(R.id.blueOnHoursEditText);
		e1.setText(currentBlueOnHours.toString());
		
		e1 = (EditText)findViewById(R.id.blueOnMinutesEditText);
		e1.setText(currentBlueOnMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.blueRSHoursEditText);
		e1.setText(currentBlueRsHours.toString());
		
		e1 = (EditText)findViewById(R.id.blueRSMinutesEditText);
		e1.setText(currentBlueRsMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.blueOffHoursEditText);
		e1.setText(currentBlueOffHours.toString());
		
		e1 = (EditText)findViewById(R.id.blueOffMinutesEditText);
		e1.setText(currentBlueOffMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.blueChasePercentageEditText);
		e1.setText(currentBlueChasePct.toString());
		
		e1 = (EditText)findViewById(R.id.whiteOnHoursEditText);
		e1.setText(currentWhiteOnHours.toString());
		
		e1 = (EditText)findViewById(R.id.whiteOnMinutessEditText);
		e1.setText(currentWhiteOnMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.whiteRSHoursEditText);
		e1.setText(currentWhiteRsHours.toString());
		
		e1 = (EditText)findViewById(R.id.whiteRSMinutesEditText);
		e1.setText(currentWhiteRsMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.whiteOffHoursEditText);
		e1.setText(currentWhiteOffHours.toString());
		
		e1 = (EditText)findViewById(R.id.whiteOffMinutesEditText);
		e1.setText(currentWhiteOffMinutes.toString());
		
		e1 = (EditText)findViewById(R.id.whiteChasePercentageEditText);
		e1.setText(currentWhiteChasePct.toString());
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
	    	case(R.id.deviceSunChangesPositiveButton):
	    		Editor e = preferences.edit();
		    	boolean userMadeUpdate = false;
		    	
		    	userMadeUpdate = compareInputToCurrent(R.id.blueOnHoursEditText, currentBlueOnHours, "uiBlueOnHours", e);	    	
		    	userMadeUpdate |= compareInputToCurrent(R.id.blueOnMinutesEditText, currentBlueOnMinutes, "uiBlueOnMinutes", e);
	    		if (userMadeUpdate)
	    		{
	    			// Save the new blue on time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'm', currentBlueOnHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'n', currentBlueOnMinutes.intValue());
	    			Amarino.sendDataToArduino(this, deviceID, 'i', new int[]{currentBlueOnHours.intValue(), currentBlueOnMinutes.intValue()});
	    			e.commit();
	    		}
	    		
	    		userMadeUpdate = false;	    		
	    		userMadeUpdate = compareInputToCurrent(R.id.blueRSHoursEditText, currentBlueRsHours, "uiBlueRsHours", e);
	    		userMadeUpdate |= compareInputToCurrent(R.id.blueRSMinutesEditText, currentBlueRsMinutes, "uiBlueRsMinutes", e);	    		
	    		if (userMadeUpdate)
	    		{
	    			// Save the new blue rs time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'o', currentBlueRsHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'p', currentBlueRsMinutes.intValue());
	    			Amarino.sendDataToArduino(this, deviceID, 'j', new int[]{currentBlueRsHours.intValue(), currentBlueRsMinutes.intValue()});
	    			e.commit();
	    		}
	    		
	    		userMadeUpdate = false;	    		
		    	userMadeUpdate = compareInputToCurrent(R.id.blueOffHoursEditText, currentBlueRsHours, "uiBlueRsHours", e);
		    	userMadeUpdate |= compareInputToCurrent(R.id.blueOffMinutesEditText, currentBlueOffMinutes, "uiBlueOffMinutes", e);	    		
	    		if (userMadeUpdate)
	    		{
	    			// Save the new blue off time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'q', currentBlueOffHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'r', currentBlueOffMinutes.intValue());
	    			Amarino.sendDataToArduino(this, deviceID, 'k', new int[]{currentBlueOffHours.intValue(), currentBlueOffMinutes.intValue()});
	    			e.commit();
	    		}
	    		
	    		userMadeUpdate = false;	    		
		    	userMadeUpdate = compareInputToCurrent(R.id.blueChasePercentageEditText, currentBlueChasePct, "uiBlueChasePct", e);
	    		if (userMadeUpdate)
	    		{
	    			// Save the new blue chase % to the Arduino.
	    			Amarino.sendDataToArduino(this, deviceID, 'l', currentBlueChasePct.intValue());
	    			e.commit();
	    		}
	    		
	    		userMadeUpdate = false;
		    	userMadeUpdate = compareInputToCurrent(R.id.whiteOnHoursEditText, currentWhiteOnHours, "uiWhiteOnHours", e);
				userMadeUpdate |= compareInputToCurrent(R.id.whiteOnMinutessEditText, currentWhiteOnMinutes, "uiWhiteOnMinutes", e);
				if (userMadeUpdate)
				{
					// Save the new white on time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 't', currentWhiteOnHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'u', currentWhiteOnMinutes.intValue());
					Amarino.sendDataToArduino(this, deviceID, 'm', new int[]{currentWhiteOnHours.intValue(), currentWhiteOnMinutes.intValue()});
					e.commit();
				}
				
				userMadeUpdate = false;	    		
				userMadeUpdate = compareInputToCurrent(R.id.whiteRSHoursEditText, currentWhiteRsHours, "uiWhiteRsHours", e);
				userMadeUpdate |= compareInputToCurrent(R.id.whiteRSMinutesEditText, currentWhiteRsMinutes, "uiWhiteRsMinutes", e);	    		
				if (userMadeUpdate)
				{
					// Save the new white rs time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'v', currentWhiteRsHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'w', currentWhiteRsMinutes.intValue());
					Amarino.sendDataToArduino(this, deviceID, 'n', new int[]{currentWhiteRsHours.intValue(), currentWhiteRsMinutes.intValue()});
					e.commit();
				}
				
				userMadeUpdate = false;	    		
				userMadeUpdate = compareInputToCurrent(R.id.whiteOffHoursEditText, currentWhiteRsHours, "uiWhiteOffHours", e);
				userMadeUpdate |= compareInputToCurrent(R.id.whiteOffMinutesEditText, currentWhiteOffMinutes, "uiWhiteOffMinutes", e);	    		
				if (userMadeUpdate)
				{
					// Save the new white off time to the Arduino.
	    			//Amarino.sendDataToArduino(this, deviceID, 'x', currentWhiteOffHours.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'y', currentWhiteOffMinutes.intValue());
					Amarino.sendDataToArduino(this, deviceID, 'o', new int[]{currentWhiteOffHours.intValue(), currentWhiteOffMinutes.intValue()});
					e.commit();
				}
				
				userMadeUpdate = false;	    		
				userMadeUpdate = compareInputToCurrent(R.id.whiteChasePercentageEditText, currentWhiteChasePct, "uiWhiteChasePct", e);
				if (userMadeUpdate)
				{
					// Save the new white chase % to the Arduino.
	    			Amarino.sendDataToArduino(this, deviceID, 'p', new int[] {currentWhiteChasePct.intValue()});
					e.commit();
				}

	    		break;
	    	case(R.id.deviceSunChangesNegativeButton):
	    		break;
    	}
    	finish();
    }     
}