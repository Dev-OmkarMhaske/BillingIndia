package com.tsysinfo.billing;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.AsyncTask;
 
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.tsysinfo.billing.database.SessionManager;
import com.tsysinfo.billing.database.WebService;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ViewExpenses extends AppCompatActivity {
    final Calendar myCalendar = Calendar.getInstance();
    TextView tv_fromdate,tv_todate,tv_BranchNo,tv_EmpNo;
    Button  bt_btnShow;
    static boolean errored = false;
    private ProgressDialog waitDialog;
    JSONArray serverResponse;
    DataBaseAdapter dba;
    Models mod;
    ArrayList<GetExpense> getExpenses;
    ListView list;
    TextView tv_date,tv_MainCategory;
    TextView tv_SubCategory,tv_Remarks,tv_Amount;
    private int mYear;
    private int mMonth;
    private int mDay;
    private String mmonth = "";

    SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);
        getSupportActionBar().setTitle(" View Expense Details");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv_todate=(TextView) findViewById(R.id.tv_todate);
        tv_fromdate=(TextView) findViewById(R.id.tv_fromdate);
//        tv_date=(TextView) findViewById(R.id.tv_date);
//        tv_MainCategory=(TextView) findViewById(R.id.tv_MainCategory);
//        tv_SubCategory=(TextView) findViewById(R.id.tv_SubCategory);
//        tv_Remarks=(TextView) findViewById(R.id.tv_Remarks);
//        tv_Amount=(TextView) findViewById(R.id.tv_Amount);
        list=(ListView) findViewById(R.id.list);

        bt_btnShow=(Button) findViewById(R.id.bt_btnShow);
        session = new SessionManager(this);
        dba = new DataBaseAdapter(this);
        mod = new Models();
        getExpenses = new ArrayList<>();



        tv_fromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(
                        ViewExpenses.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker,int selectedday, int selectedmonth,
                                           int selectedyear) {

                        int month = selectedmonth + 1;

                        if (month == 1) {
                            mmonth = "01";

                        } else if (month == 2) {

                            mmonth = "02";

                        } else if (month == 3) {

                            mmonth = "03";

                        } else if (month == 4) {

                            mmonth = "04";

                        } else if (month == 5) {

                            mmonth = "05";

                        } else if (month == 6) {

                            mmonth = "06";

                        } else if (month == 7) {

                            mmonth = "07";

                        } else if (month == 8) {

                            mmonth = "08";

                        } else if (month == 9) {

                            mmonth = "09";

                        } else if (month == 10) {

                            mmonth = "10";

                        } else if (month == 11) {

                            mmonth = "11";

                        } else if (month == 12) {

                            mmonth = "12";

                        }
                        String a = String.valueOf(selectedday);
                        if (a.length() == 1) {
                            a = "0" + selectedday;
                        }
                        final String dt1 = mmonth + "/"+ selectedyear + "/" + a;
                        tv_fromdate.setText(dt1);

                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                mDatePicker.show();

            }
        });


        tv_todate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(
                        ViewExpenses.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker,int selectedday, int selectedmonth,
                                          int selectedyear) {

                        int month = selectedmonth + 1;

                        if (month == 1) {
                            mmonth = "01";

                        } else if (month == 2) {

                            mmonth = "02";

                        } else if (month == 3) {

                            mmonth = "03";

                        } else if (month == 4) {

                            mmonth = "04";

                        } else if (month == 5) {

                            mmonth = "05";

                        } else if (month == 6) {

                            mmonth = "06";

                        } else if (month == 7) {

                            mmonth = "07";

                        } else if (month == 8) {

                            mmonth = "08";

                        } else if (month == 9) {

                            mmonth = "09";

                        } else if (month == 10) {

                            mmonth = "10";

                        } else if (month == 11) {

                            mmonth = "11";

                        } else if (month == 12) {

                            mmonth = "12";

                        }
                        String a = String.valueOf(selectedday);
                        if (a.length() == 1) {
                            a = "0" + selectedday;
                        }
                        final String dt1 = mmonth + "/"+ selectedyear + "/" + a;
                        tv_todate.setText(dt1);

                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Date");
                mDatePicker.show();

            }
        });



        bt_btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String FromDate=tv_fromdate.getText().toString();
                String ToDate=tv_todate.getText().toString();

                new AsyncCallWSearch().execute();

            }
        });
    }



    private class AsyncCallWSearch extends AsyncTask<String, Void, Void> {

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(String... params) {
            // Call Web Method
            try {
                Log.w("ExpenseActivity", "Start");
                dba.open();

                serverResponse = WebService.GetExpenses(session.getBranchNo(),tv_fromdate.getText().toString(),tv_todate.getText().toString(), session.getEmpNo(), "GetExpenses");
                dba.close();
            } catch (Exception e) {
                Log.w("ExpenseActivity", "Timeout ");
            }
            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        // Once WebService returns response
        protected void onPostExecute(Void result) {

            Log.w("ExpenseActivity", "TimeOutFlag : " + WebService.timeoutFlag);
            Log.w("ExpenseActivity", "ResponseString : "
                    + WebService.responseString);

            // Make Progress Bar invisible
            //waitDialog.cancel();
            Log.w("ExpenseActivity", "Dialog Closed");
            if (WebService.timeoutFlag == 1) {
                Log.w("ExpenseActivity", "Timeout");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ViewExpenses.this);
                builder.setTitle("Connection Time Out!");
                builder.setMessage("Please Try Again!!!").setCancelable(false).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Log.w("ExpenseActivity", "Try");
                try {
                    /*
                     * Toast.makeText(LoginActivity.this, "JSON Data:  " +
                     * WebService.responseString, Toast.LENGTH_LONG).show();
                     */
                    // Error status is false
                    if (!errored) {
                        // Based on Boolean value returned from WebService
                        if (serverResponse != null) {
                            if (serverResponse.getJSONObject(0).getString("Date").trim().equalsIgnoreCase("No")) {
                                Toast.makeText(ViewExpenses.this, "No More Item Found", Toast.LENGTH_SHORT).show();
                            } else {
                                getExpenses.clear();

                                for (int i = 0; i < serverResponse.length(); i++) {
                                    GetExpense chitInfo = new GetExpense();
                                    chitInfo.setDate(serverResponse.getJSONObject(i).getString("Date"));
                                    chitInfo.setMainCategory(serverResponse.getJSONObject(i).getString("MainCategory"));
                                    chitInfo.setSubCategory(serverResponse.getJSONObject(i).getString("SubCategory"));
                                    chitInfo.setRemarks(serverResponse.getJSONObject(i).getString("Remarks"));
                                    chitInfo.setAmount(serverResponse.getJSONObject(i).getString("Amount"));
                                    getExpenses.add(chitInfo);

                                }


                            }

                            GetExpenseListAdapter adapter = new GetExpenseListAdapter(ViewExpenses.this ,getExpenses);
                            list.setAdapter(adapter);
                        }
                        // Error status is true
                    } else {
                        Toast.makeText(ViewExpenses.this, "Server Error", Toast.LENGTH_LONG).show();
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
            //  waitDialog.show();
            // waitDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private void updateLabel() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        final String strDate = sdf.format(c.getTime());
        tv_todate.setText(strDate);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
