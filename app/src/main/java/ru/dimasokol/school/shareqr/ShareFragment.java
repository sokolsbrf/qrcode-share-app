package ru.dimasokol.school.shareqr;


import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import ru.dimasokol.school.shareqr.model.QrData;
import ru.dimasokol.school.shareqr.model.QrGenerator;
import ru.dimasokol.school.shareqr.model.QrLiveData;


public class ShareFragment extends Fragment {
    private static final String ARG_SOURCE = "source";
    private String mSourceText;

    private ImageView mResultImage;
    private View mShareButton;

    private QrLiveData mQrLiveData;

    public ShareFragment() {
        super(R.layout.fragment_share);
    }


    public static ShareFragment newInstance(String source) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SOURCE, source);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSourceText = getArguments().getString(ARG_SOURCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mResultImage = root.findViewById(R.id.qr_code);
        mShareButton = root.findViewById(R.id.button_share);
        mShareButton.setEnabled(false);

        mQrLiveData = QrGenerator.getInstance(requireContext().getFilesDir()).generate(mSourceText);
        mQrLiveData.observe(this, new Observer<QrData>() {
            @Override
            public void onChanged(QrData qrData) {
                if (qrData != null) {
                    mResultImage.setImageBitmap(qrData.getBitmap());
                    mShareButton.setEnabled(true);
                }
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = QrProvider.getUriForFile(requireContext(), "ru.dimasokol.school.qr",
                        mQrLiveData.getValue().getFile());

                Intent share = new Intent(Intent.ACTION_SEND);

                // Все возможные данные для всех возможных версий ОС и приложений
                share.setDataAndType(uri, requireContext().getContentResolver().getType(uri));
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.setClipData(ClipData.newUri(requireContext().getContentResolver(), getString(R.string.app_name), uri));
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(share);
            }
        });

        return root;
    }

}
