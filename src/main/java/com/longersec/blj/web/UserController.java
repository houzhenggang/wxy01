package com.longersec.blj.web;

import com.longersec.blj.domain.ConfigFinger;
import com.longersec.blj.domain.ConfigLogin;
import com.longersec.blj.domain.OperatorLog;
import com.longersec.blj.domain.User;
import com.longersec.blj.domain.UserPasswordLog;
import com.longersec.blj.domain.ConfigLogin;
import com.longersec.blj.domain.OperatorLog;
import com.longersec.blj.domain.User;
import com.longersec.blj.domain.UserPasswordLog;
import com.longersec.blj.domain.*;
import com.longersec.blj.service.*;
import com.longersec.blj.utils.GoogleAuthenticatorUtil;
import com.longersec.blj.utils.Operator_log;
import com.longersec.blj.utils.Sm4Utils;
import com.longersec.blj.utils.UpdateDepartmentCount;
import com.longersec.blj.utils.Validator;
import com.longersec.blj.utils.httpClient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	private UserGroupUserService userGroupUserService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private UserPasswordLogService userPasswordLogService;
	
	@Autowired
	private ConfigLoginService configLoginService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private OperatorLogService operatorLogService;

	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private ConfigFingerService configFingerService;
	
	@Autowired
	private ConfigPasswordEncryptKeyService configPasswordEncryptKeyService;

	@RequestMapping("/listUser")
	@ResponseBody
	public JSONObject listUser(User user,HttpServletRequest request, HttpSession session) {
		int page_start = Integer.parseInt(request.getParameter("start"));
		int page_length = Integer.parseInt(request.getParameter("length"));
		User p_user = (User) SecurityUtils.getSubject().getPrincipal();
		List<Integer> depart_ids = new ArrayList<>();
		if (p_user.getRole_id().equals(5)){
			//获取所在的部门
			int depart_id = p_user.getDepartment();
			depart_ids = departmentService.selectById(depart_id);
			depart_ids.add(p_user.getDepartment());
		}
		ArrayList<Object> resultUsers = new ArrayList<Object>();
		ArrayList<User> users = new ArrayList<User>();
		long total = 0;
		User _user = user;
		if(user.getSearchall() != null && "".equals(user.getSearchall())) {
			user.setSearchall(null);
		}
		resultUsers = (ArrayList<Object>)userService.findAll(user, page_start, page_length,depart_ids);
		
		if(CollectionUtils.isNotEmpty(resultUsers)) {
			users = (ArrayList<User>)resultUsers.get(0);
			total = ((ArrayList<Long>) resultUsers.get(1)).get(0);
		}
		for (User user1:users) {
			if (user1.getDepartment() != 0) {
				List<String> allParentName = departmentService.findAllParentName(user1.getDepartment());
				StringBuilder stringBuilder = new StringBuilder();
				for (Object strings : allParentName) {
					stringBuilder.append(strings).append("/");
				}
				user1.setTopName(stringBuilder.substring(0,stringBuilder.length()>1?stringBuilder.length()-1:stringBuilder.length()));
			}
			

		}
		JSONArray jsonArray = JSONArray.fromObject(users);
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		result.accumulate("recordsTotal", total);
		result.accumulate("recordsFiltered", total);
		result.accumulate("data", jsonArray);
		return result;
	}

	@RequestMapping("/addUser")
	@ResponseBody
	public JSONObject addUser(@Validated User user, BindingResult errorResult ,HttpServletRequest request, HttpSession session) {
		User p_user = (User) SecurityUtils.getSubject().getPrincipal();
		user.setCreator_id(p_user.getId());
		user.setCreate_time((int) System.currentTimeMillis());
		return this.editUser(user, errorResult, request, session);
	}
	
	public SOAPMessage formatSoapString(String soapString) {
        MessageFactory msgFactory;
        try {
            msgFactory = MessageFactory.newInstance();
            SOAPMessage reqMsg = msgFactory.createMessage(new MimeHeaders(),
                    new ByteArrayInputStream(soapString.getBytes("UTF-8")));
            reqMsg.saveChanges();
            return reqMsg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


	@RequestMapping("/editUser")
	@ResponseBody
	public JSONObject editUser(@Validated User user, BindingResult errorResult ,HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		Map<String, Object> resultMap = Validator.fieldValidate(errorResult);
		User _user = userService.checkLogin(user.getUsername());
		if(user.getId()==null||user.getId()==0||_user!=null&&user.getDynamic_auth()==1&&_user.getGoogle_auth_key().isEmpty()) {
			user.setGoogle_auth_key(GoogleAuthenticatorUtil.createSecretKey());
		}
		if(user.getSms_auth()==1&&user.getMobile().equals("")) {
			result.accumulate("msg","请输入手机号码");
		}
		
	    String regex7 = "^(?![A-Za-z0-9]+$)(?![a-z0-9\\W]+$)(?![A-Za-z\\W]+$)(?![A-Z0-9\\W]+$)[a-zA-Z0-9\\W]{8,16}$";//密码校验
	    
    	ConfigLogin configLogin = new ConfigLogin();
    	ArrayList<Object> resultConfigLogins = new ArrayList<Object>();
    	ArrayList<ConfigLogin> configLogins = new ArrayList<ConfigLogin>();
		resultConfigLogins = (ArrayList<Object>)configLoginService.findAll(configLogin, 0, 1);
    	if(CollectionUtils.isNotEmpty(resultConfigLogins)) {
    		configLogins = (ArrayList<ConfigLogin>)resultConfigLogins.get(0);
    		configLogin = configLogins.get(0);
		}
		if (!user.getPassword().equals("") && user.getId()!=null){//没有修改密码
			//密码校检
			if(configLogin.getPassword_verify()==1&&!user.getPassword().matches(regex7)) {
				result.accumulate("msg","密码复杂度：密码长度为8-32个字符，且包含大小写字母、数字和特殊字符，不支持空格");
			}else if(user.getId()!=null&&configLogin.getPassword_verification_times()>0&&userPasswordLogService.findUserPassordLastCertainTimes(user.getPassword(), configLogin.getPassword_verification_times(), user.getId())>0) {
				result.accumulate("msg","新密码不能与前"+configLogin.getPassword_verification_times()+"次设置的密码相同");
			}
		}else if (user.getId()==null){//新建用户
			//密码校检
			if(configLogin.getPassword_verify()==1&&!user.getPassword().matches(regex7)) {
				result.accumulate("msg","密码复杂度：密码长度为8-32个字符，且包含大小写字母、数字和特殊字符，不支持空格");
			}else if(user.getId()!=null&&configLogin.getPassword_verification_times()>0&&userPasswordLogService.findUserPassordLastCertainTimes(user.getPassword(), configLogin.getPassword_verification_times(), user.getId())>0) {
				result.accumulate("msg","新密码不能与前"+configLogin.getPassword_verification_times()+"次设置的密码相同");
			}
		}
        //操作日志
		OperatorLog operatorLog =Operator_log.log(request, session);
		operatorLog.setModule("用户列表");
		if(request.getParameter("password").isEmpty()) {
			user.setPlain_password(null);
		}
		if(result.get("msg")!=null) {
			result.put("success", false);	
			return result;
		}
		Boolean r = false;
		if(user.getId()!=null) {
			operatorLog.setDetails("编辑用户["+user.getUsername()+"]["+user.getRealname()+"]");
			operatorLog.setContent("编辑");
			//判断数据
			if (resultMap==null) {
				//查询旧的部门id
				int selectOldDepartment = userService.selectOldDepartment(user.getId());
				user.setPassword(Sm4Utils.encryptEcb(configPasswordEncryptKeyService.getKey(), user.getPassword()));
				r = userService.editUser(user);
				if(r) {
					//更新部门用户数量-先减少在增加
					List<Object> objects1 = UpdateDepartmentCount.userUpdateDepartmentCount(departmentService, selectOldDepartment, -1);
					List<Object> objects = UpdateDepartmentCount.userUpdateDepartmentCount(departmentService, user.getDepartment(), 1);
					operatorLog.setResult("成功");
					operatorLogService.addOperatorLog(operatorLog);
					result.put("success", true);
					result.put("win", "编辑成功");
				}else {
					result.put("success", false);
				}
			}else {
				if (resultMap!=null){
					result.accumulate("errorMessage", resultMap);
				}
				operatorLog.setResult("失败");
				result.put("success", false);
				operatorLogService.addOperatorLog(operatorLog);
			}	
		}else {
			operatorLog.setDetails("新建用户["+user.getUsername()+"]["+user.getRealname()+"]");
			operatorLog.setContent("添加");
			//判断数据
			User isexitU = userService.checkLogin(user.getUsername());
			if (resultMap==null && isexitU==null) {
				user.setPassword(Sm4Utils.encryptEcb(configPasswordEncryptKeyService.getKey(), user.getPassword()));
				r = userService.addUser(user);
				//是否操作成功
				if(r) {
					//更新部门用户数量
					List<Object> b1 = UpdateDepartmentCount.userUpdateDepartmentCount(departmentService,user.getDepartment(),1);
					operatorLog.setResult("成功");
					operatorLogService.addOperatorLog(operatorLog);
					result.accumulate("success", true);
					result.accumulate("win", "新建成功");
				}
			}else {
				if (isexitU!=null && user.getUsername()!=null){
					result.put("isexitU", "用户名已存在");
				}
				if (resultMap!=null){
					result.accumulate("errorMessage", resultMap);
				}
				operatorLog.setResult("失败");
				result.accumulate("success", false);	
				operatorLogService.addOperatorLog(operatorLog);
			}
		}

		ConfigFinger configFinger = configFingerService.getById(1);
		if(configFinger.getStatus()==1) {
			/*
			String url = configFinger.getUrl()+"/users/sync";
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("endpoint", configFinger.getEndpoint());
			jsonObject.put("pwd", configFinger.getPwd());
			jsonObject.put("oper", 1);
			jsonObject.put("deptcode", "00000000001");
			jsonObject.put("operuserid", ((User)session.getAttribute("user")).getUsername());
			jsonObject.put("operusername", ((User)session.getAttribute("user")).getRealname());
			jsonObject.put("operip", httpClient.getRemortIP(request));
			JSONObject adduser = new JSONObject();
			adduser.put("userId", user.getUsername());
			adduser.put("name", user.getRealname());
			adduser.put("email", user.getEmail());
			adduser.put("phone", user.getMobile());
			adduser.put("residence", "");
			adduser.put("residenceCode", "");
			JSONArray users = new JSONArray();
			users.add(adduser);
			String _users = users.toString();
			jsonObject.put("users", _users);
			JSONObject _result = httpClient.doPost(url, jsonObject);
			_result = httpClient.doGetStr(configFinger.getUrl()+"/code/describe?code="+_result.get("code"));
			*/
			
			String url = configFinger.getUrl()+"/IService";
			String s = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.biolink.biokee.com/\">\r\n" + 
					"				   <soapenv:Header/>\r\n" + 
					"				   <soapenv:Body>\r\n" + 
					"				      <ws:getUserInfo>\r\n" + 
					"				         <arg0>"+configFinger.getEndpoint()+"</arg0>\r\n" + 
					"				         <arg1>"+configFinger.getPwd()+"</arg1>\r\n" + 
					"				         <arg2>"+user.getUsername()+"</arg2>\r\n" + 
					"				      </ws:getUserInfo>\r\n" + 
					"				   </soapenv:Body>\r\n" + 
					"				</soapenv:Envelope>";
			String _result = httpClient.httpPostRaw(url, s, null, "UTF-8");
			System.out.println(_result);
			Pattern pattern = Pattern.compile("<return>(.*?)</return>");
			Matcher matcher = pattern.matcher(_result );
			JSONObject resultJsonObject = new JSONObject();
			if (matcher.find()) {
				resultJsonObject = JSONObject.fromObject(matcher.group(1).toString());
				if(resultJsonObject.get("code").toString().equals("C100")) {
					s = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.biolink.biokee.com/\">\r\n" + 
							"					   <soapenv:Header/>\r\n" + 
							"					   <soapenv:Body>\r\n" + 
							"					      <ws:syncUsers>\r\n" + 
							"				         	 <arg0>"+configFinger.getEndpoint()+"</arg0>\r\n" + 
							"				         	 <arg1>"+configFinger.getPwd()+"</arg1>\r\n" + 
							"					         <arg2>1</arg2>\r\n" + 
							"					         <arg3>"+((User)session.getAttribute("user")).getUsername()+"</arg3>\r\n" + 
							"						     <arg4>"+((User)session.getAttribute("user")).getRealname()+"</arg4>\r\n" + 
							"						     <arg5>"+httpClient.getRemortIP(request)+"</arg5>\r\n" + 
							"					         <arg6>1</arg6>\r\n" + 
							"					         <arg7>[{\"userId\":\""+user.getUsername()+"\",\"name\":\""+user.getUsername()+"\",\"email\":\""+user.getEmail()+"\",\"phone\":\""+user.getMobile()+"\"}]</arg7>\r\n" + 
							"					      </ws:syncUsers>\r\n" + 
							"					   </soapenv:Body>\r\n" + 
							"					</soapenv:Envelope>";
					_result = httpClient.httpPostRaw(url, s, null, "UTF-8");
					System.out.println(_result);
					pattern = Pattern.compile("<return>(.*?)</return>");
					matcher = pattern.matcher(_result );
					if (matcher.find()) {
						//resultJsonObject = JSONObject.fromObject(matcher.group(1).toString());
						if(matcher.group(1).toString().equals("C100")) {
							System.out.println("update user succeed");
						}
					}
				}else {
					s = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.biolink.biokee.com/\">\r\n" + 
							"					   <soapenv:Header/>\r\n" + 
							"					   <soapenv:Body>\r\n" + 
							"					      <ws:syncUsers>\r\n" + 
							"				         	 <arg0>"+configFinger.getEndpoint()+"</arg0>\r\n" + 
							"				         	 <arg1>"+configFinger.getPwd()+"</arg1>\r\n" + 
							"					         <arg2>0</arg2>\r\n" + 
							"					         <arg3>"+((User)session.getAttribute("user")).getUsername()+"</arg3>\r\n" + 
							"						     <arg4>"+((User)session.getAttribute("user")).getRealname()+"</arg4>\r\n" + 
							"						     <arg5>"+httpClient.getRemortIP(request)+"</arg5>\r\n" + 
							"					         <arg6>1</arg6>\r\n" + 
							"					         <arg7>[{\"userId\":\""+user.getUsername()+"\",\"name\":\""+user.getUsername()+"\",\"email\":\""+user.getEmail()+"\",\"phone\":\""+user.getMobile()+"\"}]</arg7>\r\n" + 
							"					      </ws:syncUsers>\r\n" + 
							"					   </soapenv:Body>\r\n" + 
							"					</soapenv:Envelope>";
					_result = httpClient.httpPostRaw(url, s, null, "UTF-8");
					System.out.println(_result);
					pattern = Pattern.compile("<return>(.*?)</return>");
					matcher = pattern.matcher(_result );
					if (matcher.find()) {
						//resultJsonObject = JSONObject.fromObject(matcher.group(1).toString());
						if(matcher.group(1).toString().equals("C100")) {
							System.out.println("create user succeed");
						}
					}
				}
			}
			
			if(user.getFingerdata()!=null&&user.getFingerdatachange()==1) {
				s = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.biolink.biokee.com/\">\r\n" + 
						"						   <soapenv:Header/>\r\n" + 
						"						   <soapenv:Body>\r\n" + 
						"						      <ws:enroll>\r\n" + 
						"				         	 	 <arg0>"+configFinger.getEndpoint()+"</arg0>\r\n" + 
						"				         	 	 <arg1>"+configFinger.getPwd()+"</arg1>\r\n" + 
						"					         	 <arg2>"+((User)session.getAttribute("user")).getUsername()+"</arg2>\r\n" + 
						"						     	 <arg3>"+((User)session.getAttribute("user")).getRealname()+"</arg3>\r\n" + 
						"						         <arg4>"+user.getUsername()+"</arg4>\r\n" + 
						"						         <arg5>"+user.getFingerdata()+"</arg5>\r\n" + 
						"						      </ws:enroll>\r\n" + 
						"						   </soapenv:Body>\r\n" + 
						"						</soapenv:Envelope>";
				_result = httpClient.httpPostRaw(url, s, null, "UTF-8");
				System.out.println(_result);
				pattern = Pattern.compile("<return>(.*?)</return>");
				matcher = pattern.matcher(_result );

				if (matcher.find()) {
					//resultJsonObject = JSONObject.fromObject(matcher.group(1).toString());
					if(matcher.group(1).toString().equals("C100")) {
						System.out.println("register user finger");
					}
				}
			}
		}

		return result;
	}

    @RequestMapping("checkname")
	@ResponseBody
	public JSONObject checkname(@RequestParam("username")String username){
		JSONObject result = new JSONObject();
		User isexitU = userService.checkLogin(username);
		if (isexitU==null){
			result.put("success",true);
		}else {
			result.put("success",false);
		}
		return result;
	}


	@RequestMapping("/delUser")
	@ResponseBody
	public JSONObject delUser(@RequestParam(value = "ids[]") Integer[] ids, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<Integer> _ids =  Arrays.asList(ids);
		if(_ids.isEmpty()) {
			result.put("success", false);
			result.put("msg", "id不能为空");
			return result;
		}
		for (Integer eId: _ids){
			if (eId!=1  && eId !=2 && eId !=3){
				//操作日志
				User user = userService.getUserByID(Integer.toString(eId));
				OperatorLog operatorLog =Operator_log.log(request, session);
				operatorLog.setModule("用户列表");
				operatorLog.setDetails("删除用户["+user.getUsername()+"]["+user.getRealname()+"]");
				operatorLog.setContent("删除");
				operatorLog.setResult("成功");
				operatorLogService.addOperatorLog(operatorLog);
				//查询旧的部门id
				int selectOldDepartment = userService.selectOldDepartment(eId);
				//更新部门用户数量
				UpdateDepartmentCount.userUpdateDepartmentCount(departmentService,selectOldDepartment,-1);
			}else {
				result.put("success", false);
				result.put("error","无法删除该账号");
				return result;
			}
		}
		//是否操作成功
		Boolean r = userService.delUser(_ids);
		if(r) {
			UpdateDepartmentCount.AutoUpdateGroupCounts(groupService,userGroupUserService);
			result.put("success", true);
		}else {
			result.put("success", false);
		}
		return result;
	}
    
    @RequestMapping("/editSelf")
	@ResponseBody
	public JSONObject editSelf(@RequestParam("present_password")String present_password,@RequestParam("new_password")String new_password, User user, HttpServletRequest request, HttpSession session, Model model) {
		JSONObject result = new JSONObject();
	    String regex7 = "^(?![A-Za-z0-9]+$)(?![a-z0-9\\W]+$)(?![A-Za-z\\W]+$)(?![A-Z0-9\\W]+$)[a-zA-Z0-9\\W]{8,16}$";//密码校验
	    
    	ConfigLogin configLogin = new ConfigLogin();
    	ArrayList<Object> resultConfigLogins = new ArrayList<Object>();
    	ArrayList<ConfigLogin> configLogins = new ArrayList<ConfigLogin>();
		resultConfigLogins = (ArrayList<Object>)configLoginService.findAll(configLogin, 0, 1);
    	if(CollectionUtils.isNotEmpty(resultConfigLogins)) {
    		configLogins = (ArrayList<ConfigLogin>)resultConfigLogins.get(0);
    		configLogin = configLogins.get(0);
		}
    	User user1= userService.getUserByID(session.getAttribute("userid").toString());
    	user1.setPlain_password(request.getParameter("new_password"));
	    if(configLogin.getPassword_verify()==1&&!user1.getPlain_password().matches(regex7)) {
	    	result.put("password","密码复杂度：密码长度为8-32个字符，且包含大小写字母、数字和特殊字符，不支持空格");
	    	result.put("success", false);
	    	return result;
	    }else if(configLogin.getPassword_verification_times()>0&&userPasswordLogService.findUserPassordLastCertainTimes(user1.getPlain_password(), configLogin.getPassword_verification_times(), user1.getId())>0) {
	    	result.put("S_password","新密码不能与前"+configLogin.getPassword_verification_times()+"次设置的密码相同");
	    	result.put("success", false);
	    	return result;
	    }else if(!user1.getPassword().equals(present_password)) {
	    	result.put("old_password","原密码不正确");
	    	result.put("success", false);
	    	return result;
	    }
	   
        //操作日志
		OperatorLog operatorLog =Operator_log.log(request, session);
		operatorLog.setModule("个人中心");
		operatorLog.setDetails("编辑用户["+user1.getUsername()+"]");
		operatorLog.setContent("编辑");
		//是否操作成功
		user1.setLast_change_password((int)(System.currentTimeMillis()/1000));
		user1.setPassword(Sm4Utils.encryptEcb(configPasswordEncryptKeyService.getKey(), new_password));
		Boolean r = userService.editUser(user1);
		if(r) {
			session.setAttribute("forceChangePassword", null);
			UserPasswordLog userPasswordLog = new UserPasswordLog();
			userPasswordLog.setCreate_time((int)(System.currentTimeMillis()/1000));
			userPasswordLog.setPassword(user1.getPassword());
			userPasswordLog.setLsblj_user_id(user1.getId());
			userPasswordLogService.addUserPasswordLog(userPasswordLog);
			operatorLog.setResult("成功");
			operatorLogService.addOperatorLog(operatorLog);
			result.put("success", true);
		}else{
			operatorLog.setResult("失败");
			operatorLogService.addOperatorLog(operatorLog);
			result.put("success", false);
		}
		return result;
	}
    
    @RequestMapping("/editName")
    @ResponseBody
    public JSONObject editName(User user, HttpServletRequest request, HttpSession session) {
    	JSONObject result = new JSONObject();
    	//获取id
    	User users=(User) session.getAttribute("user");
		 user.setId(users.getId());  
		 result.put("success", true);
		 if(result.getBoolean("success")){ 
			 Boolean r = userService.editName(user);
			 result.put("success", r?true:false);
		 }
		 return result;
	}

	@RequestMapping("/editstatus")
	@ResponseBody
	public JSONObject editstatus(User user, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.put("success", true);
		if (user.getId()==1){
			result.put("success", false);
			result.put("msg","该账号无法禁用");
		}

		if(result.getBoolean("success")){
			user.setFail_count(0);
			Boolean r = userService.editstatus(user);
			result.put("success", r?true:false);
			if (r==false){
				result.put("msg","修改状态失败");
			}
		}
		return result;
	}

	@RequestMapping("listSelf")
	@ResponseBody
	public JSONObject listSelf(@RequestParam("username")String username){
		JSONObject result = new JSONObject();
		User user = userService.checkLogin(username);
		if (user!=null){
			result.put("success",true);
			result.put("realname",user.getRealname());
			result.put("qq",user.getQq());
			result.put("mobile",user.getMobile());
			result.put("email",user.getEmail());
		}else {
			result.put("success",false);
		}

		return result;
	}

	@RequestMapping("/resetDynamicToken")
	@ResponseBody
	public JSONObject resetDynamicToken(User user, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.put("success", true);
		if(result.getBoolean("success")){
			user.setGoogle_auth_token_used(0);
			user.setGoogle_auth_key(GoogleAuthenticatorUtil.createSecretKey());
			Boolean r = userService.editUser(user);
			result.put("success", r?true:false);
			if (r==false){
				result.put("msg","操作失败");
			}
		}
		return result;
	}

	@RequestMapping("/verify")
	@ResponseBody
	public JSONObject verify(HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		ConfigLogin configLogin = new ConfigLogin();
		ArrayList<Object> resultConfigLogins = new ArrayList<Object>();
		ArrayList<ConfigLogin> configLogins = new ArrayList<ConfigLogin>();
		resultConfigLogins = (ArrayList<Object>)configLoginService.findAll(configLogin, 0, 1);
		if(CollectionUtils.isNotEmpty(resultConfigLogins)) {
			configLogins = (ArrayList<ConfigLogin>)resultConfigLogins.get(0);
			configLogin = configLogins.get(0);
		}
		result.put("verify",configLogin.getPassword_verify());
		return result;
	}
	
	@RequestMapping("/finger")
	@ResponseBody
	public JSONObject finger(HttpServletRequest request, HttpSession session) {
		ConfigFinger configFinger = configFingerService.getById(1);
		String userid = request.getParameter("userid");
		JSONObject result = new JSONObject();
		result.put("FingerCodes", "0000000000");
		if(configFinger.getStatus()==1) {
			String url = configFinger.getUrl()+"/IService";
			String s = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.biolink.biokee.com/\">\r\n" + 
					"				   <soapenv:Header/>\r\n" + 
					"				   <soapenv:Body>\r\n" + 
					"				      <ws:getEnroll>\r\n" + 
					"				         <arg0>"+configFinger.getEndpoint()+"</arg0>\r\n" + 
					"				         <arg1>"+Sm4Utils.decryptEcb(configPasswordEncryptKeyService.getKey(), configFinger.getPwd())+"</arg1>\r\n" + 
					"				         <arg2>"+userid+"</arg2>\r\n" + 
					"				      </ws:getEnroll>\r\n" + 
					"				   </soapenv:Body>\r\n" + 
					"				</soapenv:Envelope>";
			String _result = httpClient.httpPostRaw(url, s, null, "UTF-8");
			System.out.println(_result);
			Pattern pattern = Pattern.compile("<return>(.*?)</return>");
			Matcher matcher = pattern.matcher(_result );
			JSONObject resultJsonObject = new JSONObject();
			if (matcher.find()) {
				result.put("FingerCodes", matcher.group(1).toString());
			}
		}
		return result;
	}


	public static Map<String, Object> checkUserExport(UserService userService, String username,String realname, String password,  String email, String qq, String wechat, String mobile){
		Map<String, Object> errorMap = new HashMap<>(16);
		if(username == null) {
			errorMap.put("info",":用户名不能为空");
			errorMap.put("success", false);
			return errorMap;
		}
		if(realname == null) {
			errorMap.put("info",realname+":姓名不能为空");
			errorMap.put("success", false);
			return errorMap;
		}
		if(password.length() < 8) {
			errorMap.put("info",password+":密码长度不足8位,复杂度较低");
			errorMap.put("success", false);
			return errorMap;
		}
		errorMap.put("success", true);
		return errorMap;
	}
}
