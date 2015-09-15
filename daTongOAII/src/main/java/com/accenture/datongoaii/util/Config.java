package com.accenture.datongoaii.util;

import com.accenture.datongoaii.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class Config {
    public static final boolean DEBUG_AUTO_LOGIN = true;

    // Host & Url
    public static final String SERVER_HOST = "http://10.202.211.163:9999/oaf/api";
    //	public static final String SERVER_HOST = "http://192.168.22.107:8080/DTOAII/";
    public static final String URL_LOGIN = "/authentication";
    public static final String URL_REGISTER = "/users";
    public static final String URL_REQUIRE_VERIFY_CODE = "/captcha/";
    public static final String URL_UPLOAD_HEAD = "/user/photo";
    public static final String URL_VERIFICATION = "/captcha/verification";
    public static final String URL_CHANGE_PASSWORD = "/user/password";
    public static final String URL_TODO_LIST = "/DTOAII/todo.json";
    public static final String URL_NOTI_LIST = "/DTOAII/noti.json";
    public static final String URL_DEPT = "/DTOAII/contact.json";
    public static final String URL_CREATE_GROUP = "/DTOAII/createGroup.json";
    public static final String URL_CREATE_DEPT = "/DTOAII/createDept.json";
    public static final String URL_CREATE_ORG = "/orgs";
    public static final String URL_GET_USER_STATUS = "/users/status";
    public static final String URL_ADD_FRIEND = "/user/friends";
    public static final String URL_GET_CONTACTS = "/user/{userId}/contacts";


    // 提示文字
    public static final String SUCCESS_REGISTER = "注册成功，请登录";
    public static final String SUCCESS_LOGIN = "您已登录";
    public static final String SUCCESS_GET_VERIFY_CODE = "验证码已发送，请等待短信";

    public static final String NOTE_USERNAME_EMPTY = "请输入用户名";
    public static final String NOTE_CELL_EMPTY = "请输入手机号码";
    public static final String NOTE_CELL_NUMBER = "手机号码必须是11位";
    public static final String NOTE_CELL_FORMAT = "手机号码格式错误";
    public static final String NOTE_PASSWORD_EMPTY = "请输入密码";
    public static final String NOTE_RPT_PASSWORD_EMPTY = "请输入确认密码";
    public static final String NOTE_PASSWORD_UNMATCH = "两次密码输入不符，请重试";
    public static final String NOTE_VERIFY_CODE_EMPTY = "请输入验证码";
    public static final String NOTE_SELECT_QUESTION = "请选择一个安全问题";
    public static final String NOTE_ANSWER_EMPTY = "请填写安全问题答案";

    public static final String ERROR_NETWORK = "请检查网络连接";
    public static final String ERROR_INTERFACE = "数据解析失败，请联系应用提供商";
    public static final String ERROR_APP = "程序出错";

    public static final String PROGRESS_LOGIN = "正在登录...";
    public static final String PROGRESS_REGISTER = "正在注册...";
    public static final String PROGRESS_Q_QUESTION = "正在获取安全问题...";
    public static final String PROGRESS_SEND = "正在发送...";
    public static final String PROGRESS_GET = "正在获取...";
    public static final String PROGRESS_SUBMIT = "正在提交...";

    //设置
    public static final int GET_VERIFY_CODE_INVALID_SECONDS = 60;

    public static DisplayImageOptions getDisplayOptions() {
        return new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_default)
                .showImageOnFail(R.drawable.ic_default)
                .showImageOnLoading(R.drawable.ic_default).cacheOnDisk(true)
                .build();
    }

}
