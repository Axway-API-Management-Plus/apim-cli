package com.axway.apim.api.model;

import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.axway.apim.adapter.jackson.OrganizationSerializer;
import com.axway.apim.adapter.jackson.UserDeserializer;
import com.axway.apim.adapter.jackson.UserSerializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("RemoteHostFilter")
public class RemoteHost {
	String id;
	
	String name;
	
	Integer port;
	
	Long createdOn;
	
	@JsonDeserialize( using = UserDeserializer.class)
	User createdBy;
	
	@JsonDeserialize( using = OrganizationDeserializer.class)
	@JsonSerialize( using = OrganizationSerializer.class)
	@JsonAlias({ "organization", "organizationId" })
	Organization organization;
	
	Integer maxConnections;
	
	Boolean allowHTTP11;
	
	Boolean includeContentLengthRequest;
	
	Boolean includeContentLengthResponse;
	
	Boolean offerTLSServerName;
	
	Boolean verifyServerHostname;
	
	Integer connectionTimeout;
	
	Integer activeTimeout;
	
	Integer transactionTimeout;
	
	Integer idleTimeout;
	
	Integer maxReceiveBytes;
	
	Integer maxSendBytes;
	
	Integer inputBufferSize;
	
	Integer outputBufferSize;
	
	Integer addressCacheTimeout;
	
	Integer sslSessionCacheSize;
	
	Boolean exportCorrelationId;
	
	String[] inputEncodings;
	
	String[] outputEncodings;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	
	public String getOrganizationId() {
		return organization.getId();
	}

	public Integer getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	public Boolean getAllowHTTP11() {
		return allowHTTP11;
	}

	public void setAllowHTTP11(Boolean allowHTTP11) {
		this.allowHTTP11 = allowHTTP11;
	}

	public Boolean getIncludeContentLengthRequest() {
		return includeContentLengthRequest;
	}

	public void setIncludeContentLengthRequest(Boolean includeContentLengthRequest) {
		this.includeContentLengthRequest = includeContentLengthRequest;
	}

	public Boolean getIncludeContentLengthResponse() {
		return includeContentLengthResponse;
	}

	public void setIncludeContentLengthResponse(Boolean includeContentLengthResponse) {
		this.includeContentLengthResponse = includeContentLengthResponse;
	}

	public Boolean getOfferTLSServerName() {
		return offerTLSServerName;
	}

	public void setOfferTLSServerName(Boolean offerTLSServerName) {
		this.offerTLSServerName = offerTLSServerName;
	}

	public Boolean getVerifyServerHostname() {
		return verifyServerHostname;
	}

	public void setVerifyServerHostname(Boolean verifyServerHostname) {
		this.verifyServerHostname = verifyServerHostname;
	}

	public Integer getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Integer getActiveTimeout() {
		return activeTimeout;
	}

	public void setActiveTimeout(Integer activeTimeout) {
		this.activeTimeout = activeTimeout;
	}

	public Integer getTransactionTimeout() {
		return transactionTimeout;
	}

	public void setTransactionTimeout(Integer transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}

	public Integer getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(Integer idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public Integer getMaxReceiveBytes() {
		return maxReceiveBytes;
	}

	public void setMaxReceiveBytes(Integer maxReceiveBytes) {
		this.maxReceiveBytes = maxReceiveBytes;
	}

	public Integer getMaxSendBytes() {
		return maxSendBytes;
	}

	public void setMaxSendBytes(Integer maxSendBytes) {
		this.maxSendBytes = maxSendBytes;
	}

	public Integer getInputBufferSize() {
		return inputBufferSize;
	}

	public void setInputBufferSize(Integer inputBufferSize) {
		this.inputBufferSize = inputBufferSize;
	}

	public Integer getOutputBufferSize() {
		return outputBufferSize;
	}

	public void setOutputBufferSize(Integer outputBufferSize) {
		this.outputBufferSize = outputBufferSize;
	}

	public Integer getAddressCacheTimeout() {
		return addressCacheTimeout;
	}

	public void setAddressCacheTimeout(Integer addressCacheTimeout) {
		this.addressCacheTimeout = addressCacheTimeout;
	}

	public Integer getSslSessionCacheSize() {
		return sslSessionCacheSize;
	}

	public void setSslSessionCacheSize(Integer sslSessionCacheSize) {
		this.sslSessionCacheSize = sslSessionCacheSize;
	}

	public Boolean getExportCorrelationId() {
		return exportCorrelationId;
	}

	public void setExportCorrelationId(Boolean exportCorrelationId) {
		this.exportCorrelationId = exportCorrelationId;
	}

	public String[] getInputEncodings() {
		return inputEncodings;
	}

	public void setInputEncodings(String[] inputEncodings) {
		this.inputEncodings = inputEncodings;
	}

	public String[] getOutputEncodings() {
		return outputEncodings;
	}

	public void setOutputEncodings(String[] outputEncodings) {
		this.outputEncodings = outputEncodings;
	}
}
