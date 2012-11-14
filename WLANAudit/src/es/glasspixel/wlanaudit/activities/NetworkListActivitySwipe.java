package es.glasspixel.wlanaudit.activities;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import es.glasspixel.wlanaudit.R;
import es.glasspixel.wlanaudit.R.bool;
import es.glasspixel.wlanaudit.R.id;
import es.glasspixel.wlanaudit.R.layout;
import es.glasspixel.wlanaudit.R.string;
import es.glasspixel.wlanaudit.fragments.SavedKeysFragment;
import es.glasspixel.wlanaudit.fragments.ScanFragment;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class NetworkListActivitySwipe extends SherlockFragmentActivity
		implements ScanFragment.Callbacks {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	List<SherlockFragment> mFragments;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	List<SherlockFragment> fragments = new ArrayList<SherlockFragment>();

	private Resources res;

	private boolean screenIsLarge = false;
	protected int position = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_list_activity_swipe);

		res = getResources();
		screenIsLarge = res.getBoolean(R.bool.screen_large);

		mViewPager = (ViewPager) findViewById(R.id.pager);

		fragments.add(new ScanFragment());
		fragments.add(new SavedKeysFragment());

		if (mViewPager != null) {
			mFragments = new ArrayList<SherlockFragment>();

			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getSupportFragmentManager());

			// mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			//
			// @Override
			// public void onPageScrollStateChanged(int arg0) {
			// // TODO Auto-generated method stub
			//
			// }
			//
			// @Override
			// public void onPageScrolled(int arg0, float arg1, int arg2) {
			// // TODO Auto-generated method stub
			//
			// }
			//
			// @Override
			// public void onPageSelected(int arg0) {
			// if (arg0 != position) {
			// position = arg0;
			// mViewPager.setCurrentItem(position);
			// }
			// }
			// });
			//
			// // Set up the ViewPager with the sections adapter.
			//
			// mViewPager.post(new Runnable() {
			//
			// @Override
			// public void run() {
			// if (mViewPager.getCurrentItem() != position) {
			// mViewPager.setCurrentItem(position);
			//
			// }
			//
			// }
			// });

			mViewPager.setAdapter(mSectionsPagerAdapter);

		} else {
			SherlockFragment fragment = new SavedKeysFragment();

			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment, "tag")
					.commit();
		}

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public SherlockFragment getItem(int position) {
			// NetworkListActivitySwipe.this.position = position;

			SherlockFragment fragment;
			if (position == 0)
				fragment = new ScanFragment();
			else
				fragment = new SavedKeysFragment();

			return fragments.get(position);

		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getResources().getString(R.string.action1).toUpperCase();
			case 1:
				return getResources().getString(R.string.action2).toUpperCase();

			}
			return null;
		}
	}

	/**
	 * Lifecycle management: Activity is about to be shown
	 */
	protected void onStart() {
		super.onStart();

		// mAd.loadAd(new AdRequest());
	}

	/**
	 * Lifecycle management: Activity is being resumed, we need to refresh its
	 * contents
	 */
	protected void onResume() {
		super.onResume();

		// mAd.loadAd(new AdRequest());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);

		// outState.remove("android:support:fragments");
	}

	@Override
	public void onItemSelected(ScanResult s) {

		if (screenIsLarge) {
			SherlockFragment fragment = new SavedKeysFragment();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			// ft.add(R.id.saved_keys_fragment, fragment).commit();
			ft.replace(R.id.item_detail_container, fragment).commit();
		} else {
			mViewPager.setAdapter(mSectionsPagerAdapter);
			// mViewPager.refreshDrawableState();
		}

	}

}
