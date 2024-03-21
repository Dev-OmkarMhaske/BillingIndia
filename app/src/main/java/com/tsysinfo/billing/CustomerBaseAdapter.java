package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.CustomerTable;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class CustomerBaseAdapter extends BaseAdapter {
	private static ArrayList<CustomerSearchResults> searchArrayList;
	private LayoutInflater mInflater;
	Context context;
    String Data = "";
    public Location mLastLocation;
    String custNo="";
    Double currLatitude;
    Double currLongitude;

    SessionManager session;
    DataBaseAdapter dba;
    Models mod;

    ProgressDialog waitDialog;
    JSONArray serverResponse;
    static boolean errored = false;
    private String custid;
    private String custID;


    public CustomerBaseAdapter(Context context, ArrayList<CustomerSearchResults> results) {
		searchArrayList = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;

        session = new SessionManager(context);
        dba = new DataBaseAdapter(context);
        mod = new Models();

        waitDialog = new ProgressDialog(context);
        waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        waitDialog.setMessage("Please Wait");

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



    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //Nilima

    @Override
    public int getViewTypeCount() {
        return 1;
    }


	@SuppressLint("InflateParams")
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cust_list_row, null);
			holder = new ViewHolder();
            holder.textViewFlag = (TextView) convertView.findViewById(R.id.flag);
			holder.txtName = (TextView) convertView.findViewById(R.id.customerName);
			holder.txtAddress = (TextView) convertView.findViewById(R.id.customerAddress);
			holder.txtPhone = (TextView) convertView.findViewById(R.id.customerContact);
			holder.btnMap = (ImageView) convertView.findViewById(R.id.customerMap);
			holder.btnUplodeLoc = (ImageView) convertView.findViewById(R.id.uploadLocation);
            holder.printBarcode=(ImageView)convertView.findViewById(R.id.printBarcode);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}


       // CustomerSearchResults customerSearchResults=new CustomerSearchResults();
		if ((searchArrayList.get(position).getLatl().equalsIgnoreCase("") || searchArrayList.get(position).getLatl().equalsIgnoreCase("null"))&&(searchArrayList.get(position).getLog().equalsIgnoreCase("") || searchArrayList.get(position).getLog().equalsIgnoreCase("null")))
        {
            holder.textViewFlag.setVisibility(View.VISIBLE);
        }

		holder.txtName.setText(searchArrayList.get(position).getName());
		holder.txtAddress.setText(searchArrayList.get(position).getAddress());
		holder.txtPhone.setText(searchArrayList.get(position).getPhone());
		custID = searchArrayList.get(position).getID();

		Log.e("SearchArry",""+searchArrayList.get(position).getLatl());

          holder.printBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncCallWSBarcode().execute(custID);
                /*  Intent intent = new Intent(context,BarcodePrintActivity.class);
                intent.putExtra("custid",custID);
                context.startActivity(intent);
                ((Activity)context).finish();*/

            }
        });

		holder.btnMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			//	Toast.makeText(context, ""+custID, Toast.LENGTH_LONG).show();
                custID = searchArrayList.get(position).getID();
                Intent intent = new Intent(context,MapActivity.class);
				intent.putExtra("custid",custID);
                context.startActivity(intent);
                ((Activity)context).finish();


                Log.e("Logcust",""+custID);

			}
		});

		holder.btnUplodeLoc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			//	Toast.makeText(context, ""+custID, Toast.LENGTH_LONG).show();
                GPSTracker gps = new GPSTracker(context);
                currLatitude = 0.0;
                currLongitude = 0.0;
                custNo = "";
                // check if GPS enabled
                if (gps.canGetLocation()) {

                /*    mLastLocation = LocationServices.FusedLocationApi
                            .getLastLocation(MenuActivity.mGoogleApiClient);*/
                    currLatitude = gps.getLatitude();
                    currLongitude = gps.getLongitude();


                    custID = searchArrayList.get(position).getID();
                    if(currLatitude != 0.0 && currLongitude != 0.0) {
                        Data = custID + "," + currLatitude + "," + currLongitude;
                        custNo = custID;
                        AsyncCallWS task = new AsyncCallWS();
                        // Call execute
                        task.execute();
                    }else{
                        Toast.makeText(context, "Not able to connect GPS\nPlease move to the open space",
                                Toast.LENGTH_LONG).show();
                    }
                }


			}
		});


		return convertView;
	}

	static class ViewHolder {

		TextView txtName,textViewFlag;
		TextView txtAddress;
		TextView txtPhone;
		ImageView btnMap;
        ImageView btnUplodeLoc;
        ImageView printBarcode;
	}

    // WebService Code

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {

                Log.w("CartActivity", "Start");

                dba.open();
                serverResponse = WebService.putData(Data, session.getBranchNo(), "setCustomerLocation");
                dba.close();

                /*dba.open();
                serverResponse = WebService.customerUpdate(session.getBranchNo(),custNo, Data,currLatitude.toString(),currLongitude.toString(), "UpdateCustomer");
                dba.close();*/

                Log.e("custmerid","jj"+Data);
            } catch (Exception e) {
                Log.w("CartActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("CartActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("CartActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("CartActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("CartActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setTitle("Connection Time Out!");

                builder.setMessage("Please Try Again!!!")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();

                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

            } else {
                Log.w("CartActivity", "Try");
                try {

					/*
                     * Toast.makeText(LoginActivity.this, "JSON Data:  " +
					 * WebService.responseString, Toast.LENGTH_LONG).show();
					 */
                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("No")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Failed to Update Location.");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();

                            } else if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Success")) {

                                dba.open();
                                ContentValues args = new ContentValues();
                                args.put(CustomerTable.KEY_LATI, currLatitude);
                                args.put(CustomerTable.KEY_LONGI, currLongitude);
                                DataBaseAdapter.ourDatabase.update("customer", args, "id" + "=" + custNo, null);
                                dba.close();

                                Toast.makeText(context,
                                        "Location Updated Successfull!!!",
                                        Toast.LENGTH_LONG);


                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(context, "Server Error",
                                Toast.LENGTH_LONG).show();
                        // statusTV.setText("Error occured in invoking webservice");
                    }
                    // Re-initialize Error Status to False
                    WebService.responseString = "";
                    errored = false;

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }

        @Override
        // Make Progress Bar visible
        protected void onPreExecute() {
            waitDialog.show();
            waitDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private class AsyncCallWSBarcode extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
          custid = params[0];
            // Call Web Method
            try {
                Log.w("CartActivity", "Start");
                dba.open();
                serverResponse = WebService.barcodePrint(custid,"BarcodePrint");
                dba.close();
            } catch (Exception e) {
                Log.w("CartActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("CartActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("CartActivity", "ResponseString : " + WebService.responseString);

            // Make Progress Bar invisible
            waitDialog.cancel();
            Log.w("CartActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("CartActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setTitle("Connection Time Out!");

                builder.setMessage("Please Try Again!!!")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();

                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();

            } else {
                Log.w("CartActivity", "Try");
                try {

					/*
                     * Toast.makeText(LoginActivity.this, "JSON Data:  " +
					 * WebService.responseString, Toast.LENGTH_LONG).show();
					 */
                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {

                            if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Failure")) {

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                                        context);
                                builder2.setTitle("Failure..");
                                builder2.setMessage("Please Try Again");
                                builder2.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Log.e("info", "OK");
                                            }
                                        });
                                builder2.show();

                            } else if (serverResponse.getJSONObject(0)
                                    .getString("Status").trim()
                                    .equalsIgnoreCase("Success")) {

                                Intent intent = new Intent(context,BarcodePrintActivity.class);
                                intent.putExtra("custid",custid);
                                context.startActivity(intent);
                                ((Activity)context).finish();
                            }

                        }

                        // Error status is true
                    } else {

                        Toast.makeText(context, "Server Error",
                                Toast.LENGTH_LONG).show();
                        // statusTV.setText("Error occured in invoking webservice");
                    }
                    // Re-initialize Error Status to False

                    errored = false;

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }

        @Override
        // Make Progress Bar visible
        protected void onPreExecute() {
            waitDialog.show();
            waitDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }



}
