package com.longersec.blj.dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.longersec.blj.domain.DeviceType;

public interface DeviceTypeDao {

	public boolean editDeviceType(DeviceType deviceType);

	public boolean addDeviceType(DeviceType deviceType);

	public boolean delDeviceType(List<Integer> ids);

	public List<Object> findAll(@Param("deviceType")DeviceType deviceType, @Param("page_start")int page_start, @Param("page_length")int page_length);

    public ArrayList<DeviceType> listType();

	DeviceType checkname(String name);

	String checknameById(Integer id);

	int checDeviceType(@Param("name") String name);

}
