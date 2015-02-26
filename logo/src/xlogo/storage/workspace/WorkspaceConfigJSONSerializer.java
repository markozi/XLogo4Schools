package xlogo.storage.workspace;

import java.awt.Font;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.json.JSONArray;
import org.json.JSONObject;

import xlogo.storage.JSONSerializer;
import xlogo.storage.StorableObject;
import xlogo.storage.workspace.WorkspaceConfig.WorkspaceProperty;

public class WorkspaceConfigJSONSerializer extends JSONSerializer<WorkspaceConfig> {
	
	private static final String	FONT						= "font";
	private static final String	SIZE						= "size";
	private static final String	STYLE						= "style";
	private static final String	NAME						= "name";
	private static final String	SYNTAX_HIGHLIGHTING_STYLES	= "syntaxHighlightingStyles";
	private static final String	PRIMITIVE_STYLE				= "primitiveStyle";
	private static final String	PRIMITIVE_COLOR				= "primitiveColor";
	private static final String	OPERAND_STYLE				= "operandStyle";
	private static final String	OPERAND_COLOR				= "operandColor";
	private static final String	COMMENT_STYLE				= "commentStyle";
	private static final String	COMMENT_COLOR				= "commentColor";
	private static final String	BRACE_STYLE					= "braceStyle";
	private static final String	BRACE_COLOR					= "braceColor";
	private static final String IS_SYNTAX_HIGHLIGHTING_ENABLED	= "isSyntaxHighlightingEnabled";
	private static final String	CONTEST_SETTINGS			= "contestSettings";
	private static final String	N_OF_CONTEST_BONUS_FILES	= "nOfContestBonusFiles";
	private static final String	N_OF_CONTEST_FILES			= "nOfContestFiles";
	private static final String	IS_USER_CREATION_ALLOWED	= "isUserCreationAllowed";
	private static final String	LANGUAGE					= "language";
	private static final String	LOGO_LANGUAGE				= "logoLanguage";
	private static final String	NUMBER_OF_BACKUPS			= "numberOfBackups";
	private static final String	USER_LIST					= "userList";
	private static final String	LAST_ACTIVE_USER			= "lastActiveUser";
	
	
	public static StorableObject<WorkspaceConfig, WorkspaceProperty> createOrLoad(File wsDir){
		StorableObject<WorkspaceConfig, WorkspaceConfig.WorkspaceProperty> wc;
		wc = new StorableObject<>(WorkspaceConfig.class, wsDir).withSerializer(new WorkspaceConfigJSONSerializer());
		try {
			wc.createOrLoad();
			wc.get().setDirectory(wsDir);
			return wc;
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wc;
	}
	
	@Override
	public JSONObject serialize2JSON(WorkspaceConfig ws) {
		
		JSONObject json = new JSONObject();
		
		json.put(USER_LIST, new JSONArray(ws.getUserList()));
		json.put(LAST_ACTIVE_USER, ws.getLastActiveUser());
		json.put(NUMBER_OF_BACKUPS, ws.getNumberOfBackups().getValue());
		json.put(LANGUAGE, ws.getLanguage().getValue());
		json.put(LOGO_LANGUAGE, ws.getLogoLanguage().getValue());
		json.put(IS_USER_CREATION_ALLOWED, ws.isUserCreationAllowed());
		
		JSONObject jsonContestSettings = new JSONObject();
		jsonContestSettings.put(N_OF_CONTEST_FILES, ws.getNOfContestFiles());
		jsonContestSettings.put(N_OF_CONTEST_BONUS_FILES, ws.getNOfContestBonusFiles());
		json.put(CONTEST_SETTINGS, jsonContestSettings);
		
		JSONObject jsonSyntaxHighlightingStyles = new JSONObject()
				.put(BRACE_COLOR, ws.getBraceColor()).put(BRACE_STYLE, ws.getBraceStyle())
				.put(COMMENT_COLOR, ws.getCommentColor()).put(COMMENT_STYLE, ws.getCommentStyle())
				.put(OPERAND_COLOR, ws.getOperandColor()).put(OPERAND_STYLE, ws.getOperandStyle())
				.put(PRIMITIVE_COLOR, ws.getPrimitiveColor()).put(PRIMITIVE_STYLE, ws.getPrimitiveStyle())
				.put(IS_SYNTAX_HIGHLIGHTING_ENABLED, ws.isSyntaxHighlightingEnabled());
		json.put(SYNTAX_HIGHLIGHTING_STYLES, jsonSyntaxHighlightingStyles);
		
		JSONObject jsonFont = new JSONObject().put(NAME, ws.getFont().getName()).put(STYLE, ws.getFont().getStyle())
				.put(SIZE, ws.getFont().getSize());
		json.put(FONT, jsonFont);
		
		return json;
	}
	
	@Override
	public WorkspaceConfig deserialize(JSONObject json) {
		WorkspaceConfig ws = new WorkspaceConfig();
		
		if (json.has(USER_LIST)) {
			JSONArray jsonUserList = json.getJSONArray(USER_LIST);
			for (int i = 0; i < jsonUserList.length(); i++) {
				ws.addUser(jsonUserList.getString(i));
			}
		}
		
		if (json.has(LAST_ACTIVE_USER)) {
			ws.setLastActiveUser(json.getString(LAST_ACTIVE_USER));
		}
		
		if (json.has(NUMBER_OF_BACKUPS)) {
			ws.setNumberOfBackups(NumberOfBackups.valueOf(json.getInt(NUMBER_OF_BACKUPS)));
		}
		
		if (json.has(LANGUAGE)) {
			ws.setLanguage(Language.valueOf(json.getInt(LANGUAGE)));
		}
		
		if (json.has(LOGO_LANGUAGE)) {
			ws.setLogoLanguage(LogoLanguage.valueOf(json.getInt(LOGO_LANGUAGE)));
		}
		
		if (json.has(IS_USER_CREATION_ALLOWED)) {
			ws.setUserCreationAllowed(json.getBoolean(IS_USER_CREATION_ALLOWED));
		}
		
		if (json.has(CONTEST_SETTINGS)) {
			JSONObject jsonContestSettings = json.getJSONObject(CONTEST_SETTINGS);
			
			if (jsonContestSettings.has(N_OF_CONTEST_FILES)) {
				ws.setNOfContestFiles(jsonContestSettings.getInt(N_OF_CONTEST_FILES));
				
			}
			if (jsonContestSettings.has(N_OF_CONTEST_BONUS_FILES)) {
				ws.setNOfContestBonusFiles(jsonContestSettings.getInt(N_OF_CONTEST_BONUS_FILES));
			}
			
		}
		
		if (json.has(SYNTAX_HIGHLIGHTING_STYLES)) {
			JSONObject jsonContestSettings = json.getJSONObject(SYNTAX_HIGHLIGHTING_STYLES);
			
			if (jsonContestSettings.has(BRACE_COLOR)) {
				ws.setBraceColor(jsonContestSettings.getInt(BRACE_COLOR));
			}
			
			if (jsonContestSettings.has(BRACE_STYLE)) {
				ws.setBraceStyle(jsonContestSettings.getInt(BRACE_STYLE));
			}
			
			if (jsonContestSettings.has(COMMENT_COLOR)) {
				ws.setCommentColor(jsonContestSettings.getInt(COMMENT_COLOR));
			}
			
			if (jsonContestSettings.has(COMMENT_STYLE)) {
				ws.setCommentStyle(jsonContestSettings.getInt(COMMENT_STYLE));
			}
			
			if (jsonContestSettings.has(OPERAND_COLOR)) {
				ws.setOperandColor(jsonContestSettings.getInt(OPERAND_COLOR));
			}
			
			if (jsonContestSettings.has(OPERAND_STYLE)) {
				ws.setOperandStyle(jsonContestSettings.getInt(OPERAND_STYLE));
			}
			
			if (jsonContestSettings.has(PRIMITIVE_COLOR)) {
				ws.setPrimitiveColor(jsonContestSettings.getInt(PRIMITIVE_COLOR));
			}
			
			if (jsonContestSettings.has(PRIMITIVE_STYLE)) {
				ws.setPrimitiveStyle(jsonContestSettings.getInt(PRIMITIVE_STYLE));
			}
			
			if (jsonContestSettings.has(IS_SYNTAX_HIGHLIGHTING_ENABLED)) {
				ws.setSyntaxHighlightingEnabled(jsonContestSettings.getBoolean(IS_SYNTAX_HIGHLIGHTING_ENABLED));
			}
		}
		
		if (json.has(FONT)) {
			JSONObject jsonFont = json.getJSONObject(FONT);
			
			String name = ws.getFont().getName();
			if (jsonFont.has(NAME)) {
				name = jsonFont.getString(NAME);
			}
			
			int style = ws.getFont().getStyle();
			if (jsonFont.has(STYLE)) {
				style = jsonFont.getInt(STYLE);
			}
			
			int size = ws.getFont().getSize();
			if (jsonFont.has(SIZE)) {
				size = jsonFont.getInt(SIZE);
			}
			
			ws.setFont(new Font(name, style, size));
		}
		
		return ws;
	}
	
}
