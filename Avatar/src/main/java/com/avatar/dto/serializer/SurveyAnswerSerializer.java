package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import com.avatar.dto.WsResponse;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.util.GSonBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SurveyAnswerSerializer implements JsonSerializer<SurveyAnswer> {
	public static void main(final String[] args) {
		final GSonBuilderUtil builder = new GSonBuilderUtil();
		builder.register(WsResponse.class, new WsResponseSerializer());
		builder.register(SurveyAnswer.class, new SurveyAnswerSerializer());
		final Gson gson = builder.getGson();
		final SurveyAnswer answer = new SurveyAnswer();
		final Survey survey = new Survey();
		survey.setId(1);
		survey.setQuestionA("Question A?");
		survey.setQuestionB("Question B?");
		answer.setId(123);
		answer.setSurvey(survey);
		final WsResponse<SurveyAnswer> data = new WsResponse<SurveyAnswer>(
				ResponseStatus.success, "", answer);
		System.out.println(gson.toJson(data));

	}

	@Override
	public JsonElement serialize(final SurveyAnswer answer, final Type type,
			final JsonSerializationContext context) {
		if (answer != null) {
			final JsonObject result = new JsonObject();
			result.add("surveyAnswerId", new JsonPrimitive(answer.getId()));
			result.add("id", new JsonPrimitive(answer.getSurvey().getId()));
			result.add("questionA", new JsonPrimitive(answer.getSurvey()
					.getQuestionA()));
			result.add("questionB", new JsonPrimitive(answer.getSurvey()
					.getQuestionB()));
			return result;
		}
		return null;
	}

}
