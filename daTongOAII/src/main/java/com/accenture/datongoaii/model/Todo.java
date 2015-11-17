package com.accenture.datongoaii.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Todo implements Serializable {
//    workItemId: [string],  //待办id
//    caseKey : [string],  //事件标识
//    caseInstanceId : [string],  //事件实例id
//    fromTaskId : [string],   //上一步待办的id
//    operationId : [string],   //处理待办时点击的按钮id
//    operationName : [string],  //处理待办时点击的按钮的中文名字
//    operationCode : [string],   //处理待办时点击的按钮的英文名字
//    caseStatus : [string],    //事件的状态
//    caseStatusName : [string],   //事件状态名字
//    roleId : [int],  //角色id
//    userId : [int],   //用户id
//    title : [int],    //待办标题
//    taskName :[string],  //任务名
//    taskKey : [string],    //任务标识
//    startTime : [long],   //待办开始时间
//    endTime : [long],     //待办结束时间
//    comment : [string],   //处理意见
//    duration : [long],     //待办耗时
//    completeDate : [long],  //希望待办完成的时间
//    url:[string]   //待办对应的链接

    //    public String workItemId;
//    public String caseKey;
//    public String caseInstanceId;
//    public String fromTaskId;
//    public Integer roleId;
//    public Integer userId;
    public String title;
    //    public String taskName;
//    public String taskKey;
//    public String icon;
    public Long startTime;
    public String url;

    public static List<Todo> listFromJSON(JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        JSONArray array = json.getJSONArray("workItems");
        if (array == null) {
            return null;
        }
        List<Todo> list = new ArrayList<Todo>();
        if (array.length() == 0) {
            return list;
        }
        for (int i = 0; i < array.length(); i++) {
            Todo todo = fromJSON(array.getJSONObject(i));
            list.add(todo);
        }
        return list;
    }

    private static Todo fromJSON(JSONObject json) throws JSONException {
        Todo todo = new Todo();
        todo.title = json.getString("title");
        todo.startTime = json.getLong("startTime");
        todo.url = json.getString("url");
        return todo;
    }
}
