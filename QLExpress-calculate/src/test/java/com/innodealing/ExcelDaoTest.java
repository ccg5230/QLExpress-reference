package com.innodealing;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
public class ExcelDaoTest {
	
	@Autowired private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		try {
			XSSFWorkbook book = new XSSFWorkbook(new FileInputStream(new File("E:\\ttt.xlsx")));
			// for (int i = 0; i < 8; i++) {
			// XSSFSheet sheet =book.getSheetAt(i);
			// System.out.println(sheet.getSheetName());
			// }
			// Workbook book = Workbook.getWorkbook(new File("E:\\ttt.xlsx"));
			// String[] sheet = book.getSheetNames();
			// for (String sheetName : sheet) {
			// System.out.println(sheetName);
			// }

			String[] sheets = { "非金融企业专项指标", "bank专项指标", "secu专项指标", "insu专项指标" };
			for (String string : sheets) {
				XSSFSheet sheet = book.getSheet(string);
				int firstRow = sheet.getFirstRowNum();
				int lastRow = sheet.getLastRowNum();
				for (int i = firstRow; i < lastRow + 1; i++) {

					XSSFRow row = sheet.getRow(i);
					String tableName = row.getCell(0).getStringCellValue().replace("FIN_RATIO_", "dm_analysis_").toLowerCase();
					String field = row.getCell(1).getStringCellValue();
					String fieldName = row.getCell(2).getStringCellValue();
					String group = row.getCell(3).getStringCellValue();
//					System.out.println("tableName->" + tableName + 
//							"field->" + field + 
//							"fieldName->" + fieldName +
//							"group->" + group);
					System.out.println(fieldName + " --->"  + row.getCell(1).getCellStyle().getFillForegroundXSSFColor());
					
				}
				// System.out.println(sheet.getLastRowNum());
				// System.out.println(sheet.getSheetName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void updateFieldFromExcelToMysql(){
		XSSFWorkbook book = null;
		try {
			book = new XSSFWorkbook(new FileInputStream(new File("E:\\ttt.xlsx")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] sheets = { "非金融企业专项指标", "bank专项指标", "secu专项指标", "insu专项指标" };
		int count = 0;
		for (String string : sheets) {
			XSSFSheet sheet = book.getSheet(string);
			int firstRow = sheet.getFirstRowNum();
			int lastRow = sheet.getLastRowNum();
			for (int i = firstRow + 1; i < lastRow + 1; i++) {

				XSSFRow row = sheet.getRow(i);
				String tableName = row.getCell(0).getStringCellValue().replace("FIN_RATIO_", "dm_analysis_").toLowerCase();;
				String field = row.getCell(1).getStringCellValue();
				String fieldName = row.getCell(2).getStringCellValue();
				String group = row.getCell(3).getStringCellValue();
				/*System.out.println("tableName->" + tableName + 
						"field->" + field + 
						"fieldName->" + fieldName +
						"group->" + group);*/
				Integer percent = 2;
				if(fieldName.contains("%")){
					percent = 1;
				}else if(fieldName.contains("万")){
					percent = 0;
				}else{
					percent = 2;
				}
				
				//更新
				String sql = "update amaresun.dm_field_group_mapping set field_name = ? ,group_name = ?, percent = ? where table_name = ? and column_name = ?";
				jdbcTemplate.update(sql,fieldName, group, percent, tableName, field);
				
				//查找没有的field
//				String  countSql = "SELECT COUNT(*) FROM amaresun.dm_field_group_mapping WHERE group_name = '" + group + "' AND table_name = '" + tableName + "'";
//				int  count = jdbcTemplate.queryForObject(countSql , Integer.class);
//				if(count ==  0){
//					System.out.println("tableName->" + tableName + 
//							";field->" + field + 
//							";fieldName->" + fieldName +
//							";group->" + group);
//				}
				
				
				//默认选中初始化
				XSSFColor  color  = row.getCell(1).getCellStyle().getFillForegroundXSSFColor();
				String sqlUpdateSelected = "UPDATE dmdb.t_bond_finance_special_indicator SET selected = ? where field_name = ?";
				if(color  != null){
					jdbcTemplate.update(sqlUpdateSelected,1,field);
					count ++;
				}else{
					
					jdbcTemplate.update(sqlUpdateSelected,0,field);
				}
				
			}
		}
		System.out.println("共有" + count + "默认选择指标！");
	}
	
	
}
