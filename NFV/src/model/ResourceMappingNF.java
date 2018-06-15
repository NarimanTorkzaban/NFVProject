package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.multimap.MultiHashMap;

import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.Node;
import model.components.Path;
import model.components.RequestRouter;
import model.components.RequestSwitch;
import model.components.Server;
import model.components.SubstrateRouter;
import model.components.SubstrateSwitch;
import model.components.VirtualMachine;

public class ResourceMappingNF {
	@SuppressWarnings("unused")
	private Request request;
	private double cost=0;
	private double revenue=0;
	private double avg_hops=0;
	private int rules=0;
	private LinkedHashMap<Node, Node> nodeMap= new LinkedHashMap<Node, Node>(); //request-real 
	private HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> linkMap =
			new HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>>();
	private HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> linkMapInt =
			new HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>>();
	private MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> flowMap= 
			new MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> ();;
	@SuppressWarnings("unused")
	private boolean denied=false;
	
	public ResourceMappingNF(Request req){
		this.request=req;
	}

	public void setNodeMapping(LinkedHashMap<Node, Node> map){
		this.nodeMap=map;
	}
	
	public void setLinkMapping(HashMap<Link, ArrayList<LinkedHashMap<Link, Double>>> f){
		this.linkMap=f;
	}
	
	public void setFlowMapping(MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>  f){
		this.flowMap=f;
	}
	
	public void setLinkMappingNew(HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> f){
		this.linkMapInt=f;
	}
	
	public void setRulesAdded(int rul){
		this.rules=rul;
	}
	public int getRules(){
		return this.rules;
	}
	
	public void setEmbeddingCost(double ecost){
		this.cost=ecost;
	}
	
	public double getEmbeddingCost(){
		return this.cost;
	}
	public void setHops(double h){
		this.avg_hops=h;
	}
	
	public double getHops(){
		return this.avg_hops;
	}
	public void setEmbeddingRevenue(double erev){
		this.revenue=erev;
	}
	
	public double getEmbeddingRevenue(){
		return this.revenue;
	}
	
	public void denied() { this.denied=true; }
	public void accepted() { this.denied=false; }
	
	public boolean isDenied() { return this.denied;}
	

	public void reserveNodes(Substrate sub){
		ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(this.request.getGraph());		
			
		for  (Node node: reqNodes){
			 for (Node x: sub.getGraph().getVertices()){
				 Node mappedNode=this.nodeMap.get(node);
				 int cap=node.getCpu();
				   if (x.getId()==mappedNode.getId()){
					   //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
					   double capNew= (x.getAvailableCpu()-cap);
					   //System.out.println((int)capNew);
					   x.setAvailableCpu((int) capNew);
	
			   }
			 }	
		}
	
	}

	public void releaseNodes(Substrate sub){
		ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(this.request.getGraph());		
		if (!this.nodeMap.isEmpty()){
			for  (Node node: reqNodes){
				 for (Node x: sub.getGraph().getVertices()){
					 Node mappedNode=this.nodeMap.get(node);
					 int cap=node.getAvailableCpu();
					   if (x.getId()==mappedNode.getId()){
						   //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
						   double capNew= (x.getAvailableCpu()+cap);
						   //System.out.println((int)capNew);
						   x.setAvailableCpu((int) capNew);
		
				   }
				 }	
			}
		}

		
	}
	
	public void reserveLinks(Substrate sub){
		ArrayList<Link> reqLinks = (ArrayList<Link>) getLinks(this.request.getGraph());
		int reqLinkNum=reqLinks.size(); 
		ArrayList<Link> subLinks = (ArrayList<Link>) getLinks(sub.getGraph());

		for (int i=0;i<reqLinkNum;i++ ){
			 ArrayList<LinkedHashMap<Link, Double>> tmp = new  ArrayList<LinkedHashMap<Link, Double>>();
			 tmp = this.linkMap.get(reqLinks.get(i));
			 for (LinkedHashMap<Link, Double> x: tmp ){ //for all mappings
				 //System.out.println(tmp);
				 if (!x.isEmpty()){
					 for (Map.Entry<Link, Double> entry : x.entrySet()) {
						 if (!(entry==null)){
						    Link mappedLink = entry.getKey();
						    //sub.print();
						   // System.out.println(mappedLink);
						    Double cap = entry.getValue();
						    double newCap = mappedLink.getBandwidth()-cap;
						    //System.out.println("newCap "+newCap);
						    Pair<Node> eps = sub.getGraph().getEndpoints(subLinks.get(mappedLink.getId()));
						    //System.out.println(eps.getFirst().getId()+ " " + eps.getSecond().getId());
							Link tmplink = sub.getGraph().findEdge(eps.getFirst(), eps.getSecond());
							tmplink.setBandwidth(newCap);
						 }
					 }
				 }
			 }
		}//for all requested links

	
	}
	  
	
	
	public void releaseLinks(Substrate sub){
		
		if(!flowMap.isEmpty()) {
			 for (HashMap<Pair<Integer>, Double> mapping: flowMap.keySet() ){ //for all mappings
				 if (!mapping.isEmpty()){
				        Set<Pair<Integer>> keys = mapping.keySet();
				        for(Pair<Integer> key: keys){
				        	Double cap = mapping.get(key);
				        	Collection<List<Pair<Integer>>> collPaths = flowMap.get(mapping);
				        	if (collPaths!=null){
								ArrayList<List<Pair<Integer>>>copy = new ArrayList<List<Pair<Integer>>>(collPaths);
								for (List<Pair<Integer>> list_links: copy){
									for (Pair<Integer> tmplink: list_links) {
										updateSubLink(sub,tmplink.getFirst(), tmplink.getSecond(),cap);
			
									}
								}
				       
				        	}
				        }
				 }
			 }
		}
	}
			 
	 private boolean updateSubLink(Substrate sub,int src, int dst, double cap){
		  for (Link x: sub.getGraph().getEdges()){
			  Pair<Node> eps = sub.getGraph().getEndpoints(x);
			  if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
				// System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
				  double newCap = x.getBandwidth()+cap;
				  x.setBandwidth(newCap);
				//  System.out.println(newCap);
				  return true;
			  }
		  }
		 
		  return false;
		  
	}
	
/*	@SuppressWarnings("rawtypes")
	public double computeCost(Substrate sub){
		
		double cost=0;
		for (Link key: flows.keySet()){
			for (Path path: (List<Path>) flows.get(key)){
				for (Link link: path.getSubstrateLinks()){
					for (Link edge: sub.getGraph().getEdges()){
						if (link.getName()==edge.getName()){
							cost += path.getBandwidth();
						}
					}
				}
			}
		}
		
		Collection v = sub.getGraph().getVertices();
		Iterator itr_sub = v.iterator();
		while(itr_sub.hasNext()){
			Node subNode =  (Node) itr_sub.next();
			int aug_ID=  subNode.getId();

			Collection c = this.nodeMap.entrySet();
			Iterator itr = c.iterator();

			while(itr.hasNext()){
				 Map.Entry entry = (Map.Entry)itr.next();
				 if(aug_ID == ((Node)entry.getValue()).getId()) {
				 	if (((Node)entry.getKey()) instanceof VirtualMachine)  {
				 		cost+=((VirtualMachine) entry.getKey()).getCpu()+((VirtualMachine)entry.getKey()).getMemory()+((VirtualMachine)entry.getKey()).getDiskSpace();
			   	 	}
				    else if ((((Node)entry.getKey()) instanceof RequestRouter)){
				    	cost+= ((SubstrateRouter) subNode).getStress();
		    	 	}
				    else if ((((Node)entry.getKey()) instanceof RequestSwitch)) {
				    	cost+= ((SubstrateSwitch) subNode).getStress();
				    }
				 }
			}		
		}		
		return cost;
	}
	*/
	@SuppressWarnings("unchecked")
	public ArrayList<Node> getNodes(Graph<Node,Link> t) {
		ArrayList<Node> reqNodes =new ArrayList<Node>();

		for (Node x: t.getVertices())
			reqNodes.add(x);

		Collections.sort(reqNodes,new NodeComparator());

		return reqNodes;
	}
	@SuppressWarnings("unchecked")
	private ArrayList<Link> getLinks(Graph<Node, Link> t) {
		ArrayList<Link> links =new ArrayList<Link>();
		Collection<Link> edges =  t.getEdges();

		for (Link current : edges){
			links.add(current);
		}
		
		Collections.sort(links,new LinkComparator());
		
		return links;
	}
	
	@SuppressWarnings("rawtypes")
	void printNodeMapping(){
		Collection c = this.nodeMap.entrySet();
		Iterator itr = c.iterator();
		while(itr.hasNext()){
			 Map.Entry entry = (Map.Entry)itr.next();
			 
			 System.out.println("Virtual " +((Node)entry.getKey()).getId()+  " cpu  " +((Node)entry.getKey()).getCpu() + 
					 " Real " + ((Node) entry.getValue()).getId() + " cpu "  + ((Node) entry.getValue()).getCpu());
		}
	}

	void printFlowMapping(Substrate sub){
		
		for (HashMap<Pair<Integer>,Double> key: this.flowMap.keySet()){
			System.out.println("For Virtual Link "+ this.flowMap.keySet().toArray()[0]);
			for (List<Pair<Integer>> paths:  this.flowMap.get(key)){
				Iterator<Pair<Integer>> iterator = paths.iterator();
				while (iterator.hasNext()) {
				Pair<Integer> tmp_pair= iterator.next();
				 System.out.println(tmp_pair.getFirst()+ " -> " + tmp_pair.getSecond() +" : bw " +this.flowMap.keySet().toArray()[1]); 
				}
			}
		}	

	}
}


