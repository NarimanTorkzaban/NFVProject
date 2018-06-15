package output;

import edu.uci.ics.jung.graph.util.Pair;
import gui.SimulatorConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Request;
import model.Substrate;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.Path;
import model.components.RequestRouter;
import model.components.Server;
import model.components.SubstrateRouter;
import model.components.VirtualMachine;
import model.components.SubstrateLink;

public class Results {
	private Substrate substrate;
	private int denials = 0;
	private double cost=0;
	private double revenue=0;
	private double avgHops =0;
	private double pathlets =0;
	private double flow =0;
	private int requests=0;
	
	public Results (Substrate sub){
		this.substrate=sub;
	}

/*	public double Acceptance_Ratio(double denial, double current_request){
		return (current_request-denial)/current_request;
	}*/
	
	
	public double Generate_Revenue(Request req){

		double sum=0;
		for (Node x: req.getGraph().getVertices()){
				if (x instanceof VirtualMachine)
					sum=sum+x.getCpu()+x.getMemory()+((VirtualMachine)x).getDiskSpace();
				else if (x instanceof NF)
					sum=sum+x.getAvailableCpu();
				else if (x instanceof RequestRouter)
					sum=sum+30;
		}
		
		for (Link current: req.getGraph().getEdges())
			sum=sum+current.getBandwidth();
		
		return sum;
	}
	
	public double avgHops(LinkedHashMap<Link, List<Path>> flow){
		double avgHops =0;
		if (flow!=null){
			
			for (Link key: flow.keySet()){
				int hop_count=0;
				for (Path path: (List<Path>) flow.get(key)){
					for (Link link: path.getSubstrateLinks()){
						for (Link edge: substrate.getGraph().getEdges()){
							if (link.getName()==edge.getName()){
								hop_count++;
							}
						}
					}
				}
				avgHops += hop_count;
			}	
					
		return avgHops;
	}
	else return 0;
	}
	
	public double avgHops(HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> flow){
		double avgHops =0;
		if (flow!=null){	
		
		 	int num = flow.keySet().size();
			for (Link key: flow.keySet()){
				int hop_count=0;
				ArrayList<LinkedHashMap<Link, Double>> tmp = new  ArrayList<LinkedHashMap<Link, Double>>();
				tmp = flow.get(key);
				for (LinkedHashMap<Link, Double> subLinks: tmp){
					
					if (!subLinks.isEmpty()){
						 for (Map.Entry<Link, Double> entry : subLinks.entrySet()) {
							 if (!(entry==null)){
								 hop_count++;
							}
						}
					}
					
				}
				avgHops += hop_count;
				System.out.println(avgHops);
			}	
			avgHops=avgHops/num;
			System.out.println(avgHops);
	
		return avgHops;
	}
	else return 0;
	}
	
	
	 public double Cost_Embedding(LinkedHashMap<Link, List<Path>> flow, Request req){
		 
		 double cost = 0;
		 if (flow!=null){
			for (Link key: flow.keySet()){
				for (Path path: (List<Path>) flow.get(key)){
					for (Link link: path.getSubstrateLinks()){
						for (Link edge: substrate.getGraph().getEdges()){
							if (link.getName()==edge.getName()){
								cost += path.getBandwidth();
							}
						}
					}
				}
			}			 
		 }
		  

		   for (Node x: req.getGraph().getVertices()){

		     if (x instanceof VirtualMachine) {
		      cost=cost+x.getCpu()+x.getMemory()+((VirtualMachine)x).getDiskSpace();

		     }
		   }

		  
		  return cost;
		 }
	 
	 public double Cost_Embedding(HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> flow, Request req){
		 
		 double cost = 0;
		 if (flow!=null){
			for (Link key: flow.keySet()){
				ArrayList<LinkedHashMap<Link, Double>> tmp = new  ArrayList<LinkedHashMap<Link, Double>>();
				tmp = flow.get(key);
				for (LinkedHashMap<Link, Double> subLinks: tmp){
					
					if (!subLinks.isEmpty()){
						 for (Map.Entry<Link, Double> entry : subLinks.entrySet()) {
							 if (!(entry==null)){
							    Double cap = entry.getValue();
							    cost += cap;
							}
						}
					}
					
				}
			}			 
		 }
		  

		 for (Node x: req.getGraph().getVertices()){
			 if  (x instanceof NF){
		    	 cost=cost+x.getAvailableCpu(); 
		      }
		  }

		  
		  return cost;
		 }
	 
	
	 public static double[] Node_utilization_Server_Cpu(List<Substrate> substrates){
		 
		 double[] cpu_util= new double[substrates.size()];
		 
		 int counter_sub=0;		 
		 for (Substrate sub: substrates){
			 double servers=0;
			 double cpu_util_sub=0;
			 for (Node node: sub.getGraph().getVertices()){
				 if (node instanceof Server){
					 servers++;
					 double div = 1 - ( (double) ((Server) node).getCpu()/(double) ((Server) node).getAvailableCpu());
					 cpu_util_sub = cpu_util_sub + div;
				 }
			 }
			 cpu_util[counter_sub]= cpu_util_sub/servers;
		 		 
			 counter_sub++;
		 }	

		 return cpu_util;
		}
		
		public static double[] Node_utilization_Server_Memory(List<Substrate> substrates){
			 double[] mem_util= new double[substrates.size()];
			 int counter_sub=0;
			 
			 for (Substrate sub: substrates){
				 double servers=0;
				 double mem_util_sub=0;
				 for (Node node: sub.getGraph().getVertices()){
					 if (node instanceof Server){
						 servers++;
						 double div = 1 - ( (double) ((Server) node).getMemory()/(double) ((Server) node).getAvailableMemory());
						 mem_util_sub = mem_util_sub + div;
					 }
				 }
				 mem_util[counter_sub]= mem_util_sub/servers;
		 		 
				 counter_sub++;
			 }	

			 return mem_util;
		}
		
		public static double[] Node_utilization_Server_DiskSpace(List<Substrate> substrates){
			double[] disk_util= new double[substrates.size()];

			 int counter_sub=0;
			 for (Substrate sub: substrates){
				 double servers=0;
				 double disk_util_sub=0;
				 for (Node node: sub.getGraph().getVertices()){
					 if (node instanceof Server){
						 servers++;
						 double div = 1 - ( (double) ((Server) node).getDiskSpace()/(double) ((Server) node).getAvailableDiskSpace());
						 disk_util_sub = disk_util_sub + div;
					 }
				 }
				 disk_util[counter_sub]= disk_util_sub/servers;
				 
				 counter_sub++;
			 }	

			 return disk_util;
		}
	
	public static double[] Node_utilization_Router(List<Substrate> substrates){
		double[] router_util= new double[substrates.size()];
		 
		int counter_sub=0;		 
		 for (Substrate sub: substrates){
			 double routers=0;
			 double router_util_sub=0;
			 for (Node node: sub.getGraph().getVertices()){
				 if (node instanceof SubstrateRouter){
					 routers++;
					 double div = 1 - ( (double) ((SubstrateRouter) node).getLogicalInstances()/(double) ((SubstrateRouter) node).getAvailableLogicalInstances());
					 router_util_sub = router_util_sub + div;
				 }
			 }
			 router_util[counter_sub]= router_util_sub/routers;
		 		 
			 counter_sub++;
		 }	

		 return router_util;
	}

	
	public static double[] Link_utilization(List<Substrate> substrates){
		double[] link_util= new double[substrates.size()];

		 int counter_sub=0;
		 for (Substrate sub: substrates){
			 double link_util_sub=0;
			 for (Link link: sub.getGraph().getEdges()){
					 double div = 1 - ( (double) ((SubstrateLink) link).getBandwidth() /(double) ((SubstrateLink) link).getAvailableBandwidth());
					 link_util_sub = link_util_sub + div;
			 }
			 link_util[counter_sub]= link_util_sub/sub.getGraph().getEdgeCount();
		 		 
			 counter_sub++;
		 }	

		 return link_util;
	}
	



	public void addReq(){
    	this.requests +=1;
    }
	
	public int getReqProcessed(){
		return this.requests;
	}
	
    public void addDenial(){
    	this.denials +=1;
    }
    public int getDenials(){
    	return denials;
    }
    
    public void addCost(double reqCost){
    	this.cost +=reqCost;
    }
    
    public double getCost(){
    	return (this.cost);
    }
    
    public void addRev(double reqRev){
    	this.revenue +=reqRev;
    }
    
    public double getRevenue(){
    	return this.revenue;
    }
    
    public void addHops(double hops){
    	this.avgHops +=hops;
    }
    public double getAvgHops(){
    	return (this.avgHops);
    }
    
    public void addPLs(int pathlet_num){
    	this.pathlets +=pathlet_num;
    }
    public double getAvgPLs(){
    	return (this.pathlets);
    }

    public void addFlowCapacity(double flowCap){
    	this.flow +=flowCap;
    }
    public double getFlowCapacity(){
    	return (this.flow );
    }
	
	
	
}
