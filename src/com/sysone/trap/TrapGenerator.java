package com.sysone.trap;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class TrapGenerator {
	
	public static void main(String[] args) throws IOException {
		
		//snmp4j 에서 사용할 pdu 생성
		
		PDU trap = new PDU();
		
		trap.setType(PDU.TRAP);
		
		OID oid = new OID(".1.3.6.1.2.1.1.1.0");
		
		trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
		trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(1000)));
		trap.add(new VariableBinding(SnmpConstants.sysDescr, new OctetString("My PC Data")));
		
		Variable var = new OctetString("My system is in trouble");
		trap.add(new VariableBinding(oid, var));
		
		
		// 수신기 지정
		Address targetAddress = new UdpAddress("127.0.0.1/162");
		
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(targetAddress);
		
		Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
		snmp.send(trap, target, null, null);
		
		System.out.println(trap);
	}

}
