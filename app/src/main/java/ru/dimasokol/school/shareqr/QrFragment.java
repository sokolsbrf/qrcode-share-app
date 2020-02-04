package ru.dimasokol.school.shareqr;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import ru.dimasokol.school.shareqr.model.QrData;
import ru.dimasokol.school.shareqr.model.QrGenerator;
import ru.dimasokol.school.shareqr.model.QrLiveData;

public class QrFragment extends Fragment {

    private static final String ARG_SOURCE_TEXT = "sourceText";

    private ImageView mImageView;
    private View mShareButton;
    private QrLiveData mLiveData;

    public static QrFragment newInstance(String sourceText) {
        Bundle args = new Bundle();

        args.putString(ARG_SOURCE_TEXT, sourceText);
        QrFragment fragment = new QrFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_qr, container, false);
        String source = getArguments().getString(ARG_SOURCE_TEXT);

        mImageView = root.findViewById(R.id.qr_code_image);
        mShareButton = root.findViewById(R.id.share_button);

        mLiveData = QrGenerator.getInstance(requireContext()).generateQr(source);
        mLiveData.observe(this, new Observer<QrData>() {
            @Override
            public void onChanged(QrData qrData) {
                mImageView.setImageBitmap(qrData.getBitmap());
                mShareButton.setEnabled(true);
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);

                Uri uri = FileProvider.getUriForFile(requireContext(), "ru.sberbank.school", mLiveData.getValue().getFile());
                String mimeType = requireContext().getContentResolver().getType(uri);

                shareIntent.setDataAndType(uri, mimeType);
                shareIntent.setClipData(ClipData.newUri(requireContext().getContentResolver(), "", uri));

                // На всякий случай
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_button_description)));
            }
        });

        return root;
    }
}
