package com.example.p2ptest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView status;
	WifiP2pManager mManager;
	Channel mChannel;
	BroadcastReceiver mReceiver;
	IntentFilter mIntentFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        
	        status = (TextView) findViewById(R.id.lbl_status);
	    
	        String androidId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
	        status.setText(status.getText() + "ANDROID_ID = " + androidId + "\n");
	    
	        final Button button = (Button) findViewById(R.id.btn_listen);
	         
	        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	        mChannel = mManager.initialize(this, getMainLooper(), null);
	        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
	        
	        mIntentFilter = new IntentFilter();
	        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	        
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					status.setText(status.getText() + "thingy\n");
					mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
					    @Override
					    public void onSuccess() {
					    	 status.setText(status.getText() + "success");
					    }

					    @Override
					    public void onFailure(int reasonCode) {
					    	 status.setText(status.getText() + "failure");
					    }
					});
			    }
			});

	}
	/* register the broadcast receiver with the intent values to be matched */
	@Override
	protected void onResume() {
	    super.onResume();
	    registerReceiver(mReceiver, mIntentFilter);
	}
	/* unregister the broadcast receiver */
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(mReceiver);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
