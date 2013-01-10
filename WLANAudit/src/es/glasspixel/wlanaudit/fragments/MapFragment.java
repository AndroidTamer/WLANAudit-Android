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
package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import org.orman.mapper.Model;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.novoda.location.Locator;
import com.novoda.location.LocatorFactory;
import com.novoda.location.LocatorSettings;
import com.novoda.location.exception.NoProviderAvailable;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.WLANAuditApplication;
import es.glasspixel.wlanaudit.activities.SlidingMapActivity;
import es.glasspixel.wlanaudit.database.entities.Network;

public class MapFragment extends RoboSherlockFragment {

	@InjectView(R.id.openmapview)
	MapView myOpenMapView;

	@InjectResource(R.string.improve_precision_dialog_title)
	String improve_preciosion_dialog_title;

	@InjectResource(R.string.improve_precision_dialog_message)
	String improve_precision_dialog_message;

	@InjectResource(R.string.settings)
	String settings;

	@InjectResource(android.R.string.cancel)
	String cancel;

	@InjectResource(R.string.location_unavailable)
	String location_unavailable;

	@InjectResource(R.string.position_refreshed)
	String position_refreshed;

	@InjectResource(R.drawable.marker_blue)
	Drawable marker_blue;

	/**
	 * Constant to identify the location's settings launch when there aren't
	 * location providers enabled
	 */
	private static final int LOCATION_SETTINGS = 2;

	/**
	 * Tag to identify the class in logcat
	 */
	private static final String TAG = "MapFragment";

	private View v;

	/**
	 * OSM controller
	 */
	private MapController myMapController;
	/**
	 * Array with saved keys situations
	 */
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	/**
	 * Array with user location
	 */
	private ArrayList<OverlayItem> positionOverlayItemArray;
	private OverlayItem positionOverlay;
	/**
	 * latitude of saved key location
	 */
	protected double keyLatitude;
	/**
	 * longitude of saved key location
	 */
	protected double keyLongitude;
	/**
	 * Array with saved keys
	 */
	private List<Network> mSavedNetworks;
	/**
	 * 
	 */
	private ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay;
	/**
	 * User location in map
	 */
	protected Location myLocation;

	private Locator mLocator;
	/**
	 * Location changes listener
	 */
	private BroadcastReceiver mLocationAvailableCallBackReceiver;
	
	/**
	 * instance of parent listener
	 */
	private SlidingMapActivity listener;
	
	public interface OnMapNetworkSelected {
		public void onMapNetworkSelected(int selected_network_index);
	}

	public MapFragment() {
	}

	@Override
	public void onResume() {
		setupLocationServices();
		startReceivingLocationUpdates();
		super.onResume();
	}

	private void setupLocationServices() {
		LocatorSettings settings = new LocatorSettings(WLANAuditApplication.LOCATION_UPDATE_ACTION);
		settings.setUpdatesInterval(3 * 60 * 1000);
		settings.setUpdatesDistance(50);
		mLocator = LocatorFactory.getInstance();
		mLocator.prepare(getActivity().getApplicationContext(), settings);
		mLocationAvailableCallBackReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (myLocation == null) {
					myLocation = mLocator.getLocation();
					initLocation();
				} else {
					myLocation = mLocator.getLocation();
				}
			}
		};

	}

	@Override
	public void onPause() {
		stopReceivingLocationUpdates();
		super.onPause();
	}

	private void stopReceivingLocationUpdates() {
		getSherlockActivity().getApplicationContext().unregisterReceiver(
				mLocationAvailableCallBackReceiver);
		mLocator.stopLocationUpdates();
	}

	private void startReceivingLocationUpdates() {
		IntentFilter f = new IntentFilter();
		f.addAction(WLANAuditApplication.LOCATION_UPDATE_ACTION);
		getActivity().getApplicationContext().registerReceiver(
				mLocationAvailableCallBackReceiver, f);
		try {
			mLocator.startLocationUpdates();
		} catch (NoProviderAvailable np) {
			Log.d(TAG, "No location provider available at this time");
			AlertDialog.Builder dialogo1 = new AlertDialog.Builder(
					getSherlockActivity());
			dialogo1.setTitle(improve_preciosion_dialog_title);
			dialogo1.setMessage(improve_precision_dialog_message);
			dialogo1.setCancelable(false);
			dialogo1.setPositiveButton(settings,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, LOCATION_SETTINGS);
						}
					});
			dialogo1.setNegativeButton(cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
							dialogo1.dismiss();
						}
					});
			dialogo1.show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOCATION_SETTINGS) {

			try {
				mLocator.startLocationUpdates();
			} catch (NoProviderAvailable np) {
				Log.d(TAG, "No location provider available at this time");
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onAttach(Activity activity) {
		if (activity instanceof SlidingMapActivity)
			addListener((SlidingMapActivity) activity);
		super.onAttach(activity);
	}
	
	public void addListener(SlidingMapActivity a) {
		listener = a;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.map_layout_fragment, null);

		myOpenMapView = (MapView) v.findViewById(R.id.openmapview);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);

		anotherOverlayItemArray = new ArrayList<OverlayItem>();
		positionOverlayItemArray = new ArrayList<OverlayItem>();

		// if (mPos == -1 && savedInstanceState != null)
		// mPos = savedInstanceState.getInt("mPos");
		loadNetworksPosition();

		return v;
	}

	private void initLocation() {

		if (myLocation != null) {
			centerMap(myLocation, false);
		} else {
			Toast.makeText(getSherlockActivity(), location_unavailable,
					Toast.LENGTH_LONG).show();

		}

	}

	public void showLocation() {
		// Location lastKnownLocation = locationManager
		// .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (mLocator != null) {
			myLocation = mLocator.getLocation();
			if (myLocation != null)
				showLocation(myLocation);
		}
	}

	public void setFocused(Network s) {
		int i = 0;
		for (Network sk : mSavedNetworks) {
			if (sk.mSSID.equals(s.mSSID)) {
				break;
			}
			i++;
		}
		anotherItemizedIconOverlay.setFocusedItem(i);
		centerMap(anotherOverlayItemArray.get(i).getPoint(), false);

	}

	public void clearAllFocused() {
		// for(int i = 0; i< anotherItemizedIconOverlay.size();i++)
		// {
		anotherItemizedIconOverlay.unSetFocusedItem();

	}

	public void showLocation(Location l) {

		Toast.makeText(getSherlockActivity(), position_refreshed,
				Toast.LENGTH_LONG).show();

		changePositionInMap(l);
		this.centerMap(l, false);

	}

	public void centerMap(GeoPoint gp, boolean zoom) {

		myMapController.setCenter(gp);

		if (zoom)
			myMapController.setZoom(myOpenMapView.getMaxZoomLevel() - 5);

	}

	public void centerMap(Location l, boolean zoom) {
		final GeoPoint gp = new GeoPoint(l.getLatitude(), l.getLongitude());
		myMapController.setCenter(gp);

		if (zoom)
			myMapController.setZoom(myOpenMapView.getMaxZoomLevel() - 5);

	}

	private void loadNetworksPosition() {
		this.loadSavedKeys();
		for (Network s : mSavedNetworks) {
			anotherOverlayItemArray.add(new OverlayItem(s.mSSID, s.mBSSID, new GeoPoint(s.mLatitude,
					s.mLongitude)));

		}

		anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
				getSherlockActivity(), anotherOverlayItemArray,
				myOnItemGestureListener);
		myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);
		anotherItemizedIconOverlay.setFocusItemsOnTap(true);
		
	}

	OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() {

		@Override
		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
			
			return false;
		}

		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {
			if(listener!=null)
			{
				listener.onMapNetworkSelected(index);
			}
			return true;
		}

	};

	private void changePositionInMap(Location l) {

		if (positionOverlay != null
				&& positionOverlayItemArray.contains(positionOverlay)) {
			positionOverlayItemArray.remove(positionOverlay);
		}

		positionOverlay = new OverlayItem("My position", "", new GeoPoint(
				l.getLatitude(), l.getLongitude()));
		positionOverlay.setMarker(marker_blue);
		positionOverlayItemArray.add(0, positionOverlay);
		ItemizedOverlayWithFocus<OverlayItem> positiontemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
				getSherlockActivity(), positionOverlayItemArray, null);

		myOpenMapView.getOverlays().add(positiontemizedIconOverlay);
		myOpenMapView.invalidate();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.putInt("mPos", mPos);
	}

	protected List<Network> loadSavedKeys() {

		mSavedNetworks = Model.fetchAll(Network.class);

		return mSavedNetworks;

	}
}
