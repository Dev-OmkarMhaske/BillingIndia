package com.tsysinfo.billing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.tsysinfo.billing.database.DataBaseAdapter;
import com.tsysinfo.billing.database.Models;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class BarcodePrintActivity extends Activity {

    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    ProgressDialog waitDialog;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    String barcode_data="";
    DataBaseAdapter dba;
    Models mod;
    Bitmap bitmap = null;
    ImageView iv;
    Button button;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*

        LinearLayout l = new LinearLayout(this);
        l.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        l.setOrientation(LinearLayout.VERTICAL);
*/

        setContentView(R.layout.activity_barcode_print);
        button=(Button)findViewById(R.id.buttonBarcode);
        iv=(ImageView)findViewById(R.id.imageView);
        tv=(TextView)findViewById(R.id.textView);
        // barcode data
        Intent intent=getIntent();
         barcode_data = intent.getStringExtra("custid");
        dba= new DataBaseAdapter(BarcodePrintActivity.this);
        mod= new Models();
        dba.open();
        Cursor ip = mod.getData("iptable");
        ip.moveToFirst();
        String ipadd = ip.getString(0).trim();
        String port = ip.getString(2).trim();
        String PreFix = "http://" + ipadd + ":"+ port;
        ip.close();
        dba.close();
        String url = PreFix + "/Barcode/" + barcode_data + ".png";

        new DownloadFile().execute(url, barcode_data + ".png");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    printImage();
                }catch (FileNotFoundException e)
                {
                    Log.w("Exception",e);
                }

            }
        });


        tv.setText(barcode_data);


    }



    private void printImage() throws FileNotFoundException {


        String extStorageDirectory = Environment
                .getExternalStorageDirectory().getAbsolutePath().toString() + "/ebilling_images/Barcodes/"+barcode_data+".png";
        FileInputStream streamIn = new FileInputStream(extStorageDirectory);
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn);
        Bitmap bitmapPrint = bitmap;
        byte[] sendData = null;
        PrintPic pg = new PrintPic();
        pg.initCanvas(bitmapPrint.getWidth());
        pg.initPaint();
        pg.drawImage(0, 0, extStorageDirectory);
        sendData = pg.printDraw();
        mService.write(sendData);   //��ӡbyte�����
        mService.stop();

    }




    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
    void initilizeService()
    {
        mService = new BluetoothService(BarcodePrintActivity.this, mHandler);
        //�����������˳�����
        if (mService.isAvailable() == false) {
            Toast.makeText(BarcodePrintActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

        }
        if (!mService.isBTopen()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,
                    REQUEST_ENABLE_BT);
        } else {
            Intent connectIntent = new Intent(BarcodePrintActivity.this,
                    DeviceListActivity.class);
            startActivityForResult(connectIntent,
                    REQUEST_CONNECT_DEVICE);
        }


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null)
            mService.stop();
        mService = null;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:      //���������
                if (resultCode == Activity.RESULT_OK) {   //�����Ѿ���
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {                 //�û������������
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:     //��������ĳһ�����豸
                if (resultCode == Activity.RESULT_OK) {   //�ѵ�������б��е�ĳ���豸��
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);  //��ȡ�б������豸��mac��ַ
                    con_dev = mService.getDevByMac(address);

                    mService.connect(con_dev);
                }
                break;
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:   //������
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:  //��������
                            Log.d("��������", "��������.....");
                            break;
                        case BluetoothService.STATE_LISTEN:     //�������ӵĵ���
                        case BluetoothService.STATE_NONE:
                            Log.d("��������", "�ȴ�����.....");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:    //�����ѶϿ�����
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:     //�޷������豸
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };
    private void cropImage(){
        Paint paint = new Paint();

        String extStorageDirectory = Environment
                .getExternalStorageDirectory().toString() + "/ebilling_images/barcodes/" + barcode_data+".png";

        FileInputStream streamIn = null;
        try {
            streamIn = new FileInputStream(extStorageDirectory);

            Bitmap bitmapOrg = BitmapFactory.decodeStream(streamIn);

            final Canvas canvas = new Canvas();
            canvas.drawColor(Color.WHITE);
            // you need to insert a image flower_blue into res/drawable folder
            paint.setFilterBitmap(true);

            Log.w("PA","Width: "+bitmapOrg.getWidth());
            Log.w("PA","Height: "+bitmapOrg.getHeight());

            Bitmap croppedBmp = Bitmap.createBitmap(bitmapOrg, 0, 100, bitmapOrg.getWidth() , bitmapOrg.getHeight() - 100);
            FileOutputStream stream = new FileOutputStream(extStorageDirectory); //create your FileOutputStream here
            croppedBmp.compress(Bitmap.CompressFormat.PNG, 85, stream);
            croppedBmp.recycle();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   /* public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }*/

    public static Bitmap getResizedBitmap(Bitmap image, int newHeight, int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }


    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0]; // ->
            // http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1]; // -> maven.pdf
            String extStorageDirectory = Environment
                    .getExternalStorageDirectory().toString() + "/ebilling_images/";
            File folder = new File(extStorageDirectory, "Barcodes");
            if(!folder.exists())
            folder.mkdir();

            String[] allFiles;
            if (folder.isDirectory()) {
                allFiles = folder.list();
                for (int i = 0; i < allFiles.length; i++) {
                    File myFile = new File(folder, allFiles[i]);
                    myFile.delete();
                }
            }

            File pdfFile = new File(folder, fileName);
            try {
                pdfFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            waitDialog.cancel();




            waitDialog.cancel();

            mService = new BluetoothService(BarcodePrintActivity.this, mHandler);
            //�����������˳�����
            if (mService.isAvailable() == false) {
                Toast.makeText(BarcodePrintActivity.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();

            }
            if (!mService.isBTopen()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                Intent connectIntent = new Intent(BarcodePrintActivity.this,
                        DeviceListActivity.class);
                startActivityForResult(connectIntent,
                        REQUEST_CONNECT_DEVICE);
            }
            setData();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            waitDialog = new ProgressDialog(BarcodePrintActivity.this);
            waitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            waitDialog.setMessage("Please Wait");
            waitDialog.show();
        }
    }
void setData()
{


    String filename = "/ebilling_images/Barcodes/"+barcode_data+".png";
    File sd = Environment.getExternalStorageDirectory().getAbsoluteFile();
  /*  File dest = new File(sd, filename);
    if(dest.exists())
        dest.delete();*/


    Bitmap bmp = BitmapFactory.decodeFile(sd+filename);
    iv.setImageBitmap(bmp);



}


}


