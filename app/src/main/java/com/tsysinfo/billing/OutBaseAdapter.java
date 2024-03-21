package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.TempReciept;

import java.util.ArrayList;

public class OutBaseAdapter extends BaseAdapter {
	private static ArrayList<ReceiptSearchResults> searchArrayList;

	private LayoutInflater mInflater;
	DataBaseAdapter dba;

	Context context;

	public OutBaseAdapter(Context context,
                          ArrayList<ReceiptSearchResults> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		dba= new DataBaseAdapter(context);

	}


	public int getCount() {
		return searchArrayList.size();
	}

	public Object getItem(int position) {
		return searchArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final ReceiptSearchResults receiptSearchResults=searchArrayList.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.out_row, null);
			holder = new ViewHolder();
			holder.txtBillNo = (TextView) convertView.findViewById(R.id.billNo);
			//holder.textViewCustName = (TextView) convertView.findViewById(R.id.custName);
			holder.txtBillDate = (TextView) convertView
					.findViewById(R.id.billDate);
			holder.txtTotalAmount = (TextView) convertView
					.findViewById(R.id.totalAmount);

			holder.textPartPayment = (TextView) convertView
					.findViewById(R.id.PartPayment);

			holder.textViewPrefix = (TextView) convertView
					.findViewById(R.id.billpfx);


			holder.textdays = (TextView) convertView
					.findViewById(R.id.Days);

			holder.textApproval = (TextView) convertView
					.findViewById(R.id.Not_Appro);
			holder.textShortName = (TextView) convertView
					.findViewById(R.id.SoName);
			holder.textNetDue = (TextView) convertView
					.findViewById(R.id.NetDue);

			/*holder.txtBalanceAmount = (TextView) convertView
					.findViewById(R.id.balanceAmount);*/

			holder.checkBox=(CheckBox)convertView.findViewById(R.id.checkBox);

			holder.textViewConfirm = (TextView) convertView
					.findViewById(R.id.btnConfirm);

			holder.editTextAmount = (EditText) convertView
					.findViewById(R.id.editPayAmt);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}




		holder.textViewPrefix.setText(searchArrayList.get(position).getKEY_dbprfx());
		holder.editTextAmount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog	trackVisit = new Dialog(context);
				// Set GUI of login screen
				trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(
                                android.graphics.Color.TRANSPARENT));*/

				trackVisit.setContentView(R.layout.lay_edit);
				trackVisit.setCanceledOnTouchOutside(false);

				Button btnSave1 = (Button) trackVisit
						.findViewById(R.id.btnSave);
				Button btnCancel1 = (Button) trackVisit
						.findViewById(R.id.btnCancel);
				ImageView imgClose1 = (ImageView) trackVisit
						.findViewById(R.id.imageClose);

				final EditText edittext = (EditText) trackVisit
						.findViewById(R.id.editText);
				TextView txt = (TextView) trackVisit
						.findViewById(R.id.txtPay);

				txt.setText("Total Amount : "+receiptSearchResults.getTotalamt());

				edittext.setText(holder.editTextAmount.getText().toString());


				btnSave1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!edittext.getText().toString().equalsIgnoreCase(""))
						{
							double a=Double.parseDouble(edittext.getText().toString());
							double b= Double.parseDouble(receiptSearchResults.getTotalamt());
							if (a>b)
							{
								Toast.makeText(context, "Pay amount is greter than total", Toast.LENGTH_SHORT).show();
							}else {
								holder.editTextAmount.setText(edittext.getText().toString());
								trackVisit.dismiss();

							}
						}
						else {
							Toast.makeText(context, "Enter Amount", Toast.LENGTH_SHORT).show();
						}



					}
				});

				btnCancel1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						trackVisit.cancel();
					}
				});
				imgClose1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						trackVisit.cancel();
					}
				});

				trackVisit.show();
			}
		});


		//holder.textViewCustName.setText(searchArrayList.get(position).getCustName());
		holder.editTextAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, final boolean hasFocus) {
				if (hasFocus && holder.editTextAmount.isEnabled() && holder.editTextAmount.isFocusable()) {
					holder.editTextAmount.post(new Runnable() {
						@Override
						public void run() {
							final InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(holder.editTextAmount,InputMethodManager.SHOW_IMPLICIT);
						}
					});
				}
			}
		});
		holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
				{
					holder.editTextAmount.setVisibility(View.VISIBLE);
					holder.textViewConfirm.setVisibility(View.VISIBLE);

					final Dialog	trackVisit = new Dialog(context);
					// Set GUI of login screen
					trackVisit.requestWindowFeature(Window.FEATURE_NO_TITLE);
              /*  getWindow().setBackgroundDrawable(
                        new ColorDrawable(
                                android.graphics.Color.TRANSPARENT));*/

					trackVisit.setContentView(R.layout.lay_edit);
					trackVisit.setCanceledOnTouchOutside(false);

					Button btnSave1 = (Button) trackVisit
							.findViewById(R.id.btnSave);
					Button btnCancel1 = (Button) trackVisit
							.findViewById(R.id.btnCancel);
					ImageView imgClose1 = (ImageView) trackVisit
							.findViewById(R.id.imageClose);

					final EditText edittext = (EditText) trackVisit
							.findViewById(R.id.editText);
					TextView txt = (TextView) trackVisit
							.findViewById(R.id.txtPay);

					txt.setText("Total Amount : "+receiptSearchResults.getTotalamt());



					btnSave1.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (!edittext.getText().toString().equalsIgnoreCase(""))
							{
								double a=Double.parseDouble(edittext.getText().toString());
								double b= Double.parseDouble(receiptSearchResults.getTotalamt());
								if (a>b)
								{
									Toast.makeText(context, "Pay amount is greter than total", Toast.LENGTH_SHORT).show();
								}else {
									holder.editTextAmount.setText(edittext.getText().toString());
									trackVisit.dismiss();

								}

							}
							else {
								Toast.makeText(context, "Enter Amount", Toast.LENGTH_SHORT).show();
							}



						}
					});

					btnCancel1.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							trackVisit.cancel();
						}
					});
					imgClose1.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							trackVisit.cancel();
						}
					});

					trackVisit.show();

				}
				else {
					dba.open();
					DataBaseAdapter.ourDatabase.execSQL("Delete from "+TempReciept.DATABASE_TABLE+" where dbno='"+receiptSearchResults.getBillid()+"'");
					dba.close();
					holder.editTextAmount.setVisibility(View.GONE);
					holder.textViewConfirm.setVisibility(View.GONE);

				}
			}
		});
		holder.textViewConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (holder.editTextAmount.getText().toString().equalsIgnoreCase(""))
				{

				}else {
					if (ReceiptActivity.allocated>0) {

						double d=ReceiptActivity.remain;

						if (Double.parseDouble(holder.editTextAmount.getText().toString())<=d) {


							dba.open();
							ContentValues contentValues = new ContentValues();
							contentValues.put(TempReciept.KEY_custNo, receiptSearchResults.getKEY_custNo());
							contentValues.put(TempReciept.KEY_dbamount, receiptSearchResults.getTotalamt());
							contentValues.put(TempReciept.KEY_dbdate, receiptSearchResults.getDate());
							contentValues.put(TempReciept.KEY_dbkey, receiptSearchResults.getKEY_dbkey());
							contentValues.put(TempReciept.KEY_dbno, receiptSearchResults.getBillid());
							contentValues.put(TempReciept.KEY_dbpay, holder.editTextAmount.getText().toString());
							contentValues.put(TempReciept.KEY_dbpending, receiptSearchResults.getOutstandingamt());
							contentValues.put(TempReciept.KEY_dbprfx, "N");
							contentValues.put(TempReciept.KEY_dbsofar, receiptSearchResults.getKEY_dbsofar());
							contentValues.put(TempReciept.KEY_Rdate, receiptSearchResults.getDate());
							DataBaseAdapter.ourDatabase.insert(TempReciept.DATABASE_TABLE, null, contentValues);
							dba.close();
							holder.textViewConfirm.setText("Confirmed");
							Toast.makeText(context, "Confirmed", Toast.LENGTH_SHORT).show();
							holder.textViewConfirm.setCompoundDrawablesWithIntrinsicBounds(R.drawable.confirm, 0, 0, 0);
						ReceiptActivity.remain=ReceiptActivity.remain-Double.parseDouble(holder.editTextAmount.getText().toString());

							if(context instanceof ReceiptActivity){
								((ReceiptActivity)context).setBal();
							}

						}else
						{
							Toast.makeText(context, "You have only Rs."+d +" To allocate", Toast.LENGTH_SHORT).show();
						}
					}else {
						Toast.makeText(context, "Please Enter Received Amount First", Toast.LENGTH_SHORT).show();
					}
					}
			}
		});
		holder.txtBillNo.setText(searchArrayList.get(position).getBillid());
		holder.txtBillDate.setText(searchArrayList.get(position).getDate());
		holder.txtTotalAmount.setText(searchArrayList.get(position)
				.getTotalamt());
		/*holder.txtBalanceAmount.setText(searchArrayList.get(position)
				.getOutstandingamt());*/

		holder.textPartPayment.setText(searchArrayList.get(position).getPartPayment());
		holder.textApproval.setText(searchArrayList.get(position).getNotApproval());
		holder.textdays.setText(searchArrayList.get(position).getDays());
		holder.textShortName.setText(searchArrayList.get(position).getSoShortName());
		holder.textNetDue.setText(searchArrayList.get(position).getNetDue());
		return convertView;
	}
	public int getViewTypeCount() {
		if (getCount() != 0)
			return getCount();

		return 1;
	}
	public int getItemViewType(int position) {
		return position;
	}

	static class ViewHolder {

		TextView txtBillNo, txtBillDate, txtTotalAmount,
				btnPay,textPartPayment,textdays,textApproval,textShortName,textNetDue;


		//TextView textViewCustName;
		CheckBox checkBox;
		TextView textViewPrefix;
		TextView textViewConfirm;
		EditText editTextAmount;

	}
}
