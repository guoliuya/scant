package test2.guo.cn.scant;

import android.app.Activity;
import android.os.Bundle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;





import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;


import cropper.BitmapUtil;
import cropper.CropImageView;
import cropper.FileDownloadUtil;

public class ImageCropActivity extends Activity {
	private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;

	private CropImageView cropImageView;
	private String pre_path;
	private boolean pre_flag;
	private int screen_width, screen_height;
	private TextView tv_ok, tv_canle;
	private int x;
	private int y;
	private TextView tv_crop;

	private RelativeLayout rl_layout;
	
@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_image_crop);
	tv_ok = (TextView) findViewById(R.id.tv_ok);
	tv_canle = (TextView) findViewById(R.id.tv_canle);

	tv_ok.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			gotoNextStep();
		}
	});

	tv_canle.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {

			finish();

		}
	});
	rl_layout = (RelativeLayout) findViewById(R.id.rl_image);
	tv_crop = (TextView) findViewById(R.id.tv_crop);
	tv_crop.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			int switchsize = switchsize();
			switch (switchsize) {
			case 0:
				x = y = 1;
				break;
			case 1:
				x = 4;
				y = 3;
				break;
			case 2:
				x = 3;
				y = 4;

				break;
			case 3:
				x = 674;
				y = 250;
				break;
			default:
				break;
			}
			cropImageView.setAspectRatio(x, y);
			tv_crop.setText(x + ":" + y);
		}
	});

	DisplayMetrics dm = new DisplayMetrics();
	getWindowManager().getDefaultDisplay().getMetrics(dm);
	screen_width = dm.widthPixels;
	screen_height = dm.heightPixels;
	cropImageView = (CropImageView) findViewById(R.id.CropImageView);

	Intent preIntent = this.getIntent();
	pre_path = preIntent.getStringExtra("path");
	pre_flag = preIntent.getBooleanExtra("flag", true);

	Bitmap bitmap = null;
	
		if (pre_path != null) {
			bitmap = BitmapUtil.getBitmapFromSDCard(pre_path);
		} else {
			bitmap = BitmapUtil.temp;
		}

		

	
	if (bitmap != null) {
		bitmap = resizeSurfaceWithScreen(bitmap, screen_width, screen_height);
		int degree = readPictureDegree(pre_path);
		bitmap = rotaingImageView(degree, bitmap);
		cropImageView.setImageBitmap(bitmap);
	}
	if (pre_flag) {
		cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

	} else {
		cropImageView.setAspectRatio(1, 1);
		rl_layout.setVisibility(View.INVISIBLE);
	}
	
}
private int size_index = 0;

public int switchsize() {
	size_index++;
	if (size_index >= 3) {
		size_index = 0;
	}
	return size_index;
}

private Bitmap resizeSurfaceWithScreen(Bitmap bitmap, int screen_width, int screen_height) {

	int width = bitmap.getWidth();
	int height = bitmap.getHeight();

	if (width < screen_width && height < screen_height) {
		float scale_width = screen_width * 1.0f / width;
		float scale_height = screen_height * 1.0f / height;
		float scale = scale_width > scale_height ? scale_height : scale_width; // ???�?�????
		width *= scale;
		height *= scale;
	} else {
		if (width > screen_width) {
			height = height * screen_width / width;
			width = screen_width;
		}
		if (height > screen_height) {
			width = width * screen_height / height;
			height = screen_height;
		}
	}

	bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

	bitmap = BitmapUtil.zoomBitmap(bitmap, width, height);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
	while (baos.toByteArray().length > 1024 * 1024) {
		baos.reset();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
	}

	ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
	Bitmap bitmap_new = BitmapFactory.decodeStream(isBm, null, null);

	return bitmap_new;

}

public String saveBitmap(Bitmap bm) {
	Long tolong = System.currentTimeMillis() / 1000;
	File f = new File(FileDownloadUtil.getDefaultLocalDir("/Scan/temp/"), tolong.toString());
	if (f.exists()) {
		f.delete();
	}
	try {
		FileOutputStream out = new FileOutputStream(f);
		bm.compress(Bitmap.CompressFormat.JPEG, 80, out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (baos.toByteArray().length > 1024 * 1024) {
			baos.reset();
			bm.compress(Bitmap.CompressFormat.JPEG, 50, out);
		}
		out.flush();
		out.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return f.getAbsolutePath();
}

public void gotoNextStep() {

	try {
		Bitmap croppedImage = cropImageView.getCroppedImage();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		croppedImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
		byte[] bitmapByte = baos.toByteArray();

		int width = croppedImage.getWidth();
		int heigth = croppedImage.getHeight();

		Intent data = new Intent();
		data.putExtra("bitmap", bitmapByte);
		data.putExtra("path", saveBitmap(croppedImage));
		data.putExtra("width", width);
		data.putExtra("heigth", heigth);
		setResult(20, data);
		finish();
	} catch (Exception e) {
		e.printStackTrace();
	}

}

/**
 *
 * @param angle
 * @param bitmap
 * @return Bitmap
 */
public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
	Matrix matrix = new Matrix();
	;
	matrix.postRotate(angle);
	System.out.println("angle2=" + angle);
	Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	return resizedBitmap;
}

public int readPictureDegree(String path) {
	int degree = 0;
	try {
		ExifInterface exifInterface = new ExifInterface(path);
		int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			degree = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			degree = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			degree = 270;
			break;
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
	return degree;
}
}
