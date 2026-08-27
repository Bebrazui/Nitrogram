package org.telegram.messenger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;

import org.telegram.messenger.wsproxy.WsProxyController;
import org.telegram.tgnet.ConnectionsManager;

public class BackgroundPushSyncReceiver extends BroadcastReceiver {

    public static final String ACTION_PUSH_SYNC = "org.telegram.messenger.ACTION_PUSH_SYNC";
    private static final long SYNC_INTERVAL = 45 * 1000L; // 45 seconds

    private static PowerManager.WakeLock wakeLock;

    public static void startSync(Context context) {
        scheduleNext(context, SYNC_INTERVAL);
    }

    public static void triggerSync(Context context) {
        scheduleNext(context, 1000L);
    }

    public static void scheduleNext(Context context, long delayMs) {
        if (context == null) return;
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;

            Intent intent = new Intent(context, BackgroundPushSyncReceiver.class);
            intent.setAction(ACTION_PUSH_SYNC);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 23) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pi = PendingIntent.getBroadcast(context, 1337, intent, flags);

            long triggerAt = SystemClock.elapsedRealtime() + delayMs;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi);
            } else {
                am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi);
            }
        } catch (Throwable t) {
            FileLog.e("BackgroundPushSyncReceiver schedule failed", t);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                if (wakeLock == null) {
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "nitrogram:push_sync");
                    wakeLock.setReferenceCounted(false);
                }
                wakeLock.acquire(4000); // 4 seconds max to fetch messages
            }
        } catch (Throwable ignore) {}

        ApplicationLoader.postInitApplication();
        WsProxyController.ensureRunningSync();

        Utilities.stageQueue.postRunnable(() -> {
            try {
                for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                    if (UserConfig.getInstance(a).isClientActivated()) {
                        ConnectionsManager.getInstance(a).setAppPaused(false, false);
                        ConnectionsManager.getInstance(a).resumeNetworkMaybe();
                        MessagesController.getInstance(a).getDifference();
                    }
                }
            } catch (Throwable t) {
                FileLog.e(t);
            }
        });

        // Schedule next check
        scheduleNext(context, SYNC_INTERVAL);
    }
}
