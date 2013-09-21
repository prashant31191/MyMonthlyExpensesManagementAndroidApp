package com.mymonthlyexpenses.management_system;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;

public class CategoriesSpinnerOnItemSelectedListener implements
		OnItemSelectedListener {

	private StoreItemsArrayAdapter adapter;
	private ListView myList;

	CategoriesSpinnerOnItemSelectedListener(Activity context,
			StoreItemsArrayAdapter adapter) {
		this.adapter = adapter;
		myList = (ListView) (context.findViewById(android.R.id.list));
		myList.setAdapter(adapter);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		String selectedCategoryId = MainActivity
				.getCategoryIdBasedOnName(parent.getItemAtPosition(pos)
						.toString());

		Spinner storesSpinner = (Spinner) view.getRootView().findViewById(
				R.id.storesSpinner);

		String selectedStoreId = MainActivity
				.getStoreIdBasedOnName(storesSpinner.getSelectedItem()
						.toString());

		adapter.setStoreItem(MainActivity.getItemsBasedOnCategoryAndStore(
				MainActivity.storeItems, selectedCategoryId, selectedStoreId));

		adapter.notifyDataSetChanged();

	}

	public void onNothingSelected(AdapterView<?> arg0) {

	}

}
