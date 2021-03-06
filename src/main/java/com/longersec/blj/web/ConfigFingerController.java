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
import com.longersec.blj.domain.ConfigFinger;
import com.longersec.blj.service.ConfigFingerService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 
 */
@Controller
@RequestMapping(value = "/configFinger")
public class ConfigFingerController {

	@Autowired
	private ConfigFingerService configFingerService;

	@RequestMapping("/listConfigFinger")
	@ResponseBody
	public JSONObject listConfigFinger(ConfigFinger configFinger,HttpServletRequest request, HttpSession session) {
		int page_start = Integer.parseInt(request.getParameter("start"));
		int page_length = Integer.parseInt(request.getParameter("length"));
		ArrayList<Object> resultConfigFingers = new ArrayList<Object>();
		ArrayList<ConfigFinger> configFingers = new ArrayList<ConfigFinger>();
		long total = 0;
		resultConfigFingers = (ArrayList<Object>)configFingerService.findAll(configFinger, page_start, page_length);
		if(CollectionUtils.isNotEmpty(resultConfigFingers)) {
			configFingers = (ArrayList<ConfigFinger>)resultConfigFingers.get(0);
			total = ((ArrayList<Long>) resultConfigFingers.get(1)).get(0);
		}
		JSONArray jsonArray = JSONArray.fromObject(configFingers);
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		result.accumulate("recordsTotal", total);
		result.accumulate("recordsFiltered", total);
		result.accumulate("data", jsonArray);
		return result;
	}

	@RequestMapping("/addConfigFinger")
	@ResponseBody
	public JSONObject addConfigFinger(ConfigFinger configFinger, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(result.getBoolean("success")) {
			Boolean r = configFingerService.addConfigFinger(configFinger);
			result.accumulate("success", r?true:false);
		}
		return result;
	}

	@RequestMapping("/editConfigFinger")
	@ResponseBody
	public JSONObject editConfigFinger(ConfigFinger configFinger, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(result.getBoolean("success")) {
			Boolean r = configFingerService.editConfigFinger(configFinger);
			result.accumulate("success", r?true:false);
		}
		return result;
	}

	@RequestMapping("/delConfigFinger")
	@ResponseBody
	public JSONObject delConfigFinger(@RequestParam(value = "ids[]") Integer[] ids, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<Integer> _ids =  Arrays.asList(ids);
		result.accumulate("success", true);
		if(_ids.isEmpty()) {
			result.accumulate("success", false);
			result.accumulate("msg", "id不能为空");
		}
		if(result.getBoolean("success")) {
			Boolean r = configFingerService.delConfigFinger(_ids);
			result.accumulate("success", r);
		}
		return result;
	}
}
