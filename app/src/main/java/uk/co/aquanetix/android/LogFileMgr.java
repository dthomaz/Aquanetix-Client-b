package uk.co.aquanetix.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import uk.co.aquanetix.R;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import android.content.Context;
import android.os.Environment;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer.TransferState;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;

/**
 * Responsible for all IO to aquanetix log files.
 */
class LogFileMgr {
    
    private static final String FILENAME = "aquanetix.log";
    private static final String TAB = "\t";
    private static final Object AppendLock = new Object();
    private static final Object AmazonLock = new Object();
    private static final FileFilter ZIP_FILES = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".log.zip");
        }
    };
    
    void appendMessageAtLogFile(String level, String msg, Throwable e) {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        synchronized (AppendLock) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "aquanetix");
                dir.mkdirs();
                File f = new File(dir, FILENAME);
                PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f, true)));
                out.print(now);
                out.print(TAB);
                out.print(level);
                out.print(TAB);
                out.print(msg);
                if (e!=null) {
                    e.printStackTrace(out);
                } else if ( !msg.endsWith("\n") ) { // Add EOL if needed
                    out.println();
                }
                out.close();
            } catch (Exception ex) {
                //Not much we can do
            }
        }
    }
    
    // If loglevel is high enough, zip and commit to amazon all log files. Otherwise delete them. 
    void sync(Context ctx) {
        AquanetixSharedPreferences p = new AquanetixSharedPreferences(ctx);
        if (!p.shouldSyncLogs()) {
            return; // Not yet
        }
        File aquaDir = new File(Environment.getExternalStorageDirectory(), "aquanetix");
        if ( p.getLogLevel()==0 ) {
            deleteAllLogFiles(aquaDir);
        }
    }
    
    private void zipCurrentLogFile(Context ctx, File aquaDir) throws IOException {
        synchronized (AppendLock) {
            // Check that there is log to zip
            File logFile = new File(aquaDir, FILENAME);
            if (!logFile.exists()) {
                return;
            }
            // Create "unique" zip name
            String now = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String zipName = now + "_" + "_" + FILENAME + ".zip";
            // Zip it
            File zipFile;
            zipFile = new File(aquaDir, zipName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            try {
                byte data[] = new byte[2048];
                BufferedInputStream origin = new BufferedInputStream(new FileInputStream(logFile));
                try {
                    ZipEntry entry = new ZipEntry(logFile.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ( (count = origin.read(data)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
                // Delete it
                logFile.delete();
            } finally {
                out.close();
            }
        }
    }
    
    private void sendAllZipFilesToS3(Context ctx, String uuid, File aquaDir) throws InterruptedException {
        synchronized (AmazonLock) {
            for (File zipFile:aquaDir.listFiles(ZIP_FILES)) {
                // AWS Credentials
                String accessKey = ctx.getString(R.string.aws_accessKey);
                String secretKey = ctx.getString(R.string.aws_secretKey);
                AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
                // Send them to AWS
                TransferManager m = new TransferManager(awsCredentials);
                String name = "android_logs/" + uuid + "/" + zipFile.getName();
                Upload u = m.upload("aquanetix.co.uk", name, zipFile);
                u.waitForUploadResult();
                if (u.getState()==TransferState.Completed) {
                    zipFile.delete();
                } else {
                    throw new RuntimeException("Cannot send zip file");
                }
            }
        }
    }
    
    private void deleteAllLogFiles(File aquaDir) {
        if (aquaDir.exists()) {
            for (File f:aquaDir.listFiles()) {
                f.delete();
            }
        }
    }
    
}
