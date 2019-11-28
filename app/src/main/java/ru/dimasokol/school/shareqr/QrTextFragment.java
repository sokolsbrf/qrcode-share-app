package ru.dimasokol.school.shareqr;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class QrTextFragment extends Fragment {

    private TextView mSourceText;
    private View mGenerateButton;

    public QrTextFragment() {
        super(R.layout.fragment_qr_source);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        mSourceText = root.findViewById(R.id.qr_text);
        mGenerateButton = root.findViewById(R.id.button_generate);

        mGenerateButton.setEnabled(false);
        mSourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mGenerateButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Такой трюк убирает неприятное мерцание IME (клавиатуры).
                // Скроем её с экрана, а потом запустим нужный фрагмент с крошечной задержкой

                InputMethodManager inputService = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputService.hideSoftInputFromWindow(mSourceText.getWindowToken(), 0);

                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((GeneratorHost) requireActivity()).proceedToGeneration(mSourceText.getText().toString());
                    }
                }, 50);
            }
        });

        return root;
    }
}
