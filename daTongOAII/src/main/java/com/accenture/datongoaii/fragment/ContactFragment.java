package com.accenture.datongoaii.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.accenture.datongoaii.R;
import com.accenture.datongoaii.activity.PhoneContactActivity;
import com.accenture.datongoaii.model.Account;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Dept;
import com.accenture.datongoaii.model.FirstPinYin;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.network.HttpConnection;
import com.accenture.datongoaii.util.Config;
import com.accenture.datongoaii.util.Intepreter;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.widget.ContactAddPopupWindow;
import com.accenture.datongoaii.widget.MorePopupWindow;
import com.accenture.datongoaii.widget.SectionListView;
import com.accenture.datongoaii.widget.SectionListView.OnSectionItemClickedListener;
import com.accenture.datongoaii.widget.SectionListView.SectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactFragment extends Fragment implements
		OnSectionItemClickedListener {
	private View layoutContact;
	private SectionListView slvContact;
	private Dept dept;
	private List<Object> viewList;
	private List<Object> groupList;
	private List<Object> uList;
	private List<Object> tmpList;

	private EditText etSearch;
	private ImageView ivSearch;
	private TextView tvSearch;
	private ImageButton ibAdd;

	private View lBack;

	public static class ContactFragHandler extends Handler {
		WeakReference<ContactFragment> theFrag = null;
	}

	private void syncData() {
		viewList.addAll(dept.subDept);
		viewList.addAll(groupList);
		viewList.addAll(dept.contactList);
		viewList.addAll(uList);
		tmpList.clear();
		tmpList.addAll(viewList);
		viewList.clear();
		viewList.addAll(FirstPinYin.createPinYinGroupedList(tmpList));
		tmpList.clear();
		tmpList.addAll(viewList);
		adapter.notifyDataSetChanged();
		if (isRootList(dept)) {
			lBack.setVisibility(View.GONE);
		} else {
			lBack.setVisibility(View.VISIBLE);
			lBack.setOnClickListener(listener);
		}
	}

	private OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			viewList.clear();
			dept = dept.parent;
			syncData();
		}
	};

	private final SectionListAdapter adapter = new SectionListAdapter() {
		@Override
		public int getSectionCount() {
			if (viewList == null) {
				return 0;
			} else {
				return viewList.size();
			}
		}

		@Override
		public int getSectionItemCount(int section) {
			Object object = viewList.get(section);
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) object;
			return list.size();
		}

		@Override
		public View getSectionHeaderView(int section, View convertView,
				ViewGroup parent) {
			Object o = getItem(section, 0);
			View view = convertView;
			if (view == null) {
				view = View.inflate(parent.getContext(),
						R.layout.section_list_header, null);
			}
			TextView tv = (TextView) view.findViewById(R.id.txtLabel);
			if (o instanceof Dept) {
				tv.setText("企业通讯录");
			} else if (o instanceof Group) {
				tv.setText("我的群组");
			} else if (o instanceof Contact) {
				tv.setText(((Contact) o).mFirstPinYin);
			}
			return view;
		}

		@Override
		public View getSectionItemView(int section, int position,
				View convertView, ViewGroup parent) {
			Object o = getItem(section, position);
			View view = convertView;
			if (view == null) {
				view = View.inflate(parent.getContext(),
						R.layout.list_cell_contact, null);
			}
			TextView tv = (TextView) view.findViewById(R.id.tvName);
			ImageView iv = (ImageView) view.findViewById(R.id.ivIcon);
			ImageLoader il = ImageLoader.getInstance();
			if (o instanceof Dept) {
				Dept d = (Dept) o;
				if (d.img.length() > 0) {
					il.displayImage(d.img, iv, Config.getDisplayOptions());
				}
				tv.setText(d.name);
			} else if (o instanceof Group) {
				Group g = (Group) o;
				if (g.img.length() > 0) {
					il.displayImage(g.img, iv, Config.getDisplayOptions());
				}
				tv.setText(g.name);
			} else if (o instanceof Contact) {
				Contact c = (Contact) o;
				if (c.head.length() > 0) {
					il.displayImage(c.head, iv);
				} else {
					iv.setImageResource(R.drawable.ic_contact_p);
				}
				tv.setText(c.name);
			}
			view.setTag(o);
			return view;
		}

		@Override
		public Object getItem(int section, int position) {
			Object object = viewList.get(section);
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) object;
			return list.get(position);
		}

		@Override
		public String getSectionLabel(int section) {
			FirstPinYin o = (FirstPinYin) getItem(section, 0);
			return o.mFirstPinYin;
		}
	};

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutContact = inflater.inflate(R.layout.frag_contact, container,
				false);
		etSearch = (EditText) layoutContact.findViewById(R.id.etSearch);
		ivSearch = (ImageView) layoutContact.findViewById(R.id.ivSearch);
		tvSearch = (TextView) layoutContact.findViewById(R.id.tvSearch);
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				Logger.i("afterTextChanged", s.toString());
				if (s.toString().length() > 0) {
					ivSearch.setVisibility(View.GONE);
					tvSearch.setVisibility(View.GONE);
					viewList.clear();
					viewList.addAll(getFiltedList(tmpList, s.toString()));
					adapter.notifyDataSetChanged();
				} else {
					ivSearch.setVisibility(View.VISIBLE);
					tvSearch.setVisibility(View.VISIBLE);
					viewList.clear();
					viewList.addAll(tmpList);
					adapter.notifyDataSetChanged();
				}
			}
		});

		lBack = layoutContact.findViewById(R.id.lBack);

		slvContact = (SectionListView) layoutContact
				.findViewById(R.id.slvContact);
		viewList = new ArrayList<Object>();
		groupList = new ArrayList<Object>();
		uList = new ArrayList<Object>();
		tmpList = new ArrayList<Object>();
		slvContact.setAdapter(adapter);
		slvContact.setOnSectionItemClickedListener(this);
		getDept(Account.getInstance().getUserId(), "");

		ibAdd = (ImageButton) layoutContact.findViewById(R.id.ibAdd);
		ibAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContactAddPopupWindow capw = new ContactAddPopupWindow(v.getContext(), v);
				capw.showAsDropDown();
			}
		});

		ImageButton ibMore = (ImageButton) layoutContact
				.findViewById(R.id.ibMore);
		ibMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MorePopupWindow mpw = new MorePopupWindow(v.getContext(), v);
				mpw.showAsDropDown();
			}
		});

		return layoutContact;
	}

	@SuppressWarnings("unchecked")
	private List<List<Object>> getFiltedList(List<?> list, String str) {
		List<List<Object>> tList = new ArrayList<List<Object>>();
		for (Object o : list) {
			List<Object> ll = new ArrayList<Object>();
			List<Object> oList = (List<Object>) o;
			for (Object oo : oList) {
				ll.add(oo);
			}
			tList.add(ll);
		}
		for (int i = tList.size() - 1; i >= 0; i--) {
			List<Object> l = tList.get(i);
			for (int j = l.size() - 1; j >= 0; j--) {
				Object o = l.get(j);
				if (o instanceof Dept) {
					if (!((Dept) o).name.contains(str)) {
						l.remove(o);
					}
				} else if (o instanceof Group) {
					if (!((Group) o).name.contains(str)) {
						l.remove(o);
					}
				} else if (o instanceof Contact) {
					if (!((Contact) o).name.contains(str)) {
						l.remove(o);
					}
				}
			}
			if (l.size() == 0) {
				tList.remove(l);
			}
		}
		return tList;
	}

	public void getDept(String userId, String deptId) {
		Dept tmp = Dept.fromDataById(this.getActivity(), "");
		if (tmp != null) {
			clearData();
			dept = tmp;
			syncData();
			return;
		}
		String code = deptId.length() > 0 ? "&deptId=" + deptId : "";
		String url = Config.SERVER_HOST + "contact.json" + "?userId=" + userId
				+ code;
		new HttpConnection().get(url, new HttpConnection.CallbackListener() {
			@Override
			public void callBack(String result) {
				if (result != "fail") {
					try {
						if (Intepreter.getCommonStatusFromJson(result).statusCode == 0) {
							clearData();
							dept = Dept.fromJSON(new JSONObject(result));
							groupList.addAll(Group
									.getGroupListFromJSON(new JSONObject(result)));
							uList.addAll(Contact
									.getUListFromJSON(new JSONObject(result)));
							Dept.updateData(getActivity(), dept);
							syncData();
						}
					} catch (Exception e) {
						if (Logger.DEBUG) {
							e.printStackTrace();
						}
						show(Config.ERROR_INTERFACE);
					}
				} else {
					show(Config.ERROR_NETWORK);
				}

			}
		});
	}

	private void clearData() {
		tmpList.clear();
		viewList.clear();
		groupList.clear();
		uList.clear();
	}

	private boolean isRootList(Dept d) {
		return d.parent == null;
	}

	@Override
	public void onSectionItemClicked(SectionListView listView, View view,
			int section, int position) {
		Object o = view.getTag();
		if (o instanceof Dept) {
			// 组织
			Dept d = (Dept) o;
			if (d.id.equals("906")) {
				Intent intent = new Intent(view.getContext(),
						PhoneContactActivity.class);
				startActivity(intent);
			} else {
				clearData();
				dept = d;
				syncData();
			}
		} else {
			// 个人信息
		}
	}

	private void show(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}

}
