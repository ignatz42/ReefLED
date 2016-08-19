package org.nerdysouth.empty;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import at.abraxas.amarino.Amarino;

public class DeviceTemp extends Activity {
	private static final String DEVICE_ADDRESS = "00:18:E4:25:C2:31";
	private SharedPreferences preferences;
	
	private String deviceID;
	private Number currentWarnTempWhole;
	private Number currentWarnTempFrac;
	private Number currentMaxTempWhole;
	private Number currentMaxTempFrac;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    	
    	setContentView(R.layout.temp);

        // Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		deviceID = preferences.getString("deviceID", DEVICE_ADDRESS);
		currentWarnTempWhole = preferences.getInt("uiWarnWholeTemp", 26);
		currentWarnTempFrac = preferences.getInt("uiWarnFracTemp", 0);
		currentMaxTempWhole = preferences.getInt("uiMaxWholeTemp", 46);
		currentMaxTempFrac = preferences.getInt("uiMaxFracTemp", 0);
		
		EditText e1 = (EditText)findViewById(R.id.warnTempWholeEditText);
		e1.setText(currentWarnTempWhole.toString());

		e1 = (EditText)findViewById(R.id.warnTempFracEditText);
		e1.setText(currentWarnTempFrac.toString());
		
		e1 = (EditText)findViewById(R.id.maxTempWholeEditText);
		e1.setText(currentMaxTempWhole.toString());
		
		e1 = (EditText)findViewById(R.id.maxTempFracEditText);
		e1.setText(currentMaxTempFrac.toString());
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
	    	case(R.id.deviceTempSaveButton):
	    		Editor e = preferences.edit();
		    	boolean userMadeUpdate = false;
		    	
		    	userMadeUpdate = compareInputToCurrent(R.id.warnTempWholeEditText, currentWarnTempWhole, "uiWarnWholeTemp", e);	    	
		    	userMadeUpdate |= compareInputToCurrent(R.id.warnTempFracEditText, currentWarnTempFrac, "uiWarnFracTemp", e);
	    		if (userMadeUpdate)
	    		{
	    			// Save the new warning temp to the Arduino.
	    			//int amarinoWarnTemp = (currentWarnTempWhole.intValue() * 100 + currentWarnTempFrac.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'e', amarinoWarnTemp);
	    			Amarino.sendDataToArduino(this, deviceID, 'e', new int[]{currentWarnTempWhole.intValue(), currentWarnTempFrac.intValue()});
	    			e.commit();
	    		}
	    		
	    		userMadeUpdate = false;
		    	userMadeUpdate = compareInputToCurrent(R.id.maxTempWholeEditText, currentMaxTempWhole, "uiMaxWholeTemp", e);	    	
		    	userMadeUpdate |= compareInputToCurrent(R.id.maxTempFracEditText, currentMaxTempFrac, "uiMaxFracTemp", e);
	    		if (userMadeUpdate)
	    		{
	    			// Save the new warning temp to the Arduino.
	    			//int amarinoMaxTemp = (currentMaxTempWhole.intValue() * 100 + currentMaxTempFrac.intValue());
	    			//Amarino.sendDataToArduino(this, deviceID, 'e', amarinoMaxTemp);
	    			Amarino.sendDataToArduino(this, deviceID, 'f', new int[]{currentMaxTempWhole.intValue(), currentMaxTempFrac.intValue()});
	    			e.commit();
	    		}

	    		break;
	    	case(R.id.deviceTempCancelButton):
	    		break;
    	}
    	finish();
    }
}
