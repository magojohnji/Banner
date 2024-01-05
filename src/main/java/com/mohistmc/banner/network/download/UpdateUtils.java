package com.mohistmc.banner.network.download;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.util.MD5Util;
import mjson.Json;

import java.io.File;
import java.net.URI;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.mohistmc.banner.network.download.NetworkUtil.getConn;

public class UpdateUtils {

    private static int percentage = 0;

    public static void versionCheck() {
        System.out.println(BannerMCStart.I18N.get("update.check"));
        System.out.println(BannerMCStart.I18N.get("update.stopcheck"));

        try {
            Json json = Json.read(URI.create("https://ci.codemc.io/job/MohistMC/job/Banner-1.20.2/lastSuccessfulBuild/api/json").toURL());

            String jar_sha = BannerMCStart.getVersion();
            String build_number = json.asString("number");
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(json.asLong("timestamp")));

            if (jar_sha.equals(build_number))
                System.out.println(BannerMCStart.I18N.get("update.latest", jar_sha, build_number));
            else {
                System.out.println(BannerMCStart.I18N.get("update.detect", build_number, jar_sha, time));
            }
        } catch (Throwable e) {
            System.out.println(BannerMCStart.I18N.get("check.update.noci"));
        }
    }

    public static void downloadFile(String URL, File f) throws Exception {
        downloadFile(URL, f, null);
    }

    public static void downloadFile(String URL, File f, String md5) throws Exception {
        URLConnection conn = getConn(URL);
        System.out.println(BannerMCStart.I18N.get("download.file", f.getName(), getSize(conn.getContentLength())));
        ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
        FileChannel fc = FileChannel.open(f.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        int fS = conn.getContentLength();

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
        scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    if (rbc.isOpen()) {
                        if (percentage != Math.round((float) f.length() / fS * 100) && percentage < 100) {
                            System.out.println(BannerMCStart.I18N.get("file.download.percentage", f.getName(), percentage));
                        }
                        percentage = Math.round((float) f.length() / fS * 100);
                    }
                }, 3000, 1000, TimeUnit.SECONDS);
        fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        fc.close();
        rbc.close();
        percentage = 0;
        String MD5 = MD5Util.getMd5(f);
        if (f.getName().endsWith(".jar") && md5 != null && MD5 != null && !MD5.equals(md5.toLowerCase())) {
            f.delete();
            System.out.println(BannerMCStart.I18N.get("file.download.nook.md5", URL, MD5, md5.toLowerCase()));
            throw new Exception("md5");
        }
        System.out.println(BannerMCStart.I18N.get("download.file.ok", f.getName()));
    }

    public static String getSize(long size) {
        return (size >= 1048576L) ? (float) size / 1048576.0F + "MB" : ((size >= 1024) ? (float) size / 1024.0F + " KB" : size + " B");
    }
}
