package model;

//import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;






import java.util.Map;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
//import org.apache.commons.collections15.Transformer;
//import org.junit.Test;

import tools.LinkComparator;
import tools.NodeComparator;
import model.FoldingTransformerFixed.FoldedEdge;
import model.components.Link;
import model.components.Node;
/*import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.transformation.FoldingTransformer;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;*/
import edu.uci.ics.jung.graph.DirectedSparseGraph;
//import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


public class DirectedFatTreeL3 {
	
	private DirectedSparseGraph<Node, Link> graph = new DirectedSparseGraph<Node, Link>();
	//private Hypergraph<Node, Link> hGraph =   new DirectedSparseMultigraph<Node, Link>();
	private Hypergraph<Node, Link> hGraph=new SetHypergraph<Node, Link>();
	private MultiHashMap<Pair<Node>,List<Pair<Node>>> pathlets =  null;
	private MultiHashMap<Pair<Integer>,List<Pair<Integer>>> pathlets1 =  null;
	private MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathletsUse=null;
	private ArrayList<DCpod> podsArray = new ArrayList<DCpod>();
	private ArrayList<Node> rootSwitches = new ArrayList<Node>();
	private Node rootElement;
	private SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
	private SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();
	private SubstrateLinkFactory hlinkFactory = new SubstrateLinkFactory();
	private int pods = 4;
	AllPathsDetector<Node, Link> allp = new  AllPathsDetector<Node, Link>();
	private int[][][][][]subHyper;
	
	
	class Path {
		Node start;
		Node end;
		List<Link> path;
	    public Path(Node start, Node end, List<Link> path) {
	       this.start = start;
	       this.end = end;
	       this.path = path;
	    }

	    public Node getStart() { return this.start; }
	    public Node getEnd() { return this.end; }

	}
	
public MultiHashMap<Pair<Integer>,List<Pair<Integer>>> getPaths() {
	return this.pathlets1;
}

public MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> getPathsUsage() {
	return pathletsUse;
}

public  void setPathsUsage(MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathsUseUp) {
	this.pathletsUse = pathsUseUp;
}

public SubstrateNodeFactory getNodeFactory(){
	return this.nodeFactory;
}

public SubstrateLinkFactory getLinkFactory(){
	return this.linkFactory;
}

public int[][][][][] getPathlets(){
	return this.subHyper;
}

public int podNum() {
    return this.pods;
}

public Node getRootElement() {
    return this.rootElement;
}

public void setRootElement(Node root) {
     this.rootElement=root;
}


public DirectedSparseGraph<Node, Link> getFatTreeL3Graph() {
    return this.graph;
}

public void createFTGraph() {
	int roots=(int) (Math.pow(pods,2)/4);
	for (int j=0;j<roots;j++){ //create Root Switches
		Node rootSwitch = nodeFactory.create("switch");
	    this.graph.addVertex(rootSwitch);
	    rootSwitches.add(rootSwitch);
	   // System.out.println("aaaaaaaa" + rootSwitch.getId());
	}
	
	for (int i=0;i<pods;i++){ //create pods
		DCpod pod = new DCpod();
		if (i!=0){
			pod.setNodeFactory(podsArray.get(i-1).getNodeFactory());
			pod.setLinkFactory(podsArray.get(i-1).getLinkFactory());
		}else{
			pod.setNodeFactory(nodeFactory);
			pod.setLinkFactory(linkFactory);
		}
		pod.createPodGraph(pods);
		//DirectedSparseGraph<Node, Link> cpod = pod.getGraph();
		//System.out.println(cpod);
		podsArray.add(pod);
	}
	nodeFactory = podsArray.get(pods-1).getNodeFactory();
	linkFactory =podsArray.get(pods-1).getLinkFactory();
	
	for (DCpod current: podsArray){
		ArrayList<Node> aggSwitches = current.getAggSwitches();
		 for (Node node:current.getGraph().getVertices()){
			 this.graph.addVertex(node);
		 }
		// System.out.println(current.getGraph().getEdgeCount());
		 for (Link link:current.getGraph().getEdges()){
			 Pair<Node> endpoints = current.getGraph().getEndpoints(link);
			 this.graph.addEdge(link, endpoints,EdgeType.DIRECTED);
		 }
		// System.out.println("Pod: " );
		for (int ii=0;ii< aggSwitches.size();ii++){
			for (int jj=ii*(pods/2);jj<(ii+1)*(pods/2);jj++){
			createLink(rootSwitches.get(jj),aggSwitches.get(ii));
			//System.out.println("Agg:  " +aggSwitches.get(ii)+ " CONNECTED TO " +rootSwitches.get(jj) );
			}
			
		}
		
	}
	
	System.out.println("Nodes: " +this.graph.getVertexCount());
	System.out.println("Links: " + this.graph.getEdgeCount());
	

/*	findPathsRootTor();
	findPathsInTor();
	findPathsOutTor();
	findPathsTorVM();
	System.out.println("All paths"); //one direction taken though they are bidirectional.
	System.out.println(this.allp.getPaths());
	createHyperGraph();*/
}
private void print(){
	ArrayList<Node> nodes = new ArrayList<Node>(this.graph.getVertices());
	Collections.sort(nodes,new NodeComparator());
	ArrayList<Link> links = new ArrayList<Link>(this.graph.getEdges());
	Collections.sort(links,new LinkComparator());
	
	for (Node current : nodes){
		System.out.println("["  +  current.getId() + "]: " + current.getName()+" " + current.getCpu());
	}
	for (Link current : links){
		Pair<Node> currentNodes =this.graph.getEndpoints(current);
		System.out.println("Link: " + current.getId()+ ": " +current.getBandwidth() +":" +currentNodes.getFirst() + "->"+currentNodes.getSecond());
	}
	
}

public void createPaths(){
	//findPathsTorVM();
	//findPathsVMTor();
	findPathsInTor();
	findPathsOutTor(); //exei provlima
	//printPaths();
	

	/*
	int vcount = this.graph.getVertexCount();
	this.subHyper = new int [pods][vcount][vcount][vcount][vcount]; 
	Map<Pair<Node>,Integer> tmp = new HashMap<Pair<Node>,Integer> ();
	for (List<Link> path: this.allp.getPaths()){
		//System.out.println(path);
		Link start = path.get(0);
		Link end = path.get(path.size()-1);
		Pair<Node> epStart = this.graph.getEndpoints(start);
		Pair<Node> epEnd =this.graph.getEndpoints(end);
		//System.out.println(epStart.getFirst().getId());
		//System.out.println(epEnd.getSecond().getId());
		@SuppressWarnings("unchecked")
		Pair<Node> newp= new Pair(epStart.getFirst(),epEnd.getSecond());
		if (tmp.containsKey(newp)){
		//	System.out.println("found");
			int ind = tmp.get(newp);
			tmp.put(newp, ind+1);
		}else{
			tmp.put(newp, 1);
			//System.out.println("not");
		}
		
	}
	//System.out.println(tmp);
	
	//System.exit(0);
	 for (List<Link> path: this.allp.getPaths()){
			Link start = path.get(0);
			Link end = path.get(path.size()-1);
			Pair<Node> epStart = this.graph.getEndpoints(start);
			Pair<Node> epEnd =this.graph.getEndpoints(end);
			@SuppressWarnings("unchecked")
			Pair<Node> newp= new Pair(epStart.getFirst(),epEnd.getSecond());
			int pathNum=0;
			if (tmp.containsKey(newp)){
				pathNum= tmp.get(newp);
			}

			for (Link link : path){
				Pair<Node> ep = this.graph.getEndpoints(link);
				if (pathNum>0){
				//this.subHyper[0][epStart.getFirst().getId()][epEnd.getSecond().getId()][ep.getFirst().getId()][ep.getSecond().getId()]=1;
				//}else{
					//System.out.println(pathNum);
					this.subHyper[pathNum-1][epStart.getFirst().getId()][epEnd.getSecond().getId()]
							[ep.getFirst().getId()][ep.getSecond().getId()]=1;
					tmp.put(newp, pathNum-1);
				}
			}
	}
	 
	*/
	
	//pathlets =new MultiHashMap<Pair<Node>,List<Pair<Node>>>();
	pathlets1 =new MultiHashMap<Pair<Integer>,List<Pair<Integer>>>();
	pathletsUse = new MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>>();

	for (List<Link> path: this.allp.getPaths()){
		Link start = path.get(0);
		Pair<Node> epStart = this.graph.getEndpoints(start);
		Link end = path.get(path.size()-1);
		Pair<Node> epEnd = this.graph.getEndpoints(end);
		//@SuppressWarnings("unchecked")
		//Pair<Node> newp= new Pair(epStart.getFirst(),epEnd.getSecond());
	
		Pair<Integer> newp1= new Pair<Integer>(epStart.getFirst().getId(),epEnd.getSecond().getId());
		List<Pair<Integer>> pathlist =new ArrayList<Pair<Integer>> ();
		HashMap<List<Pair<Integer>>,Integer> pathlistUsage =new HashMap<List<Pair<Integer>>,Integer> ();
			for(Link l: path){
				Pair<Node> ep= this.graph.getEndpoints(l);
				Pair<Integer> tmp= new Pair<Integer>(ep.getFirst().getId(),ep.getSecond().getId());
				pathlist.add(tmp);
			}
		pathlistUsage.put(pathlist, 0);
		pathlets1.put(newp1,pathlist);
		pathletsUse.put(newp1, pathlistUsage);
	}
	
	
}

public void createHyperGraph(){
	findPathsTorVM();
	findPathsVMTor();
	findPathsInTor();
	findPathsOutTor();
	printPaths();

	
	for (Node node: this.graph.getVertices()){
		this.hGraph.addVertex(node);
	}
	
		
	for (List<Link> llink: this.allp.getPaths()){
/*		for(int i=0;i<llink.size();i++){
		    System.out.println(llink.get(i));
		} */
		ArrayList<Node> nodes_tmp =  getNodes(llink);
		//System.out.println(nodes_tmp);
		Link link_tmp=hlinkFactory.create("torlink");	
		this.hGraph.addEdge(link_tmp,nodes_tmp, EdgeType.DIRECTED);
		/*for (Link link: llink){
			 //Pair<Node> endpoints = this.graph.getEndpoints(link);
			
		}
*/	 }
	printHgraph();
}
private void printHgraph(){
	System.out.println("Nodes: ");
	for (Node n: this.hGraph.getVertices()){
		System.out.print(" " + n.getId());
	}
	System.out.println("Edges: ");
	for (Link l: this.hGraph.getEdges()){
		System.out.println(this.hGraph.getIncidentVertices(l));
	}
	
}

public ArrayList<Node> getNodes(List<Link> path){
	ArrayList<Node> coll =  new ArrayList<Node> ();
		for (Link link : path){
		Pair<Node> ep= this.graph.getEndpoints(link);
		//System.out.print(" " +ep.getFirst().getId()+" => "+ ep.getSecond().getId());
		if (!coll.contains(ep.getFirst()))
			coll.add(ep.getFirst());
		if (!coll.contains(ep.getSecond()))
			coll.add(ep.getSecond());
		}
	
	return coll;
	
	
}

/*@Test public void testFoldingBreaksEdgeAccess(){
	  Hypergraph<String,Integer> hGraph1=new SetHypergraph<String,Integer>();
	  hGraph1.addVertex("A");
	  hGraph1.addVertex("B");
	  hGraph1.addVertex("C");
	  hGraph1.addVertex("D");
	  hGraph1.addEdge(1,Arrays.asList("A","B"));
	  hGraph1.addEdge(2,Arrays.asList("B","A"));
	  hGraph1.addEdge(3,Arrays.asList("A","B","C"));
	  hGraph1.addEdge(4,Arrays.asList("B","C"));
	  
	  Graph<String,FoldedEdge<String,Integer>> graph=
			  FoldingTransformerFixed.foldHypergraphEdges
			  (hGraph1,UndirectedSparseGraph.<String,FoldedEdge<String,Integer>>getFactory());
	  assertEquals(new HashSet<String>(Arrays.asList("A","B","C","D")),new HashSet<String>(graph.getVertices()));
	  assertEquals(3,graph.getEdgeCount());
	  assertEquals(new HashSet<Integer>(Arrays.asList(1,2,3)),new HashSet<Integer>(graph.findEdge("A","B").getFolded()));
	  assertEquals(new HashSet<Integer>(Arrays.asList(3)),new HashSet<Integer>(graph.findEdge("A","C").getFolded()));
	  assertEquals(new HashSet<Integer>(Arrays.asList(3,4)),new HashSet<Integer>(graph.findEdge("B","C").getFolded()));
	  assertEquals(new Pair<String>("A","B"),graph.getEndpoints(graph.findEdge("A","B")));
	}*/

public void printPaths(){
	System.out.println();
	for (List<Link> path: this.allp.getPaths()){
		System.out.println(getNodes(path));
	}
	
}

private void findPathsRootTor(){
	AllPathsDetector<Node, Link> allp_tmp = new  AllPathsDetector<Node, Link>();
	
	for (Node root: rootSwitches){
		for (DCpod current: podsArray){
			for (Node TORswitch: current.getTORSwitches()){
				int hops=0;
				List<List<Link>> allPaths= new ArrayList<List<Link>>();
				while (allPaths.isEmpty()){
				System.out.println(hops + " " + root.getId()+" " +TORswitch.getId());
				allPaths =allp_tmp.getAllPathsBetweenNodes(this.graph, 
			    		root, TORswitch, hops);
					if (!allPaths.isEmpty()){
						//System.out.println(allPaths);
						this.allp.addPaths(allPaths);
					}
				hops++;
				}
			}
		}
	}
	
}

private void findPathsInTor(){

	for (DCpod current: podsArray){
		//System.out.println("Tor Switches: " +current.getTORSwitches().size());
		for (int i=0; i<current.getTORSwitches().size();i++){
			for (int j=0;j< current.getTORSwitches().size(); j++){
				if (j!=i){
					int hops=0;
					List<List<Link>> allPaths= new ArrayList<List<Link>>();
					Node start=current.getTORSwitches().get(i);
					Node end=current.getTORSwitches().get(j);
					while (allPaths.isEmpty()){
						//System.out.println(hops + " " + start.getId()+" " +end.getId());
						allPaths =allp.getAllPathsBetweenNodes(this.graph, start, end, hops);
							if (!allPaths.isEmpty()){
								//System.out.println("All paths" + allPaths);
								this.allp.addPaths(allPaths);
							}
						hops++;
					}
				}
			}
		}
	}

}

private void findPathsOutTor(){
	//System.out.println("podsArray : " +podsArray.size());
	for (DCpod current: podsArray){
		for (int i=0; i<current.getTORSwitches().size();i++){
			for (DCpod current1: podsArray){
				if (!current.equals(current1)){
					for (int j=0;j< current1.getTORSwitches().size(); j++){
						int hops=0;
						List<List<Link>> allPaths= new ArrayList<List<Link>>();
						Node start=current.getTORSwitches().get(i);
						Node end=current1.getTORSwitches().get(j);
						while (allPaths.isEmpty()){
						//System.out.println(hops + " " + start.getId()+" " +end.getId());
						allPaths =allp.getAllPathsBetweenNodes(this.graph, 
					    		start, end, hops);
							if (!allPaths.isEmpty()){
								//System.out.println(allPaths);
								this.allp.addPaths(allPaths);
							}
						hops++;
						}
					}
				}
			}
		}
	}

}

private void findPathsTorVM(){

	for (DCpod current: podsArray){
		for (Node TORswitch: current.getTORSwitches()){
			for (Node server: current.getpodServers()){
				int hops=1;
				List<List<Link>> allPaths= new ArrayList<List<Link>>();
				while (allPaths.isEmpty()){
				//System.out.println(hops + " " + TORswitch.getId()+" " +server.getId());
				allPaths =allp.getAllPathsBetweenNodes(this.graph, 
						TORswitch, server, hops);
					if (!allPaths.isEmpty()){
						//System.out.println(allPaths);
						this.allp.addPaths(allPaths);
					}
				hops++;
				if (hops>1);break;
				}
			}
		}
	}

}

private void findPathsVMTor(){

	for (DCpod current: podsArray){
		for (Node server : current.getpodServers()){
			for (Node TORswitch: current.getTORSwitches()){
				int hops=1;
				List<List<Link>> allPaths= new ArrayList<List<Link>>();
				while (allPaths.isEmpty()){
				//System.out.println(hops + " " + TORswitch.getId()+" " +server.getId());
				allPaths =allp.getAllPathsBetweenNodes(this.graph, 
						 server, TORswitch, hops);
					if (!allPaths.isEmpty()){
						//System.out.println(allPaths);
						this.allp.addPaths(allPaths);
					}
				hops++;
				if (hops>1);break;
				}
			}
		}
	}

}

private void createLink(Node parent, Node child){
	Link link,rlink;
	String type = child.getType();
	//System.out.println(type);
	
	if (type.equalsIgnoreCase("Server")){
		link=linkFactory.create("torlink");	
		rlink=linkFactory.create("torlink");	
	}
	else {
		link=linkFactory.create("interracklink");
		rlink=linkFactory.create("interracklink");
	}
    this.graph.addEdge(link, parent, child,EdgeType.DIRECTED);
    this.graph.addEdge(rlink, child, parent,EdgeType.DIRECTED);
}
///////////////


}
