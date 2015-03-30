package com.avatar.dto;

import java.lang.reflect.Type;

import com.avatar.dto.enums.ResponseStatus;

public class WsResponse<T> {
	private final ResponseStatus status;
	private final String statusMessage;
	private final T data;
	// For json serialization
	private transient Type dataType;
	private transient String dataName;

	public WsResponse(final ResponseStatus status, final String message,
			final T data) {
		this(status, message, data, "data");
	}

	public WsResponse(final ResponseStatus status, final String message,
			final T data, final String dataName) {
		this.status = status;
		this.statusMessage = message;
		this.data = data;
		this.dataName = dataName;
		this.dataType = null;
	}

	public WsResponse(final ResponseStatus status, final String message,
			final T data, final Type dataType, final String dataName) {
		this.status = status;
		this.statusMessage = message;
		this.data = data;
		this.dataType = dataType;
		this.dataName = dataName;
	}

	public T getData() {
		return data;
	}

	public String getDataName() {
		return dataName;
	}

	public Type getDataType() {
		return dataType;
	}

	public int getStatus() {
		return status.getStatus();
	}

	public String getStatusMessage() {
		return statusMessage;
	}

}
