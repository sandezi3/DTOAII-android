package com.accenture.datongoaii.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.CreateGroupActivity;
import com.accenture.datongoaii.activity.CreateOrgActivity;
import com.accenture.datongoaii.activity.PhoneContactActivity;
import com.accenture.datongoaii.Constants;

public class ContactAddPopupWindow extends PopupWindow implements OnClickListener {
    private View anchor;
    private Context context;

    public ContactAddPopupWindow(Context context, View anchor, Boolean canCreateOrg) {
        super(View.inflate(context, R.layout.view_popup_contact_add, null),
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.anchor = anchor;
        this.context = context;
        View view = getContentView();
        LinearLayout llContent = (LinearLayout) view.findViewById(R.id.llContent);
        llContent.findViewById(R.id.tvAddGroup).setOnClickListener(this);
        llContent.findViewById(R.id.tvCreateOrg).setOnClickListener(this);
        llContent.findViewById(R.id.tvAddFriend).setOnClickListener(this);
        if (!canCreateOrg) {
            View tvCreateOrg = llContent.findViewById(R.id.tvCreateOrg);
            Integer index = llContent.indexOfChild(tvCreateOrg);
            tvCreateOrg.setVisibility(View.GONE);
            llContent.getChildAt(index + 1).setVisibility(View.GONE);
        }
    }

    public void showAsDropDown() {
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new PaintDrawable(context.getResources()
                .getColor(R.color.transparent)));
        this.showAsDropDown(anchor, 0, 5);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddGroup: {
                Intent intent = new Intent(context, CreateGroupActivity.class);
                ((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_ORG);
                break;
            }
            case R.id.tvCreateOrg: {
                Intent intent = new Intent(context, CreateOrgActivity.class);
                ((Activity) context).startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_ORG);
                break;
            }
            case R.id.tvAddFriend: {
                Intent intent = new Intent(context, PhoneContactActivity.class);
                context.startActivity(intent);
                break;
            }
        }
        this.dismiss();
    }

}
