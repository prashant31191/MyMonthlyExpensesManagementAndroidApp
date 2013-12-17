package com.mymonthlyexpenses.management_system.view;

import com.mymonthlyexpenses.management_system.R;
import com.mymonthlyexpenses.management_system.R.id;
import com.mymonthlyexpenses.management_system.R.layout;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class StorePickerDialogFragment extends DialogFragment {

	private Button btn;
	private RadioGroup radioGroup;
	private String storeName;

	static String dialogTitle;

	// ---Interface containing methods to be implemented
	// by calling activity---
	public interface StorePickerDialogListener {
		void onFinishPickerDialogDialog(String storeName);
	}

	public StorePickerDialogFragment() {
		// ---empty constructor required---
	}

	// ---set the title of the dialog window---
	public void setDialogTitle(String title) {
		dialogTitle = title;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.store_picker, container);

		// ---get the Radio Button Group and Button views---
		btn = (Button) view.findViewById(R.id.storePickerBtn);
		radioGroup = (RadioGroup) view.findViewById(R.id.storePickerRadioGrp);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton costco = (RadioButton) view
						.findViewById(R.id.costcoRadio);
				RadioButton heb = (RadioButton) view
						.findViewById(R.id.hebRadio);
				RadioButton walmart = (RadioButton) view
						.findViewById(R.id.walmartRadio);
				RadioButton sprouts = (RadioButton) view
						.findViewById(R.id.sproutsRadio);
				RadioButton samsClub = (RadioButton) view
						.findViewById(R.id.samsClubRadio);

				if (costco.isChecked()) {
					storeName = "costco";
				} else if (heb.isChecked()) {
					storeName = "h.e.b";
				} else if (walmart.isChecked()) {
					storeName = "walmart";
				} else if (sprouts.isChecked()) {
					storeName = "sprouts";
				} else if (samsClub.isChecked()) {
					storeName = "sams club";
				}

				// Enable the choose button
				((Button) view.findViewById(R.id.storePickerBtn))
						.setEnabled(true);
			}
		});

		// ---event handler for the button---
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// ---gets the calling activity---
				StorePickerDialogListener activity = (StorePickerDialogListener) getActivity();

				activity.onFinishPickerDialogDialog(storeName);

				// ---dismiss the alert---
				dismiss();
			}
		});

		// ---set the title for the dialog---
		getDialog().setTitle(dialogTitle);

		return view;
	}
}
