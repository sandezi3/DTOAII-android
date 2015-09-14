package com.accenture.datongoaii.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.accenture.datongoaii.model.CommonResponse;
import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Noti;
import com.accenture.datongoaii.model.Todo;
import com.accenture.datongoaii.model.Jsons.*;

public class Intepreter {
    public static CommonResponse getCommonStatusFromJson(String json)
            throws JSONException {
        CommonResponse c = new CommonResponse();
        JSONObject obj = new JSONObject(json);
        if (obj.get(JsonCommonResponse.statusCode) instanceof String) {
            c.statusCode = Integer.parseInt((String) obj.get(JsonCommonResponse.statusCode));
        } else {
            c.statusCode = (Integer) obj.get(JsonCommonResponse.statusCode);
        }
        c.statusMsg = obj.getString(JsonCommonResponse.statusMsg);
        return c;
    }

    public static List<Todo> getTodoListFromJson(String json)
            throws JSONException {
        JSONArray todoArray = (new JSONObject(json)).getJSONObject("data")
                .getJSONArray(JsonTodoList.list);
        List<Todo> list = new ArrayList<Todo>();
        for (int i = 0; i < todoArray.length(); i++) {
            JSONObject obj = (JSONObject) todoArray.get(i);
            Todo t = new Todo();
            t.img = obj.getString(JsonTodoList.img);
            t.create = obj.getString(JsonTodoList.create);
            t.deadline = obj.getString(JsonTodoList.deadline);
            t.title = obj.getString(JsonTodoList.title);
            list.add(t);
        }
        return list;
    }

    public static List<Noti> getNotiListFromJson(String json)
            throws JSONException {
        JSONArray todoArray = (new JSONObject(json)).getJSONObject("data")
                .getJSONArray(JsonNotiList.list);
        List<Noti> list = new ArrayList<Noti>();
        for (int i = 0; i < todoArray.length(); i++) {
            JSONObject obj = (JSONObject) todoArray.get(i);
            Noti n = new Noti();
            n.img = obj.getString(JsonNotiList.img);
            n.create = obj.getString(JsonNotiList.create);
            n.deadline = obj.getString(JsonNotiList.deadline);
            n.title = obj.getString(JsonNotiList.title);
            list.add(n);
        }
        return list;
    }
}
