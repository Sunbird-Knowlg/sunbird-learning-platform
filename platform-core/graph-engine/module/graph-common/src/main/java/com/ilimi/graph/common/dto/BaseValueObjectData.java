package com.ilimi.graph.common.dto;

public class BaseValueObjectData<T> extends BaseValueObject {

	private static final long serialVersionUID = 6344538393839099702L;

	private T data;
	
	public BaseValueObjectData() {
		
	}

	public BaseValueObjectData(T data) {
		super();
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
