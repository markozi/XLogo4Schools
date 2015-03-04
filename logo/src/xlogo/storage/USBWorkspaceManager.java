package xlogo.storage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.samuelcampos.usbdrivedectector.USBDeviceDetectorManager;
import net.samuelcampos.usbdrivedectector.USBStorageDevice;
import net.samuelcampos.usbdrivedectector.events.IUSBDriveListener;
import net.samuelcampos.usbdrivedectector.events.USBStorageEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.storage.workspace.WorkspaceConfigJSONSerializer;
import xlogo.storage.workspace.WorkspaceConfig.WorkspaceProperty;

public class USBWorkspaceManager {
	private static Logger				logger	= LogManager.getLogger(USBWorkspaceManager.class.getSimpleName());
	private USBDeviceDetectorManager	driveDetector;
	private WorkspaceContainer wm;
	private Map<String, StorableObject<WorkspaceConfig, WorkspaceProperty>> usbWorkspaces;
	
	public USBWorkspaceManager(WorkspaceContainer workspaceManager) {
		this.wm = workspaceManager;
		usbWorkspaces = new HashMap<String, StorableObject<WorkspaceConfig, WorkspaceProperty>>();
	}
	
	/**
	 * Detect External Drives
	 */
	public void init() {
		if (driveDetector != null) { return; }
		
		driveDetector = new USBDeviceDetectorManager(800);
		
		for (USBStorageDevice rmDevice : driveDetector.getRemovableDevices()) {
			if (rmDevice.canRead() && rmDevice.canWrite()) {
				addUSBDrive(rmDevice);
			}
		}
		
		driveDetector.addDriveListener(new IUSBDriveListener(){
			
			@Override
			public void usbDriveEvent(USBStorageEvent event) {
				USBStorageDevice rmDevice = event.getStorageDevice();
				switch (event.getEventType()) {
					case CONNECTED:
						addUSBDrive(rmDevice);
						break;
					case REMOVED:
						removeUSBDrive(rmDevice);
						break;
				}
			}
		});
	}
	
	public void stop() {
		driveDetector.stop();
	}
	
	private void addUSBDrive(USBStorageDevice rmDevice) {
		logger.trace("USB Drive attached: " + rmDevice);
		StorableObject<WorkspaceConfig, WorkspaceProperty> usbwc = initUSBDrive(rmDevice);
		if (usbwc != null && usbwc.get() == null) {
			// TODO fail case
			return;
		}
		usbWorkspaces.put(usbwc.get().getWorkspaceName(), usbwc);
		wm.add(usbwc);
	}
		
	private void removeUSBDrive(USBStorageDevice rmDevice) {
		logger.trace("USB Drive removed: " + rmDevice);
		String deviceName = rmDevice.getSystemDisplayName();
		StorableObject<WorkspaceConfig, WorkspaceProperty> usbwc = usbWorkspaces.get(deviceName);
		wm.remove(usbwc);
		// remove cached version from the same workspace
		//File usbRoot = rmDevice.getRootDirectory();
		//File usbWsDir = StorableObject.getDirectory(usbRoot, WorkspaceConfig.USB_DEFAULT_WORKSPACE);
		//ObjectCache.getInstance().remove(usbWsDir, WorkspaceConfig.class);
	}
	
	private StorableObject<WorkspaceConfig, WorkspaceProperty> initUSBDrive(USBStorageDevice rmDevice) {
		logger.trace("Initializing USB Drive: " + rmDevice.getDeviceName());
		
		String deviceName = rmDevice.getSystemDisplayName();
		
		File usbRoot = rmDevice.getRootDirectory();
		File usbWsDir = StorableObject.getDirectory(usbRoot, WorkspaceConfig.USB_DEFAULT_WORKSPACE);
		
		StorableObject<WorkspaceConfig, WorkspaceProperty> usbwc = WorkspaceConfigJSONSerializer.createOrLoad(usbWsDir, true, true);
		try {
			usbwc = usbwc.createOrLoad();
			usbwc.get().setDirectory(usbWsDir);
			usbwc.get().setWorkspaceName(deviceName);
		}
		catch (Exception e) {
			// TODO
			e.printStackTrace();
			return null;
		}
		
		return usbwc;
	}
	
	public StorableObject<WorkspaceConfig, WorkspaceProperty> createOrLoad(String deviceName) {
		logger.trace("Initializing USB Drive: " + deviceName);
		File usbRoot = null;
		for (USBStorageDevice device : driveDetector.getRemovableDevices()) {
			if (deviceName.equals(device.getSystemDisplayName())) {
				usbRoot = device.getRootDirectory();
				break;
			}
		}
		if (usbRoot == null) { return null; }
		
		File usbWsDir = StorableObject.getDirectory(usbRoot, WorkspaceConfig.USB_DEFAULT_WORKSPACE);
		
		StorableObject<WorkspaceConfig, WorkspaceProperty> wsc = WorkspaceConfigJSONSerializer.createOrLoad(usbWsDir, true);
		wsc.get().setWorkspaceName(deviceName);
		return wsc;
	}
	
	public String getFirstUSBWorkspace(String[] workspaceNamesc) {
		for (String ws : workspaceNamesc) {
			if (isUSBDrive(ws)) {
				return ws; 
			}
		}
		return null;
	}
	
	public boolean isUSBDrive(String workspaceName) {
		if (driveDetector == null) { return false; }
		List<USBStorageDevice> devices = driveDetector.getRemovableDevices();
		logger.trace("Is '" + workspaceName + "' on a USB Drive?");
		for (USBStorageDevice device : devices) {
			if (workspaceName.contains(device.getSystemDisplayName())) {
				logger.trace("\t = Yes, corresponding USB Device found.");
				return true;
			}
			else {
				logger.trace("\t Does not correspond to " + device.getSystemDisplayName());
			}
		}
		
		logger.trace("\t = No, could not find corresponding USB Drive.");
		return false;
	}
	
	public File getWorkspaceDirectory(String workspaceName){
		if (driveDetector == null) { return null; }
		if(usbWorkspaces.containsKey(workspaceName)){
			return usbWorkspaces.get(workspaceName).getLocation();
		}
		List<USBStorageDevice> devices = driveDetector.getRemovableDevices();
		logger.trace("Is '" + workspaceName + "' on a USB Drive?");
		for (USBStorageDevice device : devices) {
			if (workspaceName.contains(device.getSystemDisplayName())) {
				logger.trace("\t = Yes, corresponding USB Device found.");
				return new File(device.getRootDirectory().toString() + File.separator + WorkspaceConfig.USB_DEFAULT_WORKSPACE);
			}
			else {
				logger.trace("\t Does not correspond to " + device.getSystemDisplayName());
			}
		}
		return null;
	}
	
	public interface WorkspaceContainer {
		public void add(StorableObject<WorkspaceConfig, WorkspaceProperty> wc);
		public void remove(StorableObject<WorkspaceConfig, WorkspaceProperty> wc);
	}
}
