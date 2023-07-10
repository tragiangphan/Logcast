package com.programme.logcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LogcatService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "Logcast";
    private static final String TAG = "Logcast";
    private final LogcatServer logcatServer = new LogcatServer();
    private Map<String, List<String>> packageErrorMap;
    Process logcatProcess;
    private static final int MAX_LINES_PER_PACKAGE = 6;
    Map<String, String> packagePIDMap = new HashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        // Package list need to check if has errors
        packageErrorMap = new HashMap<>();
        packageErrorMap.put("com.google.process.gservices", Arrays.asList("crash", "error", "err", "die"));
        packageErrorMap.put("com.samsung.android.forest", Arrays.asList("crash", "error", "err", "die"));
        packageErrorMap.put("com.android.systemui", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.datetimecontrol", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.messagebroker", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.appmanager.app", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.firmware", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.io", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.examples.aiapp_uimerge", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.videopipeline", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.networkcontrol", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.adbauthorization", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.gateway", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.devicemanagement", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.appresourceproxy", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.deviceid", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.cloudconnector.app", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.crashreporter.app", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.health", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.webserver", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.deviceapp", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.userdb", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.event", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.wificonnect", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.media", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.onvif", Arrays.asList("crash", "error", "err", "die"));
//        packageErrorMap.put("com.securityandsafetythings.webui", Arrays.asList("crash", "error", "err", "die"));

        for (String packageName : packageErrorMap.keySet()) {
            StringBuilder[] contentBuilder = {new StringBuilder()};
            StringBuilder[] csvBuilder = {new StringBuilder()};
            List<String> errorList = packageErrorMap.get(packageName);
//            Map<String, String> pidMap = new HashMap<>();
            new Thread(() -> {
                try {
                    logcatProcess = new ProcessBuilder("logcat")
                            .redirectErrorStream(true)
                            .start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                    String line = null;
                    List<String> result = new ArrayList<>();
                    while ((line = bufferedReader.readLine()) != null) {
//                    System.out.println(line);
                        boolean isPackage = line.contains(packageName);
                        boolean havePID = line.contains("ActivityManager: Start proc") && (line.contains("for service") || line.contains("for activity") || line.contains("for broadcast"));
                        if (havePID && isPackage) {
                            packagePIDMap.put(packageName, line.substring(61, line.indexOf(":", 61)));
                            System.out.println(packageName + "1 " + packagePIDMap.get(packageName));
                            break;
                        }
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    while (packagePIDMap != null) {
                        String PID = null;
                        if ((PID = packagePIDMap.get(packageName)) != null) {
                            System.out.println(packageName + "2 " + packagePIDMap.get(packageName));
                            logcatProcess = new ProcessBuilder("logcat")
                                    .redirectErrorStream(true)
                                    .start();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                            String line = null;
                            while ((line = bufferedReader.readLine()) != null) {
                                boolean containPKG = line.substring(19, 25).contains(PID);
                                boolean mustNotice = errorList.stream().anyMatch(line::contains);
                                boolean isERROR = line.contains(" E ");
                                if (containPKG) {
//                                    System.out.println(line);
                                    LogItem logItem = new LogItem(
                                            line.substring(0, 5) + "-" + Year.now(),
                                            line.substring(6, 14),
                                            line.substring(19, 25),
                                            line.charAt(31),
                                            line.substring(33, line.indexOf(":", 33)),
                                            line.substring(line.indexOf(":", 33) + 1));

                                    // Write web data
                                    contentBuilder[0].insert(0, "<tr>" +
                                            "<td class=\"short\">" + logItem.getDate() + "</td>" +
                                            "<td class=\"short\">" + logItem.getTime() + "</td>" +
                                            "<td class=\"short\">" + logItem.getPID() + "</td>" +
                                            "<td class=\"short\">" + logItem.getTag() + "</td>" +
                                            "<td class=\"medium\">" + logItem.getName() + "</td>" +
                                            "<td class=\"long\">" + logItem.getContent() + "</td>" +
                                            "</tr>");

                                    // Write csv data
                                    csvBuilder[0].insert(0, logItem.getDate() + ", " + logItem.getTime() + ", " + logItem.getPID() + ", " + logItem.getTag() + ", " + logItem.getName() + ", " + logItem.getContent() + "\n");
                                    logcatServer.setChanged(packageName, true);
                                }
                                // Get time mark 1 day previous
                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
                                LocalDateTime currentDateTime = LocalDateTime.now();
                                LocalDateTime previousDateTime = currentDateTime.minusHours(24);
                                // Split one-by-one log
                                String[] csvLines = csvBuilder[0].toString().split("\n");
                                if (!csvLines[0].toString().isEmpty()) {
                                    // Mark log position more previous than 24h
                                    int marked = 0;
                                    for (int i = 0; i < csvLines.length; i++) {
                                        String dateTime = csvLines[i].substring(0, 10) + " " + csvLines[i].substring(12, 20);
                                        LocalDateTime checkDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
                                        if (checkDateTime.isBefore(previousDateTime)) {
                                            marked = i;
                                            break;
                                        }
                                    }
                                    // Clear log more previous than 24h
                                    if (marked > 0) {
                                        String truncatedContent = String.join("\n", Arrays.copyOfRange(csvLines, marked - 1, csvLines.length));
                                        csvBuilder[0] = new StringBuilder(truncatedContent);
                                    }
                                }

                                // Split one-by-one table row for web
                                String[] logLines = contentBuilder[0].toString().split("<tr>");
                                // Clear table row position more than MAX_LINES_PER_PACKAGE
                                if (logLines.length > MAX_LINES_PER_PACKAGE) {
                                    String truncatedContent = String.join("<tr>", Arrays.copyOfRange(logLines, 0, MAX_LINES_PER_PACKAGE + 1));
                                    contentBuilder[0] = new StringBuilder(truncatedContent);
                                }

                                // Push data to Server
                                logcatServer.setHtmlData(packageName, contentBuilder[0].toString());
                                logcatServer.setCSVData(packageName, csvBuilder[0].toString());
                            }
                            bufferedReader.close();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading logs: " + e.getMessage());
                }
            }).start();
        }

        try {
            logcatServer.start();
            String serverURL = "http://" + getIPAddress() + ":" + logcatServer.getListeningPort();
            Log.i("Web Server", serverURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Tạo kênh thông báo cho phiên bản Android mới (Android 8.0 trở lên)
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Logcast",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // Tạo notification cho dịch vụ
        Notification notification = createNotification();

        // Đặt dịch vụ vào chế độ foreground
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logcatServer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Get device's IP address
    private String getIPAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    private Notification createNotification() {
        // Tạo notification cho dịch vụ
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Logcast Service")
                .setContentText("Running in foreground")
                .setSmallIcon(R.drawable.ic_adb_24px);
        return builder.build();
    }

}