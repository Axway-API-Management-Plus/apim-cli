package com.axway.apim.api.model;

public class LockUserAccount {
	
	public static enum TimePeriodUnit {
		week, 
		day,
		hour,
		minute, 
		second
	}
	
	private Boolean enabled;
	
	private Integer attempts;
	
	private Integer timePeriod;
	
	private TimePeriodUnit timePeriodUnit;
	
	private Integer lockTimePeriod;
	
	private TimePeriodUnit lockTimePeriodUnit;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public Integer getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(Integer timePeriod) {
		this.timePeriod = timePeriod;
	}

	public TimePeriodUnit getTimePeriodUnit() {
		return timePeriodUnit;
	}

	public void setTimePeriodUnit(TimePeriodUnit timePeriodUnit) {
		this.timePeriodUnit = timePeriodUnit;
	}

	public Integer getLockTimePeriod() {
		return lockTimePeriod;
	}

	public void setLockTimePeriod(Integer lockTimePeriod) {
		this.lockTimePeriod = lockTimePeriod;
	}

	public TimePeriodUnit getLockTimePeriodUnit() {
		return lockTimePeriodUnit;
	}

	public void setLockTimePeriodUnit(TimePeriodUnit lockTimePeriodUnit) {
		this.lockTimePeriodUnit = lockTimePeriodUnit;
	}

	@Override
	public String toString() {
		return "LockUserAccount [enabled=" + enabled + ", attempts=" + attempts + ", timePeriod=" + timePeriod
				+ ", timePeriodUnit=" + timePeriodUnit + ", lockTimePeriod=" + lockTimePeriod + ", lockTimePeriodUnit="
				+ lockTimePeriodUnit + "]";
	}
}
