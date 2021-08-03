package com.axway.apim.users.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONUserAdapter extends UserAdapter {
	
	private ObjectMapper mapper = new ObjectMapper();

	public JSONUserAdapter() {
	}

	public boolean readConfig(Object config) throws AppException {
		if (config==null) return false;
		if (config instanceof String == false) return false;
		String myConfig = (String)config;
		File configFile = new File(myConfig);
		if(!configFile.exists()) return false;
		try {
			this.users = mapper.readValue(configFile, new TypeReference<List<User>>(){});
		} catch (MismatchedInputException me) {
			try {
				User user = mapper.readValue(configFile, User.class);
				user.setType("internal"); // Default to internal, as external makes no sense using the CLI
				this.users = new ArrayList<User>();
				this.users.add(user);
			} catch (Exception pe) {
				throw new AppException("Cannot read user(s) from config file: " + config, ErrorCode.UNKNOWN_USER, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read user(s) from config file: " + config, ErrorCode.UNKNOWN_USER, e);
		}
		try{
			addImage(users, configFile.getCanonicalFile().getParentFile());
		}catch (Exception e){
			throw new AppException("Cannot read image for user(s) from config file: " + config, ErrorCode.UNKNOWN_USER, e);
		}
		validateCustomProperties(users);
		return true;
	}
	
	private void addImage(List<User> users, File parentFolder) throws AppException {
		for(User user : users) {
			if(user.getImageUrl()==null || user.getImageUrl().equals("")) continue;
			user.setImage(Image.createImageFromFile(new File(parentFolder + File.separator + user.getImageUrl())));
		}
	}
	
	private void validateCustomProperties(List<User> users) throws AppException {
		for(User user : users) {
			Utils.validateCustomProperties(user.getCustomProperties(), Type.user);
		}
	}
}
