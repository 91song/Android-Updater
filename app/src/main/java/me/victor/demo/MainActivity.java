package me.victor.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import me.victor.updater.Updater;
import me.victor.updater.UpdaterParams;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_normal:
                startUpdate(false);
                break;
            case R.id.btn_force:
                startUpdate(true);
                break;
            default:
                break;
        }
    }

    private void startUpdate(boolean isForceUpdate) {
        UpdaterParams params = new UpdaterParams.Builder().setUpdateTitle("发现新版本")
                .isForceUpdate(isForceUpdate)
                .setUpdateMsg("1、版本不息，优化不止。\n2、版本很牛逼。")
                .setDownloadUrl("http://imgs.todriver.com/app/yrxcjld2.4.0.apk")
                .setAuthority("me.victor.demo.fileprovider")
                .build();
        Updater.getInstance().checkUpdate(this, params);
    }
}
