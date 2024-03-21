package com.tsysinfo.billing.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.tsysinfo.billing.FileDownloader;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Sync {

    Models mod;
    SessionManager session;
    String EmpNo, BranchNo;

    public int getWeek(String curdate) {

        String input = curdate;
        String format = "yyyyMMdd";
        try {
            SimpleDateFormat df = new SimpleDateFormat(format);
            Date date = df.parse(input);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int week = cal.get(Calendar.WEEK_OF_YEAR);

            System.out.println("Input " + input + " is in week " + week);
            return week;
        } catch (ParseException e) {
            System.out.println("Could not find a week in " + input);
            return 0;
        }
    }

    public void syncdata(Context context) {

        JSONArray jobj;

        mod = new Models();
        session = new SessionManager(context);

        mod.clearDatabase(CustomerTable.DATABASE_TABLE);
        mod.clearDatabase(AllCustomer.DATABASE_TABLE);
        mod.clearDatabase(DeliveryTable.DATABASE_TABLE);
        mod.clearDatabase(ReceiptTable.DATABASE_TABLE);
        //mod.clearDatabase(SalesTypeTable.DATABASE_TABLE);
        mod.clearDatabase(AuthorityTable.DATABASE_TABLE);
        mod.clearDatabase(DistrictTable.DATABASE_TABLE);
        mod.clearDatabase(CustomerTypeTable.DATABASE_TABLE);
/*
        mod.clearDatabase(TownTable.DATABASE_TABLE);
		mod.clearDatabase(CustomerTypeTable.DATABASE_TABLE);
*/
        EmpNo = session.getEmpNo();
        BranchNo = session.getBranchNo();

        ContentValues cv = new ContentValues();

        // get Customer data
        jobj = new JSONArray();
        cv = new ContentValues();

        Date date = new Date();  // to get the date
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd"); // getting date in this format
        String formattedDate = df.format(date.getTime());
        Calendar now = Calendar.getInstance();

        System.out.println("Current week of month is : " +
                now.get(Calendar.WEEK_OF_MONTH));

        jobj = WebService.getData(EmpNo, String.valueOf(now.get(Calendar.WEEK_OF_MONTH)), "GetAllCustomer");
        try {
            if (jobj != null && jobj.length() > 0) {
                if (jobj.getJSONObject(0).getString("CustNo").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("CustNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_ID, jobj.getJSONObject(i)
                                    .getString("CustNo").trim());

                        } else {
                            cv.put(CustomerTable.KEY_ID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Name").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_NAME, jobj
                                    .getJSONObject(i).getString("Name").trim());

                        } else {
                            cv.put(CustomerTable.KEY_NAME, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Place").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_PLACE,
                                    jobj.getJSONObject(i).getString("Place")
                                            .trim());

                        } else {
                            cv.put(CustomerTable.KEY_PLACE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Address").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_ADDRESS, jobj
                                    .getJSONObject(i).getString("Address")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_ADDRESS, "");
                        }

                        if (!jobj.getJSONObject(i).getString("RegNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_REGNO,
                                    jobj.getJSONObject(i).getString("RegNo")
                                            .trim());

                        } else {
                            cv.put(CustomerTable.KEY_REGNO, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Tin").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_TIN, jobj.getJSONObject(i)
                                    .getString("Tin").trim());

                        } else {
                            cv.put(CustomerTable.KEY_TIN, "");
                        }

                        if (!jobj.getJSONObject(i).getString("pin").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_PIN, jobj.getJSONObject(i)
                                    .getString("pin").trim());

                        } else {
                            cv.put(CustomerTable.KEY_PIN, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CSTNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_CSTNO,
                                    jobj.getJSONObject(i).getString("CSTNo")
                                            .trim());

                        } else {
                            cv.put(CustomerTable.KEY_CSTNO, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Area").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_AREA, jobj
                                    .getJSONObject(i).getString("Area").trim());

                        } else {
                            cv.put(CustomerTable.KEY_AREA, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CustType").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_TYPE, jobj
                                    .getJSONObject(i).getString("CustType")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_TYPE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("RateCodeId")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_RATECODE, jobj
                                    .getJSONObject(i).getString("RateCodeId")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_RATECODE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("MobileNO").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_MOBILE, jobj
                                    .getJSONObject(i).getString("MobileNO")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_MOBILE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("LandLineNo").trim().equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_LANDLINE, jobj.getJSONObject(i).getString("LandLineNo").trim());

                        } else {
                            cv.put(CustomerTable.KEY_LANDLINE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Longitude").trim().equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_LONGI,
                                    jobj.getJSONObject(i)
                                            .getString("Longitude").trim());

                        } else {
                            cv.put(CustomerTable.KEY_LONGI, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Latitude").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_LATI, jobj
                                    .getJSONObject(i).getString("Latitude")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_LATI, "");
                        }


                        if (!jobj.getJSONObject(i).getString("customercode").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_CUSTOMER_CODE, jobj
                                    .getJSONObject(i).getString("customercode")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_CUSTOMER_CODE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("DistrictName").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_District, jobj
                                    .getJSONObject(i).getString("DistrictName")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_District, "");
                        }

                        if (!jobj.getJSONObject(i).getString("EmailId").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_Email, jobj
                                    .getJSONObject(i).getString("EmailId")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_Email, "");
                        }

                    /*    if (!jobj.getJSONObject(i).getString("ShortName").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_SHORTNAME, jobj
                                    .getJSONObject(i).getString("ShortName")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_SHORTNAME, "");
                        }*/

                       /* if (!jobj.getJSONObject(i).getString("Status").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_STATUS, jobj
                                    .getJSONObject(i).getString("Status")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_STATUS, "");
                        }*/
 /*  "CustNo":"SADECUST0000000108",
                "Name":"VENGAL ALUMINIUM STORES,",
                "Place":"TIRUVALLA.",
                "Tin":"32030556052",
                "pin":".",
                "RegNo":"32030556052",
                "CSTNo":".",
                "Area":"TIRUVALLA",
                "CustType":"",
                "RateCodeId":"",
                "MobileNO":null,
                "LandLineNo":null,
                "Longitude":"",
                "Latitude":"",
                "RootName":"TIRUVALLA",
                "customercode":"000913    "*/


                        mod.insertdata(CustomerTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        jobj = WebService.getAllCust(EmpNo, "GetAllCustomer");
        try {
            if (jobj != null && jobj.length() > 0) {
                if (jobj.getJSONObject(0).getString("CustNo").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("CustNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_ID, jobj.getJSONObject(i)
                                    .getString("CustNo").trim());

                        } else {
                            cv.put(AllCustomer.KEY_ID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Name").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_NAME, jobj
                                    .getJSONObject(i).getString("Name").trim());

                        } else {
                            cv.put(AllCustomer.KEY_NAME, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Place").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_PLACE,
                                    jobj.getJSONObject(i).getString("Place")
                                            .trim());

                        } else {
                            cv.put(AllCustomer.KEY_PLACE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Address").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_ADDRESS, jobj
                                    .getJSONObject(i).getString("Address")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_ADDRESS, "");
                        }

                        if (!jobj.getJSONObject(i).getString("RegNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_REGNO,
                                    jobj.getJSONObject(i).getString("RegNo")
                                            .trim());

                        } else {
                            cv.put(AllCustomer.KEY_REGNO, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Tin").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_TIN, jobj.getJSONObject(i)
                                    .getString("Tin").trim());

                        } else {
                            cv.put(AllCustomer.KEY_TIN, "");
                        }

                        if (!jobj.getJSONObject(i).getString("pin").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_PIN, jobj.getJSONObject(i)
                                    .getString("pin").trim());

                        } else {
                            cv.put(AllCustomer.KEY_PIN, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CSTNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_CSTNO,
                                    jobj.getJSONObject(i).getString("CSTNo")
                                            .trim());

                        } else {
                            cv.put(AllCustomer.KEY_CSTNO, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Area").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_AREA, jobj
                                    .getJSONObject(i).getString("Area").trim());

                        } else {
                            cv.put(AllCustomer.KEY_AREA, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CustType").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_TYPE, jobj
                                    .getJSONObject(i).getString("CustType")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_TYPE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("RateCodeId")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_RATECODE, jobj
                                    .getJSONObject(i).getString("RateCodeId")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_RATECODE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("MobileNO").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_MOBILE, jobj
                                    .getJSONObject(i).getString("MobileNO")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_MOBILE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("LandLineNo")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_LANDLINE, jobj
                                    .getJSONObject(i).getString("LandLineNo")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_LANDLINE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Longitude")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_LONGI,
                                    jobj.getJSONObject(i)
                                            .getString("Longitude").trim());

                        } else {
                            cv.put(AllCustomer.KEY_LONGI, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Latitude").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_LATI, jobj
                                    .getJSONObject(i).getString("Latitude")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_LATI, "");
                        }


                        if (!jobj.getJSONObject(i).getString("customercode").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_CUSTOMER_CODE, jobj
                                    .getJSONObject(i).getString("customercode")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_CUSTOMER_CODE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("DistrictName").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_District, jobj
                                    .getJSONObject(i).getString("DistrictName")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_District, "");
                        }

                        if (!jobj.getJSONObject(i).getString("EmailId").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(AllCustomer.KEY_Email, jobj
                                    .getJSONObject(i).getString("EmailId")
                                    .trim());

                        } else {
                            cv.put(AllCustomer.KEY_Email, "");
                        }

                    /*    if (!jobj.getJSONObject(i).getString("ShortName").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_SHORTNAME, jobj
                                    .getJSONObject(i).getString("ShortName")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_SHORTNAME, "");
                        }*/

                       /* if (!jobj.getJSONObject(i).getString("Status").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTable.KEY_STATUS, jobj
                                    .getJSONObject(i).getString("Status")
                                    .trim());

                        } else {
                            cv.put(CustomerTable.KEY_STATUS, "");
                        }*/
 /*  "CustNo":"SADECUST0000000108",
                "Name":"VENGAL ALUMINIUM STORES,",
                "Place":"TIRUVALLA.",
                "Tin":"32030556052",
                "pin":".",
                "RegNo":"32030556052",
                "CSTNo":".",
                "Area":"TIRUVALLA",
                "CustType":"          ",
                "RateCodeId":"",
                "MobileNO":null,
                "LandLineNo":null,
                "Longitude":"",
                "Latitude":"",
                "RootName":"TIRUVALLA",
                "customercode":"000913    "*/


                        mod.insertdata(AllCustomer.DATABASE_TABLE, cv);

                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // get Delivery data
        jobj = new JSONArray();
        cv = new ContentValues();

        jobj = WebService.getOutstanding(EmpNo, "GetOutstanding");
        try {
            if (jobj != null && jobj.length() > 0) {
                if (jobj.getJSONObject(0).getString("CustKey").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("CustKey").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_CUSTID, jobj.getJSONObject(i)
                                    .getString("CustKey").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_CUSTID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CNAME").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_CUSTNAME, jobj.getJSONObject(i)
                                    .getString("CNAME").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_CUSTNAME, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BillAmount").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_TOTALAMT, jobj.getJSONObject(i)
                                    .getString("BillAmount").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_TOTALAMT, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BKeySerial").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_BILLID, jobj.getJSONObject(i)
                                    .getString("BKeySerial").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_BILLID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BillDate").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_BILLDATE, jobj.getJSONObject(i)
                                    .getString("BillDate").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_BILLDATE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BillAmount").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_OUTSTANDINGAMT, jobj.getJSONObject(i)
                                    .getString("BillAmount").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_OUTSTANDINGAMT, "");
                        }

                        if (!jobj.getJSONObject(i).getString("NetDue").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ReceiptTable.KEY_outpending, jobj.getJSONObject(i)
                                    .getString("NetDue").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_outpending, "");
                        }

                        cv.put(ReceiptTable.KEY_OutSFkey, session.getEmpNo());

                        if (!jobj.getJSONObject(i).getString("BillNumer").trim()
                                .equalsIgnoreCase("null")) {
                            cv.put(ReceiptTable.KEY_OutBkey, jobj.getJSONObject(i)
                                    .getString("BillNumer").trim());
                        } else {
                            cv.put(ReceiptTable.KEY_OutBkey, "");
                        }


                        if (!jobj.getJSONObject(i).getString("PartPayment").trim()
                                .equalsIgnoreCase("null")) {
                            cv.put(ReceiptTable.KEY_partpaymen, jobj.getJSONObject(i)
                                    .getString("PartPayment").trim());
                        } else {
                            cv.put(ReceiptTable.KEY_partpaymen, "");
                        }


                        if (!jobj.getJSONObject(i).getString("WaitngForapproval").trim()
                                .equalsIgnoreCase("null")) {
                            cv.put(ReceiptTable.KEY_Not_Aproval, jobj.getJSONObject(i)
                                    .getString("WaitngForapproval").trim());
                        } else {
                            cv.put(ReceiptTable.KEY_Not_Aproval, "");
                        }


                        if (!jobj.getJSONObject(i).getString("BillPrefix").trim()
                                .equalsIgnoreCase("null")) {
                            cv.put(ReceiptTable.KEY_prfx, jobj.getJSONObject(i)
                                    .getString("BillPrefix").trim());


                            Log.w(" days : ", jobj.getJSONObject(i)
                                    .getString("BillPrefix").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_prfx, "");
                        }


                        if (!jobj.getJSONObject(i).getString("DelyDays").trim()
                                .equalsIgnoreCase("null")) {
                            cv.put(ReceiptTable.KEY_days, jobj.getJSONObject(i)
                                    .getString("DelyDays").trim());


                            Log.w(" days : ", jobj.getJSONObject(i)
                                    .getString("DelyDays").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_days, "");
                        }

                        if (!jobj.getJSONObject(i).getString("SOShort").trim()
                                .equalsIgnoreCase("null")) {
                            cv.put(ReceiptTable.KEY_SoName, jobj.getJSONObject(i)
                                    .getString("SOShort").trim());


                            Log.w(" SOShort : ", jobj.getJSONObject(i)
                                    .getString("SOShort").trim());

                        } else {
                            cv.put(ReceiptTable.KEY_SoName, "");
                        }

                        mod.insertdata(ReceiptTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        // get Receipt data
        jobj = new JSONArray();
        cv = new ContentValues();

        jobj = WebService.getDenomination(EmpNo, "GetCustType");
        try {
            if (jobj != null && jobj.length() > 0) {
                if (jobj.getJSONObject(0).getString("KeySerial").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("CusType").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTypeTable.KEY_CUSTTYPE, jobj.getJSONObject(i)
                                    .getString("CusType").trim());

                        } else {
                            cv.put(CustomerTypeTable.KEY_CUSTTYPE, "");
                        }
                        if (!jobj.getJSONObject(i).getString("KeySerial").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(CustomerTypeTable.KEY_ID, jobj.getJSONObject(i)
                                    .getString("KeySerial").trim());

                        } else {
                            cv.put(CustomerTypeTable.KEY_ID, "");
                        }

                        mod.insertdata(CustomerTypeTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

/*
        // get SalesType data
        jobj = new JSONArray();
        cv = new ContentValues();

        jobj = WebService.getData(EmpNo, BranchNo, "GetSalesType");
        try {
            if (jobj != null) {
                if (jobj.getJSONObject(0).getString("SalesTypeNo").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("SalesTypeNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(SalesTypeTable.KEY_ID, jobj.getJSONObject(i)
                                    .getString("SalesTypeNo").trim());

                        } else {
                            cv.put(SalesTypeTable.KEY_ID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("SalesType").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(SalesTypeTable.KEY_SALESTYPE, jobj.getJSONObject(i)
                                    .getString("SalesType").trim());

                        } else {
                            cv.put(SalesTypeTable.KEY_SALESTYPE, "");
                        }


                        mod.insertdata(SalesTypeTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/
        // get authority data

        jobj = new JSONArray();
        cv = new ContentValues();
        jobj = WebService.SyncAuthority(session.getUserId(), "Authority");

        try {
            if (jobj != null && jobj.length() > 0) {
                for (int i = 0; i < jobj.length(); i++) {

                    cv.put("form", jobj.getJSONObject(i).getString("DisplayName").trim());
                    cv.put("visible", jobj.getJSONObject(i).getString("Visible").trim());
                    cv.put("insertt", jobj.getJSONObject(i).getString("Insert").trim());
                    cv.put("print", jobj.getJSONObject(i).getString("Print").trim());

                    mod.insertdata("authority", cv);

                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // get Town data
        jobj = new JSONArray();
        cv = new ContentValues();

        jobj = WebService.getDistrict(BranchNo, "GetDistrict");
        try {
            if (jobj != null && jobj.length() > 0) {
                if (jobj.getJSONObject(0).getString("DistId").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("DistId").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DistrictTable.KEY_ID, jobj.getJSONObject(i)
                                    .getString("DistId").trim());

                        } else {
                            cv.put(DistrictTable.KEY_ID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("DistrictName").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DistrictTable.KEY_Dist, jobj.getJSONObject(i)
                                    .getString("DistrictName").trim());

                        } else {
                            cv.put(DistrictTable.KEY_Dist, "");
                        }
                        mod.insertdata(DistrictTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        jobj = WebService.getDeliveryData(EmpNo, "GetDelivery");
        try {
            if (jobj != null && jobj.length() > 0) {
                if (jobj.getJSONObject(0).getString("OrderNo").trim()
                        .equalsIgnoreCase("No")) {

                } else {
                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("OrderNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_ID, jobj.getJSONObject(i)
                                    .getString("OrderNo").trim());

                        } else {
                            cv.put(DeliveryTable.KEY_ID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("OrderDate")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_ORDERDATE, jobj
                                    .getJSONObject(i).getString("OrderDate")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_ORDERDATE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CustNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_CUSTID, jobj
                                    .getJSONObject(i).getString("CustNo")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_CUSTID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("CustomerName")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_CUSTNAME, jobj
                                    .getJSONObject(i).getString("CustomerName")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_CUSTNAME, "");
                        }

                        if (!jobj.getJSONObject(i).getString("ProdNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_PRODUCTID, jobj
                                    .getJSONObject(i).getString("ProdNo")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_PRODUCTID, "");
                        }

                        if (!jobj.getJSONObject(i).getString("ProductName")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_PRODUCTNAME, jobj
                                    .getJSONObject(i).getString("ProductName")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_PRODUCTNAME, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Quantity").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_QUANTITY, jobj
                                    .getJSONObject(i).getString("Quantity")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_QUANTITY, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BillNo").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_BIINO, jobj
                                    .getJSONObject(i).getString("BillNo")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_BIINO, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BillDate").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_BILLDATE, jobj
                                    .getJSONObject(i).getString("BillDate")
                                    .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_BILLDATE, "");
                        }


                        if (!jobj.getJSONObject(i).getString("Rate").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(DeliveryTable.KEY_PRICE,
                                    jobj.getJSONObject(i).getString("Rate")
                                            .trim());

                        } else {
                            cv.put(DeliveryTable.KEY_PRICE, "");
                        }

                        cv.put(DeliveryTable.KEY_STATUS, "0");

                        mod.insertdata(DeliveryTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
        // get Distance data
        jobj = new JSONArray();
        cv = new ContentValues();

        jobj = WebService.getData(EmpNo,BranchNo, "GetDistance");
        try {
            if (jobj != null) {
                if (jobj.getJSONObject(0).getString("Distance").trim()
                        .equalsIgnoreCase("No")) {

                } else {

                    String userid = session.getUserId();
                    String password = session.getUserPassword();
                    String empno = session.getEmpNo();
                    String empname = session.getEmpName();
                    String branchno = session.getBranchNo();
                    String address = session.getAddresse();
                    session.logoutUser();
                    session.createLoginSession(userid, password, empno, empname, branchno,
                            String.valueOf(jobj.getJSONObject(0).getInt("Distance") * 0.000621371),
                            String.valueOf(jobj.getJSONObject(0).getInt("MinDistance") * 0.000621371),address);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }

    public void syncProduct(Context context) {

        JSONArray jobj;
        mod = new Models();
        session = new SessionManager(context);

        mod.clearDatabase(ProductTable.DATABASE_TABLE);

        EmpNo = session.getEmpNo();
        BranchNo = session.getBranchNo();

        ContentValues cv = new ContentValues();

        // get Product data
        jobj = new JSONArray();
        cv = new ContentValues();

        jobj = WebService.getData(EmpNo, BranchNo, "GetProduct");

        try {
            if (jobj != null) {
                if (jobj.getJSONObject(0).getString("ProductNo").trim()
                        .equalsIgnoreCase("No")) {

                } else {

                    for (int i = 0; i < jobj.length(); i++) {

                        if (!jobj.getJSONObject(i).getString("ProductNo").trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_PRODUCTID, jobj
                                    .getJSONObject(i).getString("ProductNo")
                                    .trim());

                            String fName = jobj.getJSONObject(i)
                                    .getString("ProductNo").trim()
                                    + ".JPG";

                            String root = Environment
                                    .getExternalStorageDirectory().toString();


                            cv.put(ProductTable.KEY_IMAGE, root
                                    + "/ebilling_images/" + fName);

                        } else {
                            cv.put(ProductTable.KEY_PRODUCTID, "");
                            cv.put(ProductTable.KEY_IMAGE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("ProductName")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_NAME, jobj.getJSONObject(i)
                                    .getString("ProductName").trim());

                        } else {
                            cv.put(ProductTable.KEY_NAME, "");
                        }

                        if (!jobj.getJSONObject(i).getString("GroupName")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_GROUP, jobj
                                    .getJSONObject(i).getString("GroupName")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_GROUP, "");
                        }

                        if (!jobj.getJSONObject(i).getString("SubGroupName")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_SUBGROUP, jobj
                                    .getJSONObject(i).getString("SubGroupName")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_SUBGROUP, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BrandName")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_BRAND, jobj
                                    .getJSONObject(i).getString("BrandName")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_BRAND, "");
                        }


                        if (!jobj.getJSONObject(i).getString("Barcode").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_BARCODE, jobj
                                    .getJSONObject(i).getString("Barcode")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_BARCODE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("MRP").trim()
                                .equalsIgnoreCase("null") && jobj.getJSONObject(i).getString("MRP").trim()
                                .length() > 0) {

                            try {
                                double mrp = Double.parseDouble(jobj.getJSONObject(i)
                                        .getString("MRP").trim());

                                cv.put(ProductTable.KEY_MRP, mrp);

                            } catch (Exception e) {

                                Log.e(">>>> MRP >>>", i + " >>>>" + jobj.getJSONObject(i));
                                e.printStackTrace();
                            }

                        } else {
                            cv.put(ProductTable.KEY_MRP, "");
                        }

                        if (!jobj.getJSONObject(i).getString("DP").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_DP, jobj.getJSONObject(i)
                                    .getString("DP").trim());

                        } else {
                            cv.put(ProductTable.KEY_DP, "");
                        }


                        if (!jobj.getJSONObject(i).getString("prdROT").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_ROT, jobj.getJSONObject(i)
                                    .getString("prdROT").trim());

                        } else {
                            cv.put(ProductTable.KEY_ROT, "");
                        }

                        if (!jobj.getJSONObject(i).getString("prdPRate").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_PRATE, jobj.getJSONObject(i)
                                    .getString("prdPRate").trim());

                        } else {
                            cv.put(ProductTable.KEY_PRATE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("BrandKey").trim()
                                .equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_BRAND_SERIAL, jobj.getJSONObject(i)
                                    .getString("BrandKey").trim());

                        } else {
                            cv.put(ProductTable.KEY_PRATE, "");
                        }


                        if (!jobj.getJSONObject(i).getString("MasterCarton")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_MASTERCARTON, jobj
                                    .getJSONObject(i).getString("MasterCarton")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_MASTERCARTON, "");
                        }

                        if (!jobj.getJSONObject(i).getString("ImageFile")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_IMAGE, jobj
                                    .getJSONObject(i).getString("ImageFile")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_IMAGE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("Csz")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_CARTON_SIZE, jobj
                                    .getJSONObject(i).getString("Csz")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_CARTON_SIZE, "");
                        }

                        if (!jobj.getJSONObject(i).getString("UQC")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_UQC, jobj
                                    .getJSONObject(i).getString("UQC")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_UQC, "");
                        }
                        if (!jobj.getJSONObject(i).getString("CurStk")
                                .trim().equalsIgnoreCase("null")) {

                            cv.put(ProductTable.KEY_Stock, jobj
                                    .getJSONObject(i).getString("CurStk")
                                    .trim());

                        } else {
                            cv.put(ProductTable.KEY_Stock, "");
                        }
						/*if (!jobj.getJSONObject(i).getString("PhotoString")
                                .trim().equalsIgnoreCase("")) {

							// Log.w("Sync","PhotoString: ");

							String fName = jobj.getJSONObject(i)
									.getString("ProductNo").trim()
									+ ".png";

							String root = Environment
									.getExternalStorageDirectory().toString();


							cv.put(ProductTable.KEY_IMAGE, root
									+ "/ebilling_images/" + fName);

							*//*String encodedImage = jobj.getJSONObject(i)
									.getString("PhotoString").trim();
							// Log.v("TAG", encodedImage);
							byte[] decodedString = Base64.decode(encodedImage,
									Base64.DEFAULT);
							Bitmap decodedByte = BitmapFactory.decodeByteArray(
									decodedString, 0, decodedString.length);

							File filepath = Environment
									.getExternalStorageDirectory();

							// Create a new folder in SD Card
							File myDir = new File(filepath.getAbsolutePath()
									+ "/ebilling_images/");
							myDir.mkdirs();

							File file = new File(myDir, fName);
							if (file.exists()) {
								Log.w("Sync", "Exist " + fName);
								file.delete();
							}
							try {
								FileOutputStream out = new FileOutputStream(
										file);
								decodedByte.compress(Bitmap.CompressFormat.PNG,
										100, out);
								out.flush();
								out.close();

							} catch (Exception e) {
								e.printStackTrace();
							}*//*

						} else {
							cv.put(ProductTable.KEY_IMAGE, "");
						}*/

                        mod.insertdata(ProductTable.DATABASE_TABLE, cv);
                    }
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//   Download image code
/*
        Cursor ip = mod.getData("iptable");
        ip.moveToFirst();
        String ipadd = ip.getString(0).trim();
        String port = ip.getString(2).trim();
        String PreFix = "http://" + ipadd + ":" + port;
        ip.close();

        String sql = "select " + ProductTable.KEY_PRODUCTID + " from " + ProductTable.DATABASE_TABLE;
        Cursor curImages = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (curImages.getCount() > 0) {
            int count = 0;
            while (curImages.moveToNext()) {

                String url = PreFix + "/Photo/ProductImages/"+BranchNo+"/" + curImages.getString(0).trim() + ".JPG";
                //    new DownloadFile().execute(url, curImages.getString(0).trim() + ".JPG");
                dlodeImages(url, curImages.getString(0).trim() + ".JPG");
                count++;
            }
        }
        curImages.close();*/

    }

    public void downloadImages() {

        Cursor ip = mod.getData("iptable");
        ip.moveToFirst();
        String ipadd = ip.getString(0).trim();
        String port = ip.getString(2).trim();
        String PreFix = "http://" + ipadd + ":" + port;
        ip.close();

        String sql = "select " + ProductTable.KEY_PRODUCTID + " from " + ProductTable.DATABASE_TABLE;
        Cursor curImages = DataBaseAdapter.ourDatabase.rawQuery(sql, null);
        if (curImages.getCount() > 0) {
            int count = 0;
            while (curImages.moveToNext()) {

                String url = PreFix + "/Photo/ProductImages/" + BranchNo + "/" + curImages.getString(0).trim() + ".JPG";
                //    new DownloadFile().execute(url, curImages.getString(0).trim() + ".JPG");
                dlodeImages(url, curImages.getString(0).trim() + ".JPG");
                count++;
            }
        }
        curImages.close();
    }

    public void dlodeImages(String fileUrl, String fileName) {
        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/";
        File folder = new File(extStorageDirectory);
        folder.mkdir();


        File fileImg = new File(folder, fileName);
        if (fileImg.exists()) {
            Log.w("Sync", "Exist " + fileName);
            fileImg.delete();
        }

        try {
            fileImg.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileDownloader.downloadFile(fileUrl, fileImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0]; // ->
            // http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1]; // -> maven.pdf
            String extStorageDirectory = Environment
                    .getExternalStorageDirectory().toString() + "/ebilling_images/";
            File folder = new File(extStorageDirectory);
            folder.mkdir();


            File fileImg = new File(folder, fileName);
            if (fileImg.exists()) {
                Log.w("Sync", "Exist " + fileName);
                fileImg.delete();
            }

            try {
                fileImg.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, fileImg);
            return null;
        }
    }
}
