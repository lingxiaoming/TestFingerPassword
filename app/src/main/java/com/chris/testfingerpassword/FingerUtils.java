package com.chris.testfingerpassword;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

/**
 * 指纹识别工具类
 * Created by lingxiaoming on 2017/4/13 0013.
 */

public class FingerUtils {
    public static CancellationSignal cancellationSignal;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void callFinger(Context context, final OnCallBackListener listener) {

        FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(context);
        if (!managerCompat.isHardwareDetected()) {//判断设备是否支持
            if (listener != null) {
                listener.onSuppoertFailed();
                return;
            }
        }

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isKeyguardSecure()) {//判断是否处于安全保护中，也就是有没有密码锁之类的二重保护
            if (listener != null) {
                listener.onInsecurity();
                return;
            }
        }

        if (!managerCompat.hasEnrolledFingerprints()) {//判断是否注册过指纹
            if (listener != null) {
                listener.onEnrollFailed();
                return;
            }
        }

        if (listener != null) {
            listener.onAuthenticationStart();
        }

        cancellationSignal = new CancellationSignal();//必须重新实例化，否则cancel过一次就不能再次使用了
        managerCompat.authenticate(null, 0, cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            //当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息，比如华为的提示信息是：尝试次数过多，请稍后尝试
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                if (listener != null) {
                    listener.onAuthenticationError(errMsgId, errString);
                }
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                if (listener != null) {
                    listener.onAuthenticationHelp(helpMsgId, helpString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (listener != null) {
                    listener.onAuthenticationSuccessed(result);
                }
            }

            //当指纹验证失败的时候回调次函数，失败之后允许多次尝试，失败次数过多就会停止相应一段时间然后再停止sensor的工作
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (listener != null) {
                    listener.onAuthenticationFailed();
                }
            }
        }, null);
    }

    public static void cancel() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
        }
    }

    interface OnCallBackListener {
        void onSuppoertFailed();

        void onInsecurity();

        void onEnrollFailed();

        void onAuthenticationStart();

        void onAuthenticationError(int errMsgId, CharSequence errString);

        void onAuthenticationFailed();

        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);

        void onAuthenticationSuccessed(FingerprintManagerCompat.AuthenticationResult result);
    }
}
