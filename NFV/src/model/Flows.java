package model;

import java.util.ArrayList;

public class Flows {
	private int id=-1;
	ArrayList<Flow> flows = new ArrayList<Flow>();

	public void createFlow(int source, int dest, int cap){
		id++;
		Flow fl = new Flow (id, source, dest, cap);
		flows.add(fl);
		System.out.println(fl.getFLID());
		
	}
	
	
	public Flow getFlow(int id) {
		for (Flow fl: flows){
			if (fl.getFLID()==id){
				return fl;
			}
		}
		
		return null;
	}
	
	public int getFlowCap(int id) {
		for (Flow fl: flows){
			if (fl.getFLID()==id){
				return fl.getcap();
			}
		}
		
		return -1;
	}
	
	public boolean containsSD(int src,int dst){
		for (Flow fl: flows){
			if ((fl.getSRCID()==src) && (fl.getDSTID()==dst)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean containsS(int src){
		for (Flow fl: flows){
			//System.out.println(fl.getSRCID() + " " + src);
			if (fl.getSRCID()==src){
				//System.out.println("found");
				return true;
			}
		}
		
		return false;
	}
	
	public boolean flowcontainsS(int fid,int src){
			Flow fl= flows.get(fid);
			//System.out.println(fl.getSRCID() + " " + src);
			if (fl.getSRCID()==src){
				//System.out.println("found");
				return true;
			}
		
		
		return false;
	}
	
	public boolean flowcontainsD(int fid,int dst){
		Flow fl= flows.get(fid);
		System.out.println(fl.getDSTID() + " " + dst);
		if (fl.getDSTID()==dst){
			System.out.println("found");
			return true;
		}
	
	
	return false;
}
	
	public boolean containsD(int dst){
		for (Flow fl: flows){
			if (fl.getDSTID()==dst){
				return true;
			}
		}
		
		return false;
	}
	
	public class Flow {
		private int flowID=0;
		private int src;
		private int dst;
		private int cap;
		
		public  Flow(int id,int source, int dest, int cp){
			flowID=id;
			src=source;
			dst=dest;
			cap = cp;
			}
		
		 public int getFLID (){
			 return this.flowID;
		 }
		 
		 public int getSRCID (){
			 return this.src;
		 }
		 
		 public int getDSTID (){
			 return this.dst;
		 }
		 
		 public int getcap (){
			 return this.cap;
		 }
	}


}
