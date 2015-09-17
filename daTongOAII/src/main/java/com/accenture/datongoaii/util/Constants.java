package com.accenture.datongoaii.util;

public class Constants {

    // Bundle Tag
    public static final String BUNDLE_TAG_SELECT_QUESTION = "model.SelectQuestion";
    public static final String BUNDLE_TAG_FUNCTION = "function.register.or.forgetPswd";
    public static final String BUNDLE_TAG_SELECT_MEMBER = "function.selectMember";
    public static final String BUNDLE_TAG_FRIENDS = "bundle.friends";
    public static final String BUNDLE_TAG_CREATE_DEPT = "bundle.create.dept";
    public static final String BUNDLE_TAG_SELECT_DEPT = "bundle.select.dept";
    public static final String BUNDLE_TAG_GET_DEPT_ORG_ID = "bundle.get.dept.orgId";
    public static final String BUNDLE_TAG_GET_DEPT_DEPT_ID = "bundle.get.dept.deptId";
    public static final String BUNDLE_TAG_GET_DEPT_DEPT_NAME = "bundle.get.dept.orgName";

    // Request Code
    public static final int REQUEST_CODE_SELECT_QUESTION = 1002;
    public static final int REQUEST_CODE_FORGET_PW_LOGIN = 1003;
    public static final int REQUEST_CODE_REGISTER = 1004;
    public static final int REQUEST_CODE_CREATE_ORG = 1005;
    public static final int REQUEST_CODE_SELECT_DEPT = 1006;
    public static final int REQUEST_CODE_CREATE_DEPT = 1007;
    public static final int REQUEST_CODE_CHANGE_ORG_NAME = 1008;
    public static final int REQUEST_CODE_MANAGE_ORG = 1009;

    // Function Tag
    public static final int FUNCTION_TAG_REGISTER = 0;
    public static final int FUNCTION_TAG_FORGET_PASSWORD = 1;
    public static final int FUNCTION_TAG_CREATE_GROUP = 0;
    public static final int FUNCTION_TAG_CREATE_ORG = 1;
    public static final int FUNCTION_TAG_CREATE_DEPT = 2;

    // 推送通知
    public static final String PUSH_DATA_TAG = "Push-Data";
    public static final String PUSH_JSON_COMMAND = "command";
    public static final int PUSH_COMMAND_REFRESH_TODO = 10000;
    public static final int PUSH_COMMAND_REFRESH_NOTI = 10001;
    public static final int PUSH_COMMAND_REFRESH_CONTACT = 10002;
    public static final String PUSH_JSON_REFRESH_CONTACT_DEPT_ID = "DeptId";


}
