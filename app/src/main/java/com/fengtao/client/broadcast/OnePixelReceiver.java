package com.fengtao.client.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fengtao.client.activity.OnePixelActivity;


/**
 * Created by Administrator on 2017/7/10.
 * 监听屏幕状态的广播
 */
public class OnePixelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {    //屏幕关闭启动1像素Activity
            Intent it = new Intent(context, OnePixelActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //启动后台服务
//            context.startService(new Intent(context,BleBackgroundService.class));
            context.startActivity(it);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {   //屏幕打开 结束1像素
            context.sendBroadcast(new Intent("finish"));
            Intent main = new Intent(Intent.ACTION_MAIN);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            main.addCategory(Intent.CATEGORY_HOME);
            context.startActivity(main);
        }
    }
}