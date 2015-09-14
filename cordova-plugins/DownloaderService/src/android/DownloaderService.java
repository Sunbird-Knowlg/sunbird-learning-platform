package org.apache.cordova;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import android.net.Uri;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.*;

public class DownloaderService extends CordovaPlugin {
    private static final String LOG_TAG = "Extractor";
    /**
     * Constructor.
     */
    public DownloaderService() {
    }
        /**
         * Sets the context of the Command. This can then be used to do things like
         * get file paths associated with the Activity.
         *
         * @param cordova The context of the main Activity.
         * @param webView The CordovaWebView Cordova is running in.
         */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(LOG_TAG, "Init DownloaderService");
    }
    public boolean execute(final String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.v(LOG_TAG, "DownloaderService received:" + action);
        if(action.equals("extract")) {
            extract(args, callbackContext);
        }
        return true;
    }

    private void extract(final CordovaArgs args, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    String zipFileName = args.getString(0);
                    String outputDirectory = args.getString(1);

                    Uri zipUri = getUriForArg(zipFileName);
                    Uri outputUri = getUriForArg(outputDirectory);

                    CordovaResourceApi resourceApi = webView.getResourceApi();
                    String mimeType = resourceApi.getMimeType(zipUri);
                    if("application/zip".equals(mimeType)) {
                        unzipSync(args, callbackContext);
                    } else {
                        File zipFile = resourceApi.mapUriToFile(zipUri);
                        File outputDir = resourceApi.mapUriToFile(outputUri);
                        if(zipFile == null) {
                            callbackContext.error("zip file not found at :" + zipFileName);
                        } else if(outputDir == null ) {
                            callbackContext.error("invalid outputdir :" + outputDirectory);
                        } else {
                            TarUtils.untarFile(zipFile, outputDir, true, callbackContext);
                        }
                    }
                } catch(Exception e) {
                    callbackContext.error("Error:" + e.getMessage());
                }
            }
        });
    }

    private void unzipSync(CordovaArgs args, CallbackContext callbackContext) {
        InputStream inputStream = null;
        try {
            String zipFileName = args.getString(0);
            String outputDirectory = args.getString(1);

            // Since Cordova 3.3.0 and release of File plugins, files are accessed via cdvfile://
            // Accept a path or a URI for the source zip.
            Uri zipUri = getUriForArg(zipFileName);
            Uri outputUri = getUriForArg(outputDirectory);

            CordovaResourceApi resourceApi = webView.getResourceApi();

            File tempFile = resourceApi.mapUriToFile(zipUri);
            if (tempFile == null || !tempFile.exists()) {
                String errorMessage = "Zip file does not exist";
                callbackContext.error(errorMessage);
                Log.e(LOG_TAG, errorMessage);
                return;
            }

            File outputDir = resourceApi.mapUriToFile(outputUri);
            outputDirectory = outputDir.getAbsolutePath();
            outputDirectory += outputDirectory.endsWith(File.separator) ? "" : File.separator;
            if (outputDir == null || (!outputDir.exists() && !outputDir.mkdirs())){
                String errorMessage = "Could not create output directory";
                callbackContext.error(errorMessage);
                Log.e(LOG_TAG, errorMessage);
                return;
            }

            OpenForReadResult zipFile = resourceApi.openForRead(zipUri);

            inputStream = new BufferedInputStream(zipFile.inputStream);
            inputStream.mark(10);
            int magic = readInt(inputStream);

            if (magic != 875721283) { // CRX identifier
                inputStream.reset();
            } else {
                // CRX files contain a header. This header consists of:
                //  * 4 bytes of magic number
                //  * 4 bytes of CRX format version,
                //  * 4 bytes of public key length
                //  * 4 bytes of signature length
                //  * the public key
                //  * the signature
                // and then the ordinary zip data follows. We skip over the header before creating the ZipInputStream.
                readInt(inputStream); // version == 2.
                int pubkeyLength = readInt(inputStream);
                int signatureLength = readInt(inputStream);

                inputStream.skip(pubkeyLength + signatureLength);
            }

            // The inputstream is now pointing at the start of the actual zip file content.
            ZipInputStream zis = new ZipInputStream(inputStream);
            inputStream = zis;

            ZipEntry ze;
            byte[] buffer = new byte[32 * 1024];
            boolean anyEntries = false;

            while ((ze = zis.getNextEntry()) != null)
            {
                anyEntries = true;
                String compressedName = ze.getName();

                if (ze.isDirectory()) {
                   File dir = new File(outputDirectory + compressedName);
                   dir.mkdirs();
                } else {
                    File file = new File(outputDirectory + compressedName);
                    file.getParentFile().mkdirs();
                    if(file.exists() || file.createNewFile()){
                        Log.w("Zip", "extracting: " + file.getPath());
                        FileOutputStream fout = new FileOutputStream(file);
                        int count;
                        while ((count = zis.read(buffer)) != -1)
                        {
                            fout.write(buffer, 0, count);
                        }
                        fout.close();
                    }

                }
                zis.closeEntry();
            }
            if (anyEntries)
                callbackContext.success();
            else
                callbackContext.error("Bad zip file");
        } catch (Exception e) {
            String errorMessage = "An error occurred while unzipping.";
            callbackContext.error(errorMessage);
            Log.e(LOG_TAG, errorMessage, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static int readInt(InputStream is) throws IOException {
        int a = is.read();
        int b = is.read();
        int c = is.read();
        int d = is.read();
        return a | b << 8 | c << 16 | d << 24;
    }

    private Uri getUriForArg(String arg) {
        CordovaResourceApi resourceApi = webView.getResourceApi();
        Uri tmpTarget = Uri.parse(arg);
        return resourceApi.remapUri(
                tmpTarget.getScheme() != null ? tmpTarget : Uri.fromFile(new File(arg)));
    }
}
