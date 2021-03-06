package com.longersec.blj.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.longersec.blj.domain.ApppubServer;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.longersec.blj.domain.ConfigNetwork;
import com.longersec.blj.service.ConfigNetworkService;
import com.longersec.blj.service.ConfigService;
import com.longersec.blj.utils.SystemCommandUtil;
import com.longersec.blj.utils.httpClient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * 
 */
@Controller
@RequestMapping(value = "/configNetwork")
public class ConfigNetworkController {

	@Autowired
	private ConfigNetworkService configNetworkService;
	@Autowired
	private ConfigService configService;

	@RequestMapping("/listConfigNetwork")
	@ResponseBody
	public JSONObject listConfigNetwork(ConfigNetwork configNetwork,HttpServletRequest request, HttpSession session) {
		int page_start = Integer.parseInt(request.getParameter("start"));
		int page_length = Integer.parseInt(request.getParameter("length"));
		ArrayList<Object> resultConfigNetworks = new ArrayList<Object>();
		ArrayList<ConfigNetwork> configNetworks = new ArrayList<ConfigNetwork>();
		long total = 0;
		List<Map<String, String>> files = new ArrayList<Map<String, String>>();
		File dir = new File(configService.getByName("interfacePath").getValue());
		File network = new File(configService.getByName("network").getValue());
		File[] listFiles = dir.listFiles();
		try {
			Integer i = 0;
            String str="";
            String ipv6enable = "0";
            String ipv6autoconf = "0";
            if(network.exists()) {
    			FileInputStream inputStream2 = new FileInputStream(network.getAbsoluteFile());
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                while(( str = bufferedReader2.readLine()) != null)
    			{
                	if(str.toLowerCase().indexOf("networking_ipv6=")>=0) {
                		ipv6enable = str.substring("networking_ipv6=".length()).trim().equals("yes")?"1":"0";
                	}else if(str.toLowerCase().indexOf("ipv6_autoconf=")>=0) {
                		ipv6autoconf = str.substring("ipv6_autoconf=".length()).trim();
                	}
            		
    			}
                inputStream2.close();
                bufferedReader2.close();
            }
            
		    for(File f: listFiles){
		        if(f.isFile()&&f.getName().indexOf("ifcfg-")>=0&&f.getName().substring(0,6).equals("ifcfg-")){
		            HashMap<String, String> netConfigHashMap = new HashMap<String, String>();
		            FileInputStream inputStream;
					inputStream = new FileInputStream(f.getAbsoluteFile());
		            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		            str="";
		            netConfigHashMap.put("id", i.toString());
		            netConfigHashMap.put("name", "");
		            netConfigHashMap.put("device", "");
		            netConfigHashMap.put("ipv4addr", "");
		            netConfigHashMap.put("ipv4netmask", "");
		            netConfigHashMap.put("ipv4gateway", "");
		            netConfigHashMap.put("onboot", "");
		            netConfigHashMap.put("ipv6init", "");
		            netConfigHashMap.put("ipv6addr", "");
		            netConfigHashMap.put("ipv6netmask", "");
		            netConfigHashMap.put("ipv6gateway", "");
		            netConfigHashMap.put("ipv6_autoconf", "no");
		            
		            netConfigHashMap.put("bootproto", "static");
		            netConfigHashMap.put("ipv6enable", ipv6enable);
		            netConfigHashMap.put("ipv6autoconf", ipv6autoconf);
		            while(( str = bufferedReader.readLine()) != null)
					{
						if(str.toLowerCase().indexOf("name=")>=0) {
							netConfigHashMap.put("name", str.substring("name=".length()).trim());
						}else if(str.toLowerCase().indexOf("device=")>=0) {
							netConfigHashMap.put("device", str.substring("device=".length()).trim());
						}else if(str.toLowerCase().indexOf("ipaddr=")>=0) {
							netConfigHashMap.put("ipv4addr", str.substring("ipaddr=".length()).trim());
						}else if(str.toLowerCase().indexOf("netmask=")>=0) {
							netConfigHashMap.put("ipv4netmask", str.substring("netmask=".length()).trim());
						}else if(str.toLowerCase().indexOf("gateway=")>=0) {
							netConfigHashMap.put("ipv4gateway", str.substring("gateway=".length()).trim());
						}else if(str.toLowerCase().indexOf("onboot=")>=0) {
							netConfigHashMap.put("onboot", str.substring("onboot=".length()).trim().equals("yes")?"1":"0");
						}else if(str.toLowerCase().indexOf("bootproto=")>=0) {
							netConfigHashMap.put("bootproto", str.substring("bootproto=".length()).trim());
						}else if(str.toLowerCase().indexOf("ipv6init=")>=0) {
							netConfigHashMap.put("ipv6init", str.substring("ipv6init=".length()).trim());
						}else if(str.toLowerCase().indexOf("ipv6addr=")>=0) {
							String ipv6 = str.substring("ipv6addr=".length()).trim();
							if(ipv6.indexOf('/')>0) {
								netConfigHashMap.put("ipv6addr", ipv6.substring(0, ipv6.indexOf('/')).trim());
								netConfigHashMap.put("ipv6netmask", ipv6.substring(ipv6.indexOf('/')+1).trim());
							}else {
								netConfigHashMap.put("ipv6addr", ipv6.trim());
								netConfigHashMap.put("ipv6netmask", "");
							}
						}else if(str.toLowerCase().indexOf("ipv6_defaultgw=")>=0) {
							netConfigHashMap.put("ipv6gateway", str.substring("ipv6_defaultgw=".length()).trim());
						}else if(str.toLowerCase().indexOf("ipv6_autoconf=")>=0) {
	                		ipv6autoconf = str.substring("ipv6_autoconf=".length()).trim();
	                		netConfigHashMap.put("ipv6autoconf", ipv6autoconf);
	                	}
					}
		            netConfigHashMap.put("speed", "unknown");
					netConfigHashMap.put("status", SystemCommandUtil.execCmd("cat /sys/class/net/"+netConfigHashMap.get("device")+"/operstate").trim());
		            files.add(netConfigHashMap);
		            bufferedReader.close();
		            inputStream.close();
		        }
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*resultConfigNetworks = (ArrayList<Object>)configNetworkService.findAll(configNetwork, page_start, page_length);
		if(CollectionUtils.isNotEmpty(resultConfigNetworks)) {
			configNetworks = (ArrayList<ConfigNetwork>)resultConfigNetworks.get(0);
			total = ((ArrayList<Long>) resultConfigNetworks.get(1)).get(0);
		}*/
		JSONArray jsonArray = JSONArray.fromObject(configNetworks);
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		result.accumulate("recordsTotal", files.size());
		result.accumulate("recordsFiltered", files.size());
		result.accumulate("data", files);
		return result;
	}

	@RequestMapping("/addConfigNetwork")
	@ResponseBody
	public JSONObject addConfigNetwork(ConfigNetwork configNetwork, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(result.getBoolean("success")) {
			Boolean r = configNetworkService.addConfigNetwork(configNetwork);
			result.accumulate("success", r?true:false);
		}
		return result;
	}

	@RequestMapping("/editConfigNetwork")
	@ResponseBody
	public JSONObject editConfigNetwork(ConfigNetwork configNetwork, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		String netwFileString = configService.getByName("network").getValue();
		File network = new File(netwFileString);
		System.out.println(configNetwork.toString());
		try {
			Integer i = 0;
            String str="";
            String ipv6enable = "0";
            String ipv6autoconf = "0";
            String networkString = "";
            if(network.exists()) {
    			FileInputStream inputStream2 = new FileInputStream(network.getAbsoluteFile());
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                while(( str = bufferedReader2.readLine()) != null)
    			{
                	if(str.toLowerCase().indexOf("networking_ipv6=")>=0) {
                		networkString += "NETWORKING_IPV6="+(configNetwork.getIpv6enable()==1?"yes":"no")+"\n";
                	}else if(str.toLowerCase().indexOf("ipv6_autoconf=")>=0) {
                		networkString += "IPV6_AUTOCONF="+(configNetwork.getIpv6autoconf()==1?"yes":"no")+"\n";
                	}else {
                		networkString += str+"\n";
                	}
    			}
                inputStream2.close();
                bufferedReader2.close();
                File file =new File(netwFileString);
    			//file = new File("/tmp/n");
    			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
    			BufferedWriter bw = new BufferedWriter(fileWriter);
    			bw.write(networkString);
    			bw.close();
            }
            
            String interfaceFile = configService.getByName("interfacePath").getValue()+"/ifcfg-"+configNetwork.getDevice();
            File f = new File(interfaceFile);
	        if(f.isFile()&&f.getName().indexOf("ifcfg-")>=0&&f.getName().substring(0,6).equals("ifcfg-")){
	            HashMap<String, String> netConfigHashMap = new HashMap<String, String>();
	            FileInputStream inputStream;
				inputStream = new FileInputStream(f.getAbsoluteFile());
	            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            str="";
	            netConfigHashMap.put("id", i.toString());
	            netConfigHashMap.put("bootproto", "static");
	            netConfigHashMap.put("ipv6enable", ipv6enable);
	            netConfigHashMap.put("ipv6autoconf", ipv6autoconf);
	            String interfaceString = "";
	            
	            Boolean _ipaddr = false; 
	            Boolean _netmask = false; 
	            Boolean _gateway = false; 
	            Boolean _bootproto = false; 
	            Boolean _ipv6addr = false; 
	            Boolean _ipv6_defaultgw = false; 
	            Boolean _ipv6_autoconf = false; 
	            
	            while(( str = bufferedReader.readLine()) != null)
				{
					if(str.toLowerCase().indexOf("ipaddr=")>=0) {
						_ipaddr = true;
						interfaceString += "IPADDR="+configNetwork.getIpv4addr()+"\n";
					}else if(str.toLowerCase().indexOf("netmask=")>=0) {
						_netmask = true;
						interfaceString += "NETMASK="+configNetwork.getIpv4netmask()+"\n";
					}else if(str.toLowerCase().indexOf("gateway=")>=0) {
						_gateway = true;
						interfaceString += "GATEWAY="+configNetwork.getIpv4gateway()+"\n";
					}else if(str.toLowerCase().indexOf("bootproto=")>=0) {
						_bootproto = true;
						interfaceString += "BOOTPROTO="+configNetwork.getBootprotocol()+"\n";
					}else if(str.toLowerCase().indexOf("ipv6addr=")>=0) {
						_ipv6addr = true;
						interfaceString += "IPV6ADDR="+configNetwork.getIpv6addr()+"\n";
					}else if(str.toLowerCase().indexOf("ipv6_defaultgw=")>=0) {
						_ipv6_defaultgw = true;
						interfaceString += "IPV6_DEFAULTGW="+configNetwork.getIpv6gateway()+"\n";
					}else if(str.toLowerCase().indexOf("ipv6_autoconf=")>=0) {
						_ipv6_autoconf = true;
						interfaceString += "IPV6_AUTOCONF="+(configNetwork.getIpv6autoconf()==1?"yes":"no")+"\n";
                	}else {
                		interfaceString += str+"\n";
                	}
				}
	            if(!_ipaddr) {
					interfaceString += "IPADDR="+configNetwork.getIpv4addr()+"\n";
				}
	            if(!_netmask) {
					interfaceString += "NETMASK="+configNetwork.getIpv4netmask()+"\n";
				}
	            if(!_gateway) {
					interfaceString += "GATEWAY="+configNetwork.getIpv4gateway()+"\n";
				}
	            if(!_bootproto) {
					interfaceString += "BOOTPROTO="+configNetwork.getBootprotocol()+"\n";
				}
	            if(!_ipv6addr) {
					interfaceString += "IPV6ADDR="+configNetwork.getIpv6addr()+"\n";
				}
	            if(!_ipv6_defaultgw) {
					interfaceString += "IPV6_DEFROUTE="+configNetwork.getIpv6gateway()+"\n";
				}
	            if(!_ipv6_autoconf) {
					interfaceString += "IPV6_AUTOCONF="+configNetwork.getIpv6autoconf()+"\n";
            	}
	            bufferedReader.close();
	            inputStream.close();
	            File file =new File(interfaceFile);
    			//file = new File("/tmp/i");
    			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
    			BufferedWriter bw = new BufferedWriter(fileWriter);
    			bw.write(interfaceString);
    			bw.close();
	        }
	        result.put("success",true);
	    
		} catch (FileNotFoundException e) {
			result.put("success",false);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			result.put("success",false);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

	@RequestMapping("/delConfigNetwork")
	@ResponseBody
	public JSONObject delConfigNetwork(@RequestParam(value = "ids[]") Integer[] ids, HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		List<Integer> _ids =  Arrays.asList(ids);
		result.accumulate("success", true);
		if(_ids.isEmpty()) {
			result.accumulate("success", false);
			result.accumulate("msg", "id不能为空");
		}
		if(result.getBoolean("success")) {
			Boolean r = configNetworkService.delConfigNetwork(_ids);
			result.accumulate("success", r);
		}
		return result;
	}
/*
	@RequestMapping("/checkConfigNetwork")
	@ResponseBody
	public JSONObject checkConfigNetwork(@RequestParam("networkip") {
		JSONObject result = new JSONObject();
		List<Integer> _ids =  Arrays.asList(ids);
		result.accumulate("success", true);
		if(_ids.isEmpty()) {
			result.accumulate("success", false);
			result.accumulate("msg", "id不能为空");
		}
		if(result.getBoolean("success")) {
			Boolean r = configNetworkService.delConfigNetwork(_ids);
			result.accumulate("success", r);
		}
		return result;
	}*/


	@RequestMapping("/lping")
	@ResponseBody
	public JSONObject lping(HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.put("success", true);
		if(request.getParameter("ip")==null||httpClient.validIPAddressAll(request.getParameter("ip"))==0) {
			result.put("msg", "IP输入不正确");
			result.put("success", false);
			return result;
		}
		String msgString = SystemCommandUtil.execCmd("ping -c 4 "+request.getParameter("ip"));
		result.put("msg", msgString);
		return result;
	}
	
	@RequestMapping("/ltraceroute")
	@ResponseBody
	public JSONObject ltraceroute(HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(request.getParameter("ip")==null||httpClient.validIPAddressAll(request.getParameter("ip"))==0) {
			result.put("msg", "IP输入不正确");
			result.put("success", false);
			return result;
		}
		String msgString = SystemCommandUtil.execCmd("traceroute "+request.getParameter("ip"));
		result.put("msg", msgString);
		return result;
	}
	
	@RequestMapping("/lportest")
	@ResponseBody
	public JSONObject lportest(HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		if(request.getParameter("ip")==null||httpClient.validIPAddressAll(request.getParameter("ip"))==0) {
			result.put("msg", "IP输入不正确");
			result.put("success", false);
			return result;
		}
		if(Integer.valueOf(request.getParameter("port").toString())<1 || Integer.valueOf(request.getParameter("port").toString())>65535) {
			result.put("msg", "端口输入不正确");
			result.put("success", false);
			return result;
		}
		String msgString = SystemCommandUtil.execCmd("tcping -t 3 "+request.getParameter("ip")+" "+request.getParameter("port"));
		result.put("msg", msgString);
		return result;
	}
	
	@RequestMapping("/ltcpdump")
	@ResponseBody
	public JSONObject ltcpdump(HttpServletRequest request, HttpSession session) {
		JSONObject result = new JSONObject();
		result.accumulate("success", true);
		String msgString = SystemCommandUtil.execCmd("tcpdump "+request.getParameter("option"));
		result.put("msg", msgString);
		return result;
	}
}
