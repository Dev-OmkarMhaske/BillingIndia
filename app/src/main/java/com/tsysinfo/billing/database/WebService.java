package com.tsysinfo.billing.database;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.tsysinfo.billing.LogError;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class WebService {
    // Namespace of the Webservice - can be found in WSDL
    public static String responseString = null;
    private static String NAMESPACE = "http://tempuri.org/";
    private static int TimeOut = 2000000;
    public static int timeoutFlag = 0;
    // Webservice URL - WSDL File location
    private static String URL = "/BillingWebService.asmx";// Make

    // SOAP Action URI again Namespace + Web method name
    private static String SOAP_ACTION = "http://tempuri.org/";

    /*
        private static String URL = "/Service.svc?wsdl";

        // SOAP Action URI again Namespace + Web method name
        private static String SOAP_ACTION = "http://tempuri.org/IService/";
    */
    static SoapSerializationEnvelope envelope;
    static JSONArray jobj = null;
    HttpTransportSE androidHttpTransport;
    static Models mod;

    // Registrationf

    public static JSONArray putData(String Data, String BranchNo,
                                    String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "BranchNo : " + BranchNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("Data", Data);
        request.addProperty("BranchNo", BranchNo);


        Log.e("UploadCostmer", "" + request.toString());
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getDenomination(String Data,
                                            String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);


        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("BranchNo", Data);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray makeTransactionReciept(String MData, String Data1, String BranchNo, String Longitude, String Latitude,
                                                   String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data1);
        Log.w("Web Service", "MData : " + MData);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "Longitude : " + Longitude);
        Log.w("Web Service", "Latitude : " + Latitude);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("Data", Data1);
        request.addProperty("MData", MData);

        Log.e("Recep", "" + request.toString());
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    //makeTransaction

    public static JSONArray makeTransaction(String Data, String BranchNo, String Longitude, String Latitude,
                                            String webMethName, String TransType, String Mode) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "Longitude : " + Longitude);
        Log.w("Web Service", "Latitude : " + Latitude);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("Data", Data);
        request.addProperty("BranchNo", BranchNo);
        request.addProperty("Longitude", Longitude);
        request.addProperty("Latitude", Latitude);
        request.addProperty("TransType", TransType);
        request.addProperty("Mode", Mode);
        // Create envelope

        Log.e("MakeTran", "" + request.toString());
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray Saveorder(String Data, String BranchNo, String Longitude, String Latitude, String transType, String mode,
                                      String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "Longitude : " + Longitude);
        Log.w("Web Service", "Latitude : " + Latitude);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("Data", Data);
        request.addProperty("BranchNo", BranchNo);
        request.addProperty("Longitude", Longitude);
        request.addProperty("Latitude", Latitude);
        request.addProperty("TransType", transType);
        request.addProperty("Mode", mode);
        // Create envelope
        Log.e("saveOrder", "" + request.toString());
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }


    public static JSONArray getSubgroup(String Data, String BranchNo,
                                        String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "BranchNo : " + BranchNo);


        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("GroupName", Data);
        request.addProperty("BranchNo", BranchNo);

        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getOrders(String BranchNo, String emp,
                                      String webMethName) {


        Log.w("Web Service", "BranchNo : " + BranchNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("BranchNo", BranchNo);
        request.addProperty("EmpNo", emp);

        Log.e("getOrder", "" + request.toString());
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getOrderDetails(String BranchNo, String ordNo,
                                            String webMethName) {


        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "OrderNo : " + ordNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("BranchNo", BranchNo);
        request.addProperty("OrderNo", ordNo);
        // Create envelope

        Log.e("OrderDetail", "" + request.toString());
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getDashBoardDetail(String empno,
                                               String webMethName) {


        Log.w("Web Service", "empno : " + empno);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("EmpNo", empno);

        // Create envelope

        Log.e("DashboardDetail", "" + request.toString());
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getChequeDetails(String EmpKey,
                                             String webMethName) {


        Log.w("Web Service", "EmpKey : " + EmpKey);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("EmpKey", EmpKey);

        // Create envelope

        Log.e("getChequeDetails", "" + request.toString());
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getRecNo(String BranchNo, String empno, String webMethName) {


        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "OrderNo : " + empno);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("BranchNo", BranchNo);
        request.addProperty("EmpNo", empno);

        Log.e("OutSatnding", "" + request);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }
    // Login

    public static JSONArray Login(String userName, String passWord,
                                  String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "User Name : " + userName);
        Log.w("Web Service", "Password : " + passWord);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters

        request.addProperty("UserId", userName);
        request.addProperty("Password", passWord);


		/*
		PropertyInfo unamePI = new PropertyInfo();
		PropertyInfo passPI = new PropertyInfo();
		// Set Username
		unamePI.setName("UserId");
		// Set Value
		unamePI.setValue(userName);
		// Set dataType
		unamePI.setType(String.class);
		// Add the property to request object
		request.addProperty(unamePI);
		// Set Password
		passPI.setName("Password");
		// Set dataType
		passPI.setValue(passWord);
		// Set dataType

		passPI.setType(String.class);
		// Add the property to request object
		request.addProperty(passPI);*/
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getOutstanding(String EmpNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);


        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpKey", EmpNo);

        Log.e("getout", "hh" + request.toString());

        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    // getData
    public static JSONArray getDeliveryData(String EmpNo,
                                            String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);


        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("SfKey", EmpNo);

        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getData(String EmpNo, String BranchNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);
        Log.w("Web Service", "BranchNo : " + BranchNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpNo", EmpNo);
        request.addProperty("BranchNo", BranchNo);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getExpense(String BranchNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "BranchNo : " + BranchNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("BranchNo", BranchNo);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray GetExpenses(String BranchNo, String FromDate, String ToDate, String EmpNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "FromDate : " + FromDate);
        Log.w("Web Service", "ToDate : " + ToDate);
        Log.w("Web Service", "EmoNo : " + EmpNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("BranchNo", BranchNo);
        request.addProperty("FromDate", FromDate);
        request.addProperty("ToDate", ToDate);
        request.addProperty("EmpNo", EmpNo);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }


    public static JSONArray GetExpenseSubGroup(String BranchNo, String ExpenseType, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "ExpenseType : " + ExpenseType);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("BranchNo", BranchNo);
        request.addProperty("ExpenseType", ExpenseType);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray SaveExpense(String EmpNo, String Date, String ExpenseType, String FromTown, String ToTown, String ExpAmount,
                                        String Remarks, String Logitude, String Latitude, String UserName, String SubGroup, String webMethName) {


        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Date : " + Date);
        Log.w("Web Service", "ExpenseType : " + ExpenseType);
        Log.w("Web Service", "FromTown : " + FromTown);
        Log.w("Web Service", "ToTown : " + ToTown);
        Log.w("Web Service", "ExpAmount : " + ExpAmount);
        Log.w("Web Service", "Remarks : " + Remarks);
        Log.w("Web Service", "UserName : " + UserName);
        Log.w("Web Service", "SubGroup : " + SubGroup);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpNo", EmpNo);
        request.addProperty("Date", Date);
        request.addProperty("ExpenseType", ExpenseType);
        request.addProperty("FromTown", FromTown);
        request.addProperty("ToTown", ToTown);
        request.addProperty("ExpAmount", ExpAmount);
        request.addProperty("Remarks", Remarks);
        request.addProperty("Longitude", Logitude);
        request.addProperty("Latitude", Latitude);
        request.addProperty("UserName", UserName);
        request.addProperty("SubGroup", SubGroup);

        // Create envelope
        Log.e("ExpenseDetail", "" + request.toString());
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getAllCust(String EmpNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);


        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpNo", EmpNo);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray GeoUpdate(String EmpNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpKey : " + EmpNo);


        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpKey", EmpNo);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray getDistrict(String BranchNo, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);

        Log.w("Web Service", "BranchNo : " + BranchNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);


        request.addProperty("SfKey", BranchNo);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray barcodePrint(String custID, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "CustId: " + custID);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters

        request.addProperty("CustNo", custID);


        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;
    }
    // Change Password

    public static JSONArray ChangePassword(String MemberNo, String OldPassword,
                                           String NewPassword, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "MemberNo : " + MemberNo);
        Log.w("Web Service", "OldPassword : " + OldPassword);
        Log.w("Web Service", "NewPassword : " + NewPassword);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters
        PropertyInfo MemberNoPI = new PropertyInfo();
        PropertyInfo OldPasswordPI = new PropertyInfo();
        PropertyInfo NewPasswordPI = new PropertyInfo();
        // Set CustomerNo
        MemberNoPI.setName("MemberNo");
        // Set Value
        MemberNoPI.setValue(MemberNo);
        // Set dataType
        MemberNoPI.setType(String.class);
        // Add the property to request object
        request.addProperty(MemberNoPI);
        // Set OldPassword
        OldPasswordPI.setName("OldPassword");
        // Set Value
        OldPasswordPI.setValue(OldPassword);
        // Set dataType
        OldPasswordPI.setType(String.class);
        // Add the property to request object
        request.addProperty(OldPasswordPI);
        // Set NewPassword
        NewPasswordPI.setName("NewPassword");
        // Set Value
        NewPasswordPI.setValue(NewPassword);
        // Set dataType
        NewPasswordPI.setType(String.class);
        // Add the property to request object
        request.addProperty(NewPasswordPI);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    // Forgot Password Request

    public static JSONArray ForgotPasswordRequest(String UserId, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "UserId : " + UserId);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters
        PropertyInfo MobileNoPI = new PropertyInfo();
        // Set MobileNo
        MobileNoPI.setName("UserId");
        // Set Value
        MobileNoPI.setValue(UserId);
        // Set dataType
        MobileNoPI.setType(String.class);
        // Add the property to request object
        request.addProperty(MobileNoPI);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    // Forgot Password Response

    public static JSONArray ForgotPasswordResponse(String MobileNo,
                                                   String Answer, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "MobileNo : " + MobileNo);
        Log.w("Web Service", "Answer : " + Answer);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters
        PropertyInfo MobileNoPI = new PropertyInfo();
        PropertyInfo AnswerPI = new PropertyInfo();
        // Set MobileNo
        MobileNoPI.setName("MobileNo");
        // Set Value
        MobileNoPI.setValue(MobileNo);
        // Set dataType
        MobileNoPI.setType(String.class);
        // Add the property to request object
        request.addProperty(MobileNoPI);
        // Set MobileNo
        AnswerPI.setName("Answer");
        // Set Value
        AnswerPI.setValue(Answer);
        // Set dataType
        AnswerPI.setType(String.class);
        // Add the property to request object
        request.addProperty(AnswerPI);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    // sendLocation

    public static JSONArray sendLocation(String EmpNo, String BranchNo, String Data, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "Data : " + Data);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpNo", EmpNo);
        request.addProperty("BranchNo", BranchNo);
        request.addProperty("Data", Data);

        Log.e("Tracking2", "Data " + request.toString());
        Log.e("Tracking3", "Data " + Data.toString());


        LogError lErr2 = new LogError();
        lErr2.appendLog("Tracking2 : " + request.toString());

        LogError lErr3 = new LogError();
        lErr3.appendLog("Tracking3 : " + Data.toString());
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    // CustomerRegistration

    public static JSONArray customerRegistration(String BranchNo, String Data, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "BranchNo : " + BranchNo);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("Data", Data);
        request.addProperty("BranchNo", BranchNo);


        Log.w("Web CustomerVisite ", "Data " + request.toString());
        Log.w("Web CusterData", "Data " + Data.toString());

        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    public static JSONArray customerUpdate(String BranchNo, String custNo, String Data, String lat, String lan, String Emp, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "customer no : " + custNo);

        Log.w("Web Service", "BranchNo : " + BranchNo);

        Log.w("Web Service", "Latitude: " + lat);

        Log.w("Web Service", "Longutude : " + lan);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("Data", Data);
        request.addProperty("CustomerNo", custNo);
        request.addProperty("BranchNo", BranchNo);
        request.addProperty("Lat", lat);
        request.addProperty("Lan", lan);
        request.addProperty("EmpNo", Emp);

        Log.e("customerup", "" + request.toString());
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }


    // sendDistance

    public static JSONArray sendDistance(String EmpNo, String BranchNo, String Distance, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "Distance : " + Distance);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpNo", EmpNo);
        request.addProperty("BranchNo", BranchNo);
        request.addProperty("Distance", Distance);
        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }


    // sendDistance

    public static JSONArray sendFeedback(String EmpNo, String BranchNo, String Data, String path, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "EmpNo : " + EmpNo);
        Log.w("Web Service", "BranchNo : " + BranchNo);
        Log.w("Web Service", "Data : " + Data);
        Log.w("Web Service", "Path : " + path);

        String encodedString = "";

        if (path.trim().length() > 0) {

            Bitmap image = BitmapFactory.decodeFile(path);
            image = Bitmap.createScaledBitmap(image, 300, 390, false);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byteArray = stream.toByteArray();
            encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);

            //		Log.w(".............", "base64 : " + encodedString);

        } else {
            encodedString = "";
        }


        Log.w(".............", encodedString);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("EmpNo", EmpNo);
        request.addProperty("BranchNo", BranchNo);
        request.addProperty("Data", Data);
        request.addProperty("BArray", encodedString);

        Log.e("Feedback", "" + request.toString());

        // Create envelope
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    // Methods For SyncAuthority

    public static JSONArray SyncAuthority(String UserId, String webMethName) {

        Log.w("Web Service", "Form Name : " + webMethName);
        Log.w("Web Service", "UserId : " + UserId);

        // Create request
        SoapObject request = new SoapObject(NAMESPACE, webMethName);
        // Property which holds input parameters
        PropertyInfo bnoPI = new PropertyInfo();
        // Set Username
        bnoPI.setName("UserId");
        // Set Value
        bnoPI.setValue(UserId);
        // Set dataType
        bnoPI.setType(String.class);
        // Add the property to request object
        request.addProperty(bnoPI);

        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    @SuppressWarnings("deprecation")
    public static JSONArray serverConnection(String webMethName) {
        JSONArray jsonObj = null;
        mod = new Models();
        try {

            Cursor ip = mod.getData("iptable");
            ip.moveToFirst();
            String ipadd = ip.getString(0).trim();
            String port = ip.getString(1).trim();
            String PreFix;
            if (port.equalsIgnoreCase("")) {
                PreFix = "http://" + ipadd;
            } else {
                PreFix = "http://" + ipadd + ":" + port;
            }
            String url = PreFix + URL;

            Log.e("URL1", "MAIN" + url);

            // String url = "http://ebilling.tsysinfo.in/BillingWebService.asmx";
            ip.close();

            timeoutFlag = 0;
            Log.w("Web Service ", "HTTP TRANSPORT : ");
            HttpTransportSE androidHttpTransport = new HttpTransportSE(url, TimeOut);
            Log.w("Web Service ", "URL : " + url);
            Log.w("Web Service ", "Method : " + webMethName);

            // Thread.sleep(20000);
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION + webMethName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            // Assign it to boolean variable variable
            Log.w("Web Service ", "Data " + response.toString());
            // loginStatus = Boolean.parseBoolean(response.toString());
            responseString = response.toString();

            jsonObj = new JSONArray(response.toString());

        } catch (ConnectTimeoutException e) {
            // showAlertDialog();
            timeoutFlag = 1;
            Log.w("Web Service", "ConnectTimeoutException");
        } catch (ConnectException e) {
            // showAlertDialog();
            timeoutFlag = 1;
            Log.w("Web Service", "ConnectException");
        } catch (EOFException e) {
            // showAlertDialog();
            Log.w("Web Service", "EOFException");
        } catch (ClassCastException e) {
            e.printStackTrace();
            // showAlertDialog();
            Log.w("Web Service", "ClassCastException");
        } catch (SocketTimeoutException e) {
            timeoutFlag = 1;
            // showAlertDialog();
            Log.w("Web Service", "Timed Out");
        } catch (Exception e) {
            // Assign Error Status true in static variable 'errored'

            // HomeActivity.errored = true;
            e.printStackTrace();
        }
        // Return booleam to calling object
        return jsonObj;
    }


}
