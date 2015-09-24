package com.accenture.datongoaii.common;

import com.accenture.datongoaii.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

@SuppressWarnings("unused")
public class Config {

    public static final boolean DEBUG_AUTO_LOGIN = true;

    // Host & Url
    public static final String SERVER_HOST = "http://10.202.211.163:9999/oaf/api";
    //    public static final String SERVER_HOST = "http://10.202.211.175:8080/oaf/api";
//    public static final String SERVER_HOST = "http://192.168.22.107:8080/DTOAII/";
    public static final String URL_LOGIN = "/authentication";
    public static final String URL_AUTO_LOGIN = "/authentication/{token}";
    public static final String URL_REGISTER = "/users";
    public static final String URL_REQUIRE_VERIFY_CODE = "/captcha/";
    public static final String URL_UPLOAD_HEAD = "/user/photo";
    public static final String URL_VERIFICATION = "/captcha/verification";
    public static final String URL_CHANGE_PASSWORD = "/user/password";
    public static final String URL_TODO_LIST = "/DTOAII/todo.json";
    public static final String URL_NOTI_LIST = "/DTOAII/noti.json";
    public static final String URL_ORG = "/groups/{userId}";
    public static final String URL_DEPT = "/group/{groupId}/subgroups";
    public static final String URL_CREATE_GROUP = "/DTOAII/createGroup.json";
    public static final String URL_CREATE_DEPT = "/groups";
    public static final String URL_CREATE_ORG = "/groups";
    public static final String URL_DELETE_ORG = "/group/{groupId}";
    public static final String URL_GET_USER_STATUS = "/users/status";
    public static final String URL_ADD_FRIEND = "/user/friends";
    public static final String URL_GET_CONTACTS = "/user/{userId}/contacts";
    public static final String URL_ADD_DEPT_USER = "/group/{groupId}/users";
    public static final String URL_DELETE_DEPT = "/group/{groupId}";
    public static final String URL_MODIFY_DEPT = "/group/{groupId}";
    public static final String URL_MODIFY_USER = "/user/{userId}";
    public static final String URL_DELETE_USER = "/group/{groupId}/user/{userId}";
    public static final String URL_GET_USER_PARENT = "/user/{userId}/groups?rootGroupId={rootGroupId}";
    public static final String URL_INVITE_FRIEND = "";

    // GROUP TYPE
    public static final String GROUP_TYPE_TAG = "groupTypeId";

    public static final int GROUP_TYPE_COMPANY = 1;
    public static final int GROUP_TYPE_ORG = 2;
    public static final int GROUP_TYPE_ASSOCIATION = 3;
    public static final int GROUP_TYPE_GROUP = 4;
    public static final int GROUP_TYPE_DEPT = 5;

    // 提示文字
    public static final String SUCCESS_REGISTER = "注册成功，请登录";
    public static final String SUCCESS_LOGIN = "您已登录";
    public static final String SUCCESS_GET_VERIFY_CODE = "验证码已发送，请等待短信";
    public static final String SUCCESS_CREATE = "已创建";
    public static final String SUCCESS_ADD = "已添加";
    public static final String SUCCESS_DELETE = "已删除";
    public static final String SUCCESS_UPDATE = "已更新";

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
    public static final String NOTE_ORG_NAME_EMPTY = "请输入组织名称";
    public static final String NOTE_DEPT_NAME_EMPTY = "请输入部门名称";
    public static final String NOTE_SELECT_PARENT_DEPT = "请选择父部门";
    public static final String NOTE_DEPT_EMPTY = "该部门没有子部门和员工";
    public static final String NOTE_DEPT_PARENT_SELF = "不能设置父部门为本部门";

    public static final String ERROR_NETWORK = "请检查网络连接";
    public static final String ERROR_INTERFACE = "数据解析失败，请联系应用提供商";
    public static final String ERROR_APP = "程序出错";

    public static final String PROGRESS_LOGIN = "正在登录...";
    public static final String PROGRESS_REGISTER = "正在注册...";
    public static final String PROGRESS_Q_QUESTION = "正在获取安全问题...";
    public static final String PROGRESS_SEND = "正在发送...";
    public static final String PROGRESS_GET = "正在获取...";
    public static final String PROGRESS_SUBMIT = "正在提交...";

    public static final String ALERT_DELETE_ORG = "您确认删除该组织吗？";
    public static final String ALERT_DELETE_DEPT = "您确认删除该部门吗？";
    public static final String ALERT_DELETE_USER = "您确认删除该员工吗？";
    public static final String ALERT_PARENT_DEPT = "您确认更改父部门为{}吗？";

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
