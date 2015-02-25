package xlogo.storage;

import org.json.JSONObject;

import xlogo.storage.workspace.Serializer;

public abstract class JSONSerializer<T> implements Serializer<T>{
	public abstract JSONObject serialize2JSON(T object);
	
	public abstract T deserialize(JSONObject json);
	
	@Override
	public T deserialize(String json) {
		return deserialize(new JSONObject(json));
	}

	@Override
	public String serialize2String(T target) {
		return serialize2JSON(target).toString();
	}
}
