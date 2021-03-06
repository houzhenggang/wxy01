package com.longersec.blj.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


public class Group {

	private Integer id;

	private Integer type;
    
	@Pattern(regexp = "[\\u4e00-\\u9fa5\\w]{1,128}",message = "名称格式不对")
	@NotNull(message = "名称不能为空")
	private String name;

	private String desc;

	private Integer count;

	private Integer creator_id;

	private Integer create_time;

	private Integer department;

	private String depart_name;

	private String searchAll;

	private String topName;

	public String getTopName1() {
		return topName1;
	}

	public void setTopName1(String topName1) {
		this.topName1 = topName1;
	}

	private String topName1;

	public String getTopName() {
		return topName;
	}

	public void setTopName(String topName) {
		this.topName = topName;
	}

	public Group() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getName() {
		return name;
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

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCreator_id() {
		return creator_id;
	}

	public void setCreator_id(Integer creator_id) {
		this.creator_id = creator_id;
	}

	public Integer getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Integer create_time) {
		this.create_time = create_time;
	}

	public Integer getDepartment() {
		return department;
	}

	public void setDepartment(Integer department) {
		this.department = department;
	}

	public String getDepart_name() {
		return depart_name;
	}

	public void setDepart_name(String depart_name) {
		this.depart_name = depart_name;
	}

	@Override
	public String toString() {
		return "Group{" +
				"id=" + id +
				", type=" + type +
				", name='" + name + '\'' +
				", desc='" + desc + '\'' +
				", count=" + count +
				", creator_id=" + creator_id +
				", create_time=" + create_time +
				", department=" + department +
				", depart_name='" + depart_name + '\'' +
				", searchAll='" + searchAll + '\'' +
				", topName='" + topName + '\'' +
				", topName1='" + topName1 + '\'' +
				'}';
	}

	public String getSearchAll() {
		return searchAll;
	}

	public void setSearchAll(String searchAll) {
		this.searchAll = searchAll;
	}

}
