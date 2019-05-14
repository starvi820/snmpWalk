package com.sysone.client;

import java.io.IOException;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;



public class SNMPManager {

	Snmp snmp = null;
	String address = null;

	public SNMPManager(String add)

	{
		address = add;

	}

	public static void main(String[] args) throws IOException {

		// 161 포트 - 읽기 및 기타 작업에 사용
		// 162 포트 - 트랩 생성에 사용
		SNMPManager client = new SNMPManager("udp:127.0.0.1/1000");

		client.start();

		/**
		 *  OID - .1.3.6.1.2.1.1.1.0 => SysDec 
		 *  OID - .1.3.6.1.2.1.1.5.0 => SysName
		 *  
		 */

		String sysDescr = client.getAsString(new OID(".1.3.6.1.2.1.1.1.0"));
		System.out.println(sysDescr);
		
		String sysUpTime = client.getAsString(new OID(".1.3.6.1.2.1.1.3.0"));
		System.out.println(sysUpTime);
		
		String sysName = client.getAsString(new OID(".1.3.6.1.2.1.1.5.0"));
		System.out.println(sysName);
		
		
		


	}

		
	public void start() throws IOException {
		TransportMapping transport = new DefaultUdpTransportMapping();

		snmp = new Snmp(transport); 	// snmp 세션 시작 


		transport.listen(); 	// listen() 를 사용하지 않으면 비동기 통신

	}

	

	public String getAsString(OID oid) throws IOException {
		
		// 단일 oid를 취해서 에이전트로부터의 응답을 돌려 주는 메소드

		ResponseEvent event = get(new OID[] { oid });

		return event.getResponse().get(0).getVariable().toString();

	}

	
	
	
	public ResponseEvent get(OID oids[]) throws IOException {

		PDU pdu = new PDU();

		for (OID oid : oids) { // 여러 oid를 처리 할수 있다.

			pdu.add(new VariableBinding(oid));

		}

		pdu.setType(PDU.GET);

		ResponseEvent event = snmp.send(pdu, getTarget(), null);

		if (event != null) {

			return event;

		}

		throw new RuntimeException("시간 초과");

	}

	
	private Target getTarget() {
		
		//정보를 포함하는 타겟을 반환
		// 데이터를 가져와야하는 위치 와 방법

		Address targetAddress = GenericAddress.parse(address);

		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(2);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);
		return target;

	}

}
