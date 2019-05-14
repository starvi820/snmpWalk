package com.sysone.agent;

import java.io.File;
import java.io.IOException;import java.util.ServiceConfigurationError;

import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB.SnmpCommunityEntryRow;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.TransportMappings;

public class SNMPAgent extends BaseAgent {
	
	private String address;
	
	public SNMPAgent(String address) throws IOException{
		
		// 부트 카운터 , 설정 파일 , 기본 에이전트 생성
		// commandProcessor = SNMP 요청 처리 , 에이전트의 종료 , 인스턴스
		// bootCounterFile = 직렬화 된 부팅 카운터 정보가 있는 파일 ,(읽기/쓰기) 파일이 존재하지 않으면 파일이 종료 될때 생성
		//configFile = 직렬화 된 구성을 가진 파일 , (읽기/쓰기) 파일이 존재 하지 않는경우 생성
		
		
		
		super(new File("conf.agent"),new File("bootCounter.agent"), 
				
				new CommandProcessor(new OctetString(MPv3.createLocalEngineID())));
		this.address = address;
			
		
	}

	@Override
	protected void addCommunities(SnmpCommunityMIB comunityMIB) {
		
		// snmpv1 , snmpv2c에 필요한 보안 이름을 커뮤니티에 추가 매핑.
		
	 Variable[] com2sec = new Variable[] {
			 
			 new OctetString("public"),
			 new OctetString("cpublic"), //보안 이름
			 getAgent().getContextEngineID(), //로컬 엔진 아이디
			 new OctetString("public") , //디폴트 컨텍스트 name
			 new OctetString() , // transport tag
			 new Integer32(StorageType.nonVolatile) , //스토리지 타입
			 new Integer32(RowStatus.active) // row status
	 };
	 
	 MOTableRow row = comunityMIB.getSnmpCommunityEntry().createRow(new OctetString("public2public")
			 .toSubIndex(true),com2sec);
	 comunityMIB.getSnmpCommunityEntry().addRow((SnmpCommunityEntryRow) row);
		
		
	}

	@Override
	protected void addNotificationTargets(SnmpTargetMIB arg0, SnmpNotificationMIB arg1) {
		// 초기 알림 대상 및 필터 추가
		
	}

	@Override
	protected void addUsmUser(USM arg0) {
		// USM에 유저 추가
		
	}

	@Override //View-based Access control Model (SNMP를 통해 에이전트에 엑세스 할도록 초기 구성)
	protected void addViews(VacmMIB vacm) { // 초기 VACM 구성을 추가 
		
		vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c ,new OctetString("cpublic")
				, new OctetString("v1v2group"),StorageType.nonVolatile );
		
		vacm.addAccess(new OctetString("v1v2group"),new OctetString("public"),
				
				SecurityModel.SECURITY_MODEL_ANY,SecurityLevel.NOAUTH_NOPRIV
				,MutableVACM.VACM_MATCH_EXACT,new OctetString("fullReadView"),
				new OctetString("fullWriterView"),new OctetString("fullNotifyView")
				, StorageType.nonVolatile);
		
		vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
				new OctetString(),VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
		
		
	}

	@Override
	protected void registerManagedObjects() {
		//에이전트의 서버에서 추가 관리 대상 개체를 등록
		
	}

	@Override
	protected void unregisterManagedObjects() {
		// 에이전트의 MOServer에서 기본 MIB 모듈 등록 해제
		
	}
	
	protected void initTransportMappings() throws IOException {
		
		        transportMappings = new TransportMapping[1];
		
		        Address addr = GenericAddress.parse(address);
		
		        TransportMapping tm = TransportMappings.getInstance()
		
		                .createTransportMapping(addr);
		
		        transportMappings[0] = tm;
		
		    }
		
 
	 
	//start 메소드 = 에이전트를 시작하는데 필요한 일부 초기화 메소드 호출
	
	public void start() throws IOException{
		
		init();
		addShutdownHook();
		getServer().addContext(new OctetString("public"));
		finishInit();
		run();
		sendColdStartNotification();
		
	}
	
	
	// 클라이언트는 필요한 mo등록 가능
	
	public void registerManagedObject(ManagedObject mo) {
		
		try {
			server.register(mo, null);
		} catch (DuplicateRegistrationException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	
	public void unregisterManagedObject(MOGroup moGroup) {
		moGroup.unregisterMOs(server, getContext(moGroup));
	}

	
	

}
