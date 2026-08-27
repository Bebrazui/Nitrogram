package org.telegram.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.telegram.messenger.wsproxy.WsProxyController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.LaunchActivity;

public class NotificationsService extends Service {

    public static final String CHANNEL_ID = "nitro_keepalive_service_v2";
    public static final int NOTIFICATION_ID = 9999;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundSilent();
        ApplicationLoader.postInitApplication();
        WsProxyController.ensureRunningSync();
        keepConnectionsAlive();
    }

    private void startForegroundSilent() {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Фоновая служба Nitrogram",
                        NotificationManager.IMPORTANCE_MIN
                );
                channel.setDescription("Фоновое соединение для мгновенного получения сообщений");
                channel.setShowBadge(false);
                channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                channel.enableVibration(false);
                channel.enableLights(false);
                channel.setSound(null, null);
                if (nm != null) {
                    nm.createNotificationChannel(channel);
                }
            }

            Intent intent = new Intent(this, LaunchActivity.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 23) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Nitrogram")
                    .setContentText("Фоновое подключение активно")
                    .setContentIntent(pi)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setOngoing(true)
                    .setSilent(true)
                    .build();

            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Throwable t) {
            FileLog.e("NotificationsService startForeground failed", t);
        }
    }

    private void keepConnectionsAlive() {
        Utilities.stageQueue.postRunnable(() -> {
            try {
                for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                    if (UserConfig.getInstance(a).isClientActivated()) {
                        ConnectionsManager.getInstance(a).setAppPaused(false, false);
                        ConnectionsManager.getInstance(a).resumeNetworkMaybe();
                    }
                }
            } catch (Throwable t) {
                FileLog.e(t);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundSilent();
        WsProxyController.ensureRunningSync();
        keepConnectionsAlive();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = MessagesController.getGlobalNotificationsSettings();
        if (preferences.getBoolean("pushService", true)) {
            Intent intent = new Intent("org.telegram.start");
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        }
    }
}
