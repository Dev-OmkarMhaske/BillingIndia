package com.tsysinfo.billing.database;

/**
 * Created by apple on 16/04/18.
 */

public class AllCustomer {




        public static final String DATABASE_TABLE = "all_customer";

        public static final String KEY_ID = "id";//0
        public static final String KEY_NAME = "name";//1
        public static final String KEY_PLACE = "place";//2
        public static final String KEY_ADDRESS = "address";//3
        public static final String KEY_REGNO = "tin";//4
        public static final String KEY_TIN = "pin";//5
        public static final String KEY_PIN = "regno";//6
        public static final String KEY_CSTNO = "cstno";//7
        public static final String KEY_AREA = "area";//8
        public static final String KEY_TYPE = "type";//9
        public static final String KEY_RATECODE = "ratecode";//10
        public static final String KEY_MOBILE = "mobile";//11
        public static final String KEY_LANDLINE = "landline";//12
        public static final String KEY_LONGI = "longi";//13
        public static final String KEY_LATI = "lati";//14
        public static final String KEY_CUSTOMER_CODE = "customercode";//15
        public static final String KEY_District = "district";//16
        public static final String KEY_Email = "email";//17
        public static final String KEY_SHORTNAME = "shortname";//18
        public static final String KEY_STATUS = "ststus";//19

        static final String CREATE_TABLE = " CREATE TABLE " + DATABASE_TABLE + "("
                + KEY_ID + " TEXT," + KEY_NAME + " TEXT," + KEY_PLACE + " TEXT,"
                + KEY_ADDRESS + " TEXT," + KEY_REGNO + " TEXT," + KEY_TIN
                + " TEXT," + KEY_PIN + " TEXT," + KEY_CSTNO + " TEXT," + KEY_AREA
                + " TEXT," + KEY_TYPE + " TEXT," + KEY_RATECODE + " TEXT,"
                + KEY_MOBILE + " TEXT," + KEY_LANDLINE + " TEXT," + KEY_LONGI
                + " TEXT," + KEY_LATI + " TEXT," + KEY_CUSTOMER_CODE + " TEXT," + KEY_District + " TEXT," + KEY_Email +" TEXT," + KEY_SHORTNAME +" TEXT," + KEY_STATUS +" TEXT);";

    }


