package com.sysone.trap;

import java.io.IOException;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.agent.request.SnmpRequest;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class TrapReceiver implements CommandResponder {
	
	public TrapReceiver() {

	}

	public static void main(String[] args) {
		
		TrapReceiver trapReceiver = new TrapReceiver();
		try {
			
			trapReceiver.listen(new UdpAddress("127.0.0.1/162"));
		}

		catch (IOException e)

		{
			System.err.println("Error in Listening for Trap");
			System.err.println("Exception Message = " + e.getMessage());
		}

	}

	// snmp 에이전트에서 트랩과 응답 수신
	public synchronized void listen(TransportIpAddress address) throws IOException {
		
		AbstractTransportMapping transport; //메시지 디스패처 목록 및 최대 인바운드 메시지 크기에 대한 추상 구현
		
		
		if (address instanceof TcpAddress) {
			
			transport = new DefaultTcpTransportMapping((TcpAddress) address); //tcp 전송 매핑
		} else {
			transport = new DefaultUdpTransportMapping((UdpAddress) address); //udp 전송 매핑
		}

		ThreadPool threadPool = ThreadPool.create("DispatcherPool", 10);
		MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

		// 메시지 처리 모델 추가
		mtDispatcher.addMessageProcessingModel(new MPv1()); 
		mtDispatcher.addMessageProcessingModel(new MPv2c());

		// 모든 보안 프로토콜 추가
		SecurityProtocols.getInstance().addDefaultProtocols();
		SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));

		Snmp snmp = new Snmp(mtDispatcher, transport);
		snmp.addCommandResponder(this);

		transport.listen();
		System.out.println("Listening on " + address);

		try {
			this.wait();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	// listen() 메소드로 지정된 포트로 pdu가 수신 될 때마다 송신
	public synchronized void processPdu(CommandResponderEvent cmdRespEvent) { //트랩/알림을 잠재적으로 처리 할 수 있는 리스너에게 시작 - messagedispatcher에 의해 
		System.out.println("Received PDU...");
		PDU pdu = cmdRespEvent.getPDU();
		if (pdu != null) {

			System.out.println("Trap Type = " + pdu.getType());
			System.out.println("Variable Bindings = " + pdu.getVariableBindings());
			
			int pduType = pdu.getType();
			if ((pduType != PDU.TRAP) && (pduType != PDU.V1TRAP) && (pduType != PDU.REPORT)
					&& (pduType != PDU.RESPONSE)) {
				pdu.setErrorIndex(0);
				pdu.setErrorStatus(0);
				pdu.setType(PDU.RESPONSE);
				
				StatusInformation statusInformation = new StatusInformation(); //보고서 메시지를 반환하는데 필요한 메시지의 상태 정보
				StateReference ref = cmdRespEvent.getStateReference(); // 메시지와 관련된 상태 정보 - 응답 또는 보고서를 보내는데 사용 (snmpv3만 해당)
				try {
					
					System.out.println(cmdRespEvent.getPDU());
					cmdRespEvent.getMessageDispatcher().returnResponsePdu(cmdRespEvent.getMessageProcessingModel(),
							cmdRespEvent.getSecurityModel(), cmdRespEvent.getSecurityName(),
							cmdRespEvent.getSecurityLevel(), pdu, cmdRespEvent.getMaxSizeResponsePDU(), ref,
							statusInformation);
					
				} catch (MessageException ex) {
					System.err.println("Error while sending response: " + ex.getMessage());
					LogFactory.getLogger(SnmpRequest.class).error(ex);
				}
			}
		}
	}
}