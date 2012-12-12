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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;
import es.glasspixel.wlanaudit.database.entities.Network;
import es.glasspixel.wlanaudit.dominio.SavedKeysUtils;

public class MapFragment extends RoboSherlockFragment {
	private static final int LOCATION_SETTINGS = 2;
	private int mPos = -1;
	private int mImgRes;
	private LocationManager locationManager;
	private String bestProvider;
	private View v;
	private MapView myOpenMapView;
	private MapController myMapController;
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	private ArrayList<OverlayItem> positionOverlayItemArray;
	private OverlayItem positionOverlay;
	protected double keyLatitude;
	protected double keyLongitude;
	private List<Network> mKeys;
	private ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay;
	protected Location myLocation;

	public MapFragment() {
	}

	@Override
	public void onResume() {
		if (myLocation != null) {
			centerMap(myLocation, false);
		}

		super.onResume();
	}

	public MapFragment(int pos) {
		mPos = pos;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.map_layout_fragment, null);

		locationManager = (LocationManager) getSherlockActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		// Criteria criteria = new Criteria();
		// bestProvider = locationManager
		// .getProvider(LocationManager.NETWORK_PROVIDER);

		myOpenMapView = (MapView) v.findViewById(R.id.openmapview);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);

		anotherOverlayItemArray = new ArrayList<OverlayItem>();
		positionOverlayItemArray = new ArrayList<OverlayItem>();

		// if (locationManager != null && bestProvider != null) {

		// if (bestProvider.equals(LocationManager.NETWORK_PROVIDER)) {
		if (!locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			AlertDialog.Builder dialogo1 = new AlertDialog.Builder(
					getSherlockActivity());
			dialogo1.setTitle(getSherlockActivity().getResources().getString(
					R.string.improve_precision_dialog_title));
			dialogo1.setMessage(getSherlockActivity().getResources().getString(
					R.string.improve_precision_dialog_message));
			dialogo1.setCancelable(false);
			dialogo1.setPositiveButton(getSherlockActivity().getResources()
					.getString(R.string.settings),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivityForResult(intent, LOCATION_SETTINGS);
						}
					});
			dialogo1.setNegativeButton(getSherlockActivity().getResources()
					.getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogo1, int id) {
							dialogo1.dismiss();
						}
					});
			dialogo1.show();

		} else {

			initLocation();

		}

		if (mPos == -1 && savedInstanceState != null)
			mPos = savedInstanceState.getInt("mPos");
		loadKeysPosition();

		return v;
	}

	private void initLocation() {
		myLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 20, 0, listener);
		// if (myLocation != null) {
		// showLocation(myLocation);
		// } else {
		// Toast.makeText(getSherlockActivity(),
		// "Your location is unavailable now", Toast.LENGTH_LONG)
		// .show();
		// }

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOCATION_SETTINGS) {

			if (!locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(intent, LOCATION_SETTINGS);
			} else {

				initLocation();

			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void showLocation() {
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (lastKnownLocation != null)
			showLocation(lastKnownLocation);
	}

	public void setFocused(Network s) {
		int i = 0;
		for (Network sk : mKeys) {
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

		Toast.makeText(
				getSherlockActivity(),
				getSherlockActivity().getResources().getString(
						R.string.position_refreshed), Toast.LENGTH_LONG).show();

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

	private String printKeys(List<String> keys) {
		String r = "";
		for (String s : keys) {
			r += s + ",";
		}
		return r;
	}

	private void loadKeysPosition() {
		this.loadSavedKeys();
		for (Network s : mKeys) {
			anotherOverlayItemArray.add(new OverlayItem(s.mSSID, s
					.getPossibleDefaultKeys().size() == 1 ? s
					.getPossibleDefaultKeys().get(0) : printKeys(s
					.getPossibleDefaultKeys()), new GeoPoint(s.mLatitude,
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {

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
		positionOverlay.setMarker(getSherlockActivity().getResources()
				.getDrawable(R.drawable.marker_blue));
		// anotherOverlayItemArray.add(positionOverlay);
		// anotherOverlayItemArray.add(0, positionOverlay);
		// positionOverlayItemArray.add(positionOverlay);
		positionOverlayItemArray.add(0, positionOverlay);
		ItemizedOverlayWithFocus<OverlayItem> positiontemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
				getSherlockActivity(), positionOverlayItemArray, null);
		// myOpenMapView.getOverlays().clear();

		myOpenMapView.getOverlays().add(positiontemizedIconOverlay);
		myOpenMapView.invalidate();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mPos", mPos);
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// keyLatitude = location.getLatitude();
			// keyLongitude = location.getLongitude();
			// showLocation(location);
			myLocation = location;
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			bestProvider = provider;

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	};

	@Override
	public void onStop() {
		locationManager.removeUpdates(listener);
		super.onStop();
	}

	protected List<Network> loadSavedKeys() {
		// mKeys = SavedKeysUtils.loadSavedKeys(getSherlockActivity());
		mKeys = Model.fetchAll(Network.class);

		return mKeys;

	}
}
