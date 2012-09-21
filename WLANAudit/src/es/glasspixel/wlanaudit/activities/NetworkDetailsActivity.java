/*
 * Copyright (C) 2012 Roberto Estrada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.glasspixel.wlanaudit.activities;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.adapters.WifiNetworkAdapter;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.util.ChannelCalculator;
import es.glasspixel.wlanaudit.util.IKeyCalculator;
import es.glasspixel.wlanaudit.util.WLANXXXXKeyCalculator;
import es.glasspixel.wlanaudit.util.WiFiXXXXXXKeyCalculator;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to show the details of a given network previously scanned.
 * 
 * @author Roberto Estrada
 */
@SuppressWarnings("deprecation")
public class NetworkDetailsActivity extends SherlockActivity {

	/**
	 * Unique identifier of the scan result inside the intent extra or the
	 * savedInstanceState bundle.
	 */
	public static final String SCAN_RESULT_KEY = "scan_result";
	/**
	 * The scanned network to show details about
	 */
	private ScanResult mScannedNetwork;
	/**
	 * Widget to display network's icon
	 */
	private ImageView mNetworkIcon;
	/**
	 * Widget to display network's name
	 */
	private TextView mNetworkName;
	/**
	 * Widget to display network's BSSID
	 */
	private TextView mBssidValue;
	/**
	 * Widget to display network's encryption
	 */
	private TextView mEncryptionValue;
	/**
	 * Widget to display network's frequency
	 */
	private TextView mFrequencyValue;
	/**
	 * Widget to display network's channel number
	 */
	private TextView mChannelValue;
	/**
	 * Widget to display network's signal strength
	 */
	private TextView mIntensityValue;
	/**
	 * Widget to display network's default password (if available)
	 */
	private TextView mDefaultPassValue;
	/**
	 * Copy button
	 */
	private Button mCopyButton;
	/**
	 * List of possible default keys
	 */
	private List<String> mKeyList;

	/**
	 * Lifecycle method: Activity creation
	 * 
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If a previous instance state was saved
		if (savedInstanceState != null
				&& savedInstanceState.get(SCAN_RESULT_KEY) != null) {
			// Load the state
			mScannedNetwork = (ScanResult) savedInstanceState
					.get(SCAN_RESULT_KEY);
		} else {
			// Read the network from the intent extra passed to this activity
			mScannedNetwork = (ScanResult) getIntent().getExtras().get(
					SCAN_RESULT_KEY);
		}

		// Setting content view
		setContentView(R.layout.network_details_layout);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Obtaining handles to the widgets
		mNetworkIcon = (ImageView) findViewById(R.id.networkIcon);
		mNetworkName = (TextView) findViewById(R.id.networkName);
		mBssidValue = (TextView) findViewById(R.id.bssid_value);
		mEncryptionValue = (TextView) findViewById(R.id.encryption_value);
		mFrequencyValue = (TextView) findViewById(R.id.frequency_value);
		mChannelValue = (TextView) findViewById(R.id.channel_value);
		mIntensityValue = (TextView) findViewById(R.id.intensity_value);
		mDefaultPassValue = (TextView) findViewById(R.id.password_value);
		mCopyButton = (Button) findViewById(R.id.copyPasswordButton);

		// Setup callbacks
		setupCopyButtonCallback();
	}

	private void setupCopyButtonCallback() {
		mCopyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Alert dialog
				AlertDialog.Builder dialogBuilder = new Builder(
						NetworkDetailsActivity.this);
				dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
				dialogBuilder.setTitle(getResources().getText(
						R.string.key_copy_warn_title));
				dialogBuilder.setMessage(getResources().getText(
						R.string.key_copy_warn_text));
				dialogBuilder.setPositiveButton(R.string.ok_button,
						new DialogInterface.OnClickListener() {

							
							@Override
							public void onClick(DialogInterface dialog, int unkn) {
								if (mKeyList.size() == 1) {
									// Clipboard copy
									int sdk = android.os.Build.VERSION.SDK_INT;
									if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
										android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
										clipboard.setText(mDefaultPassValue
												.getText());
									} else {
										android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
										android.content.ClipData clip = android.content.ClipData
												.newPlainText("text label",
														mDefaultPassValue
																.getText());
										clipboard.setPrimaryClip(clip);
									}

									KeysSQliteHelper usdbh = new KeysSQliteHelper(
											NetworkDetailsActivity.this,
											"DBKeys", null, 1);

									SQLiteDatabase db = usdbh
											.getWritableDatabase();

									// Si hemos abierto correctamente la
									// base de
									// datos
									if (db != null) {
										Cursor c = db
												.query("Keys",
														new String[] {
																"nombre", "key" },
														"nombre like ?",
														new String[] { mNetworkName
																.getText()
																.toString() },
														null, null,
														"nombre ASC");
										if (c.getCount() > 0) {

										} else {

											// Insertamos los datos en la tabla
											try {
												db.execSQL("INSERT INTO Keys (nombre, key) "
														+ "VALUES ('"
														+ mNetworkName
																.getText()
														+ "', '"
														+ mDefaultPassValue
																.getText()
														+ "')");

												// Cerramos la base de datos
											} catch (SQLException e) {
												Toast.makeText(
														getApplicationContext(),
														getResources()
																.getString(
																		R.string.error_saving_key),
														Toast.LENGTH_LONG)
														.show();
											}
											db.close();
										}
									}

									// ClipboardManager clipboard =
									// (ClipboardManager)
									// getSystemService(Context.CLIPBOARD_SERVICE);
									// clipboard.setText(mDefaultPassValue
									// .getText());
									// Dialog dismissing
									dialog.dismiss();
									// Copy notification
									Toast notificationToast = Toast.makeText(
											NetworkDetailsActivity.this,
											getResources().getString(
													R.string.key_copy_success),
											Toast.LENGTH_SHORT);
									notificationToast.setGravity(
											Gravity.CENTER, 0, 0);
									notificationToast.show();
								} else if (mKeyList.size() > 1) {
									Intent i = new Intent(
											NetworkDetailsActivity.this,
											KeyListActivity.class);
									i.putStringArrayListExtra(
											KeyListActivity.KEY_LIST_KEY,
											(ArrayList<String>) mKeyList);
									startActivity(i);
								}
							}
						});
				dialogBuilder.show();
			}
		});
	}

	/**
	 * Lifecycle method: Activity is about to be shown
	 * 
	 * @see android.app.Activity#onStart()
	 */
	protected void onStart() {
		super.onStart();
		// Setting network's icon
		int signalLevel = WifiManager.calculateSignalLevel(
				mScannedNetwork.level,
				WifiNetworkAdapter.MAX_SIGNAL_STRENGTH_LEVEL);
		mNetworkIcon.setImageLevel(signalLevel);
		if (WifiNetworkAdapter.getSecurity(mScannedNetwork) != WifiNetworkAdapter.SECURITY_NONE) {
			// Set an icon of encrypted wi-fi hotspot
			mNetworkIcon.setImageState(WifiNetworkAdapter.ENCRYPTED_STATE_SET,
					false);
		}
		// Setting network's values
		mNetworkName.setText(mScannedNetwork.SSID);
		mBssidValue.setText(mScannedNetwork.BSSID);
		mEncryptionValue.setText(mScannedNetwork.capabilities);
		mFrequencyValue.setText(mScannedNetwork.frequency + " MHz");
		mChannelValue.setText(String.valueOf(ChannelCalculator
				.getChannelNumber(mScannedNetwork.frequency)));
		mIntensityValue.setText(mScannedNetwork.level + " dBm");
		// Calculating key
		if (mScannedNetwork.SSID.matches("(?:WLAN|JAZZTEL)_([0-9a-fA-F]{4})")) {
			IKeyCalculator keyCalculator = new WLANXXXXKeyCalculator();
			mKeyList = keyCalculator.getKey(mScannedNetwork);
			if (mKeyList != null) {
				mDefaultPassValue.setText(mKeyList.get(0));
			} else {
				mDefaultPassValue.setText(getString(R.string.no_default_key));
				mCopyButton.setEnabled(false);
			}
		} else if (mScannedNetwork.SSID
				.matches("(?:WLAN|YACOM|WiFi)([0-9a-fA-F]{6})")) {
			IKeyCalculator keyCalculator = new WiFiXXXXXXKeyCalculator();
			mKeyList = keyCalculator.getKey(mScannedNetwork);
			mDefaultPassValue.setText(String.valueOf(mKeyList.size()) + " "
					+ getText(R.string.number_of_keys_found));
		} else {
			mDefaultPassValue.setText(getString(R.string.no_default_key));
			mCopyButton.setEnabled(false);
		}
	}

	/**
	 * Lifecycle method: Activity state saving
	 * 
	 * @see android.app.Activity#onSaveInstanceState(Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(SCAN_RESULT_KEY, mScannedNetwork);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, NetworkListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
