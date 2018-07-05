package me.victor.updater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import java.io.File;

public class Updater {
    private Updater() {

    }

    public static Updater getInstance() {
        return Singleton.INSTANCE;
    }

    public void checkUpdate(Context context, UpdaterParams params) {
        showUpdateDialog(context, params.getUpdateTitle(), params.getUpdateMsg(),
                params.getDownloadUrl(), params.isForceUpdate(), params.getAuthority(), params.getSmallIcon());
    }

    private void showUpdateDialog(final Context context, String updateTitle, String updateMsg,
                                  final String downloadUrl, boolean isForceUpdate, final String authority,
                                  final int smallIcon) {
        final String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separator + "updater.apk";
        if (isForceUpdate) {
            new AlertDialog.Builder(context).setTitle(updateTitle)
                    .setMessage(updateMsg)
                    .setPositiveButton(context.getString(R.string.updater_now_update),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startUpdate(context, true, downloadUrl, authority, filePath,
                                            smallIcon);
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
                                    startUpdate(context, false, downloadUrl, authority, filePath,
                                            smallIcon);
                                }
                            })
                    .show();
        }
    }

    private void startUpdate(Context context, boolean isForceUpdate, String downloadUrl, String authority,
                             String filePath, int smallIcon) {
        DownloadTask downloadTask = new DownloadTask(context, isForceUpdate, authority, filePath, smallIcon);
        downloadTask.execute(downloadUrl, filePath);
    }

    private static class Singleton {
        private static final Updater INSTANCE = new Updater();
    }
}
