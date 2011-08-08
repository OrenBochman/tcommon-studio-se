package com.talend.tac.cases.executionTask;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.talend.tac.base.Base;
import com.talend.tac.cases.executePlan.TriggerDate;

public class TestAddTriggerAddCronTrigger extends TaskUtils {
    
    TriggerDate date = new TriggerDate().getFuture(24);
	boolean actualResult;	
	/***add a cron triiger, set date is by UI
	selected job is TRunJob(use tRunJob run child'job)**/
	
  //add a cron triiger of date is overdue, set date is by UI
	@Test
	@Parameters({"labelRefProJobByMainProTRunJobRun","addCronTriggerOverdue","addCronTriggerOverdueDescription"})
    public void testAddOverdueTriggerAddCronTrigger(String taskLabel,String cronTriggerLabel, String description) {
		
		selenium.refresh();
		
		addTriggerAddCronTrigger(taskLabel,cronTriggerLabel, description, "2010", 
				"Sunday", "Saturday", "January", "December");
    	selenium.setSpeed(MID_SPEED);		
		selenium.click("//button[@id='idCrontTriggerSave']");
		selenium.setSpeed(MIN_SPEED);
		selenium.setSpeed("5000");
	   	Assert.assertTrue(selenium.isTextPresent(rb.getString("trigger.error.trigger_will_never_fire")));
	    selenium.setSpeed(MIN_SPEED); 	                        
    }
	
	//add a CronTrigger,selected job is referencetjava(tjava(from referecepro))**/
	@Test(dependsOnMethods={"testAddOverdueTriggerAddCronTrigger"})
	@Parameters({ "labelReferenceproTjava","addCronTriggerByHandInputDateLabel", "addCronTriggerByHandInputDateDescription"})
	public void testAddCronByHandInputDateTrigger(String taskLabel,String addCronTrigger,String addCronTriggerDescription) throws InterruptedException{
	
		//open to execution task add trigger page
		this.clickWaitForElementPresent("!!!menu.executionTasks.element!!!");
    	selenium.setSpeed(MID_SPEED);
    	Assert.assertTrue(selenium.isElementPresent("//div[text()='"+rb.getString("menu.jobConductor")+"']"));
    	selenium.setSpeed(MIN_SPEED);
       	selenium.mouseDown("//span[text()='"+taskLabel+"']");
		selenium.click("idTriggerAdd trigger...");
		selenium.click("idTriggerAdd CRON trigger");
//		selenium.setSpeed(MID_SPEED);
		Thread.sleep(5000);
		selenium.setSpeed(MIN_SPEED);
		//type  label
		this.typeString("idJobConductorCronTriggerLabelInput",addCronTrigger);
		//type  description
		this.typeString("idJobConductorCronTriggerDescInput", addCronTriggerDescription);
    	//type minutes
		this.typeString("idJobConductorCronTriggerMintInput", date.minutes);
		//type hours
		this.typeString("idJobConductorCronTriggerHourInput", date.hours);
		//type days
		this.typeString("idJobConductorCronTriggerDayOfMonthInput", date.days);
		//type months
		this.typeString("idJobConductorCronTriggerMonthInput", date.months);
		//type years
		this.typeString("idJobConductorCronTriggerYearInput", date.years);	
		//click save button
		selenium.setSpeed(MID_SPEED);
		selenium.click("idCrontTriggerSave");
						
		selenium.setSpeed(MID_SPEED);
		if(!selenium.isElementPresent("//span[text()='"+addCronTrigger+"']")) {
			selenium.click("idTriggerRefresh");
    	}
	    Assert.assertTrue(selenium.isElementPresent("//span[text()='"+addCronTrigger+"']"));
	    selenium.setSpeed(MIN_SPEED);
	}
	
	//add a exist cron triiger, set date is by UI
	@Test(dependsOnMethods={"testAddCronByHandInputDateTrigger"})
	@Parameters({"labelReferenceproTjava","addCronTriggerByHandInputDateLabel","addCronTriggerExistTriggerDescription"})
    public void testAddExistTriggerAddCronTrigger(String taskLabel,String cronTriggerLabel, String description) {
		
		//open to execution task add trigger page
		this.clickWaitForElementPresent("!!!menu.executionTasks.element!!!");
    	selenium.setSpeed(MID_SPEED);
    	Assert.assertTrue(selenium.isElementPresent("//div[text()='"+rb.getString("menu.jobConductor")+"']"));
    	selenium.setSpeed(MIN_SPEED);
    	this.waitForElementPresent("//span[text()='"+taskLabel+"']", WAIT_TIME);
       	selenium.mouseDown("//span[text()='"+taskLabel+"']");
		selenium.click("idTriggerAdd trigger...");
		selenium.click("idTriggerAdd CRON trigger");
		this.typeString("idJobConductorCronTriggerLabelInput",cronTriggerLabel);
		//type  description
		this.typeString("idJobConductorCronTriggerDescInput", description);
    	//type minutes
		this.typeString("idJobConductorCronTriggerMintInput", date.minutes);
		//type hours
		this.typeString("idJobConductorCronTriggerHourInput", date.hours);
		//type days
		this.typeString("idJobConductorCronTriggerDayOfMonthInput", date.days);
		//type months
		this.typeString("idJobConductorCronTriggerMonthInput", date.months);
		//type years
		this.typeString("idJobConductorCronTriggerYearInput", date.years);	
		//click save button
		selenium.click("idCrontTriggerSave");
		selenium.setSpeed("5000");
		Assert.assertTrue(selenium.isTextPresent("Save failed: An execution trigger with this name already exists -- For more information see your log file"));
		selenium.setSpeed(MIN_SPEED);
		
    }
	
    @Test(dependsOnMethods={"testAddExistTriggerAddCronTrigger"})
	@Parameters({"labelRefProJobByMainProTRunJobRun","addCronTriggerLabel","addCronTriggerDescription"})
    public boolean testAddTriggerAddCronTrigger(String taskLabel,String cronTriggerLabel,String description) throws InterruptedException {
		
		addTriggerAddCronTrigger(taskLabel,cronTriggerLabel, description, "2011", 
				"Sunday", "Saturday", "January", "December");
    			
		selenium.click("idCrontTriggerSave");
				
		selenium.setSpeed(MID_SPEED);
		if(!selenium.isElementPresent("//span[text()='"+cronTriggerLabel+"']")) {
			selenium.click("idTriggerRefresh");
    	}
		Assert.assertTrue(selenium.isElementPresent("//span[text()='"+cronTriggerLabel+"']"));
		selenium.setSpeed(MIN_SPEED);
		
	    if(this.waitForCondition("//span[text()='"+taskLabel+"']//ancestor::tr" +
				"//span[text()='Error while generating job']", Base.MAX_WAIT_TIME)) {
	    	actualResult = (waitForCondition("//span[text()='"+taskLabel+"']//ancestor::tr" +
					"//span[text()='Running...']", 2));			
	    		return actualResult;
	    		
	    } else {	
				success = (waitForCondition("//span[text()='"+taskLabel+"']//ancestor::tr" +
					"//span[text()='Running...']", MAX_WAIT_TIME));			
				return actualResult;
	    
	    }
				
    }
	
}
