package cropper;

import java.io.File;

import android.os.Environment;
import android.util.Log;


public class FileDownloadUtil {
	/**
	 * ��ȡϵͳĬ��SD�����ļ�����Ŀ¼,û�о��½�
	 * 
	 * @return
	 */
	public static String getDefaultLocalDir(String subDir) {

		String path_root = getSDcardRoot();
		// String path_root = VideoApplication.sdCardRoot;
		if (path_root == null) {
			return null;
		}

		String path_dir = path_root + subDir;

		return makeDir(path_dir);
	}
	public static String getSDcardRoot() {

		if (!StorageUtil.isExternalStorageAvailable()) {
			Log.i("test", "sd��������");
			return null;
		}
		// ���sd����Ŀ¼
		File root = Environment.getExternalStorageDirectory();
		String path_root = root.getAbsolutePath();

		return path_root;
	}
	public static String makeDir(String path_dir) {
		// �½�Ŀ¼
		File dir = new File(path_dir);
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
			if (!success) {
				Log.e("test", "����Ŀ¼ʧ��:" + path_dir);
				return null;
			} else {
				Log.i("test", "�����ɹ�:" + path_dir);
			}
		}

		return path_dir;
	}
}
