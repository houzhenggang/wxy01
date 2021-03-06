package com.longersec.blj.dao;

import java.util.ArrayList;
import java.util.List;

import com.longersec.blj.domain.Device;
import org.apache.ibatis.annotations.Param;

import com.longersec.blj.domain.ApppubAccount;
import com.longersec.blj.domain.ApppubServer;
import com.longersec.blj.domain.Department;

public interface ApppubServerDao {


	public List<ApppubServer> selectAll(@Param("id") Integer id);

	public boolean editApppubServer(ApppubServer apppubServer);

	public boolean addApppubServer(ApppubServer apppubServer);

	public boolean delApppubServer(List<Integer> ids);

	boolean delUselessApppubServer(@Param("id") Integer id);

	public List<Object> findAll(@Param("apppubServer") ApppubServer apppubServer, @Param("sname") String sname, @Param("type") Integer type, @Param("page_start") int page_start, @Param("page_length") int page_length,@Param("depart_ids") List<Integer> depart_ids);

	public String selectName(@Param("id") Integer id, @Param("name") String name);

	public ApppubServer getById(@Param("id") Integer id);

	ApppubServer checkname(@Param("id") Integer id,@Param("name") String name);

	ApppubServer checkip(@Param("ip") String ip, @Param("id") Integer id);

	boolean insertMore(@Param("ArrayList") ArrayList<ApppubServer> listApppubServer);

	boolean editApppubServerList(@Param("ArrayList") ArrayList<ApppubServer> updatelistApppubServer);

	public ApppubServer getApppubServerByName (@Param("name") String name);
}
