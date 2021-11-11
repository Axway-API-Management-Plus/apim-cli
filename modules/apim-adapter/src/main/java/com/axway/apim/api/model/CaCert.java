package com.axway.apim.api.model;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFilter;

@JsonFilter("CaCertFilter")
public class CaCert {
	
	static Logger LOG = LoggerFactory.getLogger(CaCert.class);

	String certFile;
	
	String certBlob;
	
	String name;
	
	String alias;
	
	String subject;
	
	String issuer;
	
	String version;
	
	Long notValidBefore;
	
	Long notValidAfter;
	
	String signatureAlgorithm;
	
	String sha1Fingerprint;
	
	String md5Fingerprint;
	
	String expired;
	
	String notYetValid;
	
	String inbound;
	
	String outbound;

	public String getCertBlob() {
		return certBlob;
	}

	public void setCertBlob(String certBlob) {
		this.certBlob = certBlob;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getNotValidBefore() {
		return notValidBefore;
	}

	public void setNotValidBefore(Long notValidBefore) {
		this.notValidBefore = notValidBefore;
	}

	public Long getNotValidAfter() {
		return notValidAfter;
	}

	public void setNotValidAfter(Long notValidAfter) {
		this.notValidAfter = notValidAfter;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	public String getSha1Fingerprint() {
		return sha1Fingerprint;
	}

	public void setSha1Fingerprint(String sha1Fingerprint) {
		this.sha1Fingerprint = sha1Fingerprint;
	}

	public String getMd5Fingerprint() {
		return md5Fingerprint;
	}

	public void setMd5Fingerprint(String md5Fingerprint) {
		this.md5Fingerprint = md5Fingerprint;
	}

	public String getExpired() {
		return expired;
	}

	public void setExpired(String expired) {
		this.expired = expired;
	}

	public String getNotYetValid() {
		return notYetValid;
	}

	public void setNotYetValid(String notYetValid) {
		this.notYetValid = notYetValid;
	}

	public String getInbound() {
		return inbound;
	}

	public void setInbound(String inbound) {
		this.inbound = inbound;
	}

	public String getOutbound() {
		return outbound;
	}

	public void setOutbound(String outbound) {
		this.outbound = outbound;
	}

	public String getCertFile() {
		if(certFile==null) {
			String filename = null;
			String certAlias = this.getAlias();
			String[] nameParts = certAlias.split(",");
			for(String namePart : nameParts) {
				if(namePart.trim().startsWith("CN=")) {
					filename = namePart.trim().substring(3);
					break;
				}
			}
			if(filename == null) {
				LOG.warn("Could not create filename for certificate based on alias: " + this.getAlias());
				filename = "UnknownCertificate_" + ThreadLocalRandom.current().nextInt(1, 9999 + 1);
				LOG.warn("Created a random filename: " + filename + ".ctr");
			} else {
				filename = filename.replace(" ", "");
				filename = filename.replace("*", "");
				if(filename.startsWith(".")) filename = filename.replaceFirst(".", "");
			}
			certFile = filename+".crt";
			return certFile;
		}
		return certFile;
	}

	public void setCertFile(String certFile) {
		this.certFile = certFile;
	}

	public String getUseForInbound() {
		return inbound;
	}

	public void setUseForInbound(String useForInbound) {
		this.inbound = useForInbound;
	}

	public String getUseForOutbound() {
		return outbound;
	}

	public void setUseForOutbound(String useForOutbound) {
		this.outbound = useForOutbound;
	}

	@Override
	public String toString() {
		return "CaCert [name=" + name + ", alias=" + alias + ", md5Fingerprint=" + md5Fingerprint + "]";
	}

	@Override
	public boolean equals(Object o) {		
		if(o == null) return false;
		if(o instanceof CaCert) {
			CaCert otherCaCert = (CaCert)o;
			if(!otherCaCert.getCertBlob().equals(this.getCertBlob())) return false;
			if(!otherCaCert.getInbound().equals(this.getInbound())) return false;
			if(!otherCaCert.getOutbound().equals(this.getOutbound())) return false;
			return true;
		} else {
			return false;
		}
	}
}
