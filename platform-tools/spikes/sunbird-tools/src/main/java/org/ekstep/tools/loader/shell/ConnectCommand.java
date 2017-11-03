/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 *
 * @author feroz
 */
@Component
public class ConnectCommand implements CommandMarker {

	@CliCommand(value = "login", help = "Connect to the target environment")
	public String login(@CliOption(key = { "user" }, mandatory = true, help = "User name to login") final String user,
			@CliOption(key = { "password" }, mandatory = true, help = "Password to login") final String password,
			@CliOption(key = { "conf" }, mandatory = true, help = "Config file for environment") final File confFile)
			throws Exception {

		ShellContext context = ShellContext.getInstance();
		if (null != context.getCurrentUser()) {
			return "Already login with User " + context.getCurrentUser();
		}
		if (verifyLogin(user, password)) {
			Config conf = ConfigFactory.parseFile(confFile);
			context.setCurrentConfig(conf);
			context.setCurrentUser(user);
			return "Connected to " + conf.getString("env") + " as " + user;
		} else {
			return "Invalid User Cred";
		}


	}

	/**
	 * @param user
	 * @param password
	 * @return
	 */
	private boolean verifyLogin(String user, String password) {
		File credFile = new File(".cred_File");
		try {
			if (!credFile.exists()) {
				credFile.createNewFile();
				FileUtils.write(credFile, "Username,Password\n", true);
				String encPassword = encrypt(password, user);
				if (null != encPassword) {
					FileUtils.write(credFile, user + "," + encPassword + "\n", true);
					credFile.setWritable(false);
					return true;
				} else
					return false;

			} else {
				credFile.setWritable(false);
				String line = fetchLine(credFile, user);
				if (null != line) {
					String encPassword = line.split(",")[1];
					if (!password.contentEquals(decrypt(encPassword, user))) {
						return false;
					} else {
						return true;
					}
				} else {
					String encPassword = encrypt(password, user);
					credFile.setWritable(true);
					FileUtils.write(credFile, user + "," + encPassword + "\n", true);
					credFile.setWritable(false);
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param credFile
	 * @param user
	 * @return
	 */
	private String fetchLine(File credFile, String user) {
		FileReader fileReader = null;
		BufferedReader reader = null;

		try {
			fileReader = new FileReader(credFile);
			reader = new BufferedReader(fileReader);
			String line = null;
			while (null != (line = reader.readLine()))
				if (line.split(",")[0].contentEquals(user)) {
					return line;
				}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != reader) {
					reader.close();
				}

				if (null != fileReader) {
					fileReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return null;
	}

	/**
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private String encrypt(String password, String key) throws Exception {
		String strData = null;

		try {
			strData = new String(Base64.encodeBase64((password + key).getBytes()), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strData;
	}

	private String decrypt(String encPassword, String key) throws Exception {
		String strData = null;

		try {
			strData = new String(Base64.decodeBase64((encPassword + key).getBytes()), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strData;
	}

	@CliCommand(value = "logout", help = "Disconnect from the target environment")
	public String logout() {

		ShellContext context = ShellContext.getInstance();
		Config conf = context.getCurrentConfig();

		if (conf == null)
			return "Not logged in.";
		String env = conf.getString("env");

		context.setCurrentConfig(null);
		context.setCurrentUser(null);
		return "Disconnected from " + env;
	}

	@CliCommand(value = "register", help = "One Time Registration to connect to apis")
	public String register(
			@CliOption(key = { "user" }, mandatory = true, help = "User name to Register") final String user) {

		
		String clientId = "";
		String masterKey = "";

		return "Registered as :" + user + "\nwith Client Id : " + clientId + "\nand  Master Key : " + masterKey
				+ "\n The ClientID and MasterKey is copied in a file. But, please save the clientId and Master key for future use.";
		
	}
}
