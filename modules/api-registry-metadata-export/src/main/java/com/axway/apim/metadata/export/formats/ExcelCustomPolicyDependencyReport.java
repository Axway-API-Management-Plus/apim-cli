package com.axway.apim.metadata.export.formats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.state.IAPI;

public class ExcelCustomPolicyDependencyReport extends CSVCustomPolicyDependencyReport {
	
	private static Logger LOG = LoggerFactory.getLogger(ExcelCustomPolicyDependencyReport.class);

	@Override
	public void exportMetadata() throws AppException {
		String filename = CommandParameters.getInstance().getValue("filename");
		
		if(!filename.endsWith(".xlsx")) {
			filename = filename + ".xlsx";
		}
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Policy dependencies");

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Policy Name");
        cell = row.createCell(1);
        cell.setCellValue("Policy Type");
        cell = row.createCell(2);
        cell.setCellValue("API Name");
        cell = row.createCell(3);
        cell.setCellValue("API Status");
        cell = row.createCell(4);
        cell.setCellValue("API Version");
        cell = row.createCell(5);
        cell.setCellValue("No of subscribed apps");
        cell = row.createCell(6);
        cell.setCellValue("API-Id");
        
		addPolicyTypeToXls(sheet, usedRequestPolicies, "Request");
		addPolicyTypeToXls(sheet, usedRoutingPolicies, "Routing");
		addPolicyTypeToXls(sheet, usedResponsePolicies, "Response");
		addPolicyTypeToXls(sheet, usedFaulthandlerPolicies, "Faulthandler");
        try {
        	File targetFile = new File(filename);
        	FileOutputStream outputStream = new FileOutputStream(targetFile);
        	workbook.write(outputStream);
        	LOG.info("Custom-Policy dependency information exported into Excel file: '"+targetFile.getCanonicalPath()+"'");
		} catch (IOException e) {
			throw new AppException("Cant open CSV-File for writing", ErrorCode.UNXPECTED_ERROR);
		} finally {
			 try {
				workbook.close();
			} catch (IOException ignore) {}
		}
	}
	
	private void addPolicyTypeToXls(XSSFSheet sheet, Map<String, List<IAPI>> policies, String type) throws AppException {
		int rowNum = 1;
		Cell cell;

		Iterator<String> it = policies.keySet().iterator();
		while(it.hasNext()) {
			String policyName = it.next();
			List<IAPI> apis = policies.get(policyName);
			policyName = beautifyPolicyName(policyName);
			for(IAPI api : apis) {
				int noOfApps = (api.getApplications()==null) ? 0 : api.getApplications().size();
				Row row = sheet.createRow(rowNum++);
				cell = row.createCell(0);
				cell.setCellValue(policyName);
				cell = row.createCell(1);
				cell.setCellValue(type);
				cell = row.createCell(2);
				cell.setCellValue(api.getName());
				cell = row.createCell(3);
				cell.setCellValue(api.getState());
				cell = row.createCell(4);
				cell.setCellValue(api.getVersion());
				cell = row.createCell(5);
				cell.setCellValue(noOfApps);
				cell = row.createCell(6);
				cell.setCellValue(api.getId());
			}
		}
	}
}
