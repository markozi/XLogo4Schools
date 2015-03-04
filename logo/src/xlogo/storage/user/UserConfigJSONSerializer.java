package xlogo.storage.user;

import java.io.File;

import org.json.JSONObject;

import xlogo.storage.JSONSerializer;
import xlogo.storage.StorableObject;
import xlogo.storage.user.UserConfig.UserProperty;

public class UserConfigJSONSerializer extends JSONSerializer<UserConfig> {
	
	public static StorableObject<UserConfig, UserProperty> createOrLoad(File userDir){
		return createOrLoad(userDir, false);
	}
	
	public static StorableObject<UserConfig, UserProperty> createOrLoad(File userDir, boolean isDeferred){
		StorableObject<UserConfig, UserProperty> userConfig = new StorableObject<UserConfig,UserProperty>(UserConfig.class, userDir);
		try {
			userConfig = userConfig.createOrLoad();
			userConfig.get().setDirectory(userDir);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userConfig;
	}

	@Override
	public JSONObject serialize2JSON(UserConfig object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserConfig deserialize(JSONObject json) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
