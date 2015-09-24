package com.accenture.datongoaii.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.MyInfoActivity;

public class MorePopupWindow extends PopupWindow implements OnClickListener {
    private View anchor;
    private Context context;

    public MorePopupWindow(Context context) {
        super(context, null);
    }

    public MorePopupWindow(Context context, View anchor) {
        super(View.inflate(context, R.layout.view_popup_more, null),
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.anchor = anchor;
        this.context = context;
        View view = getContentView();
        view.findViewById(R.id.tvAccount).setOnClickListener(this);
        view.findViewById(R.id.tvHelp).setOnClickListener(this);
        view.findViewById(R.id.tvAbout).setOnClickListener(this);
    }

    public void showAsDropDown() {
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new PaintDrawable(context.getResources()
                .getColor(R.color.transparent)));
        this.showAsDropDown(anchor, 5, 5);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAccount:
                Intent intent = new Intent(context, MyInfoActivity.class);
                context.startActivity(intent);
                break;
            case R.id.tvHelp:
                break;
            case R.id.tvAbout:
                break;
        }
        this.dismiss();
    }

}
