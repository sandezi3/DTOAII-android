package com.accenture.datongoaii.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Constants;
import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateOrgActivity extends Activity implements View.OnClickListener {
    private Context context;

    private EditText etName;
    private View btnSelect;
    private View btnBack;
    private View btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_create_org);

        etName = (EditText) findViewById(R.id.etName);
        btnSelect = findViewById(R.id.layoutSelect);
        btnBack = findViewById(R.id.btnBack);
        btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSelect.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.equals(btnSelect)) {
            Intent intent = new Intent(view.getContext(), CreateGroupActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_SELECT_MEMBER, Constants.FUNCTION_TAG_CREATE_ORG);
            startActivityForResult(intent, Constants.REQUEST_CODE_CREATE_ORG);
            return;
        }
        if (view.equals(btnBack)) {
            finish();
            return;
        }
        if (view.equals(btnCreate)) {
            //TODO
            List<Contact> userList = new ArrayList<Contact>();
            Contact c1 = new Contact();
            c1.id = 10;
            Contact c2 = new Contact();
            c2.id = 11;
            userList.add(c1);
            userList.add(c2);

            List<Contact> invitedList = new ArrayList<Contact>();
            Contact c3 = new Contact();
            c3.cell = "18981827361";
            c3.name = "张三";
            Contact c4 = new Contact();
            c4.cell = "18016273651";
            c4.name = "王五";
            invitedList.add(c3);
            invitedList.add(c4);
            startCreateOrgConnect(etName.getEditableText().toString().trim(), userList, invitedList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CREATE_ORG
                && resultCode == RESULT_OK) {
            finish();
        }
    }

    public void startCreateOrgConnect(String name, List<Contact> userList, List<Contact> invitedList) {
        String url = Config.SERVER_HOST + Config.URL_CREATE_ORG;
        JSONObject obj = new JSONObject();
        try {
            JSONArray uArray = new JSONArray();
            for (Contact c : userList) {
                uArray.put(c.id);
            }
            JSONArray iArray = new JSONArray();
            for (Contact c : invitedList) {
                JSONObject o = new JSONObject();
                o.put("cell", c.cell);
                o.put("username", c.name);
                iArray.put(o);
            }
            obj.put("orgName", name);
            obj.put("userList", uArray);
            obj.put("invitedList", iArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Logger.i("CreateOrgActivity", "startCreateOrgConnect");
        new HttpConnection().post(url, obj, new HttpConnection.CallbackListener() {
            @Override
            public void callBack(String result) {
                if (!result.equals("fail")) {
                    ((Activity)context).finish();
                }
            }
        });
    }

}
