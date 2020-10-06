package com.axway.apim.lib.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class Utils {
	
	public static enum FedKeyType {
		FilterCircuit("<key type='FilterCircuit'>"), 
		OAuthAppProfile("<key type='OAuthAppProfile'>");
		
		private String keyType;
		
		FedKeyType(String keyType) {
			this.keyType = keyType;
		}

		public String getKeyType() {
			return keyType;
		}
	}
	
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
    	return getExternalPolicyName(policy, null);
    }
    
	public static String getExternalPolicyName(String policy, FedKeyType keyType) {
		if(keyType==null) keyType = FedKeyType.FilterCircuit;
		if(policy.startsWith("<key")) {
			policy = policy.substring(policy.indexOf(keyType.getKeyType()));
			policy = policy.substring(policy.indexOf("value='")+7, policy.lastIndexOf("'/></key>"));
		}
		return policy;
	}
}
