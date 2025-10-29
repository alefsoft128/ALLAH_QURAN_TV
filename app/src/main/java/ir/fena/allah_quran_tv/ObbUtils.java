package ir.fena.allah_quran_tv;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class ObbUtils {
    private static final String TAG = "ObbUtils";

    /**
     * Returns the app's OBB directory File.
     * Tries Context.getObbDir() first; if that fails, falls back to /Android/obb/<package>.
     * Creates the directory if it does not exist.
     *
     * @param context any valid Context (Activity/Service/Application)
     * @return File pointing to the OBB directory, or null if it couldn't be obtained/created.
     */
    public static File getObbDirectory(Context context) {
        if (context == null) {
            Log.e(TAG, "context is null");
            return null;
        }

        try {
            // Preferred, safest API
            File obb = context.getObbDir();
            if (obb != null && obb.exists() && obb.isDirectory()) {
                return obb.getAbsoluteFile();
            }
            // If obb is null or doesn't exist, try to create it
            if (obb != null) {
                if (obb.mkdirs() || obb.exists()) {
                    return obb.getAbsoluteFile();
                }
            }

            // Fallback: external storage path /Android/obb/<package>
            if (Environment.getExternalStorageState() != null &&
                    Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                File fallback = new File(Environment.getExternalStorageDirectory(),
                        "Android/obb/" + context.getPackageName());

                if (fallback.exists() && fallback.isDirectory()) {
                    return fallback.getAbsoluteFile();
                }
                if (fallback.mkdirs() || fallback.exists()) {
                    return fallback.getAbsoluteFile();
                }
            } else {
                Log.w(TAG, "External storage not mounted: " + Environment.getExternalStorageState());
            }
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException while accessing OBB dir", se);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error while obtaining OBB dir", e);
        }

        // If we reach here, we couldn't get/create the OBB directory
        Log.e(TAG, "Unable to obtain OBB directory for package: " + context.getPackageName());
        return null;
    }
}

/*
File obbDir = ObbUtils.getObbDirectory(getApplicationContext());
if (obbDir != null) {
    String path = obbDir.getAbsolutePath();
    Log.i("MyApp", "OBB dir: " + path);
} else {
    // خطای مناسب را هندل کنید
}

 */
