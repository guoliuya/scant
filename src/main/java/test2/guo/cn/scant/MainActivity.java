package test2.guo.cn.scant;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.zxing.decode.DecodeUtils;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Hashtable;

import cropper.RGBLuminanceSource;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;
    public static final int CROPIMAGES = 4;
    private String photo_path;
    private String crop_photo_path;
    private Bitmap scanBitmap;
    private Button tv_image;
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    onResultHandler((String)msg.obj);
                    break;
                case PARSE_BARCODE_FAIL:
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;

            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tv_image = (Button) findViewById(R.id.tv_image);
        tv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                } else {
                    intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_CODE:
                    try{
                        Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                        if (cursor.moveToFirst()) {
                            photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        }
                        cursor.close();
                        startCrop(photo_path);
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "未识别的二维码", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }else if(resultCode == 20){
            switch(requestCode) {
                case CROPIMAGES:
                    try {
                        if (data != null) {
                            crop_photo_path = data.getStringExtra("path");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Result result = scanningImage(crop_photo_path);
                                    if (result != null) {
                                        Message m = mHandler.obtainMessage();
                                        m.what = PARSE_BARCODE_SUC;
                                        m.obj = result.getText();
                                        mHandler.sendMessage(m);
                                    } else {
                                        String result2 = scanningImage2(crop_photo_path);
                                        if (result != null) {
                                            Message m = mHandler.obtainMessage();
                                            m.what = PARSE_BARCODE_SUC;
                                            m.obj = result2;
                                            mHandler.sendMessage(m);
                                        }else{
                                            Message m = mHandler.obtainMessage();
                                            m.what = PARSE_BARCODE_FAIL;
                                            m.obj = "未识别的二维码!";
                                            mHandler.sendMessage(m);
                                        }
                                    }
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "未识别的二维码", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
    private void onResultHandler(String resultString){
        try {
            if (TextUtils.isEmpty(resultString)) {
                Toast.makeText(MainActivity.this, "未识别的二维码", Toast.LENGTH_SHORT).show();
                return;
            }
		Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_SHORT).show();
			Uri uri =
					Uri.parse(resultString);
			Intent it = new
					Intent(Intent.ACTION_VIEW,
					uri);
			startActivity(it);
        }catch (Exception e){

        }
    }
    public void startCrop(String path) {
        Intent intent = new Intent();
        intent.putExtra("path", path);
        intent.putExtra("flag", false);
        intent.setClass(this, ImageCropActivity.class);
        startActivityForResult(intent, CROPIMAGES);
    }
    public Result scanningImage(String path) {
        if(TextUtils.isEmpty(path)){
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //????????????????

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // ????????
//		scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // ????????
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
//		int sampleSize=(int)(options.outHeight/(float)200);
//		options.inJustDecodeBounds=false;
//		if(sampleSize<=0){
//			options.inSampleSize=1;
//			scanBitmap=BitmapFactory.decodeFile(path, options);
//			Matrix matrix=new Matrix();
//			matrix.postScale(1.5f,1.5f);
//			scanBitmap=Bitmap.createBitmap(scanBitmap,0,0,scanBitmap.getWidth(),scanBitmap.getHeight(),matrix,true);
//		}else{
//			options.inSampleSize=sampleSize;
//			scanBitmap=BitmapFactory.decodeFile(path,options);
//		}
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String scanningImage2(String path) {
        if(TextUtils.isEmpty(path)){
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //		scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        String resultZbar = new DecodeUtils(DecodeUtils.DECODE_DATA_MODE_ALL)
                .decodeWithZbar(scanBitmap);
        return resultZbar;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
