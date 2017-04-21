package com.chris.testfingerpassword;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech mTts;
    private Button mAgainButton;
    private static final String TAG = "MainActivity";

    private Button mFingerButton;
    private Button mTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTTS();
        initFinger();
        initTest();
    }

    private void initTest() {
        mTestButton = (Button) findViewById(R.id.test_btn);
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DecimalFormat format1 = new DecimalFormat("###,###,###,##0.00");
                DecimalFormat format2 = new DecimalFormat("###,###,###,###.00");
                DecimalFormat format3 = new DecimalFormat("###,###,###,##");

                format1.format(1000);
                format2.format(1000);
                format3.format(1000);
            }
        });

    }

    /**
     * 指纹识别
     */
    private void initFinger() {
        mFingerButton = (Button) findViewById(R.id.finger_btn);
        mFingerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                FingerUtils.callFinger(MainActivity.this, new FingerUtils.OnCallBackListener() {
                    AlertDialog dialog;

                    @Override
                    public void onSuppoertFailed() {
                        Toast.makeText(MainActivity.this, "当前设备不支持指纹", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onInsecurity() {
                        Toast.makeText(MainActivity.this, "当前设备未处于安全保护中", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onEnrollFailed() {
                        Toast.makeText(MainActivity.this, "请在设置中设置指纹", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAuthenticationStart() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_finger, null);
                        initView(view);
                        builder.setView(view);
                        builder.setCancelable(false);
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                handler.removeMessages(0);
                                FingerUtils.cancel();
                            }
                        });
                        dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        Toast.makeText(MainActivity.this, errString.toString(), Toast.LENGTH_LONG).show();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            handler.removeMessages(0);
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Toast.makeText(MainActivity.this, "解锁失败", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                        Toast.makeText(MainActivity.this, helpString.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAuthenticationSuccessed(FingerprintManagerCompat.AuthenticationResult result) {
                        Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_LONG).show();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            handler.removeMessages(0);
                        }
                    }
                });
            }
        });
    }

    android.os.Handler handler = new android.os.Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                int i = position % 5;
                if (i == 0) {
                    tv[4].setBackground(null);
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }else {
                    tv[i].setBackground(null);
                    tv[i-1].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                position++;
                handler.sendEmptyMessageDelayed(0, 100);
            }
        }
    };


    TextView[] tv = new TextView[5];
    private int position = 0;

    private void initView(View view) {
        position = 0;
        tv[0] = (TextView) view.findViewById(R.id.tv_1);
        tv[1] = (TextView) view.findViewById(R.id.tv_2);
        tv[2] = (TextView) view.findViewById(R.id.tv_3);
        tv[3] = (TextView) view.findViewById(R.id.tv_4);
        tv[4] = (TextView) view.findViewById(R.id.tv_5);
        handler.sendEmptyMessageDelayed(0, 100);
    }

    /**
     * TTS文字转语音
     */
    private void initTTS() {
        mTts = new TextToSpeech(this, this);
        mAgainButton = (Button) findViewById(R.id.again_button);

        mAgainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sayHello();
            }
        });
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                mAgainButton.setEnabled(true);
                // Greet the user.
                sayHello();
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    private void sayHello() {
        // Select a random hello.
        String hello = "我是谁chris,hello,1+1";
        mTts.speak(hello, TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
                null);
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        super.onDestroy();
    }
}
