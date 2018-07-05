package me.victor.updater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, String> {
    private static final int NOTIFICATION_ID_DOWNLOADING = 0;
    private static final int NOTIFICATION_ID_DOWNLOADED = 1;
    private static final String CHANNEL_ID = "1";
    private static final String CHANNEL_NAME = "updater";

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private boolean isForceUpdate;
    private String authority;
    private String filePath;
    private int smallIcon;

    private PowerManager.WakeLock mWakeLock;
    private ProgressDialog mProgressDialog;
    private NotificationManager mNotificationManager;

    DownloadTask(Context context, boolean isForceUpdate, String authority, String filePath, int smallIcon) {
        this.context = context;
        this.isForceUpdate = isForceUpdate;
        this.authority = authority;
        this.filePath = filePath;
        this.smallIcon = smallIcon;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (isForceUpdate) {
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire(10 * 60 * 1000L);
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getString(R.string.updater_is_downloading));
            mProgressDialog.setMax(100);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DownloadTask.this.cancel(true);
                }
            });
            mProgressDialog.show();
        } else {
            mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (isForceUpdate) {
            mProgressDialog.setProgress(progress[0]);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setProgress(100,
                    progress[0], false)
                    .setSmallIcon(smallIcon)
                    .setContentTitle("正在下载")
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setChannelId(CHANNEL_ID);
            mNotificationManager.notify(NOTIFICATION_ID_DOWNLOADING, builder.build());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (isForceUpdate) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result == null) {
                AppUtils.installApk(context, authority, filePath);
            } else {
                Toast.makeText(context, context.getString(R.string.updater_download_error, result),
                        Toast.LENGTH_LONG).show();
            }
            AppUtils.exitApp();
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID_DOWNLOADING);
            if (result == null) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = FileProvider.getUriForFile(context, authority, new File(filePath));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(new File(filePath)),
                            "application/vnd.android.package-archive");
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIcon)
                        .setContentTitle("下载完成")
                        .setContentText("点击安装")
                        .setAutoCancel(true)
                        .setChannelId(CHANNEL_ID)
                        .setContentIntent(pendingIntent);
                mNotificationManager.notify(NOTIFICATION_ID_DOWNLOADED, builder.build());
            } else {
                Toast.makeText(context, context.getString(R.string.updater_download_error, result),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }
            int fileLength = connection.getContentLength();
            input = connection.getInputStream();
            output = new FileOutputStream(params[1]);
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
        } catch (IOException e) {
            return e.toString();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {

            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
