package com.ibm.gps.franceSickness.trackerReader;

public class SicknessDetail {

	public String name;
	public String employeeNumber;
	public String SS;
	public String workLocationCode;
	public String workLocationName;
	public String sicknessTypeFromTracker;
	public String firstYearEmployee;
	public String lastWorkDayThisSickness;
//	public String firstWorkDayThisSickness;
	public String firstSickDayThisMonthFromTracker;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getEmployeeNumber() {
		return employeeNumber;
	}
	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}
	public String getSS() {
		return SS;
	}
	public void setSS(String sS) {
		SS = sS;
	}
	public String getWorkLocationCode() {
		return workLocationCode;
	}
	public void setWorkLocationCode(String workLocationCode) {
		this.workLocationCode = workLocationCode;
	}
	public String getWorkLocationName() {
		return workLocationName;
	}
	public void setWorkLocationName(String workLocationName) {
		this.workLocationName = workLocationName;
	}
	public String getSicknessTypeFromTracker() {
		return sicknessTypeFromTracker;
	}
	public void setSicknessTypeFromTracker(String sicknessTypeFromTracker) {
		this.sicknessTypeFromTracker = sicknessTypeFromTracker;
	}
	public String getFirstYearEmployee() {
		return firstYearEmployee;
	}
	public void setFirstYearEmployee(String firstYearEmployee) {
		this.firstYearEmployee = firstYearEmployee;
	}
	public String getLastWorkDayThisSickness() {
		return lastWorkDayThisSickness;
	}
	public void setLastWorkDayThisSickness(String lastWorkDayThisSickness) {
		this.lastWorkDayThisSickness = lastWorkDayThisSickness;
	}
	public String getFirstSickDayThisMonthFromTracker() {
		return firstSickDayThisMonthFromTracker;
	}
	public void setFirstSickDayThisMonthFromTracker(
			String firstSickDayThisMonthFromTracker) {
		this.firstSickDayThisMonthFromTracker = firstSickDayThisMonthFromTracker;
	}
	
}
