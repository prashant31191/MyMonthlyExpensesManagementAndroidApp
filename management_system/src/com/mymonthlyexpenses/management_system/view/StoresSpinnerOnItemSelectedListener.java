package com.mymonthlyexpenses.management_system.view;

import java.io.IOException;
import java.io.InputStream;

import com.mymonthlyexpenses.management_system.MainActivity;
import com.mymonthlyexpenses.management_system.R;
import com.mymonthlyexpenses.management_system.StoreItemsArrayAdapter;
import com.mymonthlyexpenses.management_system.R.id;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

public class StoresSpinnerOnItemSelectedListener implements
		OnItemSelectedListener {
	private StoreItemsArrayAdapter adapter;
	private ListView myList;

	private final Activity context;

	public StoresSpinnerOnItemSelectedListener(Activity context,
			StoreItemsArrayAdapter adapter) {
		this.adapter = adapter;
		this.context = context;
		myList = (ListView) (context.findViewById(android.R.id.list));
		myList.setAdapter(adapter);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		Spinner categoriesSpinner = (Spinner) view.getRootView().findViewById(
				R.id.categoriesSpinner);

		String selectedCategoryId = MainActivity
				.getCategoryIdBasedOnName(categoriesSpinner.getSelectedItem()
						.toString());

		String selectedStoreId = MainActivity.getStoreIdBasedOnName(parent
				.getItemAtPosition(pos).toString());

		adapter.setStoreItem(MainActivity.getItemsBasedOnCategoryAndStore(
				MainActivity.storeItems, selectedCategoryId, selectedStoreId));

		adapter.notifyDataSetChanged();

		// Once we change a store we want to update the search box store items
		// array adapter
		MainActivity.searchStoreItemsArrayAdapter
				.setStoreItem(MainActivity.getItemsBasedOnStore(
						MainActivity.storeItems, selectedStoreId));
		MainActivity.searchStoreItemsArrayAdapter.notifyDataSetChanged();

		/*
		 * Load images for shopping items from assests folder
		 */

		try {
			// get input stream
			InputStream ims = context.getAssets().open(
					MainActivity.getStoreImageLocationByStoreName(
							parent.getItemAtPosition(pos).toString())
							.replaceFirst("/", ""));
			// load image as Drawable
			Drawable d = Drawable.createFromStream(ims, null);
			// set image to ImageView
			((ImageView) context.findViewById(R.id.storeImageView))
					.setImageDrawable(d);

		} catch (IOException ex) {
			Log.d("onCreate", ex.getLocalizedMessage());
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

}
