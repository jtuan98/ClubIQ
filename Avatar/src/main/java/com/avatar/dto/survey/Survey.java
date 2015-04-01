package com.avatar.dto.survey;

public class Survey {
	private Integer id; // pk
	private String questionA;
	private String questionB;

	public Survey() {
	}

	public Survey(final Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public String getQuestionA() {
		return questionA;
	}

	public String getQuestionB() {
		return questionB;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setQuestionA(final String questionA) {
		this.questionA = questionA;
	}

	public void setQuestionB(final String questionB) {
		this.questionB = questionB;
	}

}
