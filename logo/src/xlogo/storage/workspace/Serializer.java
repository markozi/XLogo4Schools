package xlogo.storage.workspace;

public interface Serializer<T> {
	public String serialize2String(T target);
	public T deserialize(String serialized);
}
