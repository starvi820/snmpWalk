package com.sysone.agent;

import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;


public class MOCreator { //ManagedObject 를 생성 하고 반환
	
	public static MOScalar<Variable> createReadOnly(OID oid , Object value) {
		return new MOScalar<Variable>(oid,MOAccessImpl.ACCESS_READ_ONLY,getVariable(value));
	}
	
	private static Variable getVariable(Object value) {
		if(value instanceof String) {
			return new OctetString((String)value);
		}
		
		throw new IllegalArgumentException("Unmanaged Type : "+value.getClass());
	}

}
