package com.axway.apim.lib.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class Utils {
	
	private static Logger LOG = LoggerFactory.getLogger(Utils.class);
	
	public static String getAPIDefinitionUriFromFile(String pathToAPIDefinition) throws AppException {
		String uriToAPIDefinition = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToAPIDefinition));
			uriToAPIDefinition = br.readLine();
			return uriToAPIDefinition;
		} catch (Exception e) {
			throw new AppException("Can't load file:" + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				br.close();
			} catch (Exception ignore) {}
		}
	}
	
	public static void progressPercentage(int remain, int total, String prefix) {
	    if (remain > total) {
	        throw new IllegalArgumentException();
	    }
	    int maxBareSize = 10; // 10unit for 100%
	    int remainProcent = ((100 * remain) / total) / maxBareSize;
	    char defaultChar = '-';
	    String icon = "*";
	    String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
	    StringBuilder bareDone = new StringBuilder();
	    bareDone.append(prefix + " [");
	    for (int i = 0; i < remainProcent; i++) {
	        bareDone.append(icon);
	    }
	    String bareRemain = bare.substring(remainProcent, bare.length());
	    System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 10 + "%");
	    if (remain == total) {
	        System.out.print("\n");
	    }
	}
	
	public static boolean askYesNo(String question) {
        return Utils.askYesNo(question, "[Y]", "[N]");
    }
	
    public static boolean askYesNo(String question, String positive, String negative) {
        Scanner input = new Scanner(System.in);
        // Convert everything to upper case for simplicity...
        positive = positive.toUpperCase();
        negative = negative.toUpperCase();
        String answer;
        do {
            System.out.print(question+ " ");
            answer = input.next().trim().toUpperCase();
        } while (!answer.matches(positive) && !answer.matches(negative));
        input.close();
        // Assess if we match a positive response
        return answer.matches(positive);
    }
    
	public static String getExternalPolicyName(String policy) {
		if(policy.startsWith("<key")) {
			policy = policy.substring(policy.indexOf("<key type='FilterCircuit'>"));
			policy = policy.substring(policy.indexOf("value='")+7, policy.lastIndexOf("'/></key>"));
		}
		return policy;
	}
	
	/**
	 * This method is replacing variables such as ${TokenEndpoint} with declared variables coming from either 
	 * the Environment-Variables or from system-properties.
	 * @param inputFile The API-Config file to be replaced and returned as String
	 * @return a String representation of the API-Config-File
	 * @throws IOException if the file can't be found
	 */
	public static String substitueVariables(File inputFile) throws IOException {
		StringSubstitutor substitutor = new StringSubstitutor(CoreParameters.getInstance().getProperties());
		String givenConfig = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
		givenConfig = StringSubstitutor.replace(givenConfig, System.getenv());
		return substitutor.replace(givenConfig);
	}
	
	public static File getStageConfig(String stage, File baseConfigFile) {
		if(stage == null) return null;
		File stageFile = new File(stage);
		if(stageFile.exists()) { // This is to support testing with dynamically created files!
			return stageFile;
		}
		if(!stage.equals("NOT_SET")) {
			String baseConfig;
			try {
				baseConfig = baseConfigFile.getCanonicalPath();
				stageFile = new File(baseConfig.substring(0, baseConfig.lastIndexOf(".")+1) + stage + baseConfig.substring(baseConfig.lastIndexOf(".")));
				File subDirStageFile = new File(stageFile.getParentFile()+"/"+stage+"/"+stageFile.getName());
				if(stageFile.exists()) {
					return stageFile;
				} else if(subDirStageFile.exists()) {
					return subDirStageFile;
				} else {
					return null;
				}
			} catch (IOException e) {
				LOG.error("Error reading stage config file", e);
				return null;
			}
		}
		LOG.debug("No stage provided");
		return null;
	}
	
	public static File locateConfigFile(String configFileName) throws AppException {
		try {
			configFileName = URLDecoder.decode(configFileName, "UTF-8");
			File configFile = new File(configFileName);
			if(configFile.exists()) return configFile;
			// This is mainly to load the samples sitting inside the package!
			String installFolder = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getParent();
			configFile = new File(installFolder + File.separator + configFileName);
			if(configFile.exists()) return configFile;
			throw new AppException("Unable to find given Config-File: '"+configFileName+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		} catch (Exception e) {
			throw new AppException("Unable to find given Config-File: '"+configFileName+"'", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}
}
