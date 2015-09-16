package com.accenture.datongoaii.widget;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.CreateDeptActivity;
import com.accenture.datongoaii.activity.CreateGroupActivity;
import com.accenture.datongoaii.activity.CreateOrgActivity;
import com.accenture.datongoaii.activity.MainActivity;
import com.accenture.datongoaii.activity.PhoneContactActivity;
import com.accenture.datongoaii.fragment.ContactFragment;
import com.accenture.datongoaii.util.Constants;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

public class ContactAddPopupWindow extends PopupWindow implements OnClickListener {
    private View anchor;
    private Context context;

    public ContactAddPopupWindow(Context context) {
        super(context, null);
    }

    public ContactAddPopupWindow(Context context, View anchor) {
        super(View.inflate(context, R.layout.view_popup_contact_add, null),
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.anchor = anchor;
        this.context = context;
        View view = getContentView();
        view.findViewById(R.id.tvAddGroup).setOnClickListener(this);
        view.findViewById(R.id.tvCreateOrg).setOnClickListener(this);
        view.findViewById(R.id.tvAddFriend).setOnClickListener(this);
        view.findViewById(R.id.tvAddDept).setOnClickListener(this);
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
                context.startActivity(intent);
                break;
            }
            case R.id.tvCreateOrg: {
                Intent intent = new Intent(context, CreateOrgActivity.class);
                context.startActivity(intent);
                break;
            }
            case R.id.tvAddFriend: {
                Intent intent = new Intent(context, PhoneContactActivity.class);
                context.startActivity(intent);
                break;
            }
            case R.id.tvAddDept: {
                Intent intent = new Intent(context, CreateDeptActivity.class);
                intent.putExtra(Constants.BUNDLE_TAG_CREATE_DEPT, ((MainActivity) context).contactFrag.org.orgId);
                ((MainActivity) context).startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_DEPT);
                break;
            }
        }
        this.dismiss();
    }

}
