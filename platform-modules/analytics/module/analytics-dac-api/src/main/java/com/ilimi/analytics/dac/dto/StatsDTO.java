package com.ilimi.analytics.dac.dto;

import java.io.Serializable;
import java.text.DecimalFormat;

public class StatsDTO implements Serializable {

	private static final long serialVersionUID = 7374039398327793271L;

	private String name;
	private String desc;
	private String value;

	public String getName() {
		return name;
	}

	public StatsDTO(String name, String desc, Float value) {
		super();
		DecimalFormat df = new DecimalFormat("###.##");
		this.name = name;
		this.desc = desc;
		this.value = df.format(value);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatsDTO [name=");
		builder.append(name);
		builder.append(", desc=");
		builder.append(desc);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

}
