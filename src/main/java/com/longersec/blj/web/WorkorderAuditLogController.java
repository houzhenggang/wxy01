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
import com.longersec.blj.domain.WorkorderAuditLog;
import com.longersec.blj.service.WorkorderAuditLogService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 
 */
@Controller
@RequestMapping(value = "/workorderAuditLog")
public class WorkorderAuditLogController {

	@Autowired
	private WorkorderAuditLogService workorderAuditLogService;

	@RequestMapping("/listWorkorderAuditLog")
	@ResponseBody
	public JSONObject listWorkorderAuditLog(WorkorderAuditLog workorderAuditLog,HttpServletRequest request, HttpSession session) {
		int page_start = Integer.parseInt(request.getParameter("start"));
		int page_length = Integer.parseInt(request.getParameter("length"));
		ArrayList<Object> resultWorkorderAuditLogs = new ArrayList<Object>();
		ArrayList<WorkorderAuditLog> workorderAuditLogs = new ArrayList<WorkorderAuditLog>();
		long total = 0;
		resultWorkorderAuditLogs = (ArrayList<Object>)workorderAuditLogService.findAll(workorderAuditLog, page_start, page_length);
		if(CollectionUtils.isNotEmpty(resultWorkorderAuditLogs)) {
			workorderAuditLogs = (ArrayList<WorkorderAuditLog>)resultWorkorderAuditLogs.get(0);
			total = ((ArrayList<Long>) resultWorkorderAuditLogs.get(1)).get(0);
		}
		JSONArray jsonArray = JSONArray.fromObject(workorderAuditLogs);
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		result.accumulate("recordsTotal", total);
		result.accumulate("recordsFiltered", total);
		result.accumulate("data", jsonArray);
		return result;
	}

	@RequestMapping("/addWorkorderAuditLog")
	@ResponseBody
	public JSONObject addWorkorderAuditLog(WorkorderAuditLog workorderAuditLog, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(result.getBoolean("success")) {
			Boolean r = workorderAuditLogService.addWorkorderAuditLog(workorderAuditLog);
			result.accumulate("success", r?true:false);
		}
		return result;
	}

	@RequestMapping("/editWorkorderAuditLog")
	@ResponseBody
	public JSONObject editWorkorderAuditLog(WorkorderAuditLog workorderAuditLog, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(result.getBoolean("success")) {
			Boolean r = workorderAuditLogService.editWorkorderAuditLog(workorderAuditLog);
			result.accumulate("success", r?true:false);
		}
		return result;
	}

	@RequestMapping("/delWorkorderAuditLog")
	@ResponseBody
	public JSONObject delWorkorderAuditLog(@RequestParam(value = "ids[]") Integer[] ids, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<Integer> _ids =  Arrays.asList(ids);
		result.accumulate("success", true);
		if(_ids.isEmpty()) {
			result.accumulate("success", false);
			result.accumulate("msg", "id不能为空");
		}
		if(result.getBoolean("success")) {
			Boolean r = workorderAuditLogService.delWorkorderAuditLog(_ids);
			result.accumulate("success", r);
		}
		return result;
	}
}
