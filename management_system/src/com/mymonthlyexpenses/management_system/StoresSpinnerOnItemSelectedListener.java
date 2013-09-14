package com.mymonthlyexpenses.management_system;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Spinner;

public class StoresSpinnerOnItemSelectedListener implements
		OnItemSelectedListener {
	private StoreItemsArrayAdapter adapter;
	private ListView myList;
	private AutoCompleteTextView searchAutoComplete;
	
	private final Activity context;

	public StoresSpinnerOnItemSelectedListener(Activity context,
			StoreItemsArrayAdapter adapter) {
		this.adapter = adapter;
		this.context = context;
		myList = (ListView) (context.findViewById(android.R.id.list));
		myList.setAdapter(adapter);
		
		searchAutoComplete = (AutoCompleteTextView) context.findViewById(R.id.autoCompleteSearchView);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

			Spinner categoriesSpinner = (Spinner) view.getRootView()
					.findViewById(R.id.categoriesSpinner);

			String selectedCategoryId = MainActivity
					.getCategoryIdBasedOnName(categoriesSpinner
							.getSelectedItem().toString());

			String selectedStoreId = MainActivity.getStoreIdBasedOnName(parent
					.getItemAtPosition(pos).toString());
			
			adapter.setStoreItem(MainActivity.getItemsBasedOnCategoryAndStore(
					MainActivity.storeItems, selectedCategoryId,
					selectedStoreId));
			
			adapter.notifyDataSetChanged();

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
