package xlogo.gui.components.fileslist;

public interface FilesListEventListener {
	public void onFileCreateRequest();
	public void onFileRenameRequest(String oldName, String newName);
	public void onFileDeleteRequest(String fileName);
	public void onFileOpened(String fileName);
	public void onFileClosed(String fileName);
}
