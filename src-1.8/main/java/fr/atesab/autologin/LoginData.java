package fr.atesab.autologin;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import fr.atesab.autologin.LoginData.LoginDataType;
import net.minecraft.client.resources.I18n;

public class LoginData {
	/**
	 * The type say how the client would react to a connection
	 * 
	 * @author ATE47
	 * @since 2.0
	 */
	public static enum LoginDataType {
		/**
		 * do nothing
		 * 
		 * @since 2.0
		 */
		DISABLE("disable"),
		/**
		 * open a connection box
		 * 
		 * @since 2.0
		 */
		SECURE_LOGIN("secure"),
		/**
		 * send automatically the password
		 * 
		 * @since 2.0
		 */
		AUTO("auto");
		private String lang;

		private LoginDataType(String lang) {
			this.lang = lang;
		}

		/**
		 * translated name of the type
		 * 
		 * @since 2.0
		 */
		public String getLang() {
			return I18n.format("autologin.type." + lang);
		}
	}

	static LoginData deserialize(Map<String, Object> data) {
		return new LoginData((String) data.getOrDefault("loginPattern", "/login %s"),
				(String) data.getOrDefault("registerPattern", "/register %s %s"),
				new String(Base64.getDecoder().decode((String) data.getOrDefault("password", ""))),
				LoginDataType.valueOf((String) data.getOrDefault("type", LoginDataType.DISABLE.name())));
	}

	private String loginPattern;
	private String registerPattern;

	private String password;

	private LoginDataType type;

	public LoginData() {
		this("/login %s", "/register %s %s", "", LoginDataType.DISABLE);
	}

	public LoginData(String loginPattern, String registerPattern, String password, LoginDataType type) {
		this.loginPattern = loginPattern;
		this.registerPattern = registerPattern;
		this.password = password;
		this.type = type;
	}

	public LoginData clone() {
		return new LoginData(loginPattern, registerPattern, password, type);
	}

	/**
	 * clone it without the password
	 * 
	 * @since 2.0
	 */
	public LoginData cloneEmpty() {
		return new LoginData(loginPattern, registerPattern, "", type);
	}

	/**
	 * true if the password is empty
	 * 
	 * @since 2.0
	 */
	public boolean emptyPassword() {
		return password.isEmpty();
	}

	String getLoginMessage() {
		return loginPattern.replace("%s", password);
	}

	public String getLoginPattern() {
		return loginPattern;
	}

	String getRegisterMessage() {
		return registerPattern.replace("%s", password);
	}

	public String getRegisterPattern() {
		return registerPattern;
	}

	public LoginDataType getType() {
		return type;
	}

	public LoginData loginPattern(String loginPattern) {
		this.loginPattern = loginPattern;
		return this;
	}

	/**
	 * test if the password match with the given one
	 * 
	 * @since 2.0
	 */
	public boolean match(String pass) {
		return pass.equals(password);
	}

	public LoginData password(String password) {
		this.password = password;
		return this;
	}

	public LoginData registerPattern(String registerPattern) {
		this.registerPattern = registerPattern;
		return this;
	}

	Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put("loginPattern", loginPattern);
		data.put("registerPattern", registerPattern);
		data.put("password", Base64.getEncoder().encodeToString(password.getBytes()));
		data.put("enabled", type.name());
		return data;
	}

	public LoginData type(LoginDataType type) {
		this.type = type;
		return this;
	}

	public LoginData update(String loginPattern, String registerPattern, String password, LoginDataType type) {
		this.loginPattern = loginPattern;
		this.registerPattern = registerPattern;
		this.password = password;
		this.type = type;
		return this;
	}
}
