package com.accenture.datongoaii.model;

public class Jsons {
	public class JsonCommonResponse {
		public static final String statusCode = "statusCode";
		public static final String statusMsg = "statusMsg";
	}

	public final class JsonTodoList extends JsonCommonResponse {
		public static final String list = "list";
		public static final String img = "img";
		public static final String title = "title";
		public static final String deadline = "deadline";
		public static final String create = "create";
	}

	public final class JsonRegister extends JsonCommonResponse {
		public static final String userId = "userId";
		public static final String password = "password";
		public static final String code = "code";
		public static final String questionId = "questionId";
		public static final String answer = "answer";
	}

	public final class JsonNotiList extends JsonCommonResponse {
		public static final String list = "list";
		public static final String img = "img";
		public static final String title = "title";
		public static final String deadline = "deadline";
		public static final String create = "create";
	}
}
