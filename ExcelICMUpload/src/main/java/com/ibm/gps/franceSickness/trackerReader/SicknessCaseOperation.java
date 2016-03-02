package com.ibm.gps.franceSickness.trackerReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;

import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.ibm.casemgmt.api.Case;
import com.ibm.casemgmt.api.CaseType;
import com.ibm.casemgmt.api.constants.ModificationIntent;
import com.ibm.casemgmt.api.context.CaseMgmtContext;
import com.ibm.casemgmt.api.context.P8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleP8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleVWSessionCache;
import com.ibm.casemgmt.api.objectref.ObjectStoreReference;
import com.ibm.casemgmt.api.properties.CaseMgmtProperties;
import com.ibm.casemgmt.api.properties.CaseMgmtProperty;
import com.ibm.gps.franceSickness.trackerReader.IBMTrackerReader;
import com.ibm.gps.franceSickness.trackerReader.SicknessDetail;

import filenet.vw.api.VWException;
import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWParticipant;
import filenet.vw.api.VWRoster;
import filenet.vw.api.VWRosterQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWWorkObject;

public class SicknessCaseOperation {

	private static String CE_URI = "http://9.51.192.8:9080/wsi/FNCEWS40MTOM/";
	private static String USER_NAME = "P8Admin";
	private static String PASSWORD = "CaseMgmp0c";
	private static String CP = "P8ConnPt1";

	private static String recordFileName = "C:/CMPocData.txt";

	// POCv2
	// private static String ROSTER_BAME = "POCv2";
	// private static String solutionPrefix = "POC2_";

	// POCv3
	// private static String ROSTER_BAME = "POCv3";
	// private static String solutionPrefix = "POC3_";

	// POCv4
	// private static String ROSTER_BAME = "POCv4";
    // private static String solutionPrefix = "POC4_";
	 
	 private static String ROSTER_BAME = "POCv7";
	 private static String solutionPrefix = "POC7_";
	 private static String identifyPrefix = "Array-";
	 
	private static final String PROPERTY_NAME_NAME = solutionPrefix + identifyPrefix + "Name";
	private static final String PROPERTY_NAME_EMPLOYEENUMBER = solutionPrefix + identifyPrefix + "EmployeeNumber";
	private static final String PROPERTY_NAME_SS = solutionPrefix + identifyPrefix + "SS";
	private static final String PROPERTY_NAME_WORKLOCATIONCODE = solutionPrefix + identifyPrefix + "WorkLocationCode";
	private static final String PROPERTY_NAME_WORKLOCATIONNAME = solutionPrefix + identifyPrefix + "WorkLocationName";
	private static final String PROPERTY_NAME_SICKNESSTYPEFROMTRACKER = solutionPrefix + identifyPrefix + "SicknessTypeFromTracker";
	private static final String PROPERTY_NAME_FIRSTYEAREMPLOYEE = solutionPrefix + identifyPrefix + "FirstYearEmployee";
	private static final String PROPERTY_NAME_LASTWORKDAYTHISSICKNESS = solutionPrefix + identifyPrefix + "LastWorkDayThisSickness";
	private static final String PROPERTY_NAME_FIRSTSICKDAYTHISMONTHFROMTRACKER = solutionPrefix + identifyPrefix + "FirstSickDayThisMonthFromTracker";

	private static final String PROPERTY_NAME_LIST_SICKCASEID = solutionPrefix + "List_SickCaseId";
	private static final String PROPERTY_NAME_LIST_EMPLOYEENUMBER = solutionPrefix + "List_EmployeeNumber";
	private static final String PROPERTY_NAME_LIST_EMPLOYEENAME = solutionPrefix + "List_EmployeeName";
	private static final String PROPERTY_NAME_LIST_PRACTITIONERNAME = solutionPrefix + "List_PractitionerName";
	private static final String PROPERTY_NAME_LIST_NUMOFSICKDAYS_IBM = solutionPrefix + "List_NumOfSickDays_IBM";
	private static final String PROPERTY_NAME_LIST_AMOUNTPAID_IBMCALC = solutionPrefix + "List_AmountPaid_IBMCalc";

	private static String caseTypeNameBatch = solutionPrefix + "DailyCPAMBatch";
	private static String caseTypeNameSickOccur = solutionPrefix + "SickEmployeeOccurrence";
	private static String targetObjectStoreName = "TARGET";
	private ObjectStore objStoreTarget;
	private ObjectStoreReference objStoreRefTarget;

	private CaseType caseTypeSickOccur;
	private CaseType caseTypeBatch;

	public SicknessCaseOperation() {
		P8ConnectionCache connCache = new SimpleP8ConnectionCache();
		Connection conn = connCache.getP8Connection(CE_URI);
		Subject subject = UserContext.createSubject(conn, USER_NAME, PASSWORD, "FileNetP8WSI");
		UserContext uc = UserContext.get();
		uc.pushSubject(subject);
		Locale origLocale = uc.getLocale();
		// uc.setLocale(1);
		CaseMgmtContext origCmctx = CaseMgmtContext.set(new CaseMgmtContext(new SimpleVWSessionCache(), connCache));
		Domain dom = Factory.Domain.fetchInstance(conn, null, null);

		objStoreTarget = Factory.ObjectStore.fetchInstance(dom, targetObjectStoreName, null);

		objStoreRefTarget = new ObjectStoreReference(objStoreTarget);

		caseTypeSickOccur = CaseType.fetchInstance(objStoreRefTarget, caseTypeNameSickOccur);
		caseTypeBatch = CaseType.fetchInstance(objStoreRefTarget, caseTypeNameBatch);

	}

	public static void main(String[] args) {
		printRecordLog("\n");
		printRecordLog("Start POC data preparation..");
		SicknessCaseOperation operation = new SicknessCaseOperation();
//		operation.deleteWorkflows();
		
		try {
			operation.createPocCase("C:\\spy\\ops\\dataR - Z sickness tracker - Katinka.xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		operation.appendSickOccurCases();
		printRecordLog("End POC data preparation..");
	}

	public void appendSickOccurCases() {
		
		// --------- REPLACE batch ID in the below ---------------------------
		
		Id batchCaseId = new Id("{E0E41352-0000-CF58-BF6E-B1C4CBCC81EB}");
		
		// --------- REPLACE batch ID in the above ---------------------------
		
		
		PropertyFilter propFilt = getBatchPropertyFilter();
		
		Case caseBatch = Case.fetchInstance(objStoreRefTarget, batchCaseId, propFilt, ModificationIntent.MODIFY);


		Case sickCase = createSickOccurrenceCase("E009", "Paul", 9, 900, "Susan", false);
		appendOneSickOccurCase(caseBatch, sickCase);

		sickCase = createSickOccurrenceCase("E010", "David", 10, 1000, "John", true);
		appendOneSickOccurCase(caseBatch, sickCase);

	
	}

	public void createPocCase(String excelFilePath) throws IOException {
		
		ArrayList<String> caseIdList = new ArrayList<String>();
		
		IBMTrackerReader reader = new IBMTrackerReader();
	    List<SicknessDetail> listSickOccur = reader.readsicknessDetailFromExcelFile(excelFilePath);
		
	    if(null != listSickOccur && listSickOccur.size() != 0){
	    	Case sickCase;
	    	for(SicknessDetail item:listSickOccur){
	    		sickCase = createSickOccurrenceCase(item);
	    		caseIdList.add(sickCase.getId().toString());
	    	}
	    }else{
	    	//TODO:confirm how to deal with the empty document
	    }
	    
		// Add the batch case
		createBatchCase(caseIdList);

	}

	public Case createBatchCase(ArrayList<String> caseIdList) {
		printRecordLog("Start create Batch Case instance..");

		Case pendingCaseBatch = Case.createPendingInstanceFetchDefaults(caseTypeBatch);

		CaseMgmtProperties propertiesBatch = pendingCaseBatch.getProperties();

		propertiesBatch.putObjectValue(solutionPrefix + "List_SickCaseId", caseIdList.toArray());

		PropertyFilter propFilt = getBatchPropertyFilter();
		
		pendingCaseBatch.save(RefreshMode.REFRESH, propFilt, ModificationIntent.MODIFY);

		printRecordLog("Created ##Batch Case ID##: " + pendingCaseBatch.getId().toString());
		printRecordLog("End create Batch Case instance..");
		return pendingCaseBatch;
	}
	
	public Case createSickOccurrenceCase(String employeeNumber, String employeeName, float numOfSickDays_IBM,
			float amountPaied_IBM, String practitionerName, boolean isWorkAccident){
		
		printRecordLog("Start create SicknessOccurence Case instance..");

		Case pendingCaseSickOccur = Case.createPendingInstanceFetchDefaults(caseTypeSickOccur);

		CaseMgmtProperties propertiesSickOccur = pendingCaseSickOccur.getProperties();

//		propertiesSickOccur.putObjectValue(PROPERTY_NAME_EMPLOYEENUMBER, employeeNumber);
//		propertiesSickOccur.putObjectValue(PROPERTY_NAME_EMPLOYEENAME, employeeName);
//		propertiesSickOccur.putObjectValue(PROPERTY_NAME_NUMOFSICKDAYS_IBM, numOfSickDays_IBM);
//		propertiesSickOccur.putObjectValue(PROPERTY_NAME_AMOUNTPAID_IBMCALC, amountPaied_IBM);
//		propertiesSickOccur.putObjectValue(PROPERTY_NAME_PRACTITIONERNAME, practitionerName);
//		propertiesSickOccur.putObjectValue(PROPERTY_NAME_ISWORKACCIDENT, isWorkAccident);
//
		PropertyFilter propFilt = new PropertyFilter();
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ID, null));
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_EMPLOYEENUMBER, null));
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_EMPLOYEENAME, null));
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_NUMOFSICKDAYS_IBM, null));
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_AMOUNTPAID_IBMCALC, null));
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_PRACTITIONERNAME, null));
//		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_ISWORKACCIDENT, null));

		pendingCaseSickOccur.save(RefreshMode.REFRESH, propFilt, ModificationIntent.MODIFY);

		printRecordLog("Created ##SicknessOccurence Case ID##: " + pendingCaseSickOccur.getId().toString());
		printRecordLog("End create SicknessOccurence Case instance..");
		return pendingCaseSickOccur;
		
	}
	
	public Case createSickOccurrenceCase(SicknessDetail sicknessDetail) {
		printRecordLog("Start create SicknessOccurence Case instance..");

		Case pendingCaseSickOccur = Case.createPendingInstanceFetchDefaults(caseTypeSickOccur);

		CaseMgmtProperties propertiesSickOccur = pendingCaseSickOccur.getProperties();

		propertiesSickOccur.putObjectValue(PROPERTY_NAME_NAME, sicknessDetail.getName());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_EMPLOYEENUMBER, sicknessDetail.getEmployeeNumber());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_SS, sicknessDetail.getSS());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_WORKLOCATIONCODE, sicknessDetail.getWorkLocationCode());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_WORKLOCATIONNAME, sicknessDetail.getWorkLocationName());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_SICKNESSTYPEFROMTRACKER, sicknessDetail.getSicknessTypeFromTracker());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_FIRSTYEAREMPLOYEE, sicknessDetail.getFirstYearEmployee());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_LASTWORKDAYTHISSICKNESS, sicknessDetail.getLastWorkDayThisSickness());
		propertiesSickOccur.putObjectValue(PROPERTY_NAME_FIRSTSICKDAYTHISMONTHFROMTRACKER, sicknessDetail.getFirstSickDayThisMonthFromTracker());

		PropertyFilter propFilt = new PropertyFilter();
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ID, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_NAME, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_EMPLOYEENUMBER, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_SS, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_WORKLOCATIONCODE, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_WORKLOCATIONNAME, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_SICKNESSTYPEFROMTRACKER, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_FIRSTYEAREMPLOYEE, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LASTWORKDAYTHISSICKNESS, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_FIRSTSICKDAYTHISMONTHFROMTRACKER, null));

		pendingCaseSickOccur.save(RefreshMode.REFRESH, propFilt, ModificationIntent.MODIFY);

		printRecordLog("Created ##SicknessOccurence Case ID##: " + pendingCaseSickOccur.getId().toString());
		printRecordLog("End create SicknessOccurence Case instance..");
		return pendingCaseSickOccur;
	}
	
	private void appendOneSickOccurCase(Case caseBatch, Case caseSickOccur) {
		
		CaseMgmtProperties propertiesSick = caseSickOccur.getProperties();
		String id = caseSickOccur.getId().toString();
		String employeeNumber = (String)propertiesSick.getObjectValue(PROPERTY_NAME_EMPLOYEENUMBER);
		/*String employeeName = (String)propertiesSick.getObjectValue(PROPERTY_NAME_EMPLOYEENAME);
		String practitionerName = (String)propertiesSick.getObjectValue(PROPERTY_NAME_PRACTITIONERNAME);
		double numOfSickDays_IBM = (double)propertiesSick.getObjectValue(PROPERTY_NAME_NUMOFSICKDAYS_IBM);
		double amountPaid_IBMCalc = (double)propertiesSick.getObjectValue(PROPERTY_NAME_AMOUNTPAID_IBMCALC);*/
		
		CaseMgmtProperties propertiesBatch = caseBatch.getProperties();
		
		putNewValueToStringList(propertiesBatch, PROPERTY_NAME_LIST_SICKCASEID, id);
		putNewValueToStringList(propertiesBatch, PROPERTY_NAME_LIST_EMPLOYEENUMBER, employeeNumber);
		/*putNewValueToStringList(propertiesBatch, PROPERTY_NAME_LIST_EMPLOYEENAME, employeeName);
		putNewValueToStringList(propertiesBatch, PROPERTY_NAME_LIST_PRACTITIONERNAME, practitionerName);
		putNewValueToDoubleList(propertiesBatch, PROPERTY_NAME_LIST_NUMOFSICKDAYS_IBM, numOfSickDays_IBM);
		putNewValueToDoubleList(propertiesBatch, PROPERTY_NAME_LIST_AMOUNTPAID_IBMCALC, amountPaid_IBMCalc);*/
		
		PropertyFilter propFilt = getBatchPropertyFilter();
		caseBatch.save(RefreshMode.REFRESH, propFilt, ModificationIntent.MODIFY);
	}

	private PropertyFilter getBatchPropertyFilter() {
		PropertyFilter propFilt = new PropertyFilter();
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PropertyNames.ID, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LIST_SICKCASEID, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LIST_EMPLOYEENUMBER, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LIST_EMPLOYEENAME, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LIST_PRACTITIONERNAME, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LIST_NUMOFSICKDAYS_IBM, null));
		propFilt.addIncludeProperty(new FilterElement(null, null, null, PROPERTY_NAME_LIST_AMOUNTPAID_IBMCALC, null));
		return propFilt;
	}
	
	private CaseMgmtProperties putNewValueToStringList(CaseMgmtProperties propertiesBatch, String propertyName, String newValue) {
		Object[] oldList = (Object[])((List<Object>)propertiesBatch.getObjectValue(propertyName)).toArray();
		if(oldList == null) {
			return propertiesBatch;
		}
		int origLength = oldList.length;
		
		ArrayList<String> appendedList = new ArrayList<String>();
		for(int i = 0; i < origLength; i++) {
			appendedList.add((String)oldList[i]);
		}
		appendedList.add(newValue);
		propertiesBatch.putObjectValue(propertyName, appendedList.toArray());
		return propertiesBatch;
	}

	private CaseMgmtProperties putNewValueToDoubleList(CaseMgmtProperties propertiesBatch, String propertyName, double newValue) {
		Object[] oldList = (Object[])((List<Object>)propertiesBatch.getObjectValue(propertyName)).toArray();
		if(oldList == null) {
			return propertiesBatch;
		}
		int origLength = oldList.length;
		
		ArrayList<Double> appendedList = new ArrayList<Double>();
		for(int i = 0; i < origLength; i++) {
			appendedList.add((double)oldList[i]);
		}
		appendedList.add(newValue);
		propertiesBatch.putObjectValue(propertyName, appendedList.toArray());
		return propertiesBatch;
	}

	public void printProperties(Case caseInstance) {
		CaseMgmtProperties properties = caseInstance.getProperties();
		List<CaseMgmtProperty> listPropertiesBatch = properties.asList();
		for (CaseMgmtProperty property : listPropertiesBatch) {
			System.out.println(
					property.getSymbolicName() + ", " + property.getDisplayName() + ", " + property.getValue());
		}
	}

	public void deleteWorkflows() {
		printRecordLog("Start delete workflows..");
		// Set the Search parameters to make the search more accurate, like the
		// where clause in SQL
		SearchParameter[] searchParams = new SearchParameter[1];
		searchParams[0] = new SearchParameter("1", "int", "1", "=");

		// The max loop
		int expectLoop = 100;

		// Se the max number of workobject to return, recommend < 500
		int maxreturn = 10;

		int i = 0;
		while (true) {
			VWWorkObject[] vwWorkObjects = queryVWWorkObjects(searchParams, maxreturn);
			if (vwWorkObjects != null) {
				printRecordLog("WorkObject number queried: " + vwWorkObjects.length);
				deleteWorkObjects(vwWorkObjects);
			} else {
				printRecordLog("No WorkObject queried, stop delete");
				break;
			}

			i++;
			if (i >= expectLoop) {
				printRecordLog("Excepted loop count " + expectLoop + " reached, stop delete.");
				break;
			}
		}

		printRecordLog("End delete workflows..");
	}

	private void deleteWorkObjects(VWWorkObject[] workObjects) {
		try {
			VWWorkObject.doDeleteMany(workObjects, true, true);
			System.out.println("delete batch successfully");
		} catch (VWException vwe) {
			System.out.println("delete batch failed");
			vwe.printStackTrace();
		}
	}

	private VWWorkObject[] queryVWWorkObjects(SearchParameter[] searchParameters, int maxreturn) {
		VWSession vwSession = new VWSession(USER_NAME, PASSWORD, CP);

		StringBuffer searchFilter = new StringBuffer("");
		Object[] substitutionVars = getFiltersFromSearchParameters(searchParameters, searchFilter);
		VWWorkObject[] vwWorkObjects = null;
		try {

			vwWorkObjects = getWorkObjectsFromRosters(vwSession, ROSTER_BAME, VWRoster.QUERY_READ_UNWRITABLE,
					searchFilter.toString(), substitutionVars, maxreturn);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (vwWorkObjects != null && vwWorkObjects.length > 0) {
			return vwWorkObjects;
		} else {
			return null;
		}
	}

	public static Object[] getFiltersFromSearchParameters(SearchParameter[] searchParams, StringBuffer filterSb) {
		ArrayList subValuesList = new ArrayList();
		int paramCount = searchParams.length;
		int validParamCount = 0;
		for (int i = 0; i < paramCount; i++) {
			SearchParameter param = searchParams[i];
			String paramValue = param.getParamValue();
			if (paramValue != null && !paramValue.trim().equalsIgnoreCase("")) {
				String operator = param.getRelationOperator();
				String paramName = param.getParamName();

				StringBuffer tmpStrSb = new StringBuffer();
				boolean validType = true;
				Object subValue = null;
				if (operator.equalsIgnoreCase(SearchParameter.OPERATOR_LIKE)) {
					tmpStrSb.append(paramName).append(" like '%").append(replaceQuoteMarks(paramValue)).append("%'");
				} else {
					tmpStrSb.append(paramName).append(" ").append(operator).append(" :")
							.append(subValuesList.size() + 1);
					String paramType = param.getParamType();
					if (paramType == null || paramType.trim().equals("")
							|| paramType.equalsIgnoreCase(SearchParameter.TYPE_STRING)) {

						subValue = paramValue;
					} else if (paramType.equalsIgnoreCase(SearchParameter.TYPE_BOOLEAN)) {
						subValue = new Boolean(paramValue);

					} else if (paramType.equalsIgnoreCase(SearchParameter.TYPE_FLOAT)) {
						try {
							subValue = new Float(paramValue);
						} catch (NumberFormatException ex) {
							validType = false;
						}

					} else if (paramType.equalsIgnoreCase(SearchParameter.TYPE_INT)) {

						try {
							subValue = new Integer(paramValue);
						} catch (NumberFormatException ex) {
							validType = false;
						}

					} else if (paramType.equalsIgnoreCase(SearchParameter.TYPE_TIME)) {
						try {

							subValue = parseDate(paramValue);
						} catch (ParseException e) {
							validType = false;
						}

					} else if (paramType.equalsIgnoreCase(SearchParameter.TYPE_PARTICIPANT)) {
						subValue = new VWParticipant(paramValue);
					} else {
						validType = false;
					}

				}
				if (validType) {
					if (validParamCount > 0) {
						filterSb.append(" and ");
					}
					filterSb.append(tmpStrSb);
					if (subValue != null) {
						subValuesList.add(subValue);
					}
					validParamCount++;
				}
			}
		}

		return subValuesList.toArray();

	}

	private static String replaceQuoteMarks(String paramValue) {
		return paramValue.replaceAll("'", "''");
	}

	public static Date parseDate(String dateStr) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat();
		return sdf.parse(dateStr);
	}

	public static VWWorkObject[] getWorkObjectsFromRosters(VWSession session, String RosterName, int queryFlag,
			String filter, Object[] substitutionVars, int maxreturn) throws VWException {

		int realMaxReturn = 100;
		if (maxreturn > 0) {
			realMaxReturn = maxreturn;
		}

		List elementList = new ArrayList();
		if (queryFlag == -1) {
			queryFlag = VWRoster.QUERY_READ_UNWRITABLE;
		}
		VWRoster roster = session.getRoster(RosterName);
		// roster.setBufferSize(1);
		VWRosterQuery query = null;
		try {
			query = roster.createQuery(null, null, null, queryFlag, filter, substitutionVars,
					VWFetchType.FETCH_TYPE_WORKOBJECT);
			query.setBufferSize(2);
			int i = 0;
			while (query.hasNext()) {
				i++;
				elementList.add(query.next());
				if (i >= realMaxReturn)
					break;
			}
		} catch (VWException vwe) {
			throw vwe;
		}
		return workObjectElementList2Array(elementList);
	}

	private static VWWorkObject[] workObjectElementList2Array(List elementList) {
		VWWorkObject[] elementArray = new VWWorkObject[elementList.size()];
		Iterator it = elementList.iterator();
		int i = 0;
		while (it.hasNext()) {
			elementArray[i++] = (VWWorkObject) it.next();
		}
		return elementArray;

	}

	public static void printRecordLog(String line) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(recordFileName, true)))) {
			System.out.println("[" + new Date() + "]");
			out.print("[" + new Date() + "]");
			System.out.println(line);
			out.println(line);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
