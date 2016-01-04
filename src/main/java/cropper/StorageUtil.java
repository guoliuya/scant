package cropper;

public class StorageUtil {
	/**
	 * �ж��ⲿ�洢�Ƿ����
	 * 
	 * @return
	 */
	public static boolean isExternalStorageAvailable() {
		return android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
	}
}
