package eu.trentorise.smartcampus.permissionprovider.manager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.model.ExtraInfo;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.ExtraInfoRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

@Component
public class ExtraInfoManager {

	@Autowired
	private ExtraInfoRepository infoRepo;

	@Autowired
	private UserRepository userRepo;

	public boolean infoAlreadyCollected(Long userId) {
		User load = userRepo.findOne(userId);
		return load != null && infoAlreadyCollected(load);
	}

	public boolean infoAlreadyCollected(User user) {
		return infoRepo.findByUser(user) != null;
	}

	public void collectInfoForUser(ExtraInfoBean info, Long userId) {
		if (info != null) {
			User load = userRepo.findOne(userId);
			if (load != null) {
				ExtraInfo entity = new ExtraInfo();
				BeanUtils.copyProperties(info, entity);
				entity.setUser(load);
				infoRepo.save(entity);
			}
		}
	}

}
