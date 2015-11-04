package com.accenture.datongoaii;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

@SuppressWarnings("unused")
public class Config {

    public static final boolean DEBUG_AUTO_LOGIN = true;

    // Host & Url
//    public static final String SERVER_HOST = "http://192.168.1.104:8888/oaf/api";
    public static final String SERVER_HOST = "http://10.202.211.100:9999/oaf/api";
//    public static final String SERVER_HOST = "http://183.203.132.158:8086/oaf/api";
    //    public static final String SERVER_HOST = "http://10.202.210.178:8080/oaf/api";
//    public static final String SERVER_HOST = "http://120.24.73.78:8086/oaf/api";
    public static final String URL_LOGIN = "/authentication";
    public static final String URL_AUTO_LOGIN = "/authentication/{token}";
    public static final String URL_LOGOUT = "/logout";
    public static final String URL_REGISTER = "/users";
    public static final String URL_REQUIRE_VERIFY_CODE = "/captcha/";
    public static final String URL_UPLOAD_HEAD = "/user/photo";
    public static final String URL_MODIFY_USER_INFO = "/user/{userId}";
    public static final String URL_VERIFICATION = "/captcha/verification";
    public static final String URL_CHANGE_PASSWORD = "/user/password";
    public static final String URL_TODO_LIST = "/DTOAII/todo.json";
    public static final String URL_NOTI_LIST = "/DTOAII/noti.json";
    public static final String URL_ORG = "/groups/{userId}";
    public static final String URL_DEPT = "/group/{groupId}/subgroups";
    public static final String URL_CREATE_DEPT = "/groups";
    public static final String URL_CREATE_ORG = "/groups";
    public static final String URL_DELETE_ORG = "/group/{groupId}";
    public static final String URL_GET_USER_STATUS = "/users/status";
    public static final String URL_ADD_FRIEND = "/user/friends";
    public static final String URL_DELETE_FRIEND = "/user/{userId}/friend/{friendUserId}";
    public static final String URL_GET_CONTACTS = "/user/{userId}/contacts";
    public static final String URL_ADD_DEPT_USER = "/group/{groupId}/users";
    public static final String URL_DELETE_DEPT = "/group/{groupId}";
    public static final String URL_MODIFY_DEPT = "/group/{groupId}";
    public static final String URL_MODIFY_USER = "/user/{userId}";
    public static final String URL_DELETE_USER = "/group/{groupId}/user/{userId}";
    public static final String URL_GET_USER_PARENT = "/user/{userId}/groups?rootGroupId={rootGroupId}";
    public static final String URL_INVITE_FRIEND = "/user/invited_friends/{cell}";
    public static final String URL_GET_USER_BY_IMID = "/user/{imId}";
    public static final String URL_GET_USERS_BY_IMIDS = "/users/search";
    public static final String URL_CREATE_GROUP = "/chatgroups";
    public static final String URL_GET_GROUPS = "/member/{imId}/joined_chatgroups";
    public static final String URL_GET_GROUP = "/chatgroup/{chatGroupId}";
    public static final String URL_GROUP_INVITE_MEMBER = "/chatgroup/{groupId}/members";
    public static final String URL_QUIT_GROUP = "/chatgroup/{groupId}/member/{imId}";
    public static final String URL_DISMISS_GROUP = "/chatgroup/{groupId}";
    public static final String URL_RENAME_GROUP = "/chatgroup/{chatGroupId}";
    public static final String URL_GET_GROUPS_BY_IDS = "/chatgroups/{chatGroupIds}";
    public static final String URL_GET_TODO_URL = "/template/queryList.html?roleId={userId}";
    public static final String URL_GET_APPS_BY_USER_ID = "/user/{userId}/apps";

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
    public static final String SUCCESS_QUIT = "已退出";
    public static final String SUCCESS_INVITE = "已邀请";
    public static final String SUCCESS_DISMISS = "已解散";

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
    public static final String NOTE_CHOSEN_CONTACT = "已选择此联系人";
    public static final String NOTE_IM_GROUP_USER_EMPTY = "请选择群聊成员";
    public static final String NOTE_NO_MORE_MESSAGE = "没有更多的消息了";
    public static final String NOTE_NO_STORAGE_CARD = "没有存储卡";
    public static final String NOTE_NO_LOCAL_PIC = "没有找到图片";
    public static final String NOTE_GROUP_REMOVED_BY_ADMIN = "你被群创建者从此群中移除";
    public static final String NOTE_GROUP_DISMISSED_BY_ADMIN = "当前群聊已被群创建者解散";
    public static final String NOTE_CONVERSATION_UPDATED = "您的会话有更新";

    public static final String NOTE_RECORD_FAIL_NO_STORAGE_CARD = "未检测到存储卡，无法录音";
    public static final String NOTE_RECORD_SLIDE_UP_CANCEL = "手指上滑，取消发送";
    public static final String NOTE_RECORD_FAIL = "录音失败";
    public static final String NOTE_RECORD_RELEASE_CANCEL = "松开手指，取消发送";
    public static final String NOTE_RECORD_NO_PERMISSION = "没有录音权限";
    public static final String NOTE_RECORD_TOO_SHORT = "录音时间太短";
    public static final String NOTE_RECORD_SEND_FAIL = "发送失败，请检测服务器是否连接";
    public static final String NOTE_RECORD_DOWNLOADING = "正在下载语音，稍后点击";

    public static final String ERROR_NETWORK = "请检查网络连接";
    public static final String ERROR_INTERFACE = "数据解析失败，请联系应用提供商";
    public static final String ERROR_APP = "程序出错";
    public static final String ERROR_IM = "聊天模块出错，您可能无法正常使用聊天功能";
    public static final String ERROR_CAMERA_INIT_FAIL = "摄像头访问失败";
    public static final String ERROR_SCAN_BARCODE = "请扫描本应用提供的二维码";

    public static final String PROGRESS_LOGIN = "正在登录...";
    public static final String PROGRESS_LOGOUT = "正在退出登录...";
    public static final String PROGRESS_REGISTER = "正在注册...";
    public static final String PROGRESS_Q_QUESTION = "正在获取安全问题...";
    public static final String PROGRESS_SEND = "正在发送...";
    public static final String PROGRESS_GET = "正在获取...";
    public static final String PROGRESS_SUBMIT = "正在提交...";
    public static final String PROGRESS_QUIT = "正在退出...";
    public static final String PROGRESS_DOWNLOADING_PICTURE = "正在下载图片...";

    public static final String ALERT_DELETE_ORG = "您确认删除该组织吗？";
    public static final String ALERT_DELETE_DEPT = "您确认删除该部门吗？";
    public static final String ALERT_DELETE_USER = "您确认删除该员工吗？";
    public static final String ALERT_PARENT_DEPT = "您确认更改父部门为 {} 吗？";
    public static final String ALERT_SWITCH_ACCOUNT = "您确认要切换账户登录吗？";
    public static final String ALERT_QUIT_GROUP = "您确认退出该群吗？";
    public static final String ALERT_DISMISS_GROUP = "您确认解散该群吗？";
    public static final String ALERT_RENAME_GROUP = "更改群名称为：";
    public static final String ALERT_KICK_MEMBER = "您确认从该群移除用户 {} 吗？";
    public static final String ALERT_DELETE_FRIEND = "删除该好友吗？";

    //设置
    public static final int HTTP_CONNECTION_EXPIRE_SECONDS = 120;
    public static final int GET_VERIFY_CODE_INVALID_SECONDS = 60;

    public static DisplayImageOptions getDisplayOptions() {
        return new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.bird)
                .showImageOnFail(R.drawable.bird)
                .showImageOnLoading(R.drawable.bird).cacheOnDisk(true)
                .build();
    }

}
