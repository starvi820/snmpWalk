package com.sysone.agent;

import java.io.IOException;

import org.snmp4j.smi.OID;

import com.sysone.client.SNMPManager;

public class TestSNMPAgent {
	
	static final OID sysDescr = new OID(".1.3.6.1.2.1.1.1.0");
	

	public static void main(String[] args) throws IOException {
		
		TestSNMPAgent client = new TestSNMPAgent("udp:127.0.0.1/161");
		client.init();
			
		
	}
	
	SNMPAgent agent = null;
		
	SNMPManager client = null; // 이전에 만든 클라이언트
	
	String address = null;
	
	public TestSNMPAgent(String add) {
		address = null;
	}
	
	public void init() throws IOException{
		agent = new SNMPAgent("0.0.0.0/161");
		agent.start();
				
		
		agent.unregisterManagedObject(agent.getSnmpv2MIB()); //BaseAgent는 기본적으로 일부 MIB를 등록하므로 등록을 취소.
		
		agent.registerManagedObject(MOCreator.createReadOnly(sysDescr,"This Description is set By Sangjin"));
		// 자신의 sysDescr을 등록하기 전에 해당 메소드를 재정의하고 필요한 mib 등록
		
		client = new SNMPManager("udp:127.0.0.1/161"); //새로 시작된 에이전트를 사용하도록 클라이언트 설정
		client.start();
		
		System.out.println(client.getAsString(sysDescr));
	}
	
	

}
