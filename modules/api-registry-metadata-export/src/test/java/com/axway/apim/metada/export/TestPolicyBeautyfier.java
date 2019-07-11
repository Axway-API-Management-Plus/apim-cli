package com.axway.apim.metada.export;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.metadata.export.formats.AbstractReportFormat;

public class TestPolicyBeautyfier {
  @Test
  public void run() {
	  String policyName = "<key type='CircuitContainer'><id field='name' value='QuickStart'/><key type='CircuitContainer'><id field='name' value='Backend Services'/><key type='CircuitContainer'><id field='name' value='Villains Product Service '/><key type='CircuitContainer'><id field='name' value='SOAP'/><key type='FilterCircuit'><id field='name' value='GetProducts'/></key></key></key></key></key>";
	  String expectedName = "/QuickStart/Backend Services/Villains Product Service/SOAP/GetProducts";
	  Assert.assertEquals(AbstractReportFormat.beautifyPolicyName(policyName), expectedName);
  }
}
