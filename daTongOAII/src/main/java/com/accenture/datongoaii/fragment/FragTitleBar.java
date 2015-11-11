package com.accenture.datongoaii.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.accenture.datongoaii.R;

/**
 * Created by leon on 11/10/15.
 * 导航栏标题类
 */
public class FragTitleBar extends Fragment implements View.OnClickListener {
    public void init(int titleId) {
        setDisplayTitle(titleId);
    }

    public void setDisplayTitle(int titleId) {
        View view = getView();
        if (view != null) {
            ((TextView) view.findViewById(R.id.tvTitle)).setText(titleId);
        }
    }

    public void showTitleCustomButton(int strId) {
        View view = getView();
        if (view != null) {
            Button btn = (Button) view.findViewById(R.id.btnCustom);
            btn.setText(strId);
            btn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_title_bar, container, false);
        view.findViewById(R.id.btnCustom).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        getActivity().finish();
    }
}
