package com.longersec.blj.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.longersec.blj.domain.CmdPolicyCmdgroup;
import com.longersec.blj.domain.DTO.Cmd;
import com.longersec.blj.domain.DTO.Users;
import com.longersec.blj.service.CmdPolicyCmdgroupService;
import com.longersec.blj.service.CmdgroupService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 
 */
@Controller
@RequestMapping(value = "/cmdPolicyCmd")
public class CmdPolicyCmdgroupController {

	@Autowired
	private CmdPolicyCmdgroupService cmdPolicyCmdService;

	@Autowired
	private CmdgroupService cmdgroupService;
	
	@RequestMapping("/listCmdPolicyCmd")
	@ResponseBody
	public JSONObject listCmdPolicyCmd(CmdPolicyCmdgroup cmdPolicyCmd,HttpServletRequest request, HttpSession session) {
		int page_start = Integer.parseInt(request.getParameter("start"));
		int page_length = Integer.parseInt(request.getParameter("length"));
		ArrayList<Object> resultCmdPolicyCmds = new ArrayList<Object>();
		ArrayList<CmdPolicyCmdgroup> cmdPolicyCmds = new ArrayList<CmdPolicyCmdgroup>();
		long total = 0;
		resultCmdPolicyCmds = (ArrayList<Object>)cmdPolicyCmdService.findAll(cmdPolicyCmd, page_start, page_length);
		if(CollectionUtils.isNotEmpty(resultCmdPolicyCmds)) {
			cmdPolicyCmds = (ArrayList<CmdPolicyCmdgroup>)resultCmdPolicyCmds.get(0);
			total = ((ArrayList<Long>) resultCmdPolicyCmds.get(1)).get(0);
		}
		JSONArray jsonArray = JSONArray.fromObject(cmdPolicyCmds);
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		result.accumulate("recordsTotal", total);
		result.accumulate("recordsFiltered", total);
		result.accumulate("data", jsonArray);
		return result;
	}
	
	@RequestMapping("/findCmdPolicyCmdAndUser")
	@ResponseBody
	public JSONObject findCmdPolicyCmdAndUser(@RequestParam("policy_id") Integer policy_id) {
		ArrayList<Cmd> resultCmdPolicyCmd = new ArrayList<Cmd>();
		ArrayList<Cmd> resultCmd = new ArrayList<Cmd>();
		resultCmdPolicyCmd = (ArrayList<Cmd>) cmdPolicyCmdService.selectById(policy_id);
		resultCmd = (ArrayList<Cmd>) cmdgroupService.selectNameAndId();
		JSONObject result = new JSONObject();
		resultCmd.removeAll(resultCmdPolicyCmd);
		JSONArray jsonArray_p_cmd = JSONArray.fromObject(resultCmdPolicyCmd);
		JSONArray jsonArray_cmd = JSONArray.fromObject(resultCmd);

		result.accumulate("success", true);
		result.accumulate("data_cmd", jsonArray_cmd);
		result.accumulate("data_p_cmd", jsonArray_p_cmd);
		return result;
	}

	@RequestMapping("/addCmdPolicyCmd")
	@ResponseBody
	public JSONObject addCmdPolicyCmd(@RequestParam(value="policy_id") Integer policy_id,@RequestParam(value="cmdgroup[]") Integer _cmdgroup, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<Integer> cmdgroup = Arrays.asList(_cmdgroup);
		result.accumulate("success", true);
		if(result.getBoolean("success")) {
			Boolean r = cmdPolicyCmdService.addCmdPolicyCmdgroup(policy_id,cmdgroup);
			result.accumulate("success", r?true:false);
		}
		return result;
	}

	@RequestMapping("/editCmdPolicyCmd")
	@ResponseBody
	public JSONObject editCmdPolicyCmd(@RequestParam(value="policy_id") Integer policy_id,@RequestParam(value="cmdgroup[]",required = false) Integer[] _cmdgroup, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<Integer> cmdgroup =_cmdgroup==null?Arrays.asList(0):Arrays.asList(_cmdgroup);
		cmdPolicyCmdService.deleteBypolicy_id(policy_id);
		boolean r = cmdPolicyCmdService.editCmdPolicyCmdgroup(policy_id, cmdgroup);
		result.accumulate("success", r);
		return result;
	}

	@RequestMapping("/delCmdPolicyCmd")
	@ResponseBody
	public JSONObject delCmdPolicyCmd(@RequestParam(value = "ids[]") String[] ids, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<String> _ids =  Arrays.asList(ids);
		result.accumulate("success", true);
		if(_ids.isEmpty()) {
			result.accumulate("success", false);
			result.accumulate("msg", "id不能为空");
		}
		if(result.getBoolean("success")) {
			Boolean r = cmdPolicyCmdService.delCmdPolicyCmdgroup(_ids);
			result.accumulate("success", r);
		}
		return result;
	}
}
