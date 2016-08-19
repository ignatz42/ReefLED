package org.nerdysouth.empty;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
//import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.abraxas.amarino.Amarino;

public class DeviceLightMax extends Activity implements OnSeekBarChangeListener {
	private static final String DEVICE_ADDRESS = "00:18:E4:25:C2:31";
	private SharedPreferences preferences;
	
	private String deviceID;
	private long deviceDelay;
	long lastChange;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    	
    	setContentView(R.layout.max);

        // Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		deviceID = preferences.getString("deviceID", DEVICE_ADDRESS);
		deviceDelay = preferences.getLong("deviceDelay", 150);
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
		
		switch (seekBar.getId()){
			case R.id.blueSeekBar:
				operation = 'c';
				break;
			case R.id.whiteSeekBar:
				operation = 'd';
				break;
		}
		
		Amarino.sendDataToArduino(this, deviceID, operation, seekBar.getProgress());
	}

    public void myClickHandler(View view) {
    	switch (view.getId()) {
    	case(R.id.enableAutoModeCheckBox):
    		CheckBox cb = (CheckBox)findViewById(R.id.enableAutoModeCheckBox);
    	    SeekBar bSb = (SeekBar)findViewById(R.id.blueSeekBar);
    	    SeekBar wSb = (SeekBar)findViewById(R.id.whiteSeekBar);
    	    if (cb.isChecked())
    	    {
    	      bSb.setEnabled(false);
    	      wSb.setEnabled(false);
    	      Amarino.sendDataToArduino(this, deviceID, 'a', new int[] {0});
    	    }
    	    else
    	    {
    	      bSb.setEnabled(true);
    	      wSb.setEnabled(true);
    	      Amarino.sendDataToArduino(this, deviceID, 'a', new int[] {1});
    	      Amarino.sendDataToArduino(this, deviceID, 'b', new int[] {128, 128, 128, 128});
    	    }
    		break;
    	
    	}
    }
}
