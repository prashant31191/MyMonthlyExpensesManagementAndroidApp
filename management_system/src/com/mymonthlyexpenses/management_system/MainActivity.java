package com.mymonthlyexpenses.management_system;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;

import com.mymonthlyexpenses.management_system.StorePickerDialogFragment.StorePickerDialogListener;
import com.mymonthlyexpenses.management_system.UpdateStoreItemDialogFragment.UpdateStoreItemDialogListener;

public class MainActivity extends FragmentActivity implements
		UpdateStoreItemDialogListener, DatePickerDialog.OnDateSetListener,
		StorePickerDialogListener {

	/*
	 * We are going to use a set of helper arrays to hold our information
	 * locally while we update it
	 */
	public static ArrayList<ShoppingItemCategory> categories = new ArrayList<ShoppingItemCategory>();
	public static ArrayList<Store> stores = new ArrayList<Store>();
	public static ArrayList<ShoppingItem> shoppingItems = new ArrayList<ShoppingItem>();
	public static ArrayList<StoreItem> storeItems = new ArrayList<StoreItem>();
	public static ArrayList<ShoppingItemUnit> shoppingItemUnits = new ArrayList<ShoppingItemUnit>();

	// Since the goal of this application is to update our store items I think
	// it makes sense to have one static JSONArray we use for it
	public static JSONArray storeItemsJSONArray;

	public Spinner storesSpinner;
	public Spinner categoriesSpinner;

	public static StoreItemsArrayAdapter storeItemsArrayAdapter;
	public static StoreItemsArrayAdapter searchStoreItemsArrayAdapter;

	private static ProgressDialog pd;

	// Handler and runnable to allow us to run a background thread while still
	// communicating with the main UI
	final Handler mHandler = new Handler();
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateResultsInUi();
		}
	};

	public Boolean readAndSaveJSONFeed(String jsonFileName, String URL) {
		StringBuilder stringBuilder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				inputStream.close();
			} else {
				Log.d("JSON", "Failed to download file");
				return false;
			}
		} catch (Exception e) {
			Log.d("readJSONFeed", e.getLocalizedMessage());
			return false;
		}

		/*
		 * Save our file to the file system
		 */

		try {
			FileOutputStream outputStream;

			outputStream = openFileOutput(jsonFileName, Context.MODE_PRIVATE);
			outputStream.write(stringBuilder.toString().getBytes());
			outputStream.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}

		return true;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		 * In order to make sure we are updaing the correct store
		 * We are going to show the user a dialog with radio buttons
		 * for each store and have them choose the store they are going
		 * to work on
		 */
		FragmentManager fragementManager = ((FragmentActivity) this)
				.getSupportFragmentManager();

		StorePickerDialogFragment storePickerDialogFragment = new StorePickerDialogFragment();
		storePickerDialogFragment.setCancelable(true);
		storePickerDialogFragment.setDialogTitle("Choose Store");
		storePickerDialogFragment.show(fragementManager, "input dialog");
	}

	@Override
	public void onStop() {
		super.onStop();
		// categories.clear();
		// stores.clear();
	}

	public static ArrayList<StoreItem> getItemsBasedOnCategoryAndStore(
			ArrayList<StoreItem> storeItems, String selectedCategoryId,
			String selectedStoreId) {

		ArrayList<StoreItem> storeItemsInCategoryAndStore = new ArrayList<StoreItem>();

		/*
		 * Add to StoreItemsInCategoryAndStore all shopping items based on a
		 * category and also update their prices based on the store item
		 * information
		 */
		// Go over all shoppingItmes
		boolean exists = false;
		boolean filtered = false;

		for (ShoppingItem shoppingItem : shoppingItems) {
			// If a shopping item is in our category
			if (shoppingItem.getCategoryId().equalsIgnoreCase(
					selectedCategoryId)) {
				// I am going to start by assuming that this shopping items does
				// not exist in the store items array
				exists = false;
				filtered = false;
				// Look for the shopping item in the store items array
				for (StoreItem storeItem : storeItems) {
					// First check if this item is not filtered

					if ((storeItem.getShoppingItemId().equalsIgnoreCase(
							shoppingItem.getId()) && (storeItem.getStoreId()
							.equalsIgnoreCase(selectedStoreId)))) {
						if (!storeItem.isFiltered()) {
							storeItemsInCategoryAndStore.add(storeItem);

							exists = true;
							break;
						} else {
							exists = true;
							filtered = true;
							break;
						}
					}
				}

				// If I checked the shopping item against all store items and
				// exists still equals false
				// Than I am going to create a new store item based on the
				// shopping item and add it to
				// The storeItemsInCategoryAndStore array.
				if ((!exists) && (!filtered)) {
					StoreItem newStoreItem = new StoreItem();
					newStoreItem.setShoppingItemCategoryId(shoppingItem
							.getCategoryId());
					newStoreItem.setShoppingItemId(shoppingItem.getId());
					newStoreItem.setShoppingItemImageLocation(shoppingItem
							.getImageLocation());
					newStoreItem.setShoppingItemName(shoppingItem.getName());
					newStoreItem.setShoppingItemDescription(shoppingItem
							.getDescription());
					newStoreItem.setStoreId(selectedStoreId);
					newStoreItem.setPrice("Unknown at this time");
					newStoreItem.setQuantity("Unknown at this time");
					newStoreItem
							.setShoppingItemUnit(getShoppingItemUnitFromUnitId(shoppingItem
									.getShoppingItemUnitId()));
					newStoreItem.setUpdated("0000-00-00 00:00:00");

					storeItemsInCategoryAndStore.add(newStoreItem);
				}

			}
		}

		return storeItemsInCategoryAndStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * Sync information from the server once the user clicks the sync from
	 * server menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.sync_from_server:
			syncFromServer();
			return true;
		case R.id.sync_to_server:
			syncToServer();
			return true;
		case R.id.add_filter:
			addFilter();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void syncFromServer() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set the title of the Alert Dialog
		alertDialogBuilder.setTitle("Sync Data?");

		alertDialogBuilder.setMessage("Click yes to sync");

		alertDialogBuilder.setCancelable(false);

		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						// startSyncFromServerAsyncTask();
						startSyncFromServerThread();
					}

				});

		alertDialogBuilder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}

				});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	}

	// }

	private void syncToServer() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set the title of the Alert Dialog
		alertDialogBuilder.setTitle("Sync Data?");

		alertDialogBuilder.setMessage("Click yes to sync");

		alertDialogBuilder.setCancelable(false);

		alertDialogBuilder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {

							@Override
							protected void onPreExecute() {
								pd = new ProgressDialog(MainActivity.this);
								pd.setTitle("Processing...");
								pd.setMessage("Please wait.");
								pd.setCancelable(false);
								pd.setIndeterminate(true);
								pd.show();
							}

							@Override
							protected Void doInBackground(String... params) {
								uploadFile(params[0], params[1]);

								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								if (pd != null) {
									pd.dismiss();
								}
							}

						};
						String fileLocation = getBaseContext().getFilesDir()
								.getPath() + "/store_items.json";
						task.execute(fileLocation,
								"http://192.168.1.124/management/syncToServer.php");
					}

				});

		alertDialogBuilder.setNegativeButton("No",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}

				});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void initCategorySpinner(Spinner spinner,
			ArrayList<ShoppingItemCategory> categoryArrayList) {
		String[] itemArray = new String[categoryArrayList.size()];
		for (int i = 0; i < itemArray.length; i++) {
			itemArray[i] = WordUtils.capitalize(categoryArrayList.get(i)
					.getName());
		}

		ArrayAdapter<String> spinnerArrayAdapter;

		spinnerArrayAdapter = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_spinner_item, itemArray);

		// Specify the layout to use when the list of choices appears
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner
		spinner.setAdapter(spinnerArrayAdapter);
	}

	private void initStoreSpinner(Spinner spinner,
			ArrayList<Store> storeArrayList) {
		String[] itemArray = new String[storeArrayList.size()];
		for (int i = 0; i < itemArray.length; i++) {
			itemArray[i] = storeArrayList.get(i).getName();
		}

		ArrayAdapter<String> spinnerArrayAdapter;

		spinnerArrayAdapter = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_spinner_item, itemArray);

		// Specify the layout to use when the list of choices appears
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.invalidate();

		// Apply the adapter to the spinner
		spinner.setAdapter(spinnerArrayAdapter);
	}

	public static ShoppingItem[] getItemsBasedOnCategory(
			ArrayList<ShoppingItem> shoppingItems, String categoryId) {

		ArrayList<ShoppingItem> shoppingItemsInCategory = new ArrayList<ShoppingItem>();

		/*
		 * Add to shoppingItemsInCategory only items that have a specific
		 * category id
		 */
		for (ShoppingItem item : shoppingItems) {
			if (item.getCategoryId().equalsIgnoreCase(categoryId))
				shoppingItemsInCategory.add(item);
		}

		ShoppingItem[] itemArray = new ShoppingItem[shoppingItemsInCategory
				.size()];
		ShoppingItem[] returnedArray = shoppingItemsInCategory
				.toArray(itemArray);

		return returnedArray;
	}

	private void initShoppingItemsArray() {

		/*
		 * Create an ArrayList of shopping item objects
		 */
		String fileLocation = getBaseContext().getFilesDir().getPath()
				+ "/shopping_items.json";

		try {
			String line;
			String s = "";
			// wrap a BufferedReader around FileReader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					fileLocation));

			// use the readLine method of the BufferedReader to read one line at
			// a time.
			// the readLine method returns null when there is nothing else to
			// read.
			while ((line = bufferedReader.readLine()) != null) {
				s += line;
			}

			bufferedReader.close();

			JSONObject jsonObject = new JSONObject(s);
			JSONArray jsonArray = new JSONArray(
					jsonObject.getString("shopping_items"));

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);
				ShoppingItem shoppingItem = new ShoppingItem();

				shoppingItem.setName(item.getString("Name"));
				shoppingItem.setId(item.getString("Id"));
				shoppingItem.setDescription(item.getString("Description"));
				shoppingItem.setCategoryId(item
						.getString("Shopping_Item_Category_Id"));
				shoppingItem.setShoppingItemUnitId(item
						.getString("Shopping_Item_Unit_Id"));
				shoppingItem.setImageLocation(item.getString("Image_Location"));

				shoppingItems.add(shoppingItem);
			}

		} catch (IOException ioe) {
			Log.d("initShoppingItemsArray", ioe.getLocalizedMessage());
		} catch (Exception e) {
			Log.d("initShoppingItemsArray", e.getLocalizedMessage());
		}
	}

	private void initStoreItemsArray() {

		/*
		 * Create an ArrayList of store item objects
		 */
		String fileLocation = getBaseContext().getFilesDir().getPath()
				+ "/store_items.json";

		try {

			String line;
			String s = "";
			// wrap a BufferedReader around FileReader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					fileLocation));

			// use the readLine method of the BufferedReader to read one line at
			// a time.
			// the readLine method returns null when there is nothing else to
			// read.
			while ((line = bufferedReader.readLine()) != null) {
				s += line;
			}

			bufferedReader.close();

			JSONObject jsonObject = new JSONObject(s);

			// This is where we init our static storeItemsJSONArray that we use
			// every time we update a store item
			storeItemsJSONArray = new JSONArray(
					jsonObject.getString("store_items"));

			for (int i = 0; i < storeItemsJSONArray.length(); i++) {
				JSONObject item = storeItemsJSONArray.getJSONObject(i);
				StoreItem storeItem = new StoreItem();

				storeItem.setId(item.getString("Id"));
				storeItem.setStoreId(item.getString("Store_Id"));
				storeItem.setShoppingItemId(item.getString("Shopping_Item_Id"));
				storeItem.setShoppingItemCategoryId(item
						.getString("Shopping_Item_Shopping_Item_Category_Id"));
				storeItem.setPrice(item.getString("Price"));
				storeItem.setQuantity(item.getString("Quantity"));
				storeItem.setUpdated(item.getString("Updated"));

				storeItems.add(storeItem);
			}

		} catch (IOException ioe) {
			Log.d("initStoreItemsArray", ioe.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("initStoreItemsArray", e.getLocalizedMessage());
			finish();
		}
	}

	private void initShoppingItemsCategoriesArray() {

		/*
		 * Create an ArrayList of shopping item category objects
		 */
		try {
			FileInputStream fIn = openFileInput("shopping_item_category.json");
			InputStreamReader isr = new InputStreamReader(fIn);

			char[] inputBuffer = new char[100];
			String s = "";
			int charRead;
			while ((charRead = isr.read(inputBuffer)) > 0) {
				// convert the chars to String
				String readString = String
						.copyValueOf(inputBuffer, 0, charRead);
				s += readString;

				inputBuffer = new char[100];
			}
			isr.close();

			JSONObject jsonObject = new JSONObject(s);
			JSONArray jsonArray = new JSONArray(
					jsonObject.getString("categories"));

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);
				ShoppingItemCategory shoppingItemCategory = new ShoppingItemCategory();

				shoppingItemCategory.setName(item.getString("Name"));
				shoppingItemCategory.setId(item.getString("Id"));
				shoppingItemCategory.setImageLocation(item
						.getString("Image_location"));

				categories.add(shoppingItemCategory);
			}
		} catch (IOException ioe) {
			Log.d("initShoppingItemsCategoriesArray", ioe.getLocalizedMessage());
		} catch (Exception e) {
			Log.d("initShoppingItemsCategoriesArray", e.getLocalizedMessage());
		}
	}

	private void initShoppingItemsUnitArray() {

		/*
		 * Create an ArrayList of shopping item unit objects
		 */
		try {
			FileInputStream fIn = openFileInput("shopping_items_unit.json");
			InputStreamReader isr = new InputStreamReader(fIn);

			char[] inputBuffer = new char[100];
			String s = "";
			int charRead;
			while ((charRead = isr.read(inputBuffer)) > 0) {
				// convert the chars to String
				String readString = String
						.copyValueOf(inputBuffer, 0, charRead);
				s += readString;

				inputBuffer = new char[100];
			}
			isr.close();

			JSONObject jsonObject = new JSONObject(s);
			JSONArray jsonArray = new JSONArray(jsonObject.getString("units"));

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);
				ShoppingItemUnit shoppingItemUnit = new ShoppingItemUnit();

				shoppingItemUnit.setName(item.getString("Name"));
				shoppingItemUnit.setId(item.getString("Id"));

				shoppingItemUnits.add(shoppingItemUnit);
			}
		} catch (IOException ioe) {
			Log.d("initShoppingItemsUnitArray", ioe.getLocalizedMessage());
		} catch (Exception e) {
			Log.d("initShoppingItemsUnitArray", e.getLocalizedMessage());
		}
	}

	private void initStoresArray() {

		/*
		 * Create an ArrayList of shopping item objects
		 */
		try {
			FileInputStream fIn = openFileInput("stores.json");
			InputStreamReader isr = new InputStreamReader(fIn);

			char[] inputBuffer = new char[100];
			String s = "";
			int charRead;
			while ((charRead = isr.read(inputBuffer)) > 0) {
				// convert the chars to String
				String readString = String
						.copyValueOf(inputBuffer, 0, charRead);
				s += readString;

				inputBuffer = new char[100];
			}
			isr.close();

			JSONObject jsonObject = new JSONObject(s);
			JSONArray jsonArray = new JSONArray(jsonObject.getString("stores"));

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);
				Store store = new Store();

				store.setName(item.getString("Name"));
				store.setId(item.getString("Id"));
				store.setStreet(item.getString("Street"));
				store.setCity(item.getString("City"));
				store.setState(item.getString("State"));
				store.setZip(item.getString("Zip"));
				store.setImageLocation(item.getString("Image_Location"));

				stores.add(store);
			}
		} catch (IOException ioe) {
			Log.d("initStoresArray", ioe.getLocalizedMessage());
		} catch (Exception e) {
			Log.d("initStoresArray", e.getLocalizedMessage());
		}
	}

	/*
	 * This method will match shopping items with store items and add to the
	 * store item object the shopping item name and image location
	 */
	private void combineShoppingItemWithStoreItems() {
		for (StoreItem storeItem : storeItems) {
			for (ShoppingItem shoppingItem : shoppingItems) {
				if (storeItem.getShoppingItemId().equalsIgnoreCase(
						shoppingItem.getId())) {
					storeItem.setShoppingItemName(shoppingItem.getName());
					storeItem.setShoppingItemDescription(shoppingItem
							.getDescription());
					storeItem.setShoppingItemImageLocation(shoppingItem
							.getImageLocation());

					// Convert shopping item unit id to a human readable string
					for (ShoppingItemUnit unit : shoppingItemUnits) {
						if (unit.getId().equalsIgnoreCase(
								shoppingItem.getShoppingItemUnitId())) {
							storeItem.setShoppingItemUnit(unit.getName());
						}
					}
				}
			}
		}
	}

	public static String getShoppingItemUnitFromUnitId(String shoppingItemUnitId) {
		for (ShoppingItemUnit unit : shoppingItemUnits) {
			if (unit.getId().equalsIgnoreCase(shoppingItemUnitId))
				return unit.getName();
		}

		return null;
	}

	public static String getCategoryIdBasedOnName(String categoryName) {
		for (ShoppingItemCategory category : categories) {
			if (category.getName().equalsIgnoreCase(categoryName))
				return category.getId();
		}

		return null;
	}

	public static String getStoreIdBasedOnName(String storeName) {
		for (Store store : stores) {
			if (store.getName().equalsIgnoreCase(storeName))
				return store.getId();
		}

		return null;
	}

	/*
	 * This method will initialize al of our helper arrays based on our local
	 * json files
	 */
	private void initDatabaseArrays() {

		/*
		 * This will only work if we already have our files from the server
		 * therefore we have to check if the files exist
		 */
		File file = getBaseContext().getFileStreamPath("store_items.json");
		if (file.exists()) {
			initStoresArray();
			initShoppingItemsCategoriesArray();
			initShoppingItemsUnitArray();

			// These are the heave loader that delay our application startup
			// time by 15s.
			initShoppingItemsArray();
			initStoreItemsArray();

			combineShoppingItemWithStoreItems();
		}
	}

	/*
	 * This is a method that is called once we come back from our item update dialog.
	 * Everything that has to do with updating items in our ArrayAdapters, JSON files and ArryList's 
	 * Is done here.
	 */
	public void onFinishInputDialog(String itemPrice, String itemSize,
			String itemName, String itemDescription, String itemUnit) {

		// debug
		// Debug.startMethodTracing("onFinishInputDialog");

		// We want to make the updatedItem a local variable that we can use
		// later to update our store items array and local JSON file
		StoreItem updatedItem = null;

		/*
		 * Sometimes, I dont know exactly how and when, both the category and store id are not
		 * being put into the json array.
		 */
		String shoppingItemCategoryId = getCategoryIdBasedOnName(categoriesSpinner
				.getSelectedItem().toString());
		String storeId = getStoreIdBasedOnName(storesSpinner.getSelectedItem()
				.toString());

		// Create the MySQL datetime string
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTimeString = fmt.format(new Date());

		// Create the MySQL datetime string
		/*
		 * SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 * String mysqlDateTime = fmt.format(new Date());
		 */

		/*
		 * ArrayAdapter update section
		 */
		// Find our item in the storeItemArrayAdapter and update its price and
		// size values. We are using the item description since it is more
		// unique than its name, which we might
		// have duplicates in our database (for good reasons).

		// One time for the general storeItemArrayAdapter
		for (int i = 0; i < storeItemsArrayAdapter.getCount(); i++) {
			StoreItem tmpItem = storeItemsArrayAdapter.getItem(i);
			if (tmpItem.getShoppingItemDescription().equalsIgnoreCase(
					itemDescription)) {
				updatedItem = new StoreItem(tmpItem);
				updatedItem.setPrice(itemPrice);
				updatedItem.setQuantity(itemSize);
				updatedItem.setUpdated(currentDateTimeString);

				// Now we can update our storeItemsArrayAdapter
				storeItemsArrayAdapter.remove(tmpItem);
				storeItemsArrayAdapter.add(updatedItem);

				// Why are we sorting? does it make sense?
				storeItemsArrayAdapter.sort(StoreItem.StoreItemComparator);

				storeItemsArrayAdapter.notifyDataSetChanged();

				// Clear the Autocomplete serach
				((AutoCompleteTextView) this
						.findViewById(R.id.autoCompleteSearchView)).setText("");
				((AutoCompleteTextView) this
						.findViewById(R.id.autoCompleteSearchView))
						.dismissDropDown();

				break;
			}
		}

		// And another time for the special serachStoreItemsArrayAdapter
		for (int i = 0; i < searchStoreItemsArrayAdapter.getCount(); i++) {
			StoreItem tmpItem = searchStoreItemsArrayAdapter.getItem(i);
			if (tmpItem.getShoppingItemDescription().equalsIgnoreCase(
					itemDescription)) {
				updatedItem = new StoreItem(tmpItem);
				updatedItem.setPrice(itemPrice);
				updatedItem.setQuantity(itemSize);
				updatedItem.setUpdated(currentDateTimeString);

				// Now we can update our storeItemsArrayAdapter
				searchStoreItemsArrayAdapter.remove(tmpItem);
				searchStoreItemsArrayAdapter.add(updatedItem);
				searchStoreItemsArrayAdapter
						.sort(StoreItem.StoreItemComparator);
				searchStoreItemsArrayAdapter.notifyDataSetChanged();

				// Clear the Autocomplete serach
				((AutoCompleteTextView) this
						.findViewById(R.id.autoCompleteSearchView)).setText("");
				((AutoCompleteTextView) this
						.findViewById(R.id.autoCompleteSearchView))
						.dismissDropDown();

				break;
			}
		}

		/*
		 * ArrayList update section
		 */
		// Next we need to update our static application storeItems ArrayList,
		// but dont forget that you need to update a specific store and NOT all
		// sotres with this item!
		boolean exist = false;

		for (StoreItem storeItem : storeItems) {
			if ((storeItem.getShoppingItemName().equalsIgnoreCase(itemName))
					&& (storeItem.getStoreId().equalsIgnoreCase(storeId))) {
				storeItem.setPrice(itemPrice);
				storeItem.setQuantity(itemSize);
				storeItem.setUpdated(currentDateTimeString);

				exist = true;

				// No need to go on...
				break;
			}
		}

		// If after checking all store items in our array we still didnt find
		// the item, than we need to add a new one
		if (!exist) {
			// Better to be sure than sorry
			if (updatedItem != null)
				storeItems.add(updatedItem);
		}

		if (updatedItem.getId() == "") {
			exist = false;
		} else {
			exist = true;
		}

		/*
		 * JSONArray update section
		 */
		// Last but not least - we need to update our store items JSONArray and
		// save it to disk
		try {
			// No need to create a new JSONObject for each iteration so we
			// declare it outside the loop
			JSONObject storeItemJSONObject;

			if (exist) {
				for (int i = 0; i < storeItemsJSONArray.length(); i++) {
					storeItemJSONObject = storeItemsJSONArray.getJSONObject(i);

					// First lest check if we already have this item in the
					// JSONArray
					if ((storeItemJSONObject.getString("Id")
							.equalsIgnoreCase(updatedItem.getId()))) {
						storeItemJSONObject.put("Price", itemPrice);
						storeItemJSONObject.put("Quantity", itemSize);
						storeItemJSONObject.put("Updated",
								currentDateTimeString);
						exist = true;

						// No need to keep on...
						break;
					}

				}
			} else {
				storeItemJSONObject = new JSONObject();

				storeItemJSONObject.put("Id", "");

				/*
				 * In an atempt to solve the issue where we have corupted json object, instead of using
				 * the storeId and shoppingItemCategoryId variables, I am going to use the same information 
				 * from the updatedItem object.
				 */
				storeItemJSONObject
						.put("Store_Id", updatedItem.getStoreId() /*storeId*/);
				storeItemJSONObject
						.put("Shopping_Item_Shopping_Item_Category_Id",
								updatedItem.getShoppingItemCategoryId() /*shoppingItemCategoryId*/);

				storeItemJSONObject.put("Shopping_Item_Id",
						updatedItem.getShoppingItemId());
				storeItemJSONObject.put("Price", itemPrice);
				storeItemJSONObject.put("Quantity", itemSize);
				storeItemJSONObject.put("Updated", currentDateTimeString);

				storeItemsJSONArray.put(storeItemJSONObject);
			}

			/*
			 * OK, we updated our storeItemsJSONArray, now its time to write it 
			 * to disk. We are going to follow Androind guidelines and move this
			 * operation out of the main UI thread and into its own thread.
			 */
			Thread writeToStoreItemsJSON = new Thread() {
				public void run() {
					updateStoreItemsJSONFile();
				}
			};
			writeToStoreItemsJSON.start();

		} catch (Exception e) {
			Log.d("onFinishInputDialog", e.getLocalizedMessage());
		}

		// debug
		// Debug.stopMethodTracing();
	}

	private void updateStoreItemsJSONFile() {
		try {
			FileOutputStream outputStream;
			outputStream = openFileOutput("store_items.json",
					Context.MODE_PRIVATE);

			// To make sure that our data is safe we are going to put a file
			// lock on it
			FileLock fl = outputStream.getChannel().tryLock();

			// Write our JSONArray to disk...
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("{\"store_items\":");
			stringBuilder.append(storeItemsJSONArray.toString());
			stringBuilder.append("}");

			outputStream.write(stringBuilder.toString().getBytes());

			// Release our lock before we close the file
			fl.release();

			outputStream.close();

		} catch (IOException ioe) {
			Log.d("updateStoreItemsJSONFile", ioe.getLocalizedMessage());
		}
	}

	/**
	 * Upload the specified file to the PHP server.
	 * 
	 */
	@SuppressWarnings("unused")
	private void uploadFile(String pathToFile, String serverURL) {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;

		String pathToOurFile = pathToFile;
		String urlServer = serverURL;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		try {
			FileInputStream fileInputStream = new FileInputStream(new File(
					pathToOurFile));

			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
							+ pathToOurFile + "\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);

			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();

			String serverHTMLResponse = connection.getContent().toString();

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * This is the old method we used inorder to sync from the database
	 */

	/*	private void startSyncFromServerAsyncTask() {
			AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

				@Override
				protected void onPreExecute() {

					pd = new ProgressDialog(MainActivity.this);
					pd.setTitle("Processing...");
					pd.setMessage("Please wait.");
					pd.setCancelable(false);
					pd.setIndeterminate(true);
					pd.show();
				}

				@Override
				protected Void doInBackground(Void... arg0) {
					try {
						readAndSaveJSONFeed(
								"shopping_items.json",
								"http://192.168.1.124/management/managementController.php?sync_shopping_items=from_server");

						readAndSaveJSONFeed(
								"shopping_item_category.json",
								"http://192.168.1.124/management/managementController.php?sync_shopping_item_category=from_server");

						readAndSaveJSONFeed(
								"stores.json",
								"http://192.168.1.124/management/managementController.php?sync_stores=from_server");

						readAndSaveJSONFeed(
								"store_items.json",
								"http://192.168.1.124/management/managementController.php?sync_store_items=from_server");

						readAndSaveJSONFeed(
								"shopping_items_unit.json",
								"http://192.168.1.124/management/managementController.php?sync_shopping_item_unit=from_server");

						// Refresh our store items
						initStoreItemsArray();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					if (pd != null) {
						pd.dismiss();
					}
				}

			};
			task.execute((Void[]) null);
		}
	*/
	/*
	 * This method will sync our application from the database in a 
	 * dedicated thread and onces its finished update our UI thread.
	 */
	private void startSyncFromServerThread() {
		/*
		 * Show a progress dialog
		 */
		pd = new ProgressDialog(MainActivity.this);
		pd.setTitle("Processing...");
		pd.setMessage("Please wait.");
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		pd.show();

		// Start a thread to sync information from the server
		Thread syncFromServerThread = new Thread() {
			public void run() {
				try {
					readAndSaveJSONFeed(
							"shopping_items.json",
							"http://192.168.1.124/management/managementController.php?sync_shopping_items=from_server");

					readAndSaveJSONFeed(
							"shopping_item_category.json",
							"http://192.168.1.124/management/managementController.php?sync_shopping_item_category=from_server");

					readAndSaveJSONFeed(
							"stores.json",
							"http://192.168.1.124/management/managementController.php?sync_stores=from_server");

					readAndSaveJSONFeed(
							"store_items.json",
							"http://192.168.1.124/management/managementController.php?sync_store_items=from_server");

					readAndSaveJSONFeed(
							"shopping_items_unit.json",
							"http://192.168.1.124/management/managementController.php?sync_shopping_item_unit=from_server");

					// Refresh our store items
					initStoreItemsArray();

					mHandler.post(mUpdateResults);

					if (pd != null) {
						pd.dismiss();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		syncFromServerThread.start();
	}

	/*
	 * This will allow us to filter our store items to only show items which
	 * have not been update before a specific date
	 */
	private void addFilter() {

		// Show a date picker dialog
		FragmentManager fragementManager = ((FragmentActivity) this)
				.getSupportFragmentManager();
		AddFilterDialogFragment addFilterDialogFragment = new AddFilterDialogFragment();

		addFilterDialogFragment.setCancelable(true);
		addFilterDialogFragment.setDialogTitle("Add Filter");
		addFilterDialogFragment.show(fragementManager, "datePicker");
	}

	/*
	 * This method is called once we pick our filter date. This is where the actual filtering of the
	 * ArrayAdapters takes place.
	 */
	public void onDateSet(DatePicker view, int year, int month, int day) {

		/*
		 * First we filter the main storeItemsArrayAdapter
		 */
		storeItemsArrayAdapter.setStoreItem(getItemsBasedOnDate(storeItems,
				year, month, day));

		String selectedCategoryId = getCategoryIdBasedOnName(categoriesSpinner
				.getSelectedItem().toString());
		String selectedStoreId = getStoreIdBasedOnName(storesSpinner
				.getSelectedItem().toString());

		storeItemsArrayAdapter.setStoreItem(getItemsBasedOnCategoryAndStore(
				storeItems, selectedCategoryId, selectedStoreId));

		storeItemsArrayAdapter.notifyDataSetChanged();

		/*
		 * Now for the serachStoreItemsArrayAdapter
		 */

		searchStoreItemsArrayAdapter.setStoreItem(getItemsBasedOnDate(
				storeItems, year, month, day));

		searchStoreItemsArrayAdapter.setStoreItem(getItemsBasedOnStore(
				storeItems, selectedStoreId));

		searchStoreItemsArrayAdapter.notifyDataSetChanged();
	}

	/**
	 * @param storeItems
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	private ArrayList<StoreItem> getItemsBasedOnDate(
			ArrayList<StoreItem> storeItems, int year, int month, int day) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String chosenDate = Integer.toString(year) + "-"
				+ Integer.toString(month + 1) + "-" + Integer.toString(day)
				+ " 00:00:00";

		Date filterDate;

		try {
			filterDate = format.parse(chosenDate);

			for (StoreItem storeItem : storeItems) {
				Date storeItemLastUpdate = format.parse(storeItem.getUpdated());

				if (storeItemLastUpdate.after(filterDate)) {
					storeItem.setFiltered(true);

					// Log.d("getItemsBasedOnDate", "Store Item Last Updated: "+
					// storeItemLastUpdate.toString());

				} else {
					storeItem.setFiltered(false);
				}
			}

		} catch (ParseException e) {
			Log.e("getItemsBasedOnDate", e.getLocalizedMessage());
		}

		// return storeItemsFilteredOnDate;
		return storeItems;
	}

	public static String getStoreImageLocationByStoreName(String storeName) {
		for (Store store : stores) {
			if (store.getName().equalsIgnoreCase(storeName)) {
				return store.getImageLocation();
			}
		}

		return "default.jpg";
	}

	public static ArrayList<StoreItem> getItemsBasedOnStore(
			ArrayList<StoreItem> storeItems, String selectedStoreId) {

		ArrayList<StoreItem> storeItemsInStore = new ArrayList<StoreItem>();

		/*
		 * Add to StoreItemsInCategoryAndStore all shopping items based on a
		 * category and also update their prices based on the store item
		 * information
		 */
		// Go over all shoppingItmes
		boolean exists = false;
		boolean filtered = false;

		for (ShoppingItem shoppingItem : shoppingItems) {
			// I am going to start by assuming that this shopping items does
			// not exist in the store items array
			exists = false;
			filtered = false;
			// Look for the shopping item in the store items array
			for (StoreItem storeItem : storeItems) {
				// First check if this item is not filtered

				if ((storeItem.getShoppingItemId().equalsIgnoreCase(
						shoppingItem.getId()) && (storeItem.getStoreId()
						.equalsIgnoreCase(selectedStoreId)))) {
					if (!storeItem.isFiltered()) {
						storeItemsInStore.add(storeItem);

						exists = true;
						break;
					} else {
						exists = true;
						filtered = true;
						break;
					}
				}
			}

			// If I checked the shopping item against all store items and
			// exists still equals false
			// Than I am going to create a new store item based on the
			// shopping item and add it to
			// The storeItemsInCategoryAndStore array.
			if ((!exists) && (!filtered)) {
				StoreItem newStoreItem = new StoreItem();
				newStoreItem.setShoppingItemCategoryId(shoppingItem
						.getCategoryId());
				newStoreItem.setShoppingItemId(shoppingItem.getId());
				newStoreItem.setShoppingItemImageLocation(shoppingItem
						.getImageLocation());
				newStoreItem.setShoppingItemName(shoppingItem.getName());
				newStoreItem.setShoppingItemDescription(shoppingItem
						.getDescription());
				newStoreItem.setStoreId(selectedStoreId);
				newStoreItem.setPrice("Unknown at this time");
				newStoreItem.setQuantity("Unknown at this time");
				newStoreItem
						.setShoppingItemUnit(getShoppingItemUnitFromUnitId(shoppingItem
								.getShoppingItemUnitId()));
				newStoreItem.setUpdated("0000-00-00 00:00:00");

				storeItemsInStore.add(newStoreItem);
			}
		}

		return storeItemsInStore;
	}

	private void updateResultsInUi() {
		// Refresh our ArrayAdapters
		String selectedStore = storesSpinner.getSelectedItem().toString();
		String selectedCategory = categoriesSpinner.getSelectedItem()
				.toString();
		String selectedStoreId = MainActivity
				.getStoreIdBasedOnName(selectedStore);
		String selectedCategoryId = MainActivity
				.getCategoryIdBasedOnName(selectedCategory);

		// Once for the main array adapter
		storeItemsArrayAdapter.setStoreItem(getItemsBasedOnCategoryAndStore(
				storeItems, selectedCategoryId, selectedStoreId));

		storeItemsArrayAdapter.notifyDataSetChanged();

		// And once for the serach array adapter
		searchStoreItemsArrayAdapter
				.setStoreItem(getItemsBasedOnCategoryAndStore(storeItems,
						selectedCategoryId, selectedStoreId));
		searchStoreItemsArrayAdapter.notifyDataSetChanged();
	}

	/*
	 * This is the method that runs after we pick a store.
	 * This is where we initialize all of our spinners and array adapters
	 * @see com.mymonthlyexpenses.management_system.StorePickerDialogFragment.StorePickerDialogListener#onFinishPickerDialogDialog(java.lang.String)
	 */
	public void onFinishPickerDialogDialog(String storeName) {
		/*
		 * Read our json files into arrays we can use as long as the application
		 * is running
		 */
		initDatabaseArrays();

		storesSpinner = (Spinner) findViewById(R.id.storesSpinner);
		categoriesSpinner = (Spinner) findViewById(R.id.categoriesSpinner);

		this.initStoreSpinner(storesSpinner, stores);
		this.initCategorySpinner(categoriesSpinner, categories);

		String selectedStore = storeName;
		String selectedCategory = categoriesSpinner.getSelectedItem()
				.toString();
		String selectedStoreId = MainActivity
				.getStoreIdBasedOnName(selectedStore);
		String selectedCategoryId = MainActivity
				.getCategoryIdBasedOnName(selectedCategory);

		// Init our one and only StoreItemsArrayAdapter
		storeItemsArrayAdapter = new StoreItemsArrayAdapter(this,
				getItemsBasedOnCategoryAndStore(storeItems, selectedCategoryId,
						selectedStoreId));

		// Init our one and only StoreItemsArrayAdapter
		searchStoreItemsArrayAdapter = new StoreItemsArrayAdapter(this,
				getItemsBasedOnStore(storeItems, selectedStoreId));

		categoriesSpinner
				.setOnItemSelectedListener(new CategoriesSpinnerOnItemSelectedListener(
						this, storeItemsArrayAdapter));

		storesSpinner
				.setOnItemSelectedListener(new StoresSpinnerOnItemSelectedListener(
						this, storeItemsArrayAdapter));

		storesSpinner
				.setSelection(getStoreSpinnerSelectionIndexBasedOnStoreName(storeName));

		// initiate search autocomplete text view
		AutoCompleteTextView searchTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteSearchView);
		searchTextView.setThreshold(3);
		searchTextView.setAdapter(searchStoreItemsArrayAdapter);

		// When we start the application I dont want to have the keyboard open
		// yet
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		/*
		 * Load images for store from assests folder
		 */

		try {
			// get input stream
			InputStream ims = this.getAssets().open(
					getStoreImageLocationByStoreName(selectedStore)
							.replaceFirst("/", ""));
			// load image as Drawable
			Drawable d = Drawable.createFromStream(ims, null);
			// set image to ImageView
			((ImageView) this.findViewById(R.id.storeImageView))
					.setImageDrawable(d);

		} catch (IOException ex) {
			Log.d("onCreate", ex.getLocalizedMessage());
		}
	}

	private int getStoreSpinnerSelectionIndexBasedOnStoreName(String storeName) {
		if (storeName.equalsIgnoreCase("costco"))
			return 0;

		if (storeName.equalsIgnoreCase("h.e.b"))
			return 1;

		if (storeName.equalsIgnoreCase("sams club"))
			return 2;

		if (storeName.equalsIgnoreCase("sprouts"))
			return 3;

		if (storeName.equalsIgnoreCase("walmart"))
			return 4;

		return 0;
	}
}
