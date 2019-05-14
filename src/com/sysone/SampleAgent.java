package com.sysone;

import java.net.*;
import java.util.*;
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
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;

import org.snmp4j.util.TreeUtils;

public class SampleAgent{
    static int defaultPort = 161; //디폴트 포트
    static String defaultIP = "127.0.0.1"; // 디폴트 아이피
    static String defaultOID = ".1.3.6.1.2.1.2.2.1.2.11"; //디폴트 oid

    static void testGetNext(String oid) throws java.io.IOException {
    	
    	
        PDU pdu = new PDU(); //pdu 생성
        pdu.add(new VariableBinding(new OID(defaultOID))); 
        pdu.setType(PDU.GET);

       
        CommunityTarget target = new CommunityTarget();
        UdpAddress targetAddress = new UdpAddress();
        targetAddress.setInetAddress(InetAddress.getByName(defaultIP));
        targetAddress.setPort(defaultPort);
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version1);
        

        //3. Make SNMP Message. Simple!
       Snmp snmp = new Snmp(new DefaultUdpTransportMapping());

        //4. Send Message and Recieve Response
        snmp.listen();
        
        ResponseEvent response = snmp.send(pdu, target);
        if (response.getResponse() == null) {
            System.out.println("Error: There is some problems.");
       } else {
            List variableBindings = response.getResponse().getVariableBindings();
            for( int i = 0; i < variableBindings.size(); i++){
                 System.out.println(variableBindings.get(i));
            }
        }
        snmp.close();
    }
    
    

    public static void main(String[] args) throws java.io.IOException {
    	
       
        if (args.length > 0) {
             defaultPort = Integer.parseInt (args[0]);
        }
        System.out.println ("PORT : " + defaultPort);

     
        if (args.length > 1) {
             defaultIP = args[1];
        }
        System.out.println ("IP : " + defaultIP);

      
        if (args.length > 2) {
            defaultOID = args[2];
        }
        System.out.println ("OID : " + defaultOID);
        try {
            testGetNext(defaultOID);
        } catch (Exception ex) {
            System.out.println ("ex *** : " + ex);
            ex.printStackTrace ();
        }
    }
}
