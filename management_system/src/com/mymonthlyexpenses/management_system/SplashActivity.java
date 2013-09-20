package com.mymonthlyexpenses.management_system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		new Handler().post(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				/*
				 * If we dont have a store_items.json file we need to go grab one from the server
				 */
				File file = getBaseContext().getFileStreamPath(
						"store_items.json");
				if (!file.exists())
					startSyncFromServerAsyncTask();
				else {
					Intent i = new Intent(SplashActivity.this,
							MainActivity.class);
					startActivity(i);

					// close this activity
					finish();
				}
			}
		});
	}

	private void startSyncFromServerAsyncTask() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {

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
				} catch (Exception e) {
					Log.d("startSyncFromServerAsyncTask",
							e.getLocalizedMessage());
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Intent i = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(i);

				// close this activity
				finish();
			}
		};
		task.execute((Void[]) null);

	}

	private Boolean readAndSaveJSONFeed(String jsonFileName, String URL) {
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

}
