package com.avatar.dto.survey;

public class SurveyAnswer {
	private Integer id; // pk
	private Integer answerA;
	private Integer answerB;
	private Survey survey;

	public Integer getAnswerA() {
		return answerA;
	}

	public Integer getAnswerB() {
		return answerB;
	}

	public Integer getId() {
		return id;
	}

	public Survey getSurvey() {
		return survey;
	}

	public void setAnswerA(final Integer answerA) {
		this.answerA = answerA;
	}

	public void setAnswerB(final Integer answerB) {
		this.answerB = answerB;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setSurvey(final Survey survey) {
		this.survey = survey;
	}

}
