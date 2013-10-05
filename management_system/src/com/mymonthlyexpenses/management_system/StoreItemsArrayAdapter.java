package com.mymonthlyexpenses.management_system;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.text.WordUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class StoreItemsArrayAdapter extends ArrayAdapter<StoreItem> implements
		OnClickListener, Filterable {
	private final Activity context;
	private ArrayList<StoreItem> storeItems;
	private ArrayList<StoreItem> backupStoreItems;
	private ItemsFilter mFilter;
	private Drawable d;

	public StoreItemsArrayAdapter(Activity context,
			ArrayList<StoreItem> storeItems) {
		super(context, R.layout.lvrowlayout2, storeItems);
		this.context = context;
		this.storeItems = storeItems;
		this.backupStoreItems = storeItems;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setStoreItem(ArrayList<StoreItem> storeItems) {
		this.storeItems.clear();
		this.storeItems.addAll(storeItems);
		this.backupStoreItems = this.storeItems;
	}

	static class ViewContainer {
		public ImageView imageView;
		public TextView itemNameTextView;
		public TextView itemDescriptionTextView;
		public TextView itemPriceTextView;
		public TextView itemUnitTextView;
		public TextView itemSizeTextView;
		public TextView itemLastUpdatedTextView;

		// We are going to keep a set of strings per viewcontainer that we can
		// use later to update our storeItem array
		public String itemName;
		public String itemDescription;
		public String itemPrice;
		public String itemUnit;
		public String itemSize;
		public String itemLastUpdated;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewContainer viewContainer;
		View rowView = view;

		// ---print the index of the row to examine---
		// Log.d("CustomArrayAdapter", String.valueOf(position));

		// ---if the row is displayed for the first time---
		if (rowView == null) {

			// Log.d("CustomArrayAdapter", "New");
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.lvrowlayout2, null, true);

			// ---create a view container object---
			viewContainer = new ViewContainer();

			// ---get the references to all the views in the row---
			viewContainer.itemNameTextView = (TextView) rowView
					.findViewById(R.id.shoppingItemNameTextView);
			viewContainer.itemDescriptionTextView = (TextView) rowView
					.findViewById(R.id.shoppingItemDescriptionTextView);
			viewContainer.itemPriceTextView = (TextView) rowView
					.findViewById(R.id.shoppingItemPriceTextView);
			viewContainer.itemUnitTextView = (TextView) rowView
					.findViewById(R.id.shoppingItemUnitTextView);
			viewContainer.itemSizeTextView = (TextView) rowView
					.findViewById(R.id.shoppingItemSizeTextView);
			viewContainer.itemLastUpdatedTextView = (TextView) rowView
					.findViewById(R.id.shoppingItemLastUpdatedTextView);
			viewContainer.imageView = (ImageView) rowView
					.findViewById(R.id.icon);

			// ---assign the view container to the rowView---
			rowView.setTag(viewContainer);

		} else {

			// ---view was previously created; can recycle---
			// Log.d("CustomArrayAdapter", "Recycling");
			// ---retrieve the previously assigned tag to get
			// a reference to all the views; bypass the findViewByID() process,
			// which is computationally expensive---
			viewContainer = (ViewContainer) rowView.getTag();
		}

		// ---customize the content of each row based on position---
		viewContainer.itemNameTextView.setText(" Name: "
				+ WordUtils.capitalize(this.getItem(position)
						.getShoppingItemName()));
		viewContainer.itemDescriptionTextView.setText(" Description: "
				+ WordUtils.capitalize(this.getItem(position)
						.getShoppingItemDescription()));
		viewContainer.itemPriceTextView.setText(" Price:$ "
				+ this.getItem(position).getPrice());
		viewContainer.itemUnitTextView.setText(" Unit: "
				+ this.getItem(position).getShoppingItemUnit());
		viewContainer.itemSizeTextView.setText(" Size: "
				+ this.getItem(position).getQuantity());
		viewContainer.itemLastUpdatedTextView.setText(" Last Updated: "
				+ this.getItem(position).getUpdated());
		// Save our item information so we can use it later when we update items

		viewContainer.itemName = this.getItem(position).getShoppingItemName();
		viewContainer.itemDescription = this.getItem(position)
				.getShoppingItemDescription();
		viewContainer.itemPrice = this.getItem(position).getPrice();
		viewContainer.itemSize = this.getItem(position).getQuantity();
		viewContainer.itemUnit = this.getItem(position).getShoppingItemUnit();
		viewContainer.itemLastUpdated = this.getItem(position).getUpdated();

		/*
		 * Load images for shopping items from assests folder
		 */

		try {
			// get input stream
			InputStream ims = getContext().getAssets().open(
					this.getItem(position).getShoppingItemImageLocation()
							.replaceFirst("/", ""));
			// load image as Drawable
			d = Drawable.createFromStream(ims, null);
			// set image to ImageView
			viewContainer.imageView.setImageDrawable(d);
		} catch (IOException ex) {
			Log.d("getView", ex.getLocalizedMessage());
		}

		/*
		 * Set an onclick listener
		 */

		rowView.setOnClickListener(this);

		return rowView;
	}

	public void onClick(View view) {
		ViewContainer viewContainer = (ViewContainer) view.getTag();

		showUpdateStoreItemDialog(viewContainer.itemName,
				viewContainer.itemDescription, viewContainer.itemUnit,
				viewContainer.itemPrice, viewContainer.itemSize);

	}

	public void showUpdateStoreItemDialog(String itemName,
			String itemDescription, String itemUnit, String itemPrice,
			String itemSize) {
		FragmentManager fragementManager = ((FragmentActivity) context)
				.getSupportFragmentManager();
		UpdateStoreItemDialogFragment updateStoreItemFragmentDialog = new UpdateStoreItemDialogFragment();

		updateStoreItemFragmentDialog.setCancelable(true);
		updateStoreItemFragmentDialog.setDialogTitle("Update:"
				+ itemDescription);
		updateStoreItemFragmentDialog.setItemUnitText(itemUnit);
		updateStoreItemFragmentDialog.setItemName(itemName);
		updateStoreItemFragmentDialog.setItemDescription(itemDescription);
		updateStoreItemFragmentDialog.setItemPrice(itemPrice);
		updateStoreItemFragmentDialog.setItemSize(itemSize);
		updateStoreItemFragmentDialog.show(fragementManager, "input dialog");

	}

	@Override
	public int getCount() {
		return storeItems.size();
	}

	@Override
	public StoreItem getItem(int position) {
		return storeItems.get(position);
	}

	@Override
	public int getPosition(StoreItem item) {
		return storeItems.indexOf(item);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ItemsFilter();
		}
		return mFilter;
	}

	private class ItemsFilter extends Filter {

		protected FilterResults performFiltering(CharSequence prefix) {
			// debug
			// Debug.startMethodTracing("performingFiltering");

			// Initiate our results object
			FilterResults results = new FilterResults();

			// No prefix is sent to filter by so we're going to send back the
			// original array
			if (prefix == null || prefix.length() == 0) {

				results.values = backupStoreItems;
				results.count = backupStoreItems.size();

			} else {
				// Compare lower case strings
				String prefixString = prefix.toString().toLowerCase();
				// Local to here so we're not changing actual array
				final ArrayList<StoreItem> items = backupStoreItems;
				final int count = items.size();
				final ArrayList<StoreItem> newItems = new ArrayList<StoreItem>(
						count);
				for (int i = 0; i < count; i++) {
					final StoreItem item = items.get(i);
					final String itemName = item.getShoppingItemName()
							.toLowerCase();
					// First match against the whole, non-splitted value
					if (itemName.contains(prefixString)) {
						newItems.add(item);
					} else {
					} /*
						* This is option and taken from the source of ArrayAdapter
						* final String[] words = itemName.split(" "); final int
						* wordCount = words.length; for (int k = 0; k < wordCount;
						* k++) { if (words[k].startsWith(prefixString)) {
						* newItems.add(item); break; } } }
						*/
				}

				// Set and return
				results.values = newItems;
				results.count = newItems.size();
			}

			// debug
			// Debug.stopMethodTracing();

			return results;
		}

		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence prefix, FilterResults results) {
			// noinspection unchecked
			storeItems = (ArrayList<StoreItem>) results.values;
			// Let the adapter know about the updated list
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
