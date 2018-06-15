package model;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import jdistlib.Uniform;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import tools.NodeComparator;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.RandomEngine;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;


public class DirectedFG {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	private RequestNodeFactory nodeFactory = new RequestNodeFactory();
	private RequestLinkFactory linkFactory = new RequestLinkFactory();
	private TrafficFG tr = new TrafficFG();

public RequestNodeFactory getNodeFactory(){
	return this.nodeFactory;
}

public RequestLinkFactory getLinkFactory(){
	return this.linkFactory;
}

public TrafficFG getTraffic(){
	return this.tr;
}


public DirectedSparseGraph<Node, Link> getDirectedFG() {
    return this.graph;
}

public void createGraph() {
    Uniform uni= new Uniform(5,10);//5,10
    //Uniform uni= new Uniform(5,10);
    Uniform uni1= new Uniform(0,1);
    int nodenum =  (int) Math.floor(uni.random());
    
    double probTemplate =uni1.random();
    System.out.println("nodenum: "+nodenum+ " " +probTemplate);
    
/*	try {
        System.in.read();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }*/
    
    if (probTemplate<0.4){//0.4
    	tr.generateTraffic(2);
    	chain(nodenum,tr, false);
    }else if (probTemplate<0.7){
    	tr.generateTraffic(3);
    	split(nodenum,tr, false);
    } else {
    	tr.generateTraffic(2);
    	agg(nodenum,tr,false);
    }
    
   // System.out.println(this.graph);
}

private void chain(int nodenum,TrafficFG tr, boolean bidirectional){
	Uniform uni= new Uniform(0,1);
	double ul= tr.getTraffic().get(0);
	double dl= tr.getTraffic().get(1);

	for (int i=0;i<nodenum;i++){
		double prob =uni.random();
		String type="ip";

		if (prob>0.7){
			type="nat";
		} else if (prob>0.35){
			type="dpi";
		}
		
		//System.out.println("type: "  + type);
		Node node = nodeFactory.create(type);
		double cpu = tr.generateTrafficLoad(type, tr.getTraffic(),bidirectional).get("load");
	   // System.out.println("cpu:  " +cpu +" for " +type);
		
	    int temp_val = (int) Math.floor(cpu);
	    node.setAvailableCpu(temp_val);
	    
	   if (bidirectional){
	   ((NF)node).setOutTrafficDL((int) Math.floor(dl));
	   ((NF)node).setInTrafficDL((int) Math.floor(dl));  
	    }
	   ((NF)node).setOutTrafficUL((int) Math.floor(ul));
		((NF)node).setInTrafficUL((int) Math.floor(ul));
	   
	   
		this.graph.addVertex(node);

	}
	
	ArrayList<Node> nodes = new ArrayList<Node>(graph.getVertices());
	Collections.sort(nodes,new NodeComparator());
	for (int i=0;i<nodenum-1;i++){
		createLinks(nodes.get(i),nodes.get(i+1),(int) Math.floor(ul),(int) Math.floor(dl),bidirectional);
	}
	
}

private void split(int nodenum,TrafficFG tr, boolean bidirectional){
	Uniform uni= new Uniform(0,1);
	Random r = new Random();
	int id = r.nextInt(nodenum-2);
	HashMap<Node, Integer> branches= new HashMap<Node, Integer> ();
	double ul= tr.getTraffic().get(0);
	double dl1= tr.getTraffic().get(1);
	double dl2= tr.getTraffic().get(2);
	//System.out.println("splitter id: "+id); // id of splitter
	//System.out.println("dl: " + dl1+ " ul: " +ul +" dl2: " +dl2);

	for (int i=0;i<nodenum;i++){ // for every node

		double tmp_dl=0;
		double tmp_ul=0;
		double prob =uni.random();
		String type="ip";
		
		if (i==id){
			type="lb";
		}
		else if (prob>0.7){
			type="nat";
		} else if (prob>0.3){
			type="dpi";
		} 
		
		Node node = nodeFactory.create(type);   ///create NF 
		//System.out.println("Node: " + i);
		int branch = r.nextInt(2)+1; //assign to branch 
		if (i>id) {
			//System.out.println("current node: " + i + "in branch "+branch);
			if (i==(id+1)) branches.put(node, 1);
			else if (i==(id+2)) branches.put(node, 2);
			else branches.put(node, branch);
		}else{
			branches.put(node, 0);
		}
		
		if (branches.get(node)==0){
			if (bidirectional) tmp_dl=dl1+dl2;
			tmp_ul=ul;
			//System.out.println("node: " + node.getId()+"  dl: " + tmp_dl+ " ul: " + tmp_ul );
		} else if (branches.get(node)==1){
			if (bidirectional)  tmp_dl=dl1;
			tmp_ul=ul/2;
			//System.out.println("a node: " + node.getId()+ " dl: " + tmp_dl+ " ul: " +tmp_ul );
		} else {
			if (bidirectional)tmp_dl=dl2;
			tmp_ul=ul/2;
			//System.out.println("b node: " + node.getId()+ "  dl: " + tmp_dl+ " ul: " +tmp_ul );
		}
		
		ArrayList<Double> traffic = new ArrayList<Double>();	
		traffic.add(tmp_ul);	
		if (bidirectional) traffic.add(tmp_dl);
		
	
		double cpu = tr.generateTrafficLoad(type, traffic, bidirectional).get("load");
		//System.out.println(" traffic: " + traffic + " load; " + cpu);
	    int temp_val = (int) Math.floor(cpu);
	    node.setAvailableCpu(temp_val);
	    if (bidirectional){
		((NF)node).setOutTrafficDL(tmp_dl);
		((NF)node).setInTrafficDL(tmp_dl);
	    }
		((NF)node).setOutTrafficUL(tmp_ul);
		((NF)node).setInTrafficUL(tmp_ul);
		
		
		this.graph.addVertex(node);
	}
	
	ArrayList<Node> nodes = new ArrayList<Node>(graph.getVertices());
	Collections.sort(nodes,new NodeComparator());
	
	for (int i=0;i<id;i++){
		if (branches.get(nodes.get(i))==0) {
			createLinks(nodes.get(i),nodes.get(i+1),ul,(dl1+dl2),bidirectional);
		}
	}
	
	Node prev1 = nodes.get(id);
	Node prev2=  nodes.get(id);
	
	for (int i=id+1;i<nodenum;i++){
		if (branches.get(nodes.get(i))==1) {
			createLinks(prev1,nodes.get(i),ul/2,dl1,bidirectional);
			prev1=nodes.get(i);
		}else if (branches.get(nodes.get(i))==2) {
			createLinks(prev2,nodes.get(i),ul/2,dl2,bidirectional);
			prev2=nodes.get(i);
		}
	}
	
	//System.exit(0);
		
	
}

private void agg(int nodenum,TrafficFG tr, boolean bidirectional){
	Uniform uni= new Uniform(0,1);
	Random r = new Random();
	int randomNum1 = r.nextInt((nodenum/2-2) + 1);
	int randomNum2 = r.nextInt((nodenum-2 - nodenum/2) + 1) + nodenum/2;
	HashMap<Node, Integer> branches= new HashMap<Node, Integer> ();
	double ul= tr.getTraffic().get(0);
	double dl= tr.getTraffic().get(1);
//	System.out.println("Splitter: " + ul + "   " +dl);
	
	for (int i=0;i<nodenum;i++){
		double tmp_dl=0;
		double tmp_ul=0;
		double prob =uni.random();
		
		String type="ip";
		//System.out.println("Node: " + i);
		if ((i==randomNum1) || (i==randomNum2)){
			type="lb";
		} else if (prob>0.7){
			type="nat";
		} else if (prob>0.3){
			type="dpi";
		}
		
		Node node = null; 
	    
		int branch = r.nextInt(2)+1;
		if ((i>randomNum1) && (i<randomNum2)){
			if (i==(randomNum1+1)) {
				type="aes";
				node = nodeFactory.create(type);  
				branches.put(node, 1);
			}
			else if (i==(randomNum1+2)){
				type="fw";
				node = nodeFactory.create(type);   
				branches.put(node, 2);
			}
			else{
				node = nodeFactory.create(type);  
				branches.put(node, branch);	
				tmp_dl=dl;
				tmp_ul=ul;
			}
		}else{
			node = nodeFactory.create(type);  
			branches.put(node, 0);
			tmp_dl=dl;
			tmp_ul=ul;
		}
		
		if ((branches.get(node)==1) || (branches.get(node)==2)){
			tmp_dl=dl/2;
			tmp_ul=ul/2;
		} 
		
		ArrayList<Double> traffic = new ArrayList<Double>();	
		traffic.add(tmp_ul);	
		traffic.add(tmp_dl);
		
	//	System.out.println("traffic : " +traffic );
		
		double cpu = tr.generateTrafficLoad(type, traffic, bidirectional).get("load");
	    int temp_val = (int) Math.floor(cpu);
	    node.setAvailableCpu(temp_val);
	    ((NF)node).setOutTrafficDL(tmp_dl);
	    ((NF)node).setInTrafficDL(tmp_dl);
	    if (bidirectional){
		   ((NF)node).setOutTrafficUL(tmp_ul);
		   ((NF)node).setInTrafficUL(tmp_ul);
	    }
		
		this.graph.addVertex(node);
	}

	
	ArrayList<Node> nodes = new ArrayList<Node>(graph.getVertices());
	Collections.sort(nodes,new NodeComparator());
	
	for (int i=0;i<randomNum1;i++){
		if (branches.get(nodes.get(i))==0) {
		createLinks(nodes.get(i),nodes.get(i+1),ul,dl,bidirectional);
		}
	}
	for (int i=randomNum2;i<nodenum-1;i++){
		if (branches.get(nodes.get(i))==0) {
		createLinks(nodes.get(i),nodes.get(i+1),ul,dl,bidirectional);
		}
	}
	
	
	Node prev1 = nodes.get(randomNum1);
	Node prev2=  nodes.get(randomNum1);
	
	for (int i=randomNum1+1;i<randomNum2+1;i++){
		if (branches.get(nodes.get(i))==1) {
			createLinks(prev1,nodes.get(i),ul/2,dl/2,bidirectional);
			prev1=nodes.get(i);
		}else if (branches.get(nodes.get(i))==2) {
			createLinks(prev2,nodes.get(i),ul/2,dl/2,bidirectional);
			prev2=nodes.get(i);
		}else{ //lb case
			createLinks(prev1,nodes.get(i),ul/2,dl/2,bidirectional);
			prev1=nodes.get(i);
			createLinks(prev2,nodes.get(i),ul/2,dl/2,bidirectional);
			prev2=nodes.get(i);
		}
	}
		
}



private void createLinks(Node parent, Node child,double ul, double dl){
	Link link,rlink;
	
	link=linkFactory.create(ul);
	rlink=linkFactory.create(dl);
	
	this.graph.addEdge(link, parent, child, EdgeType.DIRECTED);
	this.graph.addEdge(rlink, child, parent, EdgeType.DIRECTED);
  
}


private void createLinks(Node parent, Node child,double ul, double dl,boolean bidirectional){
	Link link,rlink;
	
	link=linkFactory.create(ul);
	rlink=linkFactory.create(dl);
	
	if (bidirectional){
	this.graph.addEdge(rlink, child, parent, EdgeType.DIRECTED);
	}

	this.graph.addEdge(link, parent, child, EdgeType.DIRECTED);
		
	
  
}

/*private void createLinks(Node parent, Node child,TrafficFG tr){
	Link link,rlink;
	
	
	link=linkFactory.create(tr.getTraffic().get(0));
	rlink=linkFactory.create(tr.getTraffic().get(1));
	
	this.graph.addEdge(link, parent, child, EdgeType.DIRECTED);
	this.graph.addEdge(rlink, child, parent, EdgeType.DIRECTED);
  
}*/



}
