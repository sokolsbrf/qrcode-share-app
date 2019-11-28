package ru.dimasokol.school.shareqr.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.dimasokol.school.shareqr.generator.Encoder;
import ru.dimasokol.school.shareqr.generator.ErrorCorrectionLevel;
import ru.dimasokol.school.shareqr.generator.QRCode;
import ru.dimasokol.school.shareqr.generator.WriterException;

public class QrGenerator {

    private static QrGenerator sInstance;

    public static QrGenerator getInstance(File rootDir) {
        if (sInstance == null) {
            sInstance = new QrGenerator(rootDir);
        }

        return sInstance;
    }

    private static final int QR_SIZE = 1024;
    private static final int QR_BORDERS = 32;

    private static final String FILE_NAME = "qr";
    private static final String PNG = ".png";

    private Map<String, QrLiveData> mGeneratedQrMap = new LinkedHashMap<String, QrLiveData>() {
        @Override
        protected boolean removeEldestEntry(Entry eldest) {
            return size() > 10;
        }
    };

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final File mRootDir;

    public QrGenerator(File rootDir) {
        mRootDir = rootDir;
    }

    public QrLiveData generate(String source) {
        QrLiveData liveData = mGeneratedQrMap.get(source);

        if (liveData == null) {
            liveData = new QrLiveData();
            mGeneratedQrMap.put(source, liveData);

            mExecutor.submit(new QrGenerator.Generator(liveData, source, mRootDir));
        }

        return liveData;
    }

    private static class Generator implements Runnable {
        final QrLiveData mLiveData;
        final String mSource;
        final File mRootDir;

        final Paint mBlackPaint = new Paint();

        private Generator(QrLiveData liveData, String source, File rootDir) {
            mLiveData = liveData;
            mSource = source;
            mRootDir = rootDir;

            mBlackPaint.setAntiAlias(false);
            mBlackPaint.setColor(Color.BLACK);
            mBlackPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void run() {
            Bitmap bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);

            canvas.drawColor(Color.WHITE);

            try {
                QRCode qr = Encoder.encode(mSource, ErrorCorrectionLevel.Q);

                int wPixel = (QR_SIZE - QR_BORDERS * 2) / qr.getMatrix().getWidth();
                int hPixel = (QR_SIZE - QR_BORDERS * 2) / qr.getMatrix().getHeight();

                for (int w = 0; w < qr.getMatrix().getWidth(); w++) {
                    for (int h = 0; h < qr.getMatrix().getHeight(); h++) {
                        int left = QR_BORDERS + w * wPixel;
                        int top = QR_BORDERS + h * hPixel;
                        int right = QR_BORDERS + (w + 1) * wPixel;
                        int bottom = QR_BORDERS + (h + 1) * hPixel;

                        if (qr.getMatrix().get(w, h) != 0) {
                            canvas.drawRect(left, top, right, bottom, mBlackPaint);
                        }
                    }
                }

                File out = nextFile();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(out));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();

                mLiveData.postValue(new QrData(out, bitmap));

            } catch (WriterException | IOException e) {
                mLiveData.postValue(null);
            }
        }

        private File nextFile() {
            String fileName = FILE_NAME + PNG;
            File file = new File(mRootDir, fileName);
            int counter = 0;

            while (file.exists()) {
                counter++;
                fileName = FILE_NAME + counter + PNG;
                file = new File(mRootDir, fileName);
            }

            return file;
        }
    }
}
