/*L
 *  Copyright Washington University in St. Louis
 *  Copyright SemanticBits
 *  Copyright Persistent Systems
 *  Copyright Krishagni
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/catissue-core/LICENSE.txt for details.
 */

package edu.wustl.catissuecore.testcase.bizlogic;

import edu.wustl.catissuecore.domain.pathology.DeidentifiedSurgicalPathologyReport;
import edu.wustl.catissuecore.domain.pathology.IdentifiedSurgicalPathologyReport;
import edu.wustl.catissuecore.testcase.CaTissueSuiteBaseTest;
import edu.wustl.common.domain.AbstractDomainObject;
import edu.wustl.common.util.logger.Logger;


public class DeIdentifiedSurgicalPathologyReportBizTestCases extends CaTissueSuiteBaseTest {

	AbstractDomainObject domainObject = null;
	public void testAddDeidentifiedSurgicalPathologyReport()
	{
		try
		{
			DeidentifiedSurgicalPathologyReport deIdentifiedSurgicalPathologyReport= BaseTestCaseUtility.initDeIdentifiedSurgicalPathologyReport();			
			System.out.println(deIdentifiedSurgicalPathologyReport);
			deIdentifiedSurgicalPathologyReport = (DeidentifiedSurgicalPathologyReport) appService.createObject(deIdentifiedSurgicalPathologyReport);
			IdentifiedSurgicalPathologyReport identifiedSurgicalPathologyReport=(IdentifiedSurgicalPathologyReport)BizTestCaseUtility.getObjectMap(IdentifiedSurgicalPathologyReport.class);
			identifiedSurgicalPathologyReport.setDeIdentifiedSurgicalPathologyReport(deIdentifiedSurgicalPathologyReport);
			identifiedSurgicalPathologyReport = (IdentifiedSurgicalPathologyReport) appService.updateObject(identifiedSurgicalPathologyReport);
			BizTestCaseUtility.setObjectMap(deIdentifiedSurgicalPathologyReport, DeidentifiedSurgicalPathologyReport.class);
			System.out.println("Object created successfully");
			assertTrue("Object added successfully", true);
		 }
		 catch(Exception e){
			 e.printStackTrace();
			 assertFalse(e.getMessage(), true);
		 }
	}
	
	public void testUpdateDeidentifiedSurgicalPathologyReport()
	{
		DeidentifiedSurgicalPathologyReport deIdentifiedSurgicalPathologyReport=  new DeidentifiedSurgicalPathologyReport();
    	Logger.out.info("updating domain object------->"+deIdentifiedSurgicalPathologyReport);
	    try 
		{
	    	deIdentifiedSurgicalPathologyReport=BaseTestCaseUtility.updateIdentifiedSurgicalPathologyReport(deIdentifiedSurgicalPathologyReport);	
	    	DeidentifiedSurgicalPathologyReport updatedDeIdentifiedSurgicalPathologyReport = (DeidentifiedSurgicalPathologyReport) appService.updateObject(deIdentifiedSurgicalPathologyReport);
	       	Logger.out.info("Domain object successfully updated ---->"+updatedDeIdentifiedSurgicalPathologyReport);
	       	assertTrue("Domain object successfully updated ---->"+updatedDeIdentifiedSurgicalPathologyReport, true);
	    } 
	    catch (Exception e) {
	       	Logger.out.error(e.getMessage(),e);
	 		e.printStackTrace();
	 		assertFalse(e.getMessage(), true);
	    }
	}
}
