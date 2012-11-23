package es.glasspixel.wlanaudit.fragments;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.activities.SavedKey;
import es.glasspixel.wlanaudit.database.KeysSQliteHelper;

public class MapFragment extends SherlockFragment {
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
	private ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay;

	public MapFragment() {
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

		Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);
		Log.d("MapActivity", "best provider: " + bestProvider);

		myOpenMapView = (MapView) v.findViewById(R.id.openmapview);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(4);

		anotherOverlayItemArray = new ArrayList<OverlayItem>();
		positionOverlayItemArray = new ArrayList<OverlayItem>();

		Location location = locationManager.getLastKnownLocation(bestProvider);
		if (location != null) {
			showLocation(location);
		} else {
			Toast.makeText(getSherlockActivity(),
					"Your location is unavailable now", Toast.LENGTH_LONG)
					.show();
		}

		locationManager.requestLocationUpdates(bestProvider, 20, 0, listener);

		if (mPos == -1 && savedInstanceState != null)
			mPos = savedInstanceState.getInt("mPos");
		loadKeysPosition();

		return v;
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// inflater.inflate(R.menu.menu_map_location, menu);
	// super.onCreateOptionsMenu(menu, inflater);
	// }
	//
	//
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	//
	// case R.id.check_location_menu:
	// Location lastKnownLocation = locationManager
	// .getLastKnownLocation(bestProvider);
	// if (lastKnownLocation != null) {
	// this.showLocation(lastKnownLocation);
	// }
	// break;
	// case R.id.show_keys_menu:
	// toggle();
	// break;
	//
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// return true;
	// }

	public void showLocation() {
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(bestProvider);
		if (lastKnownLocation != null)
			showLocation(lastKnownLocation);
	}

	private void showLocation(Location l) {
		final GeoPoint gp = new GeoPoint(l.getLatitude(), l.getLongitude());

		Toast.makeText(getSherlockActivity(),
				getResources().getString(R.string.position_refreshed),
				Toast.LENGTH_LONG).show();

		changePositionInMap(l);
		this.centerMap(gp, false);

	}

	private void centerMap(GeoPoint g, boolean zoom) {
		// myMapController.animateTo(g);
		myMapController.setCenter(g);

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
		for (SavedKey s : mKeys) {
			anotherOverlayItemArray.add(new OverlayItem(s.getWlan_name(), s
					.getKeys().size() == 1 ? s.getKeys().get(0) : printKeys(s
					.getKeys()),
					new GeoPoint(s.getLatitude(), s.getLongitude())));

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
		// IF (POSITIONOVERLAY != NULL
		// && POSITIONOVERLAYITEMARRAY.CONTAINS(POSITIONOVERLAY)) {
		// POSITIONOVERLAYITEMARRAY.REMOVE(POSITIONOVERLAY);
		// }
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
		positionOverlayItemArray.add(positionOverlay);
		ItemizedOverlayWithFocus<OverlayItem> positiontemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(
				getSherlockActivity(), positionOverlayItemArray, null);
		// myOpenMapView.getOverlays().clear();

		myOpenMapView.getOverlays().add(positiontemizedIconOverlay);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mPos", mPos);
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			keyLatitude = location.getLatitude();
			keyLongitude = location.getLongitude();
			// changePositionInMap(location);
			// myMapController.setCenter(new GeoPoint(keyLatitude,
			// keyLongitude));
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
	private ArrayList<SavedKey> mKeys;

	protected List<SavedKey> loadSavedKeys() {
		mKeys = new ArrayList<SavedKey>();
		KeysSQliteHelper usdbh = new KeysSQliteHelper(getSherlockActivity(),
				"DBKeys", null, 1);

		SQLiteDatabase db = usdbh.getReadableDatabase();
		Cursor c = db
				.query("Keys", new String[] { "address", "nombre", "key",
						"latitude", "longitude" }, null, null, null, null,
						"nombre ASC");
		// if (c.moveToFirst()) {
		while (c.moveToNext()) {

			String name = c.getString(c.getColumnIndex("nombre"));
			boolean nueva = true;
			for (SavedKey s : mKeys) {
				if (name.equals(s.getWlan_name())) {
					s.getKeys().add(c.getString(c.getColumnIndex("key")));
					nueva = false;
					break;
				}
			}

			if (nueva) {
				List<String> a = new ArrayList<String>();
				a.add(c.getString(c.getColumnIndex("key")));
				SavedKey k = new SavedKey(c.getString(c
						.getColumnIndex("nombre")), c.getString(c
						.getColumnIndex("address")), a, c.getFloat(c
						.getColumnIndex("latitude")), c.getFloat(c
						.getColumnIndex("longitude")));
				mKeys.add(k);
			}

		}
		// }
		c.close();
		return mKeys;

	}
}
