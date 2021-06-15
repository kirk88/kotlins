package com.example.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

/**
 * @author xc
 * @date 2018/11/16
 * @desc
 */
public class DeviceIdUtil {
    /**
     * 获得设备硬件标识
     *
     * @param context 上下文
     * @return 设备硬件标识
     */
    public static String getDeviceId(Context context) {
        final StringBuilder deviceId = new StringBuilder();

        //获得设备默认IMEI（>=6.0 需要ReadPhoneState权限）
        String imei = getIMEI(context);
        //获得AndroidId（无需权限）
        String androidId = getAndroidId(context);
        //获得设备序列号（无需权限）
        String serial = getSERIAL();
        //获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
        String uuid = getDeviceUUID().replace("-", "");

        //追加imei
        if (!TextUtils.isEmpty(imei)) {
            deviceId.append(imei);
            deviceId.append("|");
        }
        //追加androidid
        if (!TextUtils.isEmpty(androidId)) {
            deviceId.append(androidId);
            deviceId.append("|");
        }
        //追加serial
        if (!TextUtils.isEmpty(serial)) {
            deviceId.append(serial);
            deviceId.append("|");
        }
        //追加硬件uuid
        if (!TextUtils.isEmpty(uuid)) {
            deviceId.append(uuid);
        }

        if (deviceId.length() == 0) {
            deviceId.append(UUID.randomUUID().toString());
        }

        //生成SHA1，统一DeviceId长度
        try {
            byte[] hash = getHashByString(deviceId.toString());
            String sha1 = bytesToHex(hash);
            if (!TextUtils.isEmpty(sha1)) {
                //返回最终的DeviceId
                return sha1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return deviceId.toString();
    }

    //需要获得READ_PHONE_STATE权限，>=6.0，默认返回null
    @SuppressLint("HardwareIds")
    private static String getIMEI(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获得设备的AndroidId
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    @SuppressLint("HardwareIds")
    private static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    @SuppressLint("HardwareIds")
    private static String getSERIAL() {
        try {
            return Build.SERIAL;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    @SuppressLint("HardwareIds")
    private static String getDeviceUUID() {
        try {
            String dev = "3883756" +
                    Build.BOARD.length() % 10 +
                    Build.BRAND.length() % 10 +
                    Build.DEVICE.length() % 10 +
                    Build.HARDWARE.length() % 10 +
                    Build.ID.length() % 10 +
                    Build.MODEL.length() % 10 +
                    Build.PRODUCT.length() % 10 +
                    Build.SERIAL.length() % 10;
            return new UUID(dev.hashCode(),
                    Build.SERIAL.hashCode()).toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 取SHA1
     *
     * @param data 数据
     * @return 对应的hash值
     */
    private static byte[] getHashByString(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.reset();
            messageDigest.update(data.getBytes(StandardCharsets.UTF_8));
            return messageDigest.digest();
        } catch (Exception e) {
            return "".getBytes();
        }
    }

    /**
     * 转16进制字符串
     *
     * @param data 数据
     * @return 16进制字符串
     */
    private static String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        String stmp;
        for (byte datum : data) {
            stmp = (Integer.toHexString(datum & 0xFF));
            if (stmp.length() == 1)
                sb.append("0");
            sb.append(stmp);
        }
        return sb.toString().toUpperCase(Locale.CHINA);
    }
}