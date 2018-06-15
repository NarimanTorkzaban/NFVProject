package model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jdistlib.Uniform;
import gui.SimulatorConstants;

public class TrafficFG {
	
	//private Uniform uni= new Uniform(10,100); //Mbps  
	private ArrayList<Double> epTraffic= new ArrayList<Double> ();
	
	double cyclesPerPacket=1.9;
	
	public void generateTraffic(int eps){
		
		for (int i=0;i<eps;i++){
			String epName= "ep"+i;
			Uniform uni= new Uniform(10,100);
			this.epTraffic.add((uni.random()*1000)); //Kbps
		//	this.epTraffic.add(1500.0); //Kbps
		}
		
	}
	
	public ArrayList<Double> getTraffic(){
		return this.epTraffic;
	}
	

	public HashMap<String, Double>  generateTrafficLoad(String nfName, ArrayList<Double> traffic){
		HashMap<String, Double> nat = new HashMap<String, Double>();
		HashMap<String, Double> fw = new HashMap<String, Double>();
		HashMap<String, Double> dpi = new HashMap<String, Double>();
		
		
		if (nfName.equalsIgnoreCase("nat")) {
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			cyclesPerPacket=2;  //kcycles per packet
			nat.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8)))); //kcycles per sec (kcycles per packet * packet per sec)
			nat.put("tr_in_ul",ul);
			nat.put("tr_out_ul",ul);
			nat.put("tr_in_dl",dl);
			nat.put("tr_out_dl",dl);
			
			return nat;
		} else if (nfName.equalsIgnoreCase("fw")){
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			//System.out.println("Ul: " +ul+ "DL: "+dl);
			cyclesPerPacket=0.5; //kcycles
			fw.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
			fw.put("tr_in_ul",ul);
			fw.put("tr_out_ul",ul);
			fw.put("tr_in_dl",dl);
			fw.put("tr_out_dl",dl);
			
			return fw;
		}else if (nfName.equalsIgnoreCase("dpi")){
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			
			cyclesPerPacket=53;
			dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
			dpi.put("tr_in_ul",ul);
			dpi.put("tr_out_ul",ul);
			dpi.put("tr_in_dl",dl);
			dpi.put("tr_out_dl",dl);
			
			return dpi;
			
		}
		else if (nfName.equalsIgnoreCase("ip")){
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			
			cyclesPerPacket=1.874;
			dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
			dpi.put("tr_in_ul",ul);
			dpi.put("tr_out_ul",ul);
			dpi.put("tr_in_dl",dl);
			dpi.put("tr_out_dl",dl);
			
			return dpi;
			
		}
		else if (nfName.equalsIgnoreCase("vpn")){
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			
			cyclesPerPacket=8.679; //64 byte
			dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
			dpi.put("tr_in_ul",ul);
			dpi.put("tr_out_ul",ul);
			dpi.put("tr_in_dl",dl);
			dpi.put("tr_out_dl",dl);
			
			return dpi;
			
		}
		else if (nfName.equalsIgnoreCase("lb")){
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			
			cyclesPerPacket=1.5;
			dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
			dpi.put("tr_in_ul",ul);
			dpi.put("tr_out_ul",ul);
			dpi.put("tr_in_dl",dl);
			dpi.put("tr_out_dl",dl);
			
			return dpi;
			
		}
		else if (nfName.equalsIgnoreCase("aes")){
			double ul= traffic.get(0);
			double dl=traffic.get(1);
			
			cyclesPerPacket=53;
			dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
			dpi.put("tr_in_ul",ul);
			dpi.put("tr_out_ul",ul);
			dpi.put("tr_in_dl",dl);
			dpi.put("tr_out_dl",dl);
			
			return dpi;
			
		}
		
		return null; 
			
	
	}
	
	public HashMap<String, Double>  generateTrafficLoad(String nfName, ArrayList<Double> traffic,
			boolean bidirectional){
		HashMap<String, Double> nat = new HashMap<String, Double>();
		HashMap<String, Double> fw = new HashMap<String, Double>();
		HashMap<String, Double> dpi = new HashMap<String, Double>();
		HashMap<String, Double> ip = new HashMap<String, Double>();
		HashMap<String, Double> lb = new HashMap<String, Double>();
		HashMap<String, Double> aes = new HashMap<String, Double>();
		double ul =0;
		double dl =0;
		if (bidirectional){ 
			dl=traffic.get(1);
		}
		//System.out.println("traffic: " + traffic);
		
		ul= traffic.get(0);
		
		if (nfName.equalsIgnoreCase("nat")) {
			cyclesPerPacket=2; 
			if (bidirectional){
				nat.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				nat.put("tr_in_ul",ul);
				nat.put("tr_out_ul",ul);
				nat.put("tr_in_dl",dl);
				nat.put("tr_out_dl",dl);
			} else {
				nat.put("load",(cyclesPerPacket*(ul/(1.5*8))));
				nat.put("tr_in_ul",ul);
				nat.put("tr_out_ul",ul);
			}		
			return nat;
		} else if (nfName.equalsIgnoreCase("fw")){
			//System.out.println("Ul: " +ul+ "DL: "+dl);
			cyclesPerPacket=1.5; //kcycles
			if (bidirectional){
				fw.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				fw.put("tr_in_ul",ul);
				fw.put("tr_out_ul",ul);
				fw.put("tr_in_dl",dl);
				fw.put("tr_out_dl",dl);
			} else {
				fw.put("load",(cyclesPerPacket*(ul/(1.5*8))));
				fw.put("tr_in_ul",ul);
				fw.put("tr_out_ul",ul);
			}
			return fw;
		} 
		else if (nfName.equalsIgnoreCase("dpi")){		
			cyclesPerPacket=53;
			if (bidirectional){
				dpi.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				dpi.put("tr_in_ul",ul);
				dpi.put("tr_out_ul",ul);
				dpi.put("tr_in_dl",dl);
				dpi.put("tr_out_dl",dl);
			} else {
				dpi.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				dpi.put("tr_in_ul",dl);
				dpi.put("tr_out_ul",dl);
			}
			
			return dpi;
			
		} else if (nfName.equalsIgnoreCase("ip")){		
			cyclesPerPacket=1.874;
			if (bidirectional){
				ip.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				ip.put("tr_in_ul",ul);
				ip.put("tr_out_ul",ul);
				ip.put("tr_in_dl",dl);
				ip.put("tr_out_dl",dl);
			} else {
				ip.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				ip.put("tr_in_ul",ul);
				ip.put("tr_out_ul",ul);
			}
			
			return ip;
			
		}else if (nfName.equalsIgnoreCase("lb")){		
			cyclesPerPacket=1.5;
			if (bidirectional){
				lb.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				lb.put("tr_in_ul",ul);
				lb.put("tr_out_ul",ul);
				lb.put("tr_in_dl",dl);
				lb.put("tr_out_dl",dl);
			} else {
				lb.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				lb.put("tr_in_ul",ul);
				lb.put("tr_out_ul",ul);
			}
			
			return lb;
			
		}else if (nfName.equalsIgnoreCase("aes")){		
			cyclesPerPacket=53;
			if (bidirectional){
				aes.put("load",(cyclesPerPacket*((ul+dl)/(1.5*8))));
				aes.put("tr_in_ul",ul);
				aes.put("tr_out_ul",ul);
				aes.put("tr_in_dl",dl);
				aes.put("tr_out_dl",dl);
			} else {
				aes.put("load",(cyclesPerPacket*((ul)/(1.5*8))));
				aes.put("tr_in_dl",ul);
				aes.put("tr_out_dl",ul);
			}
			
			return aes;
			
		}
		
		
		return null; 
			
	
	}
	

}
