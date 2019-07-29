package com.jieli.stream.dv.gdxxx.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jieli.stream.dv.gdxxx.R;
import com.jieli.stream.dv.gdxxx.ui.base.BaseFragment;


public class HelpFragment extends BaseFragment {
    private String tag = getClass().getSimpleName();

    public static HelpFragment newInstance(){
        return new HelpFragment();
    }

    public HelpFragment() {
        // Required empty public constructor
    }

    ImageView imgHelp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_help, container, false);
        ImageView ivBack = (ImageView) view.findViewById(R.id.about_return);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().onBackPressed();
                }
            }
        });

        imgHelp = (ImageView) view.findViewById(R.id.imgHelp);
        imgHelp.setFocusable(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }






    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();

    }


}
