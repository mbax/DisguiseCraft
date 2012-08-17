package pgDev.bukkit.DisguiseCraft.update;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;


public class DCUpdateChecker {
	
	public static String dcPage = "http://dev.bukkit.org/server-mods/disguisecraft/";
	
	public static String getLatestVersion() {
		try {
			URL devPage = new URL(dcPage);
			BufferedReader in = new BufferedReader(new InputStreamReader(devPage.openStream()));
			
			String importantLine = "";
			String line;
			while ((line = in.readLine()) != null) {
				if (line.trim().equalsIgnoreCase("<dt>Recent files</dt>")) {
					importantLine = filterHTML(in.readLine());
				}
			}
			
			in.close();
			
			if (importantLine.equals("")) {
				return "Error while checking!";
			}
			String[] files = importantLine.split("R: ");
			return files[1].trim();
		} catch (Exception e) {
			DisguiseCraft.logger.log(Level.WARNING , "Error checking for updates", e);
		}
		return "Error during check!";
	}
	
	// Filter out HTML
	public static String filterHTML(String toFilter) {
		String HTMLfiltered = "";
		boolean HTMLtag = false;
		for (int i=0; i<toFilter.length(); i++) {
			if (toFilter.charAt(i) == '<') {
				HTMLtag = true;
			} else if (toFilter.charAt(i) == '>') {
				if (HTMLtag == false) {
					HTMLfiltered = HTMLfiltered + toFilter.charAt(i);
				}
				else {
					HTMLtag = false;
				}
			} else {
				if (HTMLtag == false) {
					HTMLfiltered = HTMLfiltered + toFilter.charAt(i);
				}
			}
		}
		return HTMLfiltered;
	}
}
