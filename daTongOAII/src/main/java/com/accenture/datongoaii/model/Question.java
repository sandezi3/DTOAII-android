package com.accenture.datongoaii.model;

import com.accenture.datongoaii.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 3250514376233327053L;
    public String id;
    public String text;
    public boolean selected;

    public static Question fromJSON(JSONObject json) {
        Question q = new Question();
        try {
            q.id = json.getString("id");
            q.text = json.getString("text");
            q.selected = false;
        } catch (JSONException e) {
            Logger.e("Question", e.getMessage());
        }
        return q;
    }

    public static List<Question> getListFromJSON(JSONObject json) {
        List<Question> list = new ArrayList<Question>();
        try {
            JSONArray array = json.getJSONArray("questions");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                list.add(Question.fromJSON(obj));
            }
        } catch (JSONException e) {
            Logger.e("Question", e.getMessage());
        }
        return list;
    }
}
