package ru.dimasokol.school.shareqr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import ru.dimasokol.school.shareqr.generator.Encoder;
import ru.dimasokol.school.shareqr.generator.ErrorCorrectionLevel;
import ru.dimasokol.school.shareqr.generator.QRCode;
import ru.dimasokol.school.shareqr.generator.WriterException;

public class MainActivity extends AppCompatActivity {

    public static final int POINT = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            QRCode encoded = Encoder.encode("https://ya.ru", ErrorCorrectionLevel.Q);
            Log.d("QR", encoded.toString());

            Bitmap bitmap = Bitmap.createBitmap(encoded.getMatrix().getWidth() * POINT,
                    encoded.getMatrix().getHeight() * POINT, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);

            for (int w = 0; w < encoded.getMatrix().getWidth(); w++) {
                for (int h = 0; h < encoded.getMatrix().getHeight(); h++) {
                    if (encoded.getMatrix().get(w, h) != 0) {
                        canvas.drawRect(w * POINT, h * POINT, w * POINT + POINT, h * POINT + POINT, paint);
                    }
                }
            }

            ImageView resultView = findViewById(R.id.qr_code);
            resultView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
