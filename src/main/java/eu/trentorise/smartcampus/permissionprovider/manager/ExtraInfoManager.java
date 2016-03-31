package eu.trentorise.smartcampus.permissionprovider.manager;

import java.net.URLEncoder;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.model.ExtraInfo;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.ExtraInfoRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

@Component
public class ExtraInfoManager {

	@Autowired
	private WeLiveLogger logger;
	
	@Autowired
	private ExtraInfoRepository infoRepo;

	@Autowired
	private UserRepository userRepo;

	@Value("${api.token}")
	private String token;

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
			ExtraInfo entity = info == null ? new ExtraInfo(): new ExtraInfo(info);
			entity.setUser(load);
			infoRepo.save(entity);
			
			if (info != null) {
				sendAddUser(info, userId);
			
				if (info.getPilot() != null) {
					Map<String,Object> logMap = new HashMap<String, Object>();
					logMap.put("UserID", ""+userId);
					logMap.put("Pilot", info.getPilot());
					logger.log(WeLiveLogger.USER_EXTRA_INFO, logMap);
				}
			}
		}
	}

	/**
	 * @param info
	 * @throws RemoteException 
	 * @throws SecurityException 
	 */
	private void sendAddUser(ExtraInfoBean info, Long userId) throws SecurityException, RemoteException {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("CC_UserID", userId);
		map.put("pilot",info.getPilot());
		map.put("firstName", info.getName());
		map.put("surname", info.getSurname());
		map.put("email", info.getEmail());
		
		map.put("isMale", "M".equals(info.getGender()));
		Calendar c = Calendar.getInstance();
		c.setTime(info.getBirthdate());
		map.put("birthdayDay",c.get(Calendar.DAY_OF_MONTH));
		map.put("birthdayMonth",c.get(Calendar.MONTH)+1);
		map.put("birthdayYear",c.get(Calendar.YEAR));
		map.put("isDeveloper", info.isDeveloper());
		map.put("zipCode", info.getZip());
		map.put("country", info.getCountry());
		map.put("city", info.getCity());
		map.put("address", info.getAddress());
		if (info.getLanguage() != null) map.put("languages", StringUtils.arrayToCommaDelimitedString(info.getLanguage()));
		map.put("cmd", "{\"/Challenge62-portlet.clsidea/add-new-user\":{}}");
		
		String postJSON = call("https://dev.welive.eu/api/jsonws/invoke", map, Collections.<String,String>singletonMap("Authorization", "Basic " + token));
		System.err.print(postJSON);
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
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.setHeader(key, headers.get(key));
			}
		}		

		try {
			String bodyStr = "";
			for (String key : body.keySet()) {
				bodyStr += "&"+key+"="+URLEncoder.encode(""+body.get(key), "UTF-8");
			}
			StringEntity input = new StringEntity(bodyStr, "UTF-8");
			
			input.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			post.setEntity(input);

			resp = getDefaultHttpClient(null).execute(post);
			String response = EntityUtils.toString(resp.getEntity(),"UTF-8");
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
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
