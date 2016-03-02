package com.ibm.gps.franceSickness.trackerReader;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.gps.franceSickness.util.StringUtil;

public class IBMTrackerReader {
	private Object getCellValue(Cell cell) {
		System.out.println("cell type " +cell.getCellType());
	    switch (cell.getCellType()) {
	    case Cell.CELL_TYPE_STRING:
	        return cell.getStringCellValue();
	 
	    case Cell.CELL_TYPE_BOOLEAN:
	        return cell.getBooleanCellValue();
	 
	    case Cell.CELL_TYPE_NUMERIC:
	        return cell.getNumericCellValue();
	    }
	   
	 
	    return "";
	}
	
	private String[] splitValue(String text, String symbol){
		
		String[] valueArray;
		if(StringUtil.isNotEmpty(text)){
			valueArray = text.split(symbol);
		}else{
			valueArray = null;
		}
		
		return valueArray;
	}
	
	public List<SicknessDetail> readsicknessDetailFromExcelFile(String excelFilePath) throws IOException {
	    List<SicknessDetail> listSickOccur = new ArrayList<SicknessDetail>();
	    FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
	 
	    Workbook workbook = getWorkbook(inputStream,excelFilePath);
	    Sheet firstSheet = workbook.getSheetAt(0);
	    Iterator<Row> iterator = firstSheet.iterator();
	   iterator.next();
	    while (iterator.hasNext()) {
	        Row nextRow = iterator.next();
	        Iterator<Cell> cellIterator = nextRow.cellIterator();
	        SicknessDetail sicknessDetail = new SicknessDetail();
	 
	        while (cellIterator.hasNext()) {
	            Cell nextCell = cellIterator.next();
	            int columnIndex = nextCell.getColumnIndex();
	 System.out.println(columnIndex);
	 System.out.println( getCellValue(nextCell));
	            switch (columnIndex) {
	            case 0:
	            	sicknessDetail.setName(String.valueOf(getCellValue(nextCell)));
	            	
	                break;
	                
	            case 1:
	            	String[] location = splitValue(String.valueOf(getCellValue(nextCell)),"-");
	            	if(null  != location && location.length == 2){
	            		sicknessDetail.setWorkLocationCode(location[0]);
	            		sicknessDetail.setWorkLocationName(location[1]);
	            	}
	            	break;
	            case 2:
	            	sicknessDetail.setEmployeeNumber(String.valueOf(getCellValue(nextCell)));
	                break;
	            case 3:
	            	sicknessDetail.setSS(String.valueOf(getCellValue(nextCell)));
	            case 4:
	            	sicknessDetail.setSicknessTypeFromTracker(String.valueOf(getCellValue(nextCell)));
	            case 5:
	            	sicknessDetail.setFirstYearEmployee(String.valueOf(getCellValue(nextCell)));
	                break;
	            case 8:
	            	sicknessDetail.setLastWorkDayThisSickness(String.valueOf(getCellValue(nextCell)));
	            case 9:
	            	sicknessDetail.setFirstSickDayThisMonthFromTracker(String.valueOf(getCellValue(nextCell)));
	            	default:
	            		break;
	            }
	 
	 
	        }
	        listSickOccur.add(sicknessDetail);
	    }
	 
	    workbook.close();
	    inputStream.close();
	 
	    return listSickOccur;
	}
	
	private Workbook getWorkbook(FileInputStream inputStream, String excelFilePath)
	        throws IOException {
	    Workbook workbook = null;
	 
	    if (excelFilePath.endsWith("xlsx")) {
	        workbook = new XSSFWorkbook(inputStream);
	    } else if (excelFilePath.endsWith("xls")) {
	        workbook = new HSSFWorkbook(inputStream);
	    } else {
	        throw new IllegalArgumentException("The specified file is not Excel file");
	    }
	 
	    return workbook;
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		 String excelFilePath = "C:\\spy\\ops\\data\\R - Z sickness tracker - Katinka.xlsx";
		 IBMTrackerReader reader = new IBMTrackerReader();
		    List<SicknessDetail> listSickOccur = reader.readsicknessDetailFromExcelFile(excelFilePath);
		    System.out.println(listSickOccur.toString());
	}

}
