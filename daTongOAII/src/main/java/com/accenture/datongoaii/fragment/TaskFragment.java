package com.accenture.datongoaii.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accenture.datongoaii.Constants;
import com.accenture.datongoaii.DTOARequest;
import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.AppActivity;
import com.accenture.datongoaii.activity.GroupPortalActivity;
import com.accenture.datongoaii.adapter.AppGridAdapter;
import com.accenture.datongoaii.adapter.GroupPortalListAdapter;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.App;
import com.accenture.datongoaii.model.AppCategory;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.widget.ExpandGridView;
import com.accenture.datongoaii.widget.ExpandListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class TaskFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final int HANDLER_TAG_REFRESH_VIEW = 0;

    private Context context;
    private TaskFragment fragment;
    private View pbLayout;
    private LinearLayout llContent;

    private List<AppCategory> categories;

    private FragmentHandler handler = new FragmentHandler(this);

    public static class FragmentHandler extends Handler {
        WeakReference<TaskFragment> theFragment;

        public FragmentHandler(TaskFragment TaskFragment) {
            this.theFragment = new WeakReference<TaskFragment>(TaskFragment);
        }

        @Override
        public void handleMessage(Message message) {
            TaskFragment mFragment = theFragment.get();
            switch (message.what) {
                case HANDLER_TAG_REFRESH_VIEW:
                    break;
            }
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = this.getActivity();
        fragment = this;
        View layoutTask = inflater.inflate(R.layout.frag_task, container, false);
        pbLayout = layoutTask.findViewById(R.id.pbLayout);
        llContent = (LinearLayout) layoutTask.findViewById(R.id.llContent);

        pbLayout.setVisibility(View.VISIBLE);
        categories = new ArrayList<AppCategory>();
        startGetAppsByUserId(Account.getInstance().getUserId());
        return layoutTask;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent instanceof ExpandGridView) {
            App app = (App) ((ExpandGridView) parent).getAdapter().getItem(position);
            Intent intent = new Intent(context, AppActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_APP, app);
            startActivity(intent);
        } else if (parent instanceof ExpandListView) {
            Group group = (Group) ((ExpandListView) parent).getAdapter().getItem(position);
            Intent intent = new Intent(context, GroupPortalActivity.class);
            intent.putExtra(Constants.BUNDLE_TAG_GROUP, group);
            String type = (String) parent.getTag();
            intent.putExtra(Constants.BUNDLE_TAG_GROUP_TYPE, type);
            startActivity(intent);
        }
    }

    @SuppressWarnings("unchecked")
    public void refresh() {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (categories == null || categories.size() == 0) {
                    return;
                }
                llContent.removeAllViews();
                for (AppCategory category : categories) {
                    if (category.apps != null && category.apps.size() > 0) {
                        addTitle(category.type, 16);
                        addGridView(category.apps);
                    } else if (category.groups != null && category.groups.size() > 0) {
                        addTitle(category.type, 16);
                        addListView(category.groups, category.type);
                    }
                }
            }
        });
    }

    private void addTitle(String title, int size) {
        TextView t = new TextView(context);
        t.setTextSize(size);
        t.setText(title);
        t.setBackgroundColor(getResources().getColor(R.color.white_transparent_1));
        t.setPadding(20, 5, 20, 5);
        t.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.height_top_bar));
        if (size == 16) {
            params.setMargins(0, 10, 0, 0);
        }
        t.setLayoutParams(params);
        llContent.addView(t);
    }

    private void addGridView(List<App> list) {
        if (list != null && list.size() > 0) {
            ExpandGridView gridView = new ExpandGridView(context);
            gridView.setBackgroundColor(getResources().getColor(R.color.white));
            AppGridAdapter adapter = new AppGridAdapter(context, list);
            gridView.setOnItemClickListener(fragment);
            gridView.setAdapter(adapter);
            gridView.setVerticalSpacing(5);
            gridView.setNumColumns(5);
            llContent.addView(gridView);
        }
    }

    private void addListView(List<Group> list, String type) {
        if (list != null && list.size() > 0) {
            ExpandListView listView = new ExpandListView(context);
            listView.setBackgroundColor(getResources().getColor(R.color.white));
            GroupPortalListAdapter adapter = new GroupPortalListAdapter(context, list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(fragment);
            listView.setTag(type);
            llContent.addView(listView);
        }
    }

    /**
     * 网络获取Apps
     *
     * @param userId 用户ID
     */
    private void startGetAppsByUserId(Integer userId) {
        DTOARequest.getInstance(context).startGetAppsByUserId(userId, new DTOARequest.RequestListener() {
            @Override
            public void callback(String result) {
                pbLayout.setVisibility(View.INVISIBLE);
                try {
                    List<AppCategory> list = AppCategory.listFromJSON(new JSONObject(result));
                    if (list != null && list.size() > 0) {
                        categories.clear();
                        categories.addAll(list);
                        refresh();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void callbackError() {

            }
        });
    }

}
