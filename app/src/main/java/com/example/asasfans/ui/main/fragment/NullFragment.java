package com.example.asasfans.ui.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.asasfans.R;

/**
 * @author LEN5010
 * @description 空占位 Fragment，用于未知或暂不可用页面的兜底展示。
 */
public class NullFragment extends Fragment {
    public static NullFragment newInstance() {
        NullFragment fragment = new NullFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }
}
