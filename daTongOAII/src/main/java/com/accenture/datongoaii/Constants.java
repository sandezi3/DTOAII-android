package com.accenture.datongoaii;

public class Constants {

    // Bundle Tag
    public static final String BUNDLE_TAG_SELECT_QUESTION = "model.SelectQuestion";
    public static final String BUNDLE_TAG_FUNCTION = "function.register.or.forgetPswd";
    public static final String BUNDLE_TAG_SELECT_MEMBER = "function.selectMember";
    public static final String BUNDLE_TAG_FRIENDS = "bundle.friends";
    public static final String BUNDLE_TAG_CREATE_DEPT = "bundle.create.dept";
    public static final String BUNDLE_TAG_SELECT_DEPT = "bundle.select.dept";
    public static final String BUNDLE_TAG_GET_DEPT_DEPT_ID = "bundle.get.dept.deptId";
    public static final String BUNDLE_TAG_GET_DEPT_DEPT_NAME = "bundle.get.dept.orgName";
    public static final String BUNDLE_TAG_ORG_IS_MANAGE_MODE = "bundle.manage.org.isManagerMode";
    public static final String BUNDLE_TAG_CREATE_DEPT_DEPT_ID = "bundle.create.dept.deptId";
    public static final String BUNDLE_TAG_CREATE_DEPT_DEPT_NAME = "bundle.create.dept.orgName";
    public static final String BUNDLE_TAG_SELECT_DEPT_MULTI_MODE = "bundle.select.dept.multi.mode";
    public static final String BUNDLE_TAG_ADD_DEPT_USER = "bundle.manage.dept.add.user";
    public static final String BUNDLE_TAG_SELECT_USER_NAME = "bundle.select.user.name";
    public static final String BUNDLE_TAG_SELECT_USER_CELL = "bundle.select.user.cell";
    public static final String BUNDLE_TAG_SELECT_USER_ID = "bundle.select.user.id";
    public static final String BUNDLE_TAG_SELECT_USER_MULTI_MODE = "bundle.select.user.multi.mode";
    public static final String BUNDLE_TAG_SELECT_USER_MULTI_MODE_RESULT = "bundle.select.user.multi.mode.result";
    public static final String BUNDLE_TAG_MANAGE_DEPT = "bundle.manage.dept";
    public static final String BUNDLE_TAG_MANAGE_DEPT_SELECT_PARENT = "bundle.manage.dept.select.parent.list";
    public static final String BUNDLE_TAG_SELECT_PHONE_CONTACT = "bundle.select.phone.contact";
    public static final String BUNDLE_TAG_MANAGE_USER = "bundle.manage.user";
    public static final String BUNDLE_TAG_MANAGE_USER_DEPT = "bundle.manage.user.dept";
    public static final String BUNDLE_TAG_PARENT_DEPT_INVALID = "bundle.manage.dept.parent.invalid";
    public static final String BUNDLE_TAG_CONTACT_PROFILE = "bundle.contact.profile";
    public static final String BUNDLE_TAG_GROUP_PROFILE = "bundle.group.profile";
    public static final String BUNDLE_TAG_CONTACT_PROFILE_IS_FROM_MY_FRIENDS = "bundle.contact.profile.is.from.my.friends";
    public static final String BUNDLE_TAG_APP = "bundle.app.url";
    public static final String BUNDLE_TAG_RENAME_DEPT_NEW_NAME = "bundle.rename.dept.new.name";

    // Request Code
    public static final int REQUEST_CODE_SELECT_QUESTION = 1002;
    public static final int REQUEST_CODE_FORGET_PW_LOGIN = 1003;
    public static final int REQUEST_CODE_REGISTER = 1004;
    public static final int REQUEST_CODE_CREATE_ORG = 1005;
    public static final int REQUEST_CODE_SELECT_DEPT = 1006;
    public static final int REQUEST_CODE_CREATE_DEPT = 1007;
    public static final int REQUEST_CODE_CHANGE_ORG_NAME = 1008;
    public static final int REQUEST_CODE_MANAGE_ORG = 1009;
    public static final int REQUEST_CODE_SELECT_USER = 1010;
    public static final int REQUEST_CODE_ADD_DEPT_USER = 1011;
    public static final int REQUEST_CODE_MANAGE_DEPT = 1012;
    public static final int REQUEST_CODE_CHANGE_DEPT_PARENT = 1013;
    public static final int REQUEST_CODE_MANAGE_USER = 1014;
    public static final int REQUEST_CODE_CREATE_GROUP = 1015;
    public static final int REQUEST_CODE_MANAGE_GROUP = 1016;
    public static final int REQUEST_CODE_GROUP_INVITE_MEMBER = 1017;
    public static final int REQUEST_CODE_CAMERA = 1018;
    public static final int REQUEST_CODE_LOCAL = 1019;
    public static final int REQUEST_CODE_DELETE_FRIEND = 1020;

    // Function Tag
    public static final int FUNCTION_TAG_REGISTER = 0;
    public static final int FUNCTION_TAG_FORGET_PASSWORD = 1;
    public static final int FUNCTION_TAG_CREATE_GROUP = 0;
    public static final int FUNCTION_TAG_CREATE_ORG = 1;
    public static final int FUNCTION_TAG_CREATE_DEPT = 2;

    // Handler Tag
    public static final int HANDLER_TAG_DISMISS_PROGRESS_DIALOG = 0;

    // 推送通知
    public static final String PUSH_DATA_TAG = "Push-Data";
    public static final String PUSH_JSON_COMMAND = "command";
    public static final int PUSH_COMMAND_REFRESH_TODO = 10000;
    public static final int PUSH_COMMAND_REFRESH_NOTI = 10001;
    public static final int PUSH_COMMAND_REFRESH_CONTACT = 10002;
    public static final String PUSH_JSON_REFRESH_CONTACT_DEPT_ID = "DeptId";

    // 透传命令
    public static final String BROADCAST_CMD = "cmd.broadcast";
    public static final String BROADCAST_CMD_PREFIX_TAG = "00cmd00";

    public static final String BROADCAST_CMD_DELETE_FRIEND = "deleteUserFriend";
    public static final String BROADCAST_CMD_ADD_FRIEND = "addFriend";
    public static final String BROADCAST_CMD_ACCEPT_FRIEND = "acceptFriend";

    public static final String BROADCAST_CMD_GROUP_RENAME = "deleteUserFriend";
    public static final String BROADCAST_CMD_GROUP_MEMBER_REMOVED = "deleteUserFriend";
    public static final String BROADCAST_CMD_GROUP_DISMISS = "addFriend";
    public static final String BROADCAST_CMD_GROUP_ADD_MEMBER = "acceptFriend";

}
