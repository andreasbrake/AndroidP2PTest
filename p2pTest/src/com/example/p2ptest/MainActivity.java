package com.example.p2ptest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that uses WiFI Direct APIs to discover and connect with available devices.
 *
 */
public class MainActivity extends Activity implements ChannelListener {

	public static final String TAG = "P2P";
	private TextView status;
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;

	private WifiP2pManager mManager;
	private Channel mChannel;
	private BroadcastReceiver mReceiver;

	WifiP2pConfig config;

	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private final IntentFilter mIntentFilter = new IntentFilter();

	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		status = (TextView) findViewById(R.id.lbl_status);

		String androidId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		status.setText(status.getText() + "ANDROID_ID = " + androidId + "\n");

		// indicates a change in the wifi p2p status
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

		// indicates a change in the list of available peers
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// indicates the state of wifi p2p connectivity has changed
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		// indicates this device's details have changed
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

		final Button button = (Button) findViewById(R.id.btn_listen);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
					@Override
					public void onSuccess() {
						status.setText(status.getText() + "success\n");
					}

					@Override
					public void onFailure(int reasonCode) {
						status.setText(status.getText() + "failure\n");
					}
				});
			}
		});

	}

	/**
	 * register the broadcast receiver with the intent values to be matched
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		registerReceiver(mReceiver, mIntentFilter);
	}

	/**
	 * unregister the broadcast receiver
	 */
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	 /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    private PeerListListener peerListListener = new PeerListListener() {
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peerList) {

			// Out with the old, in with the new.
			peers.clear();
			peers.addAll(peerList.getDeviceList());

			// If an AdapterView is backed by this data, notify it
			// of the change. For instance, if you have a ListView of available
			// peers, trigger an update.
			((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

			if (peers.size() == 0) {
				Log.d(MainActivity.TAG, "No devices found");
				return;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// TODO this method can be enhanced
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

	
	// TODO add disconnect method
	public void connect(WifiP2pConfig config) {
		// Picking the first device found on the network.
		WifiP2pDevice device = peers.get(0);

		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;

		mManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(MainActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onChannelDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
