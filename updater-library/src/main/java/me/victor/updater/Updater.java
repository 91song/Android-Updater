package me.victor.updater;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

public class Updater {
    private long mEnqueueId;
    private String mAuthority;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long downloadCompletedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if (mEnqueueId != downloadCompletedId) {
                return;
            }
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mEnqueueId);
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = manager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        int fileUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        String fileUri = cursor.getString(fileUriIdx);
                        AppUtils.installApk(context, mAuthority, Uri.parse(fileUri).getPath());

                    } else {
                        int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        String fileName = cursor.getString(fileNameIdx);
                        AppUtils.installApk(context, mAuthority, fileName);
                    }
                    context.unregisterReceiver(mReceiver);
                }
            }
            cursor.close();
        }
    };

    private Updater() {

    }

    public static Updater getInstance() {
        return Singleton.INSTANCE;
    }

    public void checkUpdate(Context context, UpdaterParams params) {
        showUpdateDialog(context, params.getUpdateTitle(), params.getUpdateMsg(),
                params.getDownloadUrl(), params.isForceUpdate(), params.getAuthority());
    }

    private void showUpdateDialog(final Context context, String updateTitle, String updateMsg,
                                  final String downloadUrl, boolean isForceUpdate, final String authority) {
        if (isForceUpdate) {
            new AlertDialog.Builder(context).setTitle(updateTitle)
                    .setMessage(updateMsg)
                    .setPositiveButton(context.getString(R.string.updater_now_update),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startForceUpdate(context, downloadUrl, authority);
                                }
                            })
                    .setCancelable(false)
                    .show();
        } else {
            new AlertDialog.Builder(context).setTitle(updateTitle)
                    .setMessage(updateMsg)
                    .setNegativeButton(context.getString(R.string.updater_cancel_update), null)
                    .setPositiveButton(context.getString(R.string.updater_now_update),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startUpdate(context, downloadUrl, authority);
                                }
                            })
                    .show();
        }
    }

    private void startUpdate(Context context, String downloadUrl, String authority) {
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle(context.getString(R.string.updater_is_downloading));
        request.setDescription(context.getResources().getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,
                String.format("updater_%s.apk", System.currentTimeMillis()));
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        mEnqueueId = manager.enqueue(request);
        mAuthority = authority;
        context.registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void startForceUpdate(Context context, String downloadUrl, String authority) {
        DownloadTask downloadTask = new DownloadTask(context, authority);
        downloadTask.execute(downloadUrl);
    }

    private static class Singleton {
        private static final Updater INSTANCE = new Updater();
    }
}
