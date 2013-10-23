package com.mymonthlyexpenses.management_system;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UpdateStoreItemDialogFragment extends DialogFragment {

	EditText txtPrice;
	EditText txtSize;
	TextView txtUnit;
	TextView txtPricePerUnit;
	Button btn;
	static String dialogTitle;
	String itemUnit;
	String itemName;
	String itemDescription;

	// In order for us to be able to present as part of the dialog the exisiting
	// price and size we need to keep them as strings
	String itemPrice;
	String itemSize;

	// ---Interface containing methods to be implemented
	// by calling activity---
	public interface UpdateStoreItemDialogListener {
		void onFinishInputDialog(String itemPrice, String itemSize,
				String itemName, String itemDescription, String itemUnit);
	}

	public UpdateStoreItemDialogFragment() {
		// ---empty constructor required---
	}

	// ---set the title of the dialog window---
	public void setDialogTitle(String title) {
		dialogTitle = title;
	}

	public void setItemUnitText(String itemUnit) {
		this.itemUnit = itemUnit;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.fragment_update_store_item_dialog, container);

		// ---get the EditText and Button views---
		txtPrice = (EditText) view.findViewById(R.id.txtPrice);
		txtSize = (EditText) view.findViewById(R.id.txtSize);

		txtPricePerUnit = (TextView) view
				.findViewById(R.id.itemPricePerUnitTextView);

		try {

			if ((Double.valueOf(itemPrice) != null)
					&& (Double.valueOf(itemSize) != 0)) {
				DecimalFormat df = new DecimalFormat("0.##");
				txtPricePerUnit.setText("$ "
						+ String.valueOf((df.format(Double.valueOf(itemPrice)
								/ Double.valueOf(itemSize)))));
			}

		} catch (NumberFormatException exception) {
			txtPricePerUnit.setText("0");
		}

		txtUnit = (TextView) view.findViewById(R.id.itemUnitTextView);
		txtUnit.setText(itemUnit);

		// Show the user the current price and size
		if (!itemPrice.equalsIgnoreCase("Unknown At This Time")) {
			txtPrice.setText(itemPrice);
			txtSize.setText(itemSize);
		}

		btn = (Button) view.findViewById(R.id.btnDone);

		// ---event handler for the button---
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// ---gets the calling activity---
				UpdateStoreItemDialogListener activity = (UpdateStoreItemDialogListener) getActivity();

				activity.onFinishInputDialog(txtPrice.getText().toString(),
						txtSize.getText().toString(), itemName,
						itemDescription, itemUnit);
				// ---dismiss the alert---
				dismiss();
			}
		});

		// ---show the keyboard automatically---
		txtPrice.requestFocus();
		getDialog().getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		// ---set the title for the dialog---
		getDialog().setTitle(dialogTitle);

		return view;
	}

	public void setItemPrice(String itemPrice) {
		this.itemPrice = itemPrice;

	}

	public void setItemSize(String itemSize) {
		this.itemSize = itemSize;

	}
}