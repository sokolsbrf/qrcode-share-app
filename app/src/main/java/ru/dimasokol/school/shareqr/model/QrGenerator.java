package ru.dimasokol.school.shareqr.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import ru.dimasokol.school.shareqr.generator.Encoder;
import ru.dimasokol.school.shareqr.generator.ErrorCorrectionLevel;
import ru.dimasokol.school.shareqr.generator.QRCode;

public class QrGenerator {

    private static QrGenerator sInstance;

    public static QrGenerator getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new QrGenerator(context);
        }

        return sInstance;
    }

    private static final int BITMAP_SIZE = 1024;
    private static final String FILENAME = "qr.png";

    private String mLastSource;
    private QrLiveData mLastLiveData;
    private final Context mContext;

    public QrGenerator(Context context) {
        mContext = context.getApplicationContext();
    }

    public QrLiveData generateQr(final String source) {
        if (source.equals(mLastSource)) {
            return mLastLiveData;
        }

        mLastLiveData = new QrLiveData();
        mLastSource = source;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QRCode encoded = Encoder.encode(source, ErrorCorrectionLevel.Q);

                    Bitmap bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888);
                    int point = BITMAP_SIZE / encoded.getMatrix().getWidth();

                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setAntiAlias(false);

                    for (int w = 0; w < encoded.getMatrix().getWidth(); w++) {
                        for (int h = 0; h < encoded.getMatrix().getHeight(); h++) {
                            if (encoded.getMatrix().get(w, h) != 0) {
                                canvas.drawRect(w * point,
                                        h * point,
                                        w * point + point,
                                        h * point + point,
                                        paint);
                            }
                        }
                    }

                    final File file = new File(mContext.getFilesDir(), FILENAME);

                    OutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(file));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    mLastLiveData.postValue(new QrData(bitmap, file));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return mLastLiveData;
    }

}
