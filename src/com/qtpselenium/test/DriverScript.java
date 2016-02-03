package com.qtpselenium.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.qtpselenium.xls.read.Xls_Reader;


public class DriverScript {

	//suite.xlsx global variables
	public static Logger APP_LOGS;
	public Xls_Reader suiteXLS;
	public int currentSuiteID;
	public String currentTestSuite;
	
	//Current test suite
	public static Xls_Reader currentTestSuiteXLS;
	public static int currentTestCaseID;
	public static String currentTestCaseName;
	public static int currentTestStepID;
	public String currentkeyword;
	public int currentTestDataSetID=2;
	public keywords keywords;
	public 	Method method[];
	public static Method capturescreenShot_method;
	public String keyword_execution_result;
	public ArrayList<String> resultSet; //result of executing one set of keyword
	public String data; //hold the data of the excel 
	public String object; //hold object column value
	
	//properties
	public static Properties CONFIG;
	public static Properties OR;
	
	public DriverScript() throws NoSuchMethodException, SecurityException
	{
		
		keywords = new keywords();
		method = keywords.getClass().getMethods();//extract methods once
		//capturescreenShot_method =keywords.getClass().getMethod("captureScreenshot",String.class,String.class);
		
	}
	
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchMethodException, SecurityException {
		//initalize
		FileInputStream fs = new FileInputStream(System.getProperty("user.dir")+"//src//com//qtpselenium//config//config.properties");
		CONFIG= new Properties();
		CONFIG.load(fs);
		
		FileInputStream or = new FileInputStream(System.getProperty("user.dir")+"//src//com//qtpselenium//config//OR.properties");
		OR= new Properties();
		OR.load(or);
		
	/*	System.out.println(CONFIG.getProperty("testsiteURL"));
		System.out.println(OR.getProperty("name"));*/

		DriverScript test = new DriverScript();
		test.start();

	}
	
	public void start() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		//initialize the applications log
		APP_LOGS = Logger.getLogger("devpinoyLogger");
		APP_LOGS.debug("Start");
		APP_LOGS.debug("Properties Loaded. Testing Started");
		//1) check run mode of test Suite
		APP_LOGS.debug("Initialize suite xlsx");
		suiteXLS = new Xls_Reader(System.getProperty("user.dir")+ "//src//com//qtpselenium//xls//TestSuite.xlsx");
		
		
		for(currentSuiteID=2;currentSuiteID<=suiteXLS.getRowCount(Constants.TEST_SUITE_SHEET);currentSuiteID++)
		{
			APP_LOGS.debug(suiteXLS.getCellData(Constants.TEST_SUITE_SHEET, Constants.Test_Suite_ID, currentSuiteID)+"--"+suiteXLS.getCellData(Constants.TEST_SUITE_SHEET, "Runmode", currentSuiteID));
			//test suite name = test suite xls file having test cases 
			currentTestSuite=suiteXLS.getCellData(Constants.TEST_SUITE_SHEET, Constants.Test_Suite_ID, currentSuiteID);
			if(suiteXLS.getCellData(Constants.TEST_SUITE_SHEET, Constants.RUNMODE, currentSuiteID).equals(Constants.RUNMODE_YES))
			{
				//execute the test cases in the suite
				APP_LOGS.debug("***Exceuting***"+ suiteXLS.getCellData(Constants.TEST_SUITE_SHEET, Constants.Test_Suite_ID, currentSuiteID));
				currentTestSuiteXLS = new Xls_Reader(System.getProperty("user.dir")+ "//src//com//qtpselenium//xls//"+currentTestSuite+".xlsx");
								
				//iterate through all the test cases in the suite // for loop for test cases in test suite
				for(currentTestCaseID=2;currentTestCaseID<=currentTestSuiteXLS.getRowCount(Constants.TEST_CASES_SHEET);currentTestCaseID++)
				{
					
					APP_LOGS.debug(currentTestSuiteXLS.getCellData(Constants.TEST_CASES_SHEET, Constants.TCID, currentTestCaseID)+"--"+currentTestSuiteXLS.getCellData(Constants.TEST_CASES_SHEET, Constants.RUNMODE, currentTestCaseID));
					currentTestCaseName = currentTestSuiteXLS.getCellData(Constants.TEST_CASES_SHEET, Constants.TCID, currentTestCaseID);
					
					
					if(currentTestSuiteXLS.getCellData(Constants.TEST_CASES_SHEET,Constants.RUNMODE, currentTestCaseID).equals(Constants.RUNMODE_YES))
					{
						APP_LOGS.debug("Exceution of test case "+"  "+currentTestCaseName);
					
						//only having test data sheet would be execute
						if(currentTestSuiteXLS.isSheetExist(currentTestCaseName)) {
						//RUN as many times as number of test data sets with runmode Y
					
						for(currentTestDataSetID=2;currentTestDataSetID<=currentTestSuiteXLS.getRowCount(currentTestCaseName);currentTestDataSetID++)
						{
							resultSet = new ArrayList<String>();  
							APP_LOGS.debug("Iteration number"+(currentTestDataSetID-1));
						//checking the run mode for the current data set
					
							if(currentTestSuiteXLS.getCellData(currentTestCaseName, Constants.RUNMODE, currentTestDataSetID).equals(Constants.RUNMODE_YES)) 
							{
							//exceute the keywords of test case through all keywords
							executekeywords(); //multiple sets of data
							
							}
							createXLSReport();
			}
						
		} else{
						//simply execute keywords having no test data
						//execute the keywords of test case through all keywords
				resultSet= new ArrayList<String>(); 
						executekeywords(); //no data with the test
						createXLSReport();
					}
				} 
			}
		}
	}
}
	
	public void executekeywords() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
			{
		//which dont have test data
		for(currentTestStepID=2;currentTestStepID<=currentTestSuiteXLS.getRowCount(Constants.TEST_STEPS_SHEET);currentTestStepID++)
		{
			 if(currentTestCaseName.equals(currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.TCID, currentTestStepID)))
			 {
				 
			data = currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.DATA, currentTestStepID);
			if(data.startsWith(Constants.DATA_START_COL))
			{
				
				//read the sheet data value from the corresponding column 
				data=currentTestSuiteXLS.getCellData(currentTestCaseName, data.split(Constants.DATA_SPLIT)[1], currentTestDataSetID);
				
			}else if(data.startsWith(Constants.CONFIG)){
				//read actual data value from config.properties		
				data=CONFIG.getProperty(data.split(Constants.DATA_SPLIT)[1]);
			}else{
				//by default read actual data value from or.properties
				data=OR.getProperty(data);
			}
			
			object =currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.OBJECT, currentTestStepID);
			currentkeyword=currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.KEYWORD, currentTestStepID);
				APP_LOGS.debug(currentkeyword);
				//code to execute the keywords as well
				//reflection API
				for(int i=0; i<method.length;i++)
				{
					if(method[i].getName().equals(currentkeyword))
					{
						
						keyword_execution_result = (String) method[i].invoke(keywords,object,data);
						APP_LOGS.debug(keyword_execution_result);
						resultSet.add(keyword_execution_result);
						//report the result
					/*	capturescreenShot_method.invoke(keywords,
								currentTestSuite+"_"+currentTestCaseName+"_TS"+currentTestStepID+"_"+(currentTestDataSetID-1),
								keyword_execution_result);*/
					}
				}
			 }
		}
	}
	
	public void createXLSReport()
	{
		String colName=Constants.RESULT +(currentTestDataSetID-1);
		boolean isColExist=false;
		
		for(int c=0;c<currentTestSuiteXLS.getColumnCount(Constants.TEST_STEPS_SHEET);c++){
			
			if(currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET,c , 1).equals(colName)){
				isColExist=true;
				break;
			}
		}
		
		if(!isColExist)
			currentTestSuiteXLS.addColumn(Constants.TEST_STEPS_SHEET, colName);
		int index=0;
		for(int i=2;i<=currentTestSuiteXLS.getRowCount(Constants.TEST_STEPS_SHEET);i++){
			
			if(currentTestCaseName.equals(currentTestSuiteXLS.getCellData(Constants.TEST_STEPS_SHEET, Constants.TCID, i))){
				if(resultSet.size()==0)
					currentTestSuiteXLS.setCellData(Constants.TEST_STEPS_SHEET, colName, i, Constants.KEYWORD_SKIP);
				else	
					currentTestSuiteXLS.setCellData(Constants.TEST_STEPS_SHEET, colName, i, resultSet.get(index));
				index++;
			}
			
			
		}
		if(resultSet.size() == 0){
			// skip
			currentTestSuiteXLS.setCellData(currentTestCaseName, Constants.RESULT, currentTestDataSetID, Constants.KEYWORD_SKIP);
			return;
		}else{
			for(int i=0;i<resultSet.size();i++){
				if(!resultSet.get(i).equals(Constants.KEYWORD_PASS)){
					currentTestSuiteXLS.setCellData(currentTestCaseName, Constants.RESULT, currentTestDataSetID, resultSet.get(i));
					return;
				}
			}
		}
		currentTestSuiteXLS.setCellData(currentTestCaseName, Constants.RESULT, currentTestDataSetID, Constants.KEYWORD_PASS);
	
		//	if(!currentTestSuiteXLS.getCellData(currentTestCaseName, "Runmode",currentTestDataSetID).equals("Y")){}
		
		
	}

}
