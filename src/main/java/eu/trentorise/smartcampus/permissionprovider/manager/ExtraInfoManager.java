package eu.trentorise.smartcampus.permissionprovider.manager;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.model.ExtraInfo;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.ExtraInfoRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExtraInfoManager {

	private static final String DEFAULT_ROLE = "Citizen";
	
	@Autowired
	private WeLiveLogger logger;
	
	@Autowired
	private ExtraInfoRepository infoRepo;

	@Autowired
	private UserRepository userRepo;

	@Value("${api.token}")
	private String token;

	@Value("${welive.lum}")
	private String lumEndpoint;

	public boolean infoAlreadyCollected(Long userId) {
		User load = userRepo.findOne(userId);
		return load != null && infoAlreadyCollected(load);
	}

	public boolean infoAlreadyCollected(User user) {
		return infoRepo.findByUser(user) != null;
	}

	public void collectInfoForUser(ExtraInfoBean info, Long userId) throws SecurityException, RemoteException {
		User load = userRepo.findOne(userId);
		if (load != null) {
			ExtraInfo entity = createEntity(info);
//			ExtraInfo entity = info == null ? new ExtraInfo(): new ExtraInfo(info);
			entity.setUser(load);
			
			if (!StringUtils.hasText(load.email())) {
				addEmail(load, info.getEmail());
			}
			
			if (info != null) {
				sendAddUser(info, userId);
				infoRepo.save(entity);
			
				if(info.getPilot() != null) {
					Map<String,Object> logMap = new HashMap<String, Object>();
					logMap.put("userid", ""+userId);
					logMap.put("pilot", info.getPilot());
					logger.log(WeLiveLogger.USER_EXTRA_INFO, logMap);
				}
			}
		}
	}

	/**
	 * @param user
	 * @param email
	 */
	private void addEmail(User user, String email) {
		user.updateEmail(email);
		userRepo.save(user);
	}

	/**
	 * @param info
	 * @return
	 */
	private ExtraInfo createEntity(ExtraInfoBean info) {
		ExtraInfo entity = info == null ? new ExtraInfo(): new ExtraInfo(info);
		if (!StringUtils.hasText(entity.getRole())) {
			entity.setRole(DEFAULT_ROLE);
		}
		
		return entity;
	}

	/**
	 * @param info
	 * @throws RemoteException 
	 * @throws SecurityException 
	 */
	private void sendAddUser(ExtraInfoBean info, Long userId) throws SecurityException, RemoteException {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("ccUserId", userId);
		map.put("pilot",info.getPilot());
		map.put("firstName", info.getName());
		map.put("surname", info.getSurname());
		map.put("email", info.getEmail());
		
		if (info.getGender() != null && !info.getGender().isEmpty()) {
			map.put("isMale", !"F".equals(info.getGender()));	
		}
		
		if (info.getBirthdate() != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(info.getBirthdate());
			map.put("birthdayDay",c.get(Calendar.DAY_OF_MONTH));
			map.put("birthdayMonth",c.get(Calendar.MONTH)+1);
			map.put("birthdayYear",c.get(Calendar.YEAR));
		} 
		
		map.put("isDeveloper", info.isDeveloper());
		map.put("role", StringUtils.hasText(info.getRole()) ? info.getRole() : "Citizen");
		map.put("zipCode", info.getZip());
		map.put("country", info.getCountry());
		map.put("city", info.getCity());
		map.put("address", info.getAddress());
		
		if (info.getLanguage() != null) map.put("languages", info.getLanguage());
		if (info.getKeywords() != null) {
			map.put("tags", StringUtils.commaDelimitedListToStringArray(info.getKeywords()));
		}
		
		map.put("status", info.getStatus());
		map.put("isAdult", info.isAdult());
		
//		map.put("cmd", "{\"/Challenge62-portlet.clsidea/add-new-user\":{}}");
//		String postJSON = call("https://dev.welive.eu/api/jsonws/invoke", map, Collections.<String,String>singletonMap("Authorization", "Basic " + token));
		try {
		String postJSON = call(lumEndpoint, map, Collections.<String,String>singletonMap("Authorization", "Basic " + token));
		System.err.print(postJSON);
		} catch (RemoteException e) {
			System.err.println(e.getStackTrace());
			throw e;
		}
		
	}
	
	private static HttpClient getDefaultHttpClient(HttpParams inParams) {
		if (inParams != null) {
			return new DefaultHttpClient(inParams);
		} else {
			return new DefaultHttpClient();
		}
	}
	private static String call(String url, Map<String,Object> body, Map<String,String> headers) throws RemoteException {
		final HttpResponse resp;
		final HttpPost post = new HttpPost(url);

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.setHeader(key, headers.get(key));
			}
		}		

		try {
//			String bodyStr = "";
//			for (String key : body.keySet()) {
//				bodyStr += "&"+key+"="+URLEncoder.encode(""+body.get(key), "UTF-8");
//			}
			String bodyStr = new ObjectMapper().writeValueAsString(body);
			StringEntity input = new StringEntity(bodyStr, "UTF-8");
			
//			input.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			input.setContentType("application/json; charset=UTF-8");
			post.setEntity(input);

			resp = getDefaultHttpClient(null).execute(post);
			String response = EntityUtils.toString(resp.getEntity(),"UTF-8");
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Map<String,Object> respMap = new ObjectMapper().readValue(response, HashMap.class);
				if (respMap != null
						&& ( StringUtils.hasText((String)respMap.get("exception")) 
								|| Boolean.TRUE.equals(respMap.get("error")))) 
				{
					throw new RemoteException((String)respMap.get("exception"));
				}
				return response;
			}
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN
					|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new SecurityException();
			}

			String msg = "";
			try {
				msg = response.substring(response.indexOf("<h1>") + 4,
						response.indexOf("</h1>", response.indexOf("<h1>")));
			} catch (Exception e) {
				msg = resp.getStatusLine().toString();
			}
			throw new RemoteException(msg);
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
}
