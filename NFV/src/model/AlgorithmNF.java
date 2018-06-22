package model;

import gui.SimulatorConstants;

import java.io.BufferedWriter;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import com.artelys.knitro.api.KTRConstants;
import com.artelys.knitro.api.KTRException;
import com.artelys.knitro.api.KTRISolver;
import com.artelys.knitro.api.KTRSolver;

import algorithm.ProblemVirtualization;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import output.Results;
import tools.LinkComparator;
import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.Server;
import model.components.SubstrateLink;

/**
 * DUMMY Algorithm Class.
 */

public class AlgorithmNF {
	 

	private String id;
	private String state;
	private Substrate InPs;
	private static List<Substrate> substrates ;
	private List<Request> reqs;
	private List<Node> nfs;
    private  static int  MAX_TYPES = 3;
    private Results results=null;
    

	public AlgorithmNF(String id) {
		this.id = id;
		this.state = "available";
	}
	
	public AlgorithmNF(String id,Results results) {
		this.id = id;
		this.state = "available";
		this.results=results;
		
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}
	
	public Results getResults() {
		return this.results;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	@SuppressWarnings("static-access")
	public void addSubstrate(List<Substrate> substrates) {
		this.substrates = substrates;
	}
	
	public void addNFs(List<Node> nfs) {
		this.nfs=nfs;
	}
	
	public void addRequests(List<Request> reqs) {
		this.reqs = reqs;
	}
	
	public void addInPs(Substrate InPs){
		this.InPs = InPs;
	}
	
	
	

public boolean runAlgorithm (){
	boolean retres = false;
		
	 if  (this.id.contentEquals("NF_Paths")){
			retres=NFplacementPathlets1();
	 }
	 else if  (this.id.contentEquals("NF_Paths_alt")){
			retres=NFplacementPathletsAlt1();
	 }
	 else if  (this.id.contentEquals("trust_nf")){
			retres=placementTrustNF();
	 }
				
	return retres;
}
	
public static void waiting (int n){
        
        long t0, t1;

        t0 =  System.currentTimeMillis();

        do{
            t1 = System.currentTimeMillis();
        }
        
        while (t1 - t0 < n);
}

	
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

public List<Node> getNFs(Graph<Node,Link> t) {
	ArrayList<Node> reqNFs =new ArrayList<Node>();

	for (Node x: t.getVertices()){
		if (((x) instanceof Server  )  || ((x) instanceof NF  ) ) 
			reqNFs.add(x);
	}

	return reqNFs;
}

public Node getSimilarNF(Node vnf, ArrayList<Node> nfList) {
	System.out.println("Checking vnf: " +vnf.getName().replaceAll("\\d",""));
	for (Node x: nfList){
		System.out.println("nf " + x.getName().replaceAll("\\d","")  );
		if (x.getName().replaceAll("\\d","").equalsIgnoreCase(vnf.getName().replaceAll("\\d",""))) 
			return x;
	}

	return null;
}

public boolean exists(Collection<Link> coll, Node node, Graph<Node,Link> g){
	//System.out.println("checking existance: " + node.getId());
	Iterator<Link> iter = coll.iterator();
	while (iter.hasNext()) {
	    Link elem = iter.next();
	    Pair<Node> currentNodes = g.getEndpoints(elem);
	    //System.out.println("checking currentNodes: " + currentNodes);
	    if (currentNodes.contains(node)){
	    	// System.out.println("Found");
	    	return true;
	    }
	}
	return false;
}

public Link getLink(Node a, Node b, Collection<Link> coll,HashMap<Integer, Pair<Node>> linkEPs){ 
	Link l = null;
//	System.out.println("checking Link: " + a.getId()+ " _  " + b.getId());
	Iterator<Link> iter = coll.iterator();
	while (iter.hasNext()) {
	    Link elem = iter.next();
	  //  System.out.println("checking Link: " + elem.getId());
	    Pair<Node> currentNodes = linkEPs.get(elem.getId());
		//System.out.println(currentNodes);
	    if (currentNodes.getFirst().getId()==a.getId()){
	    //	 System.out.println("checking Node F: " + currentNodes.getFirst().getId());
	    	if(currentNodes.getSecond().getId()==b.getId()){
	    	//	System.out.println("checking Node S: " + currentNodes.getSecond().getId());
	    		return elem;
	    	}
	    }
	}
	return l;
}

public Link getLink(int a,int  b, Collection<Link> coll,HashMap<Integer, Pair<Node>> linkEPs){ 
	Link l = null;
	Iterator<Link> iter = coll.iterator();
	//System.out.println(a + "  " +b );
	while (iter.hasNext()) {
	    Link elem = iter.next();
	    Pair<Node> currentNodes = linkEPs.get(elem.getId());
	   // System.out.println("currentNodes " +currentNodes );
	    if (currentNodes.getFirst().getId()==a){
	    	if(currentNodes.getSecond().getId()==b){
	    		return elem;
	    	}
	    }
	}
	return l;
}

public Node getSimilarVNF(Node nf, ArrayList<Node> vnfList) {
	
	for (Node x: vnfList){
		if (x.getName().replaceAll("\\d","").equalsIgnoreCase(nf.getName().replaceAll("\\d",""))) 
			return x;
	}

	return null;
}

public boolean checkType(Node node, Node vnf){
	if (node.getType().equalsIgnoreCase(vnf.getType())){
		return true;
	} else if (node.getType().equalsIgnoreCase("Server") && vnf.getType().equalsIgnoreCase("NF") ){
		return true;
	}
	return false;
}


private boolean updateSubLink(Substrate sub,int src, int dst, double cap){
	  for (Link x: sub.getGraph().getEdges()){
		  Pair<Node> eps = sub.getGraph().getEndpoints(x);
		  if ((eps.getFirst().getId()==src) && (eps.getSecond().getId()==dst)){
			  //System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
			  double newCap = x.getBandwidth()-cap;
			  if ((newCap<0.1) && (newCap>0)){
				  System.out.println("Found edge: " + src+ " -> " +dst + " for " + eps.getFirst().getId()+" -> "+eps.getSecond().getId());
				  System.out.println(x.getBandwidth() + " cap: " +cap +" "+newCap);
			  }
			  if (eps.getFirst().getType().equalsIgnoreCase("switch") ){
			  		updateSubstrateTCAM(sub, eps.getFirst());
			  }
			  x.setBandwidth((int)newCap);
			  //System.out.println(newCap);
			  return true;
		  }
	  }
	 
	  return false;
	  
}

private boolean updateSubstrate(Substrate sub, Node node, double cap){
	   for (Node x: sub.getGraph().getVertices()){
		   if (x.getId()==node.getId()){
			  // System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
			   double capNew= (x.getAvailableCpu()-cap);
			   //System.out.println((int)capNew);
			   x.setAvailableCpu((int) capNew);
			   return true;
		   }
		 
	   }
	   return false;
}
	 
private boolean updateSubstrateTCAM(Substrate sub, Node node){
	   for (Node x: sub.getGraph().getVertices()){
		   if (x.getId()==node.getId()){
			  // System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
			   int capNew= x.getTCAM()-1;
			   //System.out.println((int)capNew);
			   x.setTCAM(capNew);
			   return true;
		   }
		 
	   }
	   return false;
}


public 	double[][][][] RoundingMapping(int subNodesNumAug, 
		int nodesSFC, double[][] sfcLinks , double[] subCapAug, double[] sfcNodes, double[][][][] xVar, double[][][][] fVar){
	int subNodesNum=subNodesNumAug-nodesSFC;
	
	double[][][][] xVarFinal =new double [subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
	double[][][][] xVarIm =new double [subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
	int index1=0;
	int index2=0;
	int index3=0;
	int index4=0;

	
	for(int k=0; k< nodesSFC; k++){
		for(int m=0; m<nodesSFC; m++){
			double flow=0;	
			for (int u=0;u<subNodesNumAug;u++){
				for (int v=0;v<subNodesNumAug;v++){
					flow = fVar[u][v][k][m];	
					xVarIm[u][v][k][m]= flow*xVar[u][v][k][m];
				}
			}
		}
	}
	//one way link	
	for(int k=0; k< nodesSFC; k++){
		for(int m=0; m<nodesSFC; m++){
		double max=0;
			if (sfcLinks[k][m]!=0) {
				for (int u=0;u<subNodesNumAug;u++){
					if (u==subNodesNum+k) {
						for (int v=0;v<subNodesNumAug;v++){
						//	System.out.println("X" + u+ " "+v + " " + k +" "+ m  +" " +xVarIm[u][v][k][m] + " " +max );
							if (xVarIm[u][v][k][m]>max){
								max=xVarIm[u][v][k][m];
								index1=u;
								index2=v;
								index3=k;
								index4=m;
							}
						}
						xVarFinal[index1][index2][index3][index4]=1;
					}
					//System.out.println("x" + index1+ " "+index2 + " " + index3+" "+ index4  +" " +xVarFinal[index1][index2][index3][index4]  );
				}
			}
		}
	}
	//second direction
	for(int k=0; k< nodesSFC; k++){
		for(int m=0; m<nodesSFC; m++){
		double max=0;
			if (sfcLinks[k][m]!=0) {
				for (int u=0;u<subNodesNumAug;u++){
						for (int v=0;v<subNodesNumAug;v++){
							if (v==subNodesNum+m) {
							//	System.out.println("x" + u+ " "+v + " " + k +" "+ m  +" " +xVarIm[u][v][k][m] + " " +max );
								if (xVarIm[u][v][k][m]>max){
									max=xVarIm[u][v][k][m];
									index1=u;
									index2=v;
									index3=k;
									index4=m;
								}
							}
						}
						if (max>0)
							xVarFinal[index1][index2][index3][index4]=1;
					//System.out.println("x" + index1+ " "+index2 + " " + index3+" "+ index4  +" " +xVarFinal[index1][index2][index3][index4]  );
				}
			}
		}
	}
	
	System.out.println("+++++++++ ");
		for(int k=0; k< nodesSFC; k++){
			for(int m=0; m<nodesSFC; m++){
				for (int u=0;u<subNodesNumAug;u++){
					for (int v=0;v<subNodesNumAug;v++){
						if (xVarFinal[u][v][k][m]==1)
							System.out.println("x" + u+ " "+v + " " + k +" "+ m  );
					}
				}
			}
		}
		if (checkFeasibility(xVarFinal,subNodesNum,subNodesNumAug,nodesSFC,
				sfcLinks,subCapAug, sfcNodes)){
			System.out.println("Found feasible mapping solution!!");
			return  xVarFinal;
		}
	
		
		return null;
}

public boolean checkFeasibility(double[][][][] xVarFinal, int subNodesNum, int subNodesNumAug, int nodesSFC, 
		double[][] sfcLinks, double[]subCapAug, double[] sfcNodes){
	//placement 1
	System.out.println(subNodesNum+ " "+subNodesNumAug + " "+ nodesSFC );
	
	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for(int m=0; m<nodesSFC; m++){
			double sum =0;
			if (sfcLinks[p-subNodesNum][m]!=0) {
				for (int w=0;w<subNodesNum;w++){	
					 sum=sum+xVarFinal[p][w][p-subNodesNum][m];
				}
			if (sum!=1) return false;
			}
		}	
	}
	
	//node capacity constraints (2)
	for (int v=0;v<subNodesNum;v++){
		double cpuCap = subCapAug[v];
			double cpuReq=0;
			ArrayList<Integer> visited= new ArrayList<Integer> ();
			ArrayList<Integer> visited_rev= new ArrayList<Integer> ();
			for(int m=0; m<nodesSFC; m++){
				for (int u=subNodesNum;u<subNodesNumAug;u++){
					if (sfcLinks[u-subNodesNum][m]!=0){
						if (!(visited.contains(u-subNodesNum))){
						//System.out.println("x" + u + "  " +v + " " + (u-subNodesNum) + " " +m);
						cpuReq = sfcNodes[u-subNodesNum]*xVarFinal[u][v][u-subNodesNum][m];
						visited.add(u-subNodesNum);
						}
					}
					else if (sfcLinks[m][u-subNodesNum]!=0){
						if (!(visited.contains(u-subNodesNum))){
						//System.out.println("x1 " + v + "  " +u + " " + m + " " +(u-subNodesNum));
						cpuReq = sfcNodes[u-subNodesNum]*xVarFinal[v][u][m][u-subNodesNum];
						visited.add(u-subNodesNum);
						}
					}			
				}
			}
		//System.out.println("lEQ "+cpuCap);
		if (Double.compare(cpuReq, cpuCap)>0) return false;
	}
	
	
/*	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for(int m=0; m< nodesSFC; m++){
			if(m!=p-subNodesNum){
				List<Integer> tmp = new ArrayList<Integer> ();
				List<Integer> tmp1 = new ArrayList<Integer> ();
				tmp.addAll(Arrays.asList(p-subNodesNum,m));
				tmp1.addAll(Arrays.asList(p-subNodesNum,m));
				if (requestedLinks.containsKey(tmp) && requestedLinks.containsKey(tmp1)){
					for (int w=0;w<subNodesNum;w++){
						double sum = xVarFinal[p][w][p-subNodesNum][m] -xVarFinal[w][p][m][p-subNodesNum];	
						if (sum!=0)  return false;
					}
				}
			}
		}
}*/
	
	

/*	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for (int w=0;w<subNodesNum;w++){
			for(int m=0; m< nodesSFC; m++){
				for(int l=0; l< nodesSFC; l++){
				double sum =0;
				sum=xVarFinal[p][w][p-subNodesNum][m]-xVarFinal[p][w][p-subNodesNum][l];
				if (sum!=0) return false;
				}
			}
		}
	}
	
	//
	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for (int w=0;w<subNodesNum;w++){
			for(int m=0; m< nodesSFC; m++){
				for(int l=0; l< nodesSFC; l++){
					double sum =0;
					sum = xVarFinal[p][w][p-subNodesNum][m] - xVarFinal[w][p][l][p-subNodesNum];
					if (sum!=0) return false;
				}
			}
		}
	}*/
	
	return true;
}

	 
public Node getNodeById (ArrayList<Node> nodes, int id){
		 for (Node x: nodes){
			 if (x.getId()==id)
				 return x;
		 }
		 return null;
	 }
	 
public static void printMap(Map mp) {
		    Iterator it = mp.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        Node v = (Node) pair.getKey();
		        Node s= (Node) pair.getValue();
		        System.out.println(v.getId()+":"+v.getName() + " = " +  s.getId()+":"+ s.getName());
		        if ((v.getType().contentEquals("RRH")) || v.getType().contentEquals("Router")){
		        	System.out.println(v.getType()+ " " + v.getCoords().getX()+ " " +v.getCoords().getY());
		        System.out.println(s.getType()+ " " + s.getCoords().getX()+ " " +s.getCoords().getY());
		        }
		    }
		}
	 

public static void printSolutionResults(KTRISolver solver, int solveStatus) throws KTRException {
    if (solveStatus != 0) {
        System.out.println("Failed to solve the problem, final status = " + solveStatus);
        return;
    }

    System.out.println("Solution found");
    System.out.format("%25s = %.2e%n", "Objective value", solver.getObjValue());
    System.out.format("%25s = (", "Solution point");
    Iterator<Double> it = solver.getXValues().iterator();
    while (it.hasNext()) {
        System.out.format("%.2f", it.next());
        if (it.hasNext())
            System.out.print(", ");
    }
    System.out.println(") ");
    
    

    
    
    for(int i=0; i<solver.getProblem().getNNZH(); i++)
    	System.out.printf("x[%d] = %f \t", i,solver.getXValues(i));
    System.out.println();

    for(int i=0; i<Math.pow(solver.getProblem().getNNZH(),2); i++) {
    	System.out.printf("f[%d] = %f \t", i,solver.getXValues(solver.getProblem().getNNZH()+i));
    	if(i%solver.getProblem().getNNZH()==5)
    		System.out.println();
    }
    
    
}

public int[][] getGraphMatrix(Graph<Node,Link> t){
	
	int[][] matrix  = new int[t.getVertexCount()][t.getVertexCount()];
	for(Link current: getLinks(t)) {
		matrix[t.getEndpoints(current).getFirst().getId()][t.getEndpoints(current).getSecond().getId()] = 1;
	}
	return matrix;
	
}

public double[][] getBWMatrix(Graph<Node,Link> t){
	double[][] matrix  = new double[t.getVertexCount()][t.getVertexCount()];
	for(Link current: getLinks(t)) {
		matrix[t.getEndpoints(current).getFirst().getId()][t.getEndpoints(current).getSecond().getId()] = current.getBandwidth();
	}
	return matrix;
}

public double[] getCPUMatrix(Graph<Node,Link> t) {
	double[] matrix = new double[t.getVertexCount()];
	for(Node current: getNodes(t)) 
		matrix[current.getId()] = current.getAvailableCpu();
	return matrix;
}

public double[] getTrustMatrix(Graph<Node,Link> t) {
	double[] matrix = new double[t.getVertexCount()];
	for(Node current: getNodes(t)) 
		matrix[current.getId()] = current.getTrust();
	return matrix;
}
public int getCount(int[][] arr) {
	int count=0;
	for(int i=0; i<arr.length; i++)
		for(int j=0 ; j<arr[i].length; j++)
			if(arr[i][j]!=0)
				count++;
	return count;
			
}

	 
private boolean NFplacementPathletsAlt1() {
		 double[][] retres=new double[reqs.size()][1];
		 double[] subCapAug=null;
		 double[][] subLinksAug = null;
		 int[][][][][] pathlet = null;
		 int [] flowCap=null;
				 
		 Writer writer1=null;
		 Writer writer=null;
		
		 String path = "results/pathlets_alt";
		 String filename = "substratePaths" +System.currentTimeMillis() + ".txt";
		 String filename1 = "requestsPath" + System.currentTimeMillis() +".txt";
		 try {
			 writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
			 writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

		Substrate substrateCopy =  substrates.get(0);
	       
			
		for (Request req: reqs){
		
		ResourceMappingNF reqMap = new ResourceMappingNF(req);
		ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(req.getGraph());
		ArrayList<Link> reqLinks = (ArrayList<Link>) getLinks(req.getGraph());
		
		ArrayList<Node> subNodes = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
		ArrayList<Link> subLinks = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
		
		//Substrate substrateCopy =  (Substrate) substrates.get(0).getCopy();
		
		writer.write("Request: " +req.getId()+ " " +req.getStartDate() +  "\n");
		writer1.write("Request: " +req.getId()+ " " +req.getStartDate() +  "\n");
		//this.results.addReq();
		
	/////////////////////////////////////// 
			double[] sfcNodes = new double[reqNodes.size()];
			String[] sfcType= new String [reqNodes.size()];
			int sizeOfSFC = sfcNodes.length;
			int nodesSFC = sizeOfSFC;
			double[][] sfcLinks = new double[sizeOfSFC][sizeOfSFC];
			double embedding_rev=0;
			
	/////////////////////////////////////// 		
		
			int subNodesNum=substrateCopy.getGraph().getVertexCount();
			int subNodesNumAug = subNodesNum+reqNodes.size();
			String[] subTypeAug =  new String[subNodesNumAug];

			
			subCapAug =  new double [subNodesNumAug];
			flowCap =  new int [subNodesNumAug];
			for (Node sn: subNodes){
				subCapAug[sn.getId()]=sn.getAvailableCpu();
				
				/*if (sn.getId()==6) subCapAug[sn.getId()]=66802;
				else if (sn.getId()==7) subCapAug[sn.getId()]=400000;
				else if (sn.getId()==9) subCapAug[sn.getId()]=216668;
				else if (sn.getId()==10) subCapAug[sn.getId()]=233334;
				else if (sn.getId()==14) subCapAug[sn.getId()]=108401;
				else if (sn.getId()==15) subCapAug[sn.getId()]=233334;
				else if (sn.getId()==17) subCapAug[sn.getId()]=250134;
				else if (sn.getId()==18) subCapAug[sn.getId()]=216668;
				else if (sn.getId()==22) subCapAug[sn.getId()]=108401;
				else if (sn.getId()==23) subCapAug[sn.getId()]=250134;
				else if (sn.getId()==25) subCapAug[sn.getId()]=91735;
				else if (sn.getId()==26) subCapAug[sn.getId()]=366667;
				else if (sn.getId()==30) subCapAug[sn.getId()]=125068;
				else if (sn.getId()==31) subCapAug[sn.getId()]=91735;
				else if (sn.getId()==33) subCapAug[sn.getId()]=500000;
				else if (sn.getId()==34) subCapAug[sn.getId()]=200001;
				*/
				subTypeAug[sn.getId()]=sn.getType();
				if (sn.getType().equalsIgnoreCase("switch")){
					flowCap[sn.getId()]=sn.getTCAM();
				}
				writer.write("Node: " + sn.getId() + " Type: " + sn.getType() +" CPU: " + sn.getAvailableCpu() 
						+ " TCAM: " + sn.getTCAM() +  "\n");
				//System.out.println(sn.getId() +" " + sn.getType() + " " +sn.getTCAM() + " "+sn.getAvailableCpu() );
			}
			
			for (Node rn: reqNodes){
				embedding_rev += rn.getAvailableCpu();
				subCapAug[subNodesNum+rn.getId()] = rn.getAvailableCpu();
				subTypeAug[subNodesNum+rn.getId()] = "Server";
				writer1.write("Node: " + rn.getId() + " Type: " + rn.getType() +" CPU: " + rn.getAvailableCpu()+"\n");
			}
		
			//create augmented links;
			subLinksAug =  new double [subNodesNumAug][subNodesNumAug];
			//add substrate
			for (Link current : subLinks){
				Pair<Node> x =  substrateCopy.getGraph().getEndpoints(current);
				subLinksAug[x.getFirst().getId()][x.getSecond().getId()]= current.getBandwidth();
			/*	if (current.getId()==0) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==1) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==2) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==3) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 5.960464477539062E-7;
				else if (current.getId()==7) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==8) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==17) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 5.960464477539062E-7;
				else if (current.getId()==18) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 5.960464477539062E-7;
				else if (current.getId()==22) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==25) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==33) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==34) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==39) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==40) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==48) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==49) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==56) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 0;
				else if (current.getId()==53) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0;
				else if (current.getId()==58) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0;
				else if (current.getId()==61) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0;
				else if (current.getId()==62) subLinksAug[x.getFirst().getId()][x.getSecond().getId()] = 7600000.0*/;
				writer.write("Link " + current.getId()+ " : " + x.getFirst().getId() + " -> " + x.getSecond().getId()+" BW: " +
						current.getBandwidth()+"\n");
			}
			//add requested 
			for (Link y: reqLinks){
				embedding_rev+=y.getBandwidth();
				Pair<Node> tmp = req.getGraph().getEndpoints(y);
				//System.out.println("bw: "+y.getBandwidth());
				sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
				subLinksAug[subNodesNum+tmp.getFirst().getId()][subNodesNum+ tmp.getSecond().getId()]= 0;
				writer1.write("Link " + y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
						 y.getBandwidth()+"\n");
			}	
			
		//	System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug[0]));
			//add requested to AUG substrate
			for (Node x: reqNodes){		
				//System.out.println(x.getId()+ " " + reqNodes.size());
				sfcNodes[x.getId()] = x.getAvailableCpu();
				sfcType[x.getId()] = "Server";// x.getType(); 
				int new_id = subNodesNum+ x.getId();
			//	for (int i=0;i<subNodesNum;i++){
				for (Node sn: subNodes) {
					if (sn.getType().equalsIgnoreCase("server")){
					int i = sn.getId();
					subLinksAug[new_id][i] = Double.MAX_VALUE;
					subLinksAug[i][new_id] = Double.MAX_VALUE;
					}
				}
			}		
			//System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug));
			
			System.out.println("subNodesNum: " +subNodesNum);
			System.out.println("subNodesNumAug: " +subNodesNumAug);
			 reqMap.setEmbeddingRevenue(embedding_rev);


			 System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
			System.out.println("sizeOfSFC: "+sizeOfSFC);
			nodesSFC = sizeOfSFC;
			System.out.println("flowCap: "+Arrays.toString(sfcLinks));
			
			System.out.println("subCapAug: "+Arrays.toString(subCapAug));
			System.out.println("subNodesNumAug: "+subNodesNumAug);
			System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug));
		//	System.exit(0);
	   

	///////////////////////////////////////

		req.print();
		try {
			System.out.println("Pathlets: Starting opt:" + req.getId());
			IloCplex cplex = new IloCplex();
			cplex.setParam(IloCplex.DoubleParam.TiLim, 600);
			cplex.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
			cplex.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
			int denial=0;	

			int count = 0;
			IloNumVar[][][][] x = new IloNumVar[subNodesNumAug][subNodesNumAug][][];

				for (int u=0;u<subNodesNumAug;u++){
					x[u]=new IloNumVar[subNodesNumAug][][];
					for (int v=0;v<subNodesNumAug;v++){				
						x[u][v]=new IloNumVar[nodesSFC][];
						for(int k=0; k< nodesSFC; k++){
							x[u][v][k]=new IloNumVar[nodesSFC];
							for(int m=0; m<nodesSFC; m++){
								x[u][v][k]=cplex.numVarArray(nodesSFC, 0, 1,IloNumVarType.Int);
								count++;
							}
						}
					}
				}
			

			count=0;
			//System.out.println("nodesSFC: " +nodesSFC );
			IloNumVar[][][][] fl = new IloNumVar [subNodesNumAug][subNodesNumAug][][];
	
		
				for (int u=0;u<subNodesNumAug;u++){
					fl[u]=new IloNumVar[subNodesNumAug][][];
					for (int v=0;v<subNodesNumAug;v++){
						fl[u][v]=new IloNumVar[nodesSFC][];
							for(int k=0; k<nodesSFC; k++){
								fl[u][v][k]= new IloNumVar[nodesSFC];
								for(int m=0; m<nodesSFC; m++){
									fl[u][v][k][m]=cplex.numVar(0, 1000000000);
									//System.out.println(u + " " +v + " " +k + " " + m + " " +p);	
									count++;
								}
						}
					}
				}
			
			
		

			
			/*****************************Objective Function **************************************************/
			
			IloLinearNumExpr cost = cplex.linearNumExpr();
			////////////////////////////////////////////////////////////////////////
			//It builds the first summation of the objective function//////////////
			//////////////////////////////////////////////////////////////////////
	
				for (int u=0;u<subNodesNumAug;u++){
					for (int v=0;v<subNodesNumAug;v++){
						for(int k=0; k< nodesSFC; k++){
							for(int m=0; m< nodesSFC; m++){
								cost.addTerm(1, x[u][v][k][m]);
							}
						}
					}
				}
	
			
			////////////////////////////////////////////////////////////////////////
			//It builds the second summation of the objective function//////////////
			//////////////////////////////////////////////////////////////////////
			count = 0;
			IloLinearNumExpr flows = cplex.linearNumExpr();
			double dmd =0;
			//calculate normalization factor
			for(int i=0;i< sizeOfSFC; i++){
				for(int j=0; j<sizeOfSFC; j++){
					dmd = dmd+sfcLinks[i][j];
				}
			}
		//	System.out.println(dmd);
		
			for(int i=0; i< sizeOfSFC; i++){
				for(int j=0; j<sizeOfSFC; j++){
						for (int u=0;u<subNodesNum;u++){
							for (int v=0;v<subNodesNum;v++){
								flows.addTerm(1/(dmd+Double.MIN_VALUE +Double.MIN_VALUE), fl[u][v][i][j]);														
							}
						}
					
				}
				
			}
		
			
			IloNumExpr expr = cplex.sum(flows,cost);
			cplex.addMinimize(expr);		
			
			
	/*****************************Capacity Constraints **************************************************/
			//node capacity constraints
			count=0;		
			for (int v=0;v<subNodesNum;v++){ //for every substrate
				double cpuCap = subCapAug[v];
				IloLinearNumExpr cpuReq = cplex.linearNumExpr();
					ArrayList<Integer> visited= new ArrayList<Integer> (); //visited v-nodes
					for(int m=0; m<nodesSFC; m++){
						for (int u=subNodesNum;u<subNodesNumAug;u++){ //for every pseudo
							if (sfcLinks[u-subNodesNum][m]!=0){ //if requested link exists
								if (!(visited.contains(u-subNodesNum))){ //have not visited v-node before
								//if (v==6) System.out.println("x" + u + "  " +v + " " + (u-subNodesNum) + " " +m);
							
								cpuReq.addTerm(sfcNodes[u-subNodesNum], x[u][v][u-subNodesNum][m]);
								
								visited.add(u-subNodesNum);
								}
							}
							else if (sfcLinks[m][u-subNodesNum]!=0){ //if reverse exists
								if (!(visited.contains(u-subNodesNum))){
								 //if (v==6)System.out.println("x1 " + v + "  " +u + " " + m + " " +(u-subNodesNum));
					
								cpuReq.addTerm(sfcNodes[u-subNodesNum], x[v][u][m][u-subNodesNum]);
	
								visited.add(u-subNodesNum);
								}
							}
						}
					}
				//System.out.println("lEQ "+cpuCap);
				cplex.addLe(cpuReq, cpuCap);
				count++;
			}
			//System.out.println("1 : " +count);
			
			//link capacity constraints (1)
		 
				for (int u=0;u<subNodesNumAug;u++){
					for (int v=0;v<subNodesNumAug;v++){
					//	System.out.println("subLinksAug" + u + "  " +v + " " + p);
						double cap =subLinksAug[u][v];
						IloLinearNumExpr bwReq = cplex.linearNumExpr();;
							for(int k=0; k< nodesSFC; k++){
								for(int m=0; m< nodesSFC; m++){
									bwReq.addTerm(1,fl[u][v][k][m]);
								}
							}
					cplex.addLe(bwReq, cap);
					count++;
					}
				}	
		
			System.out.println("2 : " +count);
			
			//link capacity constraints (2)
	
				for (int u=0;u<subNodesNumAug;u++){
					for (int v=0;v<subNodesNumAug;v++){
					//	double cap = subLinksAug[u][v];
							for(int k=0; k< nodesSFC; k++){
								for(int m=0; m< nodesSFC; m++){
									double cap = sfcLinks[k][m];
									IloLinearNumExpr bwReq1 = cplex.linearNumExpr();
									IloLinearNumExpr bwReq2 = cplex.linearNumExpr();
									bwReq1.addTerm(1,fl[u][v][k][m]);
									bwReq2.addTerm(cap,x[u][v][k][m]);
									cplex.addLe(bwReq1, bwReq2);
									count++;
							}
						}
					}
				}
			
			 System.out.println("3 : " +count);
			
			//flow capacity constraints
				for (int v=0;v<subNodesNum;v++){
					if (subTypeAug[v].equalsIgnoreCase("switch")) {
					double flowC = flowCap[v];
					IloLinearNumExpr fcReq = cplex.linearNumExpr();
					for (int u=0;u<subNodesNum;u++){
						for(int i=0; i< sizeOfSFC; i++){
							for(int j=0; j<sizeOfSFC; j++){
								fcReq.addTerm(1,x[u][v][i][j]);
								//if (v==1) System.out.println("x" + u + "  " +v + " " + i + " " +j);
							}
						}		
						}
					cplex.addLe(fcReq, flowC);
					//System.out.println("flowC " + flowC);
					count++;
					}
				}	
		
			//System.exit(0);
			System.out.println("4 : " +count);
		

			/*****************************Placement and Assignment Constraints **************************************************/				
			
			
		    ///CON   (20)
/*			for (int p=subNodesNum;p<subNodesNumAug;p++){
				//System.out.println("x" + p);
					for(int m=0; m< nodesSFC; m++){
						if (sfcLinks[p-subNodesNum][m]!=0) {
							IloLinearNumExpr assignments1 = cplex.linearNumExpr();
							for (int w=0;w<subNodesNum;w++){	
							if (p==40) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
							assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
							}
							System.out.println("EQ "+1);
							cplex.addEq(assignments1, 1);
							//System.out.println(count+1);
							count++;
					}		
				}
					
			}
			
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				//System.out.println("x" + p);
					for(int m=0; m< nodesSFC; m++){
						if (sfcLinks[m][p-subNodesNum]!=0) {
							IloLinearNumExpr assignments1 = cplex.linearNumExpr();
							for (int w=0;w<subNodesNum;w++){	
							if (p==40)  System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
							assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
							}
							//System.out.println("EQ "+1);
							cplex.addEq(assignments1, 1);
							//System.out.println(count+1);
							count++;
					}		
				}
					
			}*/
		
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				//System.out.println("x" + p);
					for(int m=0; m< nodesSFC; m++){
						IloLinearNumExpr assignments1 = cplex.linearNumExpr();
						ArrayList<Integer> visited= new ArrayList<Integer> (); //visited s-node
						for (int w=0;w<subNodesNum;w++){
							if (sfcLinks[p-subNodesNum][m]!=0) {
								if (!(visited.contains(w))){ //have not visited s-node before
								assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
								//if (w==1) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
								visited.add(w);
								}
							}
							else if (sfcLinks[m][p-subNodesNum]!=0){ //if reverse exists
								if (!(visited.contains(w))){ //have not visited s-node before
									assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
									//if (w==1)  System.out.println("x" + w + "  " +p + " " + m+" "+ (p-subNodesNum));
									visited.add(w);
								}
							}
						}
						if (visited.size()>0){
						//System.out.println("EQ "+1);
						cplex.addEq(assignments1, 1);
						count++;
						}
						//System.out.println("aaaaaaaaaaaaaaaaa");
						//if ((p==41) && (m==4)) System.exit(0);
						count++;
				}	
			}
			
			System.out.println("5: " + count);
		//	System.exit(0);
			
			//anticollocation
		/*	for (int w=0;w<subNodesNum;w++){
                if (!subTypeAug[w].equalsIgnoreCase("switch")) {
             //   System.out.println("w: " + w);
                IloLinearNumExpr assignments1 = cplex.linearNumExpr();
                for (int p=subNodesNum;p<subNodesNumAug;p++){
                        ArrayList<Integer> visited_new= new ArrayList<Integer> ();
                        //System.out.println("p: " + p);
                                for(int m=0; m< nodesSFC; m++){
                          //                      System.out.println("m: " + m);
                                                if (!((sfcLinks[p-subNodesNum][m]==0)&&(sfcLinks[m][p-subNodesNum]==0)))  {
                                                        if (!(visited_new.contains(p))){
                                                                visited_new.add(p);
                            //System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m );
                            //System.out.println("x" + w + "  " +p + " " + m + " " +(p-subNodesNum));
                                                    			assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
                                                                assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
                                                        }
                                        }
                                }

                }
               //System.out.println("lEQ "+1);
                cplex.addLe(assignments1, 1);
                //System.out.println(count+1);
                count++;
                }
        }*/
			
			//System.out.println("5 : " +count);
			
			///26
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				for (int w=0;w<subNodesNum;w++){
				//IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
				for(int m=0; m< nodesSFC; m++){
					if (sfcLinks[p-subNodesNum][m]!=0) {
							for(int l=0; l< nodesSFC; l++){
								if (l!=m){
								if (sfcLinks[p-subNodesNum][l]!=0) {
									IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
								//	if (p==37) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
								//	if (p==37) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +l);
									assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
									assignments1.addTerm(-1, x[p][w][p-subNodesNum][l]);
									cplex.addEq(assignments1, 0);
								//	if (p==37) System.out.println(" eq 0");
									count++;
								}
							}
							}
						}
					}
				}
			}
			//reverse
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				for (int w=0;w<subNodesNum;w++){
				//IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
				for(int m=0; m< nodesSFC; m++){
					if (sfcLinks[m][p-subNodesNum]!=0) {
							for(int l=0; l< nodesSFC; l++){
								if (l!=m){
									if (sfcLinks[l][p-subNodesNum]!=0) {
										IloLinearNumExpr assignments1 = cplex.linearNumExpr();
										//if (p==40) System.out.println("x" + w + "  " +p + " " + m + "  " +(p-subNodesNum));
										//if (p==40) System.out.println("x" + w + "  " +p + " " + l + "  " +(p-subNodesNum));
										assignments1.addTerm(1, x[w][p][m][p-subNodesNum]);
										assignments1.addTerm(-1, x[w][p][l][p-subNodesNum]);
										//if (p==40) System.out.println(" eq 0");
										cplex.addEq(assignments1, 0);
										count++;
									}
								}
							}
						}
					}
				}
			}
			
			
			//System.out.println("6 : " +count);
			//october (27)
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				for (int w=0;w<subNodesNum;w++){
				for(int m=0; m< nodesSFC; m++){
					if (sfcLinks[p-subNodesNum][m]!=0) {
							for(int l=0; l< nodesSFC; l++){
								if (sfcLinks[l][p-subNodesNum]!=0) {
									IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
									//if (p==36) System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
									///if (p==36) System.out.println("x" + w + "  " +p + " " + l + "  " +(p-subNodesNum));
									assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
									assignments1.addTerm(-1, x[w][p][l][p-subNodesNum]);
									//if (p==36) System.out.println(" eq 0");
									cplex.addEq(assignments1, 0);
									count++;
								}
							}
						}
					}
				}
			}
			

			//System.out.println("7 : " +count);
			
			//last added //october ( 25)
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				//for (int w=0;w<subNodesNum;w++){
				IloLinearNumExpr assignments1 = cplex.linearNumExpr();
				for(int k=0; k< nodesSFC; k++){
					for(int m=0; m< nodesSFC; m++){
								for (int w=0;w<subNodesNum;w++){
								
									if (k!=(p-subNodesNum)){
									 //if (p==37) System.out.println("x" + p + "  " +w + " " + k+ " " +m);
									assignments1.addTerm(1, x[p][w][k][m]);
									//assignments1.addTerm(1, x[w][p][m][k]);
									//cplex.addEq(assignments1, 0);
									}
									if (m!=(p-subNodesNum)){
										// if (p==36) System.out.println("x" + w + "  " +p + " " + k+ " " +m);
									assignments1.addTerm(1, x[w][p][k][m]);
									//cplex.addEq(assignments1, 0);
									}
									count++;
								
							}
					}
				}
				cplex.addEq(assignments1, 0);
			}
			
				
			
			
			
			//System.out.println  bidirectional
		for (int p=subNodesNum;p<subNodesNumAug;p++){
			for(int m=0; m< nodesSFC; m++){
				if(m!=(p-subNodesNum)){
				if ((sfcLinks[p-subNodesNum][m]!=0) && (sfcLinks[m][p-subNodesNum]!=0)){
				for (int w=0;w<subNodesNum;w++){
					IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
					IloLinearNumExpr assignments2 = cplex.linearNumExpr();
					//System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m);
					assignments1.addTerm(1, x[p][w][p-subNodesNum][m]);
					assignments2.addTerm(1, x[w][p][m][p-subNodesNum]);
					//System.out.println("x" + w + "  " +p + " " +m + " " +(p-subNodesNum));
					//System.out.println("EQ 1");
					cplex.addEq(assignments1, assignments2);
					count++;
				}
				}
				}
			}
		}
		

			
	/*****************************Flow Constraints **************************************************/
		
			//oti mpainei se kombo bgainei gia normal substrate 
			for (int u=0;u<subNodesNum;u++){
				for(int k=0; k< nodesSFC; k++){
					for(int m=0; m< nodesSFC; m++){		
						IloLinearNumExpr flowCon2 = cplex.linearNumExpr();
						IloLinearNumExpr flowCon3 = cplex.linearNumExpr();	
						for (int l=0;l<subNodesNumAug;l++){
							flowCon2.addTerm(1,fl[l][u][k][m]); //incoming
							flowCon3.addTerm(1,fl[u][l][k][m]);
						}
						cplex.addEq(flowCon2, flowCon3);
					}
				}
			}
			//System.out.println("11 : " +count);
			for (int u=subNodesNum;u<subNodesNumAug;u++){
				for(int m=0; m< nodesSFC; m++){	
					double capVirt =sfcLinks[u-subNodesNum][m];
					IloLinearNumExpr flowCon1 = cplex.linearNumExpr();	
					for (int l=0;l<subNodesNum;l++){
						flowCon1.addTerm(1,fl[u][l][u-subNodesNum][m]); //outgoing
					}
					cplex.addEq(flowCon1, capVirt);
				}
			}
			//System.out.println("12 : " +count);
			for (int u=subNodesNum;u<subNodesNumAug;u++){
				for(int m=0; m< nodesSFC; m++){	
					double capVirt =sfcLinks[m][u-subNodesNum];
					IloLinearNumExpr flowCon1 = cplex.linearNumExpr();	
					for (int l=0;l<subNodesNum;l++){
						flowCon1.addTerm(1,fl[l][u][m][u-subNodesNum]); //incoming
					}
					cplex.addEq(flowCon1, capVirt);
				}
			}
					
			//System.out.println("13 : " +count);
			
					
			cplex.exportModel("LPsimple_alt1.lp");
			
			long solveStartTime = System.nanoTime();
			boolean solvedOK = cplex.solve();
			long solveEndTime = System.nanoTime();
			long solveTime = solveEndTime - solveStartTime;

			req.print();
			if (solvedOK) {
				System.out.println("###################################");
				System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() + ", Time: " + ((double) solveTime / 1000000000.0));

				cplex.output().println("Solution value = " + cplex.getObjValue());
				System.out.println("###################################");
			
				int counterA=0;
				//nodeMapping
				LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();//requested-real
				//ArrayList<Node> subNodes = getNodes(substrateCopy.getGraph());
				int rules_added=0;
				double embedding_cost=0;
				ArrayList<Integer> fvisited = new ArrayList<Integer>();
				double[][][][] xVar =new double [subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
				for (int u=0;u<subNodesNumAug;u++){
					for (int v=0;v<subNodesNumAug;v++){				
						for(int k=0; k< nodesSFC; k++){
							for(int m=0; m<nodesSFC; m++){
								xVar[u][v][k][m]=cplex.getValue(x[u][v][k][m]);
								if (xVar[u][v][k][m]>0.00001) 
									{
									counterA++;
										System.out.println(" Vlink: " + k +"->"+m +" mapped to: " + u +"->"+v + " " );
										if ((u==(subNodesNum+k)) && !fvisited.contains(k)) {
											//System.out.println(subCapAug[u]);
											embedding_cost +=reqNodes.get(k).getAvailableCpu();
											nodeMap.put(reqNodes.get(k),((List<Node>) subNodes).get(v));
											if (!(updateSubstrate(substrateCopy,subNodes.get(v),reqNodes.get(k).getAvailableCpu())))
												 throw new ArithmeticException("Substrate Node Capacity not updated");
											fvisited.add(k);
										}
										 if ((v==(subNodesNum+m)) && !fvisited.contains(m)) {
										//	System.out.println(subCapAug[v]);
											embedding_cost +=reqNodes.get(m).getAvailableCpu();
											nodeMap.put(reqNodes.get(m),((List<Node>) subNodes).get(u));
											if (!(updateSubstrate(substrateCopy,subNodes.get(u),reqNodes.get(m).getAvailableCpu())))
												 throw new ArithmeticException("Substrate Node Capacity not updated");
											fvisited.add(m);
										}
										if ((u!=(subNodesNum+k)) && (v!=(subNodesNum+m))  && (u!=(subNodesNum+m)) && (v!=(subNodesNum+k))) {
											if(subTypeAug[u].equalsIgnoreCase("switch"))
													rules_added+=1;
										}
									}
								}
							}
						}
					}
				
				//System.out.println("subNodesNum:" +subNodesNum);
				//System.out.println("subNodesNumAug:" +subNodesNumAug);
				MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new 
						MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();
				// HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> lmapint =
					//new HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>>();
				double avg_hops=0;
			
				int counterB=0;
				double[][][][] fVar =new double [subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
				for(int k=0; k< nodesSFC; k++){
					for(int m=0; m<nodesSFC; m++){
						//System.out.println("checking link: " +k+" -> "+m);
						int paths_used=0;
						int hops_link_path=0;
						//Set<Integer> visitedSol = new HashSet<Integer>();
						for (int u=0;u<subNodesNumAug;u++){
							for (int v=0;v<subNodesNumAug;v++){				
						
								fVar[u][v][k][m]=cplex.getValue(fl[u][v][k][m]);
								if (fVar[u][v][k][m]>0.1) {
									counterB++;
	/*								if (visitedSol.contains(u)){
										paths_used++;
									} else{
										visitedSol.add(u);
									}*/
									System.out.println(" Vlink: " + k +"->"+m +" mapped to: " + u +"->"+v + 
											"has flow" + fVar[u][v][k][m]);
									//update substrate
									//System.out.println(collPaths);
									
									Pair<Integer> key= new Pair<Integer>(u,v);
									HashMap<Pair<Integer>,Double> key4lmap=new HashMap<Pair<Integer>,Double>();
									key4lmap.put(key, fVar[u][v][k][m]);
									List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>> ();
									tmpPath.add(key);
									lmap.put(key4lmap,tmpPath);
									//System.out.println("adding: " +key4lmap+ " " +tmpPath);
									//System.out.println(subNodesNum);
								//	System.out.println(u+ " " +v + " " + (k+subNodesNum)+ " "+ k +" " +(m+subNodesNum)+" " +m);
									if ((u!=(k+subNodesNum)) && (v!=(m+subNodesNum))){
										if ((v!=(k+subNodesNum)) && (u!=(m+subNodesNum))){
										    embedding_cost=embedding_cost+fVar[u][v][k][m];
										//	System.out.println(fVar[u][v][k][m]+ " " +u +" "+v);
										 if (!(updateSubLink(substrateCopy,u,v, fVar[u][v][k][m])))
											 throw new ArithmeticException("Substrate Link Capacity not updated");
										 hops_link_path++;
										//System.out.println("hops_link_path");
										}
									}
									
								
								}
									
							}
								
						}
						//System.out.println ( "hops_link_path: " +hops_link_path);
						avg_hops = avg_hops+hops_link_path;
	/*					if (hops_link_path>0){
							paths_used++;
							avg_hops+=(double)(hops_link_path/(paths_used));
							System.out.println ( " " +paths_used+ " " + avg_hops);
						}*/
					}
				}
				avg_hops = 	avg_hops / reqLinks.size();
				//substrateCopy.print();
				//System.out.println ( " avg_hops " + avg_hops  +"  and rules  " + rules_added +"  for links" + reqLinks.size());
				System.out.println (counterA+ " " +counterB);
				
				//substrateCopy.print();
				reqMap.setNodeMapping(nodeMap);
				reqMap.setFlowMapping(lmap);
				reqMap.setRulesAdded(rules_added);
				reqMap.setHops(avg_hops);
				System.out.println(embedding_cost+ " " + embedding_rev + " " +rules_added + " " +avg_hops);
				
				System.out.println("###############################################");
				System.out.println(nodeMap);
				System.out.println(lmap);
				reqMap.accepted();
				reqMap.setEmbeddingCost(embedding_cost);
				req.setRMapNF(reqMap);
				//if (counterA>counterB){
				
/*
				try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/
				
				//System.exit(0);
				writer1.write("Node Mapping: "+ nodeMap+ "\n");
				writer1.write("Link Mapping: "+ lmap+ "\n");
				
			}else{
				System.out.println("Did not found an answer for Mapping");
				reqMap.denied();
				req.setRMapNF(reqMap);
				substrateCopy.print();
				req.print();
				
/*				try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/
			}	
			cplex.end();	
			//System.exit(0);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
		
			
		}//all requests
		
		
		
		 } catch (IOException ex) {
			  // report
			} finally {
			   try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
			}
		
		return true;
	}

	 
	 
private boolean NFplacement_simple() {
		 double[][] retres=new double[reqs.size()][13];
		

	Set<Node> hs = new HashSet<>();
	Writer writer1=null;
	Writer writer=null;
	try {
		String path = "results/pathlets_alt";
		String filename = "substrate1Level" +System.currentTimeMillis() + ".txt";
		long name=System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
		String filename1 = "requests1Level" + + name +".txt";
	    writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));
	
	
	//SFCs (req)
	int numSFCs=reqs.size();
	int id_req = 0;
	for (Request req: reqs){
		ResourceMappingNF reqMap = new ResourceMappingNF(req);
		Substrate substrateCopy =  substrates.get(0);
		Graph<Node, Link> sub= substrateCopy.getGraph();
	
		
		ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
		ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
		writer.write("Request: " +req.getId()+ " \n");
		int subNodesNum = subNodesList.size();			
		double[] subNodes =  new double[subNodesNum];
		String[] subNodeTypes =  new String[subNodesNum];
		
		for (Node x: subNodesList){		
			subNodes[x.getId()] = x.getAvailableCpu();
			subNodeTypes[x.getId()] = x.getType();
			writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu() + "\n");
		}
		//Adjacency Matrix Substrate
		double[][] subLinks = new double[subNodesNum][subNodesNum];
		for (Link y: subLinksList){
			Pair<Node> tmp = sub.getEndpoints(y);
			subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
			writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
					y.getBandwidth()+"\n");
		}
		//int subLinksNum = subNodesNum*subNodesNum;
		
		
		/*double[] subNodes = new double[]{50,60};
		int subNodesNum = subNodes.length;
		String[] subNodeTypes  = new String[]{"Server","Server"};
		double[][] subLinks = new double[][] {{10000,50},{60,10000}};*/
		
		System.out.println("subNodesNum: "+subNodesNum);	
		System.out.println("subCap: "+Arrays.toString(subNodes));
		System.out.println("subNodeTypes: "+Arrays.toString(subNodeTypes));
		System.out.println("subLinks: "+Arrays.deepToString(subLinks));
		//System.out.println("subLinksNum: "+subLinksNum);
		
		//substrateCopy.print();
		int sfcNodes_num =  req.getGraph().getVertexCount();

		double[] sfcNodes =new double[sfcNodes_num];
		String[] sfcNodeTypes =new String[sfcNodes_num];
		String[] sfcNodeFunctions =new String[sfcNodes_num];
		
		int counter=0;

		double proc_revenue=0;
		writer1.write("Request: " +req.getId()+ " \n");
		
		ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
		for (Node node: req_n){
			sfcNodes[node.getId()]=node.getAvailableCpu();
			proc_revenue += node.getAvailableCpu();
			sfcNodeTypes[node.getId()]=node.getType();
			sfcNodeFunctions[node.getId()]=node.getName();
			writer1.write("Node: " + node.getId() + " Type: " + node.getType() +" CPU: " + node.getAvailableCpu()+"\n");
		}
		
	
		double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max			

		double bw_revenue=0;
		ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
		for (Link y: links){
		Pair<Node> tmp = req.getGraph().getEndpoints(y);
		sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
		writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
				 y.getBandwidth()+"\n");
		bw_revenue+=y.getBandwidth();
		}
		
		ArrayList<Node> sfc= req.getNodes(req.getGraph());
		int sizeOfSFC=sfc.size();
		int numNodesSFC = sfc.size();
		
/*		double[] sfcNodes = new double[] {50,50};
		int sizeOfSFC = sfcNodes.length;
		int numNodesSFC  = sizeOfSFC;
		String[] sfcNodeTypes = new String[]{"NF","NF"};
		double[][] sfcLinks = new double[][]{{0,10},{20,0}};*/

		reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);
		System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
		System.out.println("sfcNodeTypes: "+Arrays.deepToString(sfcNodeTypes));
		System.out.println("sfcLinks: "+Arrays.toString(sfcLinks[0]));
		System.out.println("numSFCs"+numSFCs);				

		//System.exit(0);


try {
	 	IloCplex cplex = new IloCplex();
		cplex.setParam(IloCplex.DoubleParam.TiLim, 60);
		//cplex.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
		//cplex.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
			
		int denial=0;				
		
		
		/*****************************System Variables **************************************************/
		// x^ij_u if instance j of NF i is installed on susbtrate node u then x^ij_u=1	\
		//counter=0;
		IloNumVar[][] x = new IloNumVar[subNodesNum][];
		for (int u=0;u<subNodesNum;u++){
			x[u]=new IloNumVar[sizeOfSFC];
			for(int i=0; i< sizeOfSFC; i++){
				x[u]=cplex.numVarArray(sizeOfSFC, 0, 1, IloNumVarType.Int);
			}
		}
		//System.out.println(counter);

		
		//System.out.println(counter);
		//System.exit(0);
		IloNumVar[][][][] fl = new IloNumVar[subNodesNum][subNodesNum][][];
		counter=0;
		for (int u=0;u<subNodesNum;u++){
			fl[u]=new IloNumVar[subNodesNum][][];
			for (int v=0;v<subNodesNum;v++){
				fl[u][v]=new IloNumVar[sizeOfSFC][];
				for(int i=0; i< sizeOfSFC; i++){
					fl[u][v][i]=new IloNumVar[sizeOfSFC];
					for(int j=0; j<sizeOfSFC; j++){
						fl[u][v][i]=cplex.numVarArray(sizeOfSFC, 0, 1000000000);
					}
				}
			}
		}
		
	//	System.out.println(counter);
	//	System.exit(0);

/*****************************Objective Function **************************************************/

		IloLinearNumExpr cost = cplex.linearNumExpr();
		////////////////////////////////////////////////////////////////////////
		//It builds the first summation of the objective function//////////////
		//////////////////////////////////////////////////////////////////////
		for (int u=0;u<subNodesNum;u++){
			for(int i=0; i< sizeOfSFC; i++){
				cost.addTerm(1, x[u][i]);
			}
		}
	
		
		
		////////////////////////////////////////////////////////////////////////
		//It builds the second summation of the objective function//////////////
		/////////////////////////////////////////////////////////////////////\
		IloLinearNumExpr flows = cplex.linearNumExpr();

			double demand =0;
			//calculate normalization factor
			for(int i=0;i< numNodesSFC; i++){
				for(int j=0; j<numNodesSFC; j++){
					demand = demand+sfcLinks[i][j];
				}
			}
			for(int i=0; i< numNodesSFC; i++){
				for(int j=0; j<numNodesSFC; j++){
					//demand = demand+sfcLinks[f][k][m];
					for (int u=0;u<subNodesNum;u++){
						for (int v=0;v<subNodesNum;v++){
							flows.addTerm(1, fl[u][v][i][j]);
							//flows.addTerm(1/(demand+Double.MIN_VALUE), fl[u][v][i][j]);
							//System.out.println(u + " " +v + " " +f + " " + k + " " +m);								
						}
					}
					
				}
			}
	
		
		//create objective minimization
		IloNumExpr expr = cplex.sum(flows,cost);
		cplex.addMinimize(expr);
		
		
/*****************************Capacity Constraints **************************************************/
		counter=0;
		//node capacity (15)
		for (int u=0;u<subNodesNum;u++){
			IloLinearNumExpr cpuReq = cplex.linearNumExpr();
			for(int i=0; i<numNodesSFC; i++){
				double cpuNF = sfcNodes[i];
				cpuReq.addTerm(cpuNF,x[u][i]);
				//System.out.println("x: " + u + " " +i + " " + cpuNF);
		
			}
			double cpu = subNodes[u];
			cplex.addLe(cpuReq, cpu);
			//System.out.println("leq: " + cpu);
			counter++;
		}
		
		System.out.println("Counter (16): " + counter);
		//link capacity (3)
		for (int u=0;u<subNodesNum;u++){
			for (int v=0;v<subNodesNum;v++){
				IloLinearNumExpr bwReq = cplex.linearNumExpr();
				for(int i=0; i< numNodesSFC; i++){
					for(int j=0; j<numNodesSFC; j++){
						bwReq.addTerm(1,fl[u][v][i][j]);
					}
				}
				double cap = subLinks[u][v];		
				cplex.addLe(bwReq, cap);
				counter++;
			}
		}		
		
		System.out.println("Counter (16): " + counter);
/*****************************Placement and Assignment Constraints **************************************************/				
	// (17) cannot place NF to node of different type
		for (int u=0;u<subNodesNum;u++){
			IloLinearNumExpr assignment1 = cplex.linearNumExpr();
			for (int i=0;i<numNodesSFC;i++){
				if (!((subNodeTypes[u].equalsIgnoreCase(sfcNodeTypes[i])) ||
						(subNodeTypes[u].equalsIgnoreCase("Server") && 
								sfcNodeTypes[i].equalsIgnoreCase("NF")) )){
						    //System.out.println(subNodeTypes[u]+ " " +sfcNodeTypes[i]);
							//System.out.println(u + " "+i);
						    assignment1.addTerm(1,x[u][i]);
						    
				}
			}
			cplex.addEq(assignment1, 0);
			//System.out.println("eq 0");
		    counter++;
		}

	
	System.out.println("Counter (17): " + counter);

	//(18) nf instance can be mapped to at most 1 substrate node of same type
		for (int i=0;i<numNodesSFC;i++){
			IloLinearNumExpr assignment1 = cplex.linearNumExpr();
			for (int u=0;u<subNodesNum;u++){			
				if (((subNodeTypes[u].equalsIgnoreCase(sfcNodeTypes[i])) ||
						(subNodeTypes[u].equalsIgnoreCase("Server") && 
								sfcNodeTypes[i].equalsIgnoreCase("NF")) )){
					assignment1.addTerm(1,x[u][i]);
					
					//System.out.println(subNodeTypes[u]+ " " +sfcNodeTypes[i]);
					//System.out.println(u + " "+i);
				}
			}
			cplex.addEq(assignment1, 1);
			counter++;
		//	System.out.println("leq 1");
		}

	System.out.println("Counter (18): " + counter);

	for (int u=0;u<subNodesNum;u++){
		IloLinearNumExpr assignment1 = cplex.linearNumExpr();
		for (int i=0;i<numNodesSFC;i++){			
			if (((subNodeTypes[u].equalsIgnoreCase(sfcNodeTypes[i])) ||
					(subNodeTypes[u].equalsIgnoreCase("Server") && 
							sfcNodeTypes[i].equalsIgnoreCase("NF")) )){
				assignment1.addTerm(1,x[u][i]);
				
				//System.out.println(subNodeTypes[u]+ " " +sfcNodeTypes[i]);
				//System.out.println(u + " "+i);
			}
		}
		cplex.addLe(assignment1, 1);
		counter++;
	//	System.out.println("leq 1");
	}

/*****************************Flow Constraints **************************************************/
	   

		for (int u=0;u<subNodesNum;u++){
				for(int i=0; i< numNodesSFC; i++){
					for(int j=0; j<numNodesSFC; j++){
						IloLinearNumExpr flow2 = cplex.linearNumExpr();
						IloLinearNumExpr x_var = cplex.linearNumExpr();
						IloLinearNumExpr x_var1 = cplex.linearNumExpr();
						IloNumExpr expr1=cplex.numExpr();
						double cap = sfcLinks[i][j];
					//	double inv_cap = sfcLinks[m][k];
						x_var.addTerm(cap, x[u][i]);
						x_var1.addTerm(-1*cap, x[u][j]);
						 expr1 = cplex.sum(x_var,x_var1);
						for (int v=0;v<subNodesNum;v++){
							flow2.addTerm(1, fl[u][v][i][j]);	
							flow2.addTerm(-1, fl[v][u][i][j]);
						}
						cplex.addEq(flow2, expr1);
					}
				}
				
			}

		
		
	 //7

			
		
		cplex.exportModel("lpex1a.lp");
		long solveStartTime = System.nanoTime();
		boolean solvedOK = cplex.solve();
		long solveEndTime = System.nanoTime();
		long solveTime = solveEndTime - solveStartTime;
		System.out.println("solvedOK: " + solvedOK);
		
		req.print();
		if (solvedOK) {

				System.out.println("###################################");
				System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() + ", Time: " + ((double) solveTime / 1000000000.0));

				cplex.output().println("Solution value = " + cplex.getObjValue());
				System.out.println("###################################");
				//substrateCopy.print();
				
				//nodeMapping
				LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();//requested-real
				//ArrayList<Node> subNodes = getNodes(substrateCopy.getGraph());
				int rules_added=0;
				double embedding_cost=0;
	
				double[][] xVar =new double [subNodesNum][sizeOfSFC];
				for (int u=0;u<subNodesNum;u++){
					for (int i=0;i<sizeOfSFC;i++){
							xVar[u][i] = cplex.getValue(x[u][i]);
							if (xVar[u][i] > 0){
								Node tmp = subNodesList.get(u);
								embedding_cost +=req_n.get(i).getAvailableCpu();
								nodeMap.put(req_n.get(i),subNodesList.get(u));
								if (!(updateSubstrate(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu())))
									 throw new ArithmeticException("Substrate Node Capacity not updated");

								 System.out.println("Node  " + i + " to " + u +"  " +xVar[u][i]);
							}
					}
				}	
				
			

				MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new 
						MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();

				double avg_hops=0;
				
				double bw_cost=0;
				double[][][][] fVar1 =new double [subNodesNum][subNodesNum][sizeOfSFC][sizeOfSFC];
			for(int k=0; k< numNodesSFC; k++){
				for(int m=0; m< numNodesSFC; m++){
					int hops_link_path=0;	
						for (int u=0;u<subNodesNum;u++){
								for (int v=0;v<subNodesNum;v++){
								fVar1[u][v][k][m] = cplex.getValue(fl[u][v][k][m]);
								// System.out.println("SFC : " +k+" "+m + " to " + u+ " "+v +" "+ fVar1[u][v][k][m]);
									if (fVar1[u][v][k][m]>0.00000001){
								   embedding_cost=embedding_cost+fVar1[u][v][k][m];
									Pair<Integer> key= new Pair<Integer>(u,v);
									HashMap<Pair<Integer>,Double> key4lmap=new HashMap<Pair<Integer>,Double>();
									key4lmap.put(key, fVar1[u][v][k][m]);
									List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>> ();
									tmpPath.add(key);
									lmap.put(key4lmap,tmpPath);
									 System.out.println("Link : " +k+" "+m + " to " + u+ " "+v +" "+ fVar1[u][v][k][m]);
									 bw_cost+=fVar1[u][v][k][m];
									 
									 if (!(updateSubLink(substrateCopy,u,v, fVar1[u][v][k][m])))
										 throw new ArithmeticException("Substrate Link Capacity not updated");
									 hops_link_path++;
									 if (subNodesList.get(u).getType().equalsIgnoreCase("switch"))
										 rules_added++;
									}
							}
						}
						avg_hops=avg_hops+hops_link_path;
					}
				}
				
				avg_hops = avg_hops/links.size();
				
				System.out.println ( " avg_hops " + avg_hops +"  and rules  " + rules_added +"  for links" +links.size());
				//System.out.println ( " avg_hops " + avg_hops  +"  and rules  " + rules_added +"  for links" + reqLinks.size());
				
				
				//substrateCopy.print();
				reqMap.setNodeMapping(nodeMap);
				reqMap.setFlowMapping(lmap);
				reqMap.setRulesAdded(rules_added);
				reqMap.setHops(avg_hops);
				System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) + " " +rules_added + " " +avg_hops);
				
				System.out.println("###############################################");
				System.out.println(nodeMap);
				System.out.println(lmap);
				reqMap.accepted();
				reqMap.setEmbeddingCost(embedding_cost);
				req.setRMapNF(reqMap);
				try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }
				writer1.write("Node Mapping: "+ nodeMap+ "\n");
				writer1.write("Link Mapping: "+ lmap+ "\n");
				
			}else{
				System.out.println("Did not found an answer for Mapping");
				reqMap.denied();
				req.setRMapNF(reqMap);
			}	
			cplex.end();	
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
		
			
		}//all requests
		
		
		
		 } catch (IOException ex) {
			  // report
			} finally {
			   try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
			}
		
		return true;
	}
   

private boolean NFplacementPathlets1() {
	 double[][] retres=new double[reqs.size()][1];
	 double[] subCapAug=null;
	 double[][][] subLinksAug = null;
	 int[][][][][] pathlet = null;
	 int [] flowCap=null;
	 //with pathlets	 
	 Writer writer1=null;
	 Writer writer=null;
	
	 String path = "results/pathlets";
	 String filename = "substratePaths" +System.currentTimeMillis() + ".txt";
	 String filename1 = "requestsPath" + System.currentTimeMillis() +".txt";
	 try {
		 writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
		 writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));

	 
	 MultiHashMap<Pair<Integer>,List<Pair<Integer>>> pathlets= substrates.get(0).getFTL3().getPaths();
	 MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathletsUse= substrates.get(0).getFTL3().getPathsUsage();
	 
/*	 System.out.println(pathletsUse);
	 try {
	        System.in.read();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	 */
	 Set<Pair<Integer>> keySet = pathlets.keySet( );
	 Iterator<Pair<Integer>> keyIterator = keySet.iterator();
	 
	 //get max number of paths per pair;
	 int paths_max = 0;
	 while( keyIterator.hasNext( ) ) {
		 	Pair<Integer> key = keyIterator.next();
		 	if (pathlets.size(key)>	paths_max)
		 		paths_max = pathlets.size(key);
	  }

	 Substrate substrateCopy =  substrates.get(0);
       
		
	for (Request req: reqs){
	ResourceMappingNF reqMap = new ResourceMappingNF(req);
	ArrayList<Node> reqNodes = (ArrayList<Node>) getNodes(req.getGraph());
	ArrayList<Link> reqLinks = (ArrayList<Link>) getLinks(req.getGraph());
	
	ArrayList<Node> subNodes = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
	ArrayList<Link> subLinks = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
	
	//Substrate substrateCopy =  (Substrate) substrates.get(0).getCopy();
	
	writer.write("Request: " +req.getId()+ " " +req.getStartDate() +  "\n");
	writer1.write("Request: " +req.getId()+ " " +req.getStartDate() +  "\n");
	//this.results.addReq();
	
/////////////////////////////////////// 
		double[] sfcNodes = new double[reqNodes.size()];
		String[] sfcType= new String [reqNodes.size()];
		int sizeOfSFC = sfcNodes.length;
		int nodesSFC = sizeOfSFC;
		double[][] sfcLinks = new double[sizeOfSFC][sizeOfSFC];
		double embedding_rev=0;
		
/////////////////////////////////////// 		
	
		int subNodesNum=substrateCopy.getGraph().getVertexCount();
		int subNodesNumAug = subNodesNum+reqNodes.size();
		String[] subTypeAug =  new String[subNodesNumAug];

		
		subCapAug =  new double [subNodesNumAug];
		flowCap =  new int [subNodesNumAug];
		for (Node sn: subNodes){
			subCapAug[sn.getId()]=sn.getAvailableCpu();
			subTypeAug[sn.getId()]=sn.getType();
			if (sn.getType().equalsIgnoreCase("switch")){
				flowCap[sn.getId()]=sn.getTCAM();
			}
			writer.write("Node: " + sn.getId() + " Type: " + sn.getType() +" CPU: " + sn.getAvailableCpu() 
					+ " TCAM: " + sn.getTCAM() +  "\n");
			//System.out.println(sn.getId() +" " + sn.getType() + " " +sn.getTCAM() + " "+sn.getAvailableCpu() );
		}
		
		for (Node rn: reqNodes){
			embedding_rev += rn.getAvailableCpu();
			subCapAug[subNodesNum+rn.getId()] = rn.getAvailableCpu();
			subTypeAug[subNodesNum+rn.getId()] = "Server";
			writer1.write("Node: " + rn.getId() + " Type: " + rn.getType() +" CPU: " + rn.getAvailableCpu()+"\n");
		}
	
		//create augmented links;
		subLinksAug =  new double [paths_max][subNodesNumAug][subNodesNumAug];
		//add substrate
		for (Link current : subLinks){
			Pair<Node> x =  substrateCopy.getGraph().getEndpoints(current);
			subLinksAug[0][x.getFirst().getId()][x.getSecond().getId()]= current.getBandwidth();
			writer.write("Link " + current.getId()+ " : " + x.getFirst().getId() + " -> " + x.getSecond().getId()+" BW: " +
					current.getBandwidth()+"\n");
		}
		//add requested 
		for (Link y: reqLinks){
			embedding_rev+=y.getBandwidth();
			Pair<Node> tmp = req.getGraph().getEndpoints(y);
			//System.out.println("bw: "+y.getBandwidth());
			sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
			subLinksAug[0][subNodesNum+tmp.getFirst().getId()][subNodesNum+ tmp.getSecond().getId()]= 0;
			writer1.write("Link " + y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
					 y.getBandwidth()+"\n");
		}	
		
		//add requested to AUG substrate
		for (Node x: reqNodes){		
			//System.out.println(x.getId()+ " " + reqNodes.size());
			sfcNodes[x.getId()] = x.getAvailableCpu();
			sfcType[x.getId()] = "Server";// x.getType(); 
			int new_id = subNodesNum+ x.getId();
		//	for (int i=0;i<subNodesNum;i++){
			for (Node sn: subNodes) {
				if (sn.getType().equalsIgnoreCase("server")){
				int i = sn.getId();
				subLinksAug[0][new_id][i] = Integer.MAX_VALUE;
				subLinksAug[0][i][new_id] = Integer.MAX_VALUE;
				}
			}
		}		
		//System.out.println("subLinksAug: "+Arrays.deepToString(subLinksAug));
		
		
		 pathlet = new int [paths_max][subNodesNum][subNodesNum][subNodesNum][subNodesNum];		 
		 Set<Pair<Integer>> keySet1 = pathlets.keySet( );
		 Iterator<Pair<Integer>> keyIterator1 = keySet1.iterator();
		//add pathlets
		 while (keyIterator1.hasNext( ) ) {
			 	Pair<Integer> key = keyIterator1.next();
			 	//System.out.println("checking pathlet: "+ key);
			 	int pid=0;
				Collection<List<Pair<Integer>>> value =  pathlets.get(key);
				Iterator<List<Pair<Integer>>> iterator = value.iterator();
				//if  (key.getFirst()==5) System.out.println("checking pathlet: "+ value);
					while (iterator.hasNext()) {
						 List<Pair<Integer>> list =iterator.next();	
							//System.out.println("checking pathlet: "+ list);
						 if (list.size()==1) break;
						 double min_bw =Double.MAX_VALUE; //find also the most loaded link in path;
						 for(Pair<Integer> clink: list){
							//get pairs of links
							 pathlet[pid][key.getFirst()][key.getSecond()][clink.getFirst()][clink.getSecond()]=1;
							//	System.out.println(pid+ " " +key.getFirst()+ " " +key.getSecond()+ " " +clink.getFirst()+ " "+
								//		 clink.getSecond() + " value: " + pathlet[pid][key.getFirst()][key.getSecond()][clink.getFirst()][clink.getSecond()]);
							  if (subLinksAug[0][clink.getFirst()][clink.getSecond()]<min_bw){
								min_bw= subLinksAug[0][clink.getFirst()][clink.getSecond()];
								
							  }
							 // System.out.println(subLinksAug[0][clink.getFirst()][clink.getSecond()]);
							  //System.out.println("link bw: " +min_bw);
						 }//iterate in links for path
					subLinksAug[pid][key.getFirst()][key.getSecond()] = min_bw; // most loaded link in path;
					//if  (key.getFirst()==5)
						//System.out.println(pid+ " " +key.getFirst()+ " " +key.getSecond()+ " " +min_bw);
					pid++;
					} //iterate in paths for particular pair 
		  }
		// System.out.println("pathlet: "+Arrays.deepToString(pathlet[1]));
		// System.exit(0);
		 reqMap.setEmbeddingRevenue(embedding_rev);

///////////////////////////////////////


	try {
		System.out.println("Pathlets: Starting opt:" + req.getId());
		IloCplex cplex = new IloCplex();
		cplex.setParam(IloCplex.DoubleParam.TiLim, 600);
		cplex.setParam(IloCplex.DoubleParam.ObjULim,Double.MAX_VALUE);
		cplex.setParam(IloCplex.DoubleParam.ObjLLim,-1*Double.MAX_VALUE);
		int denial=0;	

		int count = 0; // x_uv,p^ij
		IloNumVar[][][][][] x = new IloNumVar[paths_max][subNodesNumAug][subNodesNumAug][][];
		for (int p=0;p<paths_max;p++){
			x[p]=new IloNumVar[subNodesNumAug][][][];
			for (int u=0;u<subNodesNumAug;u++){
				x[p][u]=new IloNumVar[subNodesNumAug][][];
				for (int v=0;v<subNodesNumAug;v++){				
					x[p][u][v]=new IloNumVar[nodesSFC][];
					for(int k=0; k< nodesSFC; k++){
						x[p][u][v][k]=new IloNumVar[nodesSFC];
						for(int m=0; m<nodesSFC; m++){
							x[p][u][v][k]=cplex.numVarArray(nodesSFC, 0, 1,IloNumVarType.Int);
							count++;
						}
					}
				}
			}
		}

		count=0;//fl_uv,p^ij
		IloNumVar[][][][][] fl = new IloNumVar [paths_max][subNodesNumAug][subNodesNumAug][][];
		for (int p=0;p<paths_max;p++){
			fl[p]=new IloNumVar[subNodesNumAug][][][];
			for (int u=0;u<subNodesNumAug;u++){
				fl[p][u]=new IloNumVar[subNodesNumAug][][];
				for (int v=0;v<subNodesNumAug;v++){
					fl[p][u][v]=new IloNumVar[nodesSFC][];
						for(int k=0; k<nodesSFC; k++){
							fl[p][u][v][k]= new IloNumVar[nodesSFC];
							for(int m=0; m<nodesSFC; m++){
								fl[p][u][v][k][m]=cplex.numVar(0, 1000000000);
								//System.out.println(u + " " +v + " " +k + " " + m + " " +p);	
								count++;
							}
					}
				}
			}
		}
		
	

		
		/*****************************Objective Function **************************************************/
		
		IloLinearNumExpr cost = cplex.linearNumExpr();
		////////////////////////////////////////////////////////////////////////
		//It builds the first summation of the objective function//////////////
		//////////////////////////////////////////////////////////////////////
		for (int p=0;p<paths_max;p++){
			for (int u=0;u<subNodesNumAug;u++){
				for (int v=0;v<subNodesNumAug;v++){
					for(int k=0; k< nodesSFC; k++){
						for(int m=0; m< nodesSFC; m++){
							cost.addTerm(1, x[p][u][v][k][m]);
						}
					}
				}
			}
		}
		
		////////////////////////////////////////////////////////////////////////
		//It builds the second summation of the objective function//////////////
		//////////////////////////////////////////////////////////////////////
		count = 0;
		IloLinearNumExpr flows = cplex.linearNumExpr();
		double dmd =0;
		//calculate normalization factor
		for(int i=0;i< sizeOfSFC; i++){
			for(int j=0; j<sizeOfSFC; j++){
				dmd = dmd+sfcLinks[i][j];
			}
		}
	//	System.out.println(dmd);
	
		for(int i=0; i< sizeOfSFC; i++){
			for(int j=0; j<sizeOfSFC; j++){
				for (int p=0;p<paths_max;p++){
					for (int u=0;u<subNodesNum;u++){
						for (int v=0;v<subNodesNum;v++){
							double path_w = 1;
							for (int k=0;k<subNodesNum;k++){
								for (int m=0;m<subNodesNum;m++){
									//System.out.println(pathlet[p][u][v][k][m] + " " +k + " " +m);
									path_w= path_w+ pathlet[p][u][v][k][m];
								}
							}
							if (path_w>1) path_w--; //correction for non_pathlets
							//flows.addTerm(path_w, fl[p][u][v][i][j]);
							//System.out.println(u + " " +v + " " +i + " " + j + " " +p + " " +path_w);	
							flows.addTerm(path_w/(dmd+Double.MIN_VALUE), fl[p][u][v][i][j]);														
						}
					}
				}
				
			}
			
		}
	
		
		IloNumExpr expr = cplex.sum(flows,cost);
		cplex.addMinimize(expr);		
		
		
/*****************************Capacity Constraints **************************************************/
		//node capacity constraints //(20)
		count=0;		
		for (int v=0;v<subNodesNum;v++){
			double cpuCap = subCapAug[v];
			IloLinearNumExpr cpuReq = cplex.linearNumExpr();
				ArrayList<Integer> visited= new ArrayList<Integer> ();
				//ArrayList<Integer> visited_rev= new ArrayList<Integer> ();
				for(int m=0; m<nodesSFC; m++){
					for (int u=subNodesNum;u<subNodesNumAug;u++){
						if (sfcLinks[u-subNodesNum][m]!=0){
							if (!(visited.contains(u-subNodesNum))){
							//System.out.println("x" + u + "  " +v + " " + (u-subNodesNum) + " " +m);
								for (int p=0;p<paths_max;p++){
									cpuReq.addTerm(sfcNodes[u-subNodesNum], x[p][u][v][u-subNodesNum][m]);
								}
								visited.add(u-subNodesNum);
							}
						}
						else if (sfcLinks[m][u-subNodesNum]!=0){
							if (!(visited.contains(u-subNodesNum))){
							//System.out.println("x1 " + v + "  " +u + " " + m + " " +(u-subNodesNum));
								for (int p=0;p<paths_max;p++){
								cpuReq.addTerm(sfcNodes[u-subNodesNum], x[p][v][u][m][u-subNodesNum]);
								}
								visited.add(u-subNodesNum);
							}
						}		
					}
				}
			//System.out.println("lEQ "+cpuCap);
			cplex.addLe(cpuReq, cpuCap);
			count++;
		}
		//System.out.println("1 : " +count);
		
		//link capacity constraints (21)
		for (int p=0;p<paths_max;p++){ 
			for (int u=0;u<subNodesNumAug;u++){
				for (int v=0;v<subNodesNumAug;v++){
				//	System.out.println("subLinksAug" + u + "  " +v + " " + p);
					double cap =subLinksAug[p][u][v];
					IloLinearNumExpr bwReq = cplex.linearNumExpr();;
						for(int k=0; k< nodesSFC; k++){
							for(int m=0; m< nodesSFC; m++){
								bwReq.addTerm(1,fl[p][u][v][k][m]);
							}
						}
				cplex.addLe(bwReq, cap);
				count++;
				}
			}	
		}
		//System.out.println("2 : " +count);
		
		//link capacity constraints (22)
		for (int p=0;p<paths_max;p++){
			for (int u=0;u<subNodesNumAug;u++){
				for (int v=0;v<subNodesNumAug;v++){
						for(int k=0; k< nodesSFC; k++){
							for(int m=0; m< nodesSFC; m++){
								double cap = sfcLinks[k][m];
								IloLinearNumExpr bwReq1 = cplex.linearNumExpr();
								IloLinearNumExpr bwReq2 = cplex.linearNumExpr();
								bwReq1.addTerm(1,fl[p][u][v][k][m]);
								bwReq2.addTerm(cap,x[p][u][v][k][m]);
								cplex.addLe(bwReq1, bwReq2);
								count++;
						}
					}
				}
			}
		}
		System.out.println("3 : " +count);
	
		//flow capacity constraints (23) //pathlet only between switches
		for(int i=0; i< sizeOfSFC; i++){
			for(int j=0; j<sizeOfSFC; j++){
				for (int p=0;p<paths_max;p++){
					int pathCounnter=0;
					IloLinearNumExpr fcReq = cplex.linearNumExpr();
					for (int u=0;u<subNodesNum;u++){
						if (subTypeAug[u].equalsIgnoreCase("switch")) {
						double flowC = flowCap[u];	
							for (int v=0;v<subNodesNum;v++){
								if (subTypeAug[v].equalsIgnoreCase("switch")) {
									if (existsPath(pathletsUse,p,u,v,subNodesNum,pathlet)==0) {
									fcReq.addTerm(1,x[p][u][v][i][j]);
									pathCounnter++;
									}
								}
						}
						if (pathCounnter>0) {	
						cplex.addLe(fcReq, flowC);
						count++;
						}
						}
					}
				}
			}
		}	
			
/*			for (int v=0;v<subNodesNum;v++){
				if (subTypeAug[v].equalsIgnoreCase("switch")) {
				double flowC = flowCap[v];
				IloLinearNumExpr fcReq = cplex.linearNumExpr();
				for (int p=0;p<paths_max;p++){
					for (int u=0;u<subNodesNum;u++){
						for(int i=0; i< sizeOfSFC; i++){
							for(int j=0; j<sizeOfSFC; j++){
							fcReq.addTerm(1,x[p][u][v][i][j]);
							}
						}		
					}
				}
				cplex.addLe(fcReq, flowC);
				count++;
				}
			}	*/

		//System.out.println("4 : " +count);
	

		/*****************************Placement and Assignment Constraints **************************************************/				
				//(24)
		for (int p=subNodesNum;p<subNodesNumAug;p++){
				//System.out.println("x" + p);
					for(int m=0; m< nodesSFC; m++){
						IloLinearNumExpr assignments1 = cplex.linearNumExpr();
						ArrayList<Integer> visited= new ArrayList<Integer> (); //visited s-node
							for (int w=0;w<subNodesNum;w++){
								if (sfcLinks[p-subNodesNum][m]!=0) {
									if (!(visited.contains(w))){ //have not visited s-node before
										for (int p1=0;p1<paths_max;p1++){
					//if (p==37) System.out.println("x " + p1 + " " + p + "  " +w + " " + (p-subNodesNum) + " " +m);
										assignments1.addTerm(1, x[p1][p][w][p-subNodesNum][m]);
										}
									visited.add(w);
									}
								}
								else if (sfcLinks[m][p-subNodesNum]!=0){ //if reverse exists
									if (!(visited.contains(w))){ //have not visited s-node before
										for (int p1=0;p1<paths_max;p1++){
										assignments1.addTerm(1, x[p1][w][p][m][p-subNodesNum]);
					//if (p==37) System.out.println("x " + p1 + " " + w + "  " +p + " " + m+ " " +(p-subNodesNum) );
										}
										visited.add(w);
									}
								}
							}
						if (visited.size()>0){
						//System.out.println("eq 1");
						cplex.addEq(assignments1, 1);
						count++;
						}
				}	
			}
			
			System.out.println("5: " + count);
			//anticollocation
		/*for (int w=0;w<subNodesNum;w++){
			if (!subTypeAug[w].equalsIgnoreCase("switch")) {
			//System.out.println("w: " + w);
			IloLinearNumExpr assignments1 = cplex.linearNumExpr();
			for (int p=subNodesNum;p<subNodesNumAug;p++){	
				ArrayList<Integer> visited_new= new ArrayList<Integer> ();
				//System.out.println("p: " + p);
					for(int m=0; m< nodesSFC; m++){
					//		System.out.println("m: " + m);
							if (!((sfcLinks[p-subNodesNum][m]==0)&&(sfcLinks[m][p-subNodesNum]==0)))  {
								if (!(visited_new.contains(p))){
									visited_new.add(p);
									for (int p1=0;p1<paths_max;p1++){
										//System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m +" "+p1);
										//System.out.println("x" + w + "  " +p + " " + (m) + " " +(p-subNodesNum) + " " +p1);
									assignments1.addTerm(1, x[p1][p][w][p-subNodesNum][m]);
									assignments1.addTerm(1, x[p1][w][p][m][p-subNodesNum]);
									}
								}
						}
					}
			
			}
			//System.out.println("lEQ "+1);
			cplex.addLe(assignments1, 1);
			//System.out.println(count+1);
			count++;
			}
		}*/
			//last added //october ( 25)
			for (int p=subNodesNum;p<subNodesNumAug;p++){
				IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
				for(int k=0; k< nodesSFC; k++){
					if (k!=(p-subNodesNum)){
						for(int m=0; m< nodesSFC; m++){
							for (int w=0;w<subNodesNum;w++){
								for (int p1=0;p1<paths_max;p1++){
									if (k!=(p-subNodesNum)){
										assignments1.addTerm(1, x[p1][p][w][k][m]);
									}
									if (m!=(p-subNodesNum)){
										assignments1.addTerm(1, x[p1][w][p][k][m]);
									}
								}
							}
						}								
					}
				}
				cplex.addEq(assignments1, 0);
				count++;
			}
			//System.out.println("9 : " +count);

			///26
		for (int p=subNodesNum;p<subNodesNumAug;p++){
			for (int w=0;w<subNodesNum;w++){
			//IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
			for(int m=0; m< nodesSFC; m++){
				if (sfcLinks[p-subNodesNum][m]!=0) {
						for(int l=0; l< nodesSFC; l++){
							if (l!=m){
								if (sfcLinks[p-subNodesNum][l]!=0) {
									for (int p1=0;p1<paths_max;p1++){
									IloLinearNumExpr assignments1 = cplex.linearNumExpr();									
									assignments1.addTerm(1, x[p1][p][w][p-subNodesNum][m]);
									assignments1.addTerm(-1, x[p1][p][w][p-subNodesNum][l]);
									cplex.addEq(assignments1, 0);
									count++;
									}
								}
							}
						}
					}
				}
			}
		}
			//System.out.println("6 : " +count);
		//reverse
		for (int p=subNodesNum;p<subNodesNumAug;p++){
			for (int w=0;w<subNodesNum;w++){
			//IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
			for(int m=0; m< nodesSFC; m++){
				if (sfcLinks[m][p-subNodesNum]!=0) {
						for(int l=0; l< nodesSFC; l++){
							if (l!=m){
								if (sfcLinks[l][p-subNodesNum]!=0) {
									for (int p1=0;p1<paths_max;p1++){
									IloLinearNumExpr assignments1 = cplex.linearNumExpr();									
									assignments1.addTerm(1, x[p1][w][p][m][p-subNodesNum]);
									assignments1.addTerm(-1, x[p1][w][p][l][p-subNodesNum]);
									cplex.addEq(assignments1, 0);
									count++;
									}
								}
							}
						}
					}
				}
			}
		}
		
		//System.out.println("7 : " +count);
		//october (26)
		for (int p=subNodesNum;p<subNodesNumAug;p++){
			for (int w=0;w<subNodesNum;w++){
			for(int m=0; m< nodesSFC; m++){
				if (sfcLinks[p-subNodesNum][m]!=0) {
						for(int l=0; l< nodesSFC; l++){
							if (sfcLinks[l][p-subNodesNum]!=0) {
								for (int p1=0;p1<paths_max;p1++){
								IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
								assignments1.addTerm(1, x[p1][p][w][p-subNodesNum][m]);
								assignments1.addTerm(-1, x[p1][w][p][l][p-subNodesNum]);
								cplex.addEq(assignments1, 0);
								count++;
								}
							}
						}
					}
				}
			}
		}
		//System.out.println("8 : " +count);
		

		//System.out.println only for bidirectional chains
	for (int p=subNodesNum;p<subNodesNumAug;p++){
		for(int m=0; m< nodesSFC; m++){  ///????
			if(m!=p-subNodesNum){
				if ((sfcLinks[p-subNodesNum][m]!=0) && (sfcLinks[m][p-subNodesNum]!=0)){
					for (int w=0;w<subNodesNum;w++){
						for (int p1=0;p1<paths_max;p1++){
							IloLinearNumExpr assignments1 = cplex.linearNumExpr();	
							IloLinearNumExpr assignments2 = cplex.linearNumExpr();
							///System.out.println("x" + p + "  " +w + " " + (p-subNodesNum) + " " +m+ " "+p1);
							assignments1.addTerm(1, x[p1][p][w][p-subNodesNum][m]);
							assignments2.addTerm(1, x[p1][w][p][m][p-subNodesNum]);
							//System.out.println("x" + w + "  " +p + " " +m + " " +(p-subNodesNum)+ " "+p1);
							//System.out.println("EQ 1");
							cplex.addEq(assignments1, assignments2);
							count++;
						}
					}
				}
			}
		}
	}
	
	//System.out.println("9 : " +count);


/*****************************Flow Constraints **************************************************/
		//oti mpainei se kombo bgainei gia normal substrate 
		for (int u=0;u<subNodesNum;u++){
			for(int k=0; k< nodesSFC; k++){
				for(int m=0; m< nodesSFC; m++){		
					IloLinearNumExpr flowCon2 = cplex.linearNumExpr();
					IloLinearNumExpr flowCon3 = cplex.linearNumExpr();	
					for (int l=0;l<subNodesNumAug;l++){
						for (int p=0;p<paths_max;p++){
						flowCon2.addTerm(1,fl[p][l][u][k][m]); //incoming
						flowCon3.addTerm(1,fl[p][u][l][k][m]); //outgoing
						}
					}
					cplex.addEq(flowCon2, flowCon3);;
				}
			}
		}
		
		
		//System.out.println("11 : " +count);
		for (int u=subNodesNum;u<subNodesNumAug;u++){
			for(int m=0; m< nodesSFC; m++){	
				double capVirt =sfcLinks[u-subNodesNum][m];
				IloLinearNumExpr flowCon1 = cplex.linearNumExpr();	
				for (int l=0;l<subNodesNum;l++){
					for (int p=0;p<paths_max;p++){
					flowCon1.addTerm(1,fl[p][u][l][u-subNodesNum][m]); //outgoing
					}
				}
				cplex.addEq(flowCon1, capVirt);
			}
		}
		//System.out.println("12 : " +count);
		for (int u=subNodesNum;u<subNodesNumAug;u++){
			for(int m=0; m< nodesSFC; m++){	
				double capVirt =sfcLinks[m][u-subNodesNum];
				IloLinearNumExpr flowCon1 = cplex.linearNumExpr();	
				for (int l=0;l<subNodesNum;l++){
					for (int p=0;p<paths_max;p++){
					flowCon1.addTerm(1,fl[p][l][u][m][u-subNodesNum]); //incoming
					}
				}
				cplex.addEq(flowCon1, capVirt);
			}
		}
				
				
				
				
		//System.out.println("13 : " +count);
		
				
		cplex.exportModel("LPsimple.lp");
		
		long solveStartTime = System.nanoTime();
		boolean solvedOK = cplex.solve();
		long solveEndTime = System.nanoTime();
		long solveTime = solveEndTime - solveStartTime;

		
		if (solvedOK) {
			System.out.println("###################################");
			System.out.println( "Found an answer! CPLEX status: " + cplex.getStatus() + ", Time: " + ((double) solveTime / 1000000000.0));

			cplex.output().println("Solution value = " + cplex.getObjValue());
			System.out.println("###################################");
		
			
			//nodeMapping
			LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();//requested-real
		//	ArrayList<Node> subNodes = getNodes(substrateCopy.getGraph());
			int counterA=0;
			int rules_added=0;
			double embedding_cost=0;
			ArrayList<Integer> fvisited = new ArrayList<Integer>();
			double[][][][][] xVar =new double [paths_max][subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
			for (int p=0;p<paths_max;p++){
			for (int u=0;u<subNodesNumAug;u++){
				for (int v=0;v<subNodesNumAug;v++){				
					for(int k=0; k< nodesSFC; k++){
						for(int m=0; m<nodesSFC; m++){
							xVar[p][u][v][k][m]=cplex.getValue(x[p][u][v][k][m]);
							if (xVar[p][u][v][k][m]>0.1) 
								{
								counterA++;
									System.out.println("Path: " + p + " Vlink: " + k +"->"+m +" mapped to: " + u +"->"+v + " " );
									if ((u==(subNodesNum+k)) && !fvisited.contains(k)) {
										//System.out.println(subCapAug[u]);
										embedding_cost +=reqNodes.get(k).getAvailableCpu();
										nodeMap.put(reqNodes.get(k),((List<Node>) subNodes).get(v));
										if (!(updateSubstrate(substrateCopy,subNodes.get(v),reqNodes.get(k).getAvailableCpu())))
											 throw new ArithmeticException("Substrate Node Capacity not updated");
										fvisited.add(k);
									}
									 if ((v==(subNodesNum+m)) && !fvisited.contains(m)) {
									//	System.out.println(subCapAug[v]);
										embedding_cost +=reqNodes.get(m).getAvailableCpu();
										nodeMap.put(reqNodes.get(m),((List<Node>) subNodes).get(u));
										if (!(updateSubstrate(substrateCopy,subNodes.get(u),reqNodes.get(m).getAvailableCpu())))
											 throw new ArithmeticException("Substrate Node Capacity not updated");
										fvisited.add(m);
									}
/*									 if ((u!=(subNodesNum+k)) && (v!=(subNodesNum+m))  && (u!=(subNodesNum+m)) && (v!=(subNodesNum+k))) {
										if(subTypeAug[u].equalsIgnoreCase("switch"))
												rules_added+=1;
									}*/
								}
							}
						}
					}
				}
			}
			System.out.println(embedding_cost);
			
			MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new 
					MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();
			 HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>> lmapint =
						new HashMap<Pair<Integer>, ArrayList<HashMap<Pair<Integer>, Double>>>();
			double avg_hops=0;
            int counterB=0;
			double[][][][][] fVar =new double [paths_max][subNodesNumAug][subNodesNumAug][nodesSFC][nodesSFC];
			for(int k=0; k< nodesSFC; k++){
				int hops_link=0;
				int paths_used=0;
				for(int m=0; m<nodesSFC; m++){
					//System.out.println("checking link: " +k+" -> "+m);
					int hops_link_path=0;
					
					for (int p=0;p<paths_max;p++){
						ArrayList<HashMap<Pair<Integer>, Double>> rpath = new ArrayList<HashMap<Pair<Integer>, Double>>();
					for (int u=0;u<subNodesNumAug;u++){
						for (int v=0;v<subNodesNumAug;v++){				
					
							fVar[p][u][v][k][m]=cplex.getValue(fl[p][u][v][k][m]);
							if (fVar[p][u][v][k][m]>0.0001) {
								counterB++;
								System.out.println("Path: " + p + " Vlink: " + k +"->"+m +" mapped to: " + u +"->"+v + 
										"has flow" + fVar[p][u][v][k][m]);
								//update substrate
								Pair<Integer> key= new Pair<Integer>(u,v);
								//check first if pathlet
								Collection<List<Pair<Integer>>> collPaths = pathlets.get(key); 
								HashMap<Pair<Integer>,Double> key4lmap=new HashMap<Pair<Integer>,Double>(); 
								key4lmap.put(key, fVar[p][u][v][k][m]);
								rpath.add(key4lmap); //THIS FOR MAPPING
								
								int hops=0;
								//System.out.println(collPaths);
								if (collPaths!=null){
									ArrayList<List<Pair<Integer>>>copy = 
											new ArrayList<List<Pair<Integer>>>(collPaths);
									List<Pair<Integer>> tmpPath = copy.get(p);
									lmap.put(key4lmap,tmpPath);
									for (Pair<Integer> pair: tmpPath) {
										hops++;
										System.out.println(pair + " " +hops);
										embedding_cost += fVar[p][u][v][k][m];
										 if (!(updateSubLink
												 (substrateCopy,pair.getFirst(), 
														 pair.getSecond(),fVar[p][u][v][k][m])))
											 throw new ArithmeticException("Substrate Link Capacity not updated");
									
									}
								}
								else if ((u!=(subNodesNum+k)) && (v!=(subNodesNum+m))  && (u!=(subNodesNum+m)) && (v!=(subNodesNum+k))) {
									hops++;
									Pair<Integer> tmpkey= new Pair<Integer>(u,v);
									List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>> ();
									tmpPath.add(tmpkey);
									lmap.put(key4lmap,tmpPath);
									embedding_cost += fVar[p][u][v][k][m];
									//System.out.println(u + " " +v + " " +hops);
									 if (!(updateSubLink (substrateCopy,u,v,fVar[p][u][v][k][m])))
										 throw new ArithmeticException("Substrate Link Capacity not updated");
								}
								if (hops>0){
									hops_link_path+=hops;
									//System.out.println ("hops_link_path: " +hops_link_path);
								}
							}
								
							}
							
						}
						if (sfcLinks[k][m]>0){
						Pair<Integer> vlink= new Pair<Integer>(k,m);
						lmapint.put(vlink, rpath);
						}
					}
					
					if (hops_link_path>0){
						hops_link +=hops_link_path;
						paths_used+=1;
					}
				}
				if (paths_used>0){
					avg_hops+=(double)(hops_link/(paths_used));
					//System.out.println (hops_link + " " +paths_used+ " " + avg_hops);
				}
			}
			avg_hops = 	avg_hops / reqLinks.size();

			//System.out.println ( " avg_hops " + avg_hops  +"  and rules  " + rules_added +"  for links" + reqLinks.size());

			//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
			//System.out.println(lmapint);
			
			writer1.write("Node Mapping: "+ nodeMap+ "\n");
			writer1.write("Link Mapping: "+ lmap+ "\n");
			req.print();
//			writer1.write("Link Mapping: "+ lmapint+ "\n");
			//substrateCopy.print();
			reqMap.setNodeMapping(nodeMap);
			reqMap.setFlowMapping(lmap);
			//reqMap.setLinkMappingNew(lmapint);
			reqMap.setHops(avg_hops);
			System.out.println(counterA+ "  " +counterB);
			System.out.println(embedding_cost+ " " + embedding_rev + " " +rules_added + " " +avg_hops);
			
			System.out.println("###############################################");
			System.out.println(nodeMap);
			System.out.println(lmap);
			
			//System.out.println(pathletsUse);
			
			 Set<HashMap<Pair<Integer>,Double>> keySetPaths = lmap.keySet();
			 Iterator<HashMap<Pair<Integer>,Double>> keyIteratorPath = keySetPaths.iterator();
			 
			 while(keyIteratorPath.hasNext( ) ) {
				// System.out.println("start end");
				 	HashMap<Pair<Integer>,Double> keyCap = keyIteratorPath.next();
				 	Collection<List<Pair<Integer>>> keyCapValue = lmap.get(keyCap);
				 	Set<Pair<Integer>> key = keyCap.keySet();
				 	Iterator<Pair<Integer>> keyIteratorinPath = key.iterator();
				 	 while(keyIteratorinPath.hasNext( ) ) {
				 	 Pair<Integer> keyPath =  keyIteratorinPath.next();
				 	 //System.out.println("start end:" + keyPath);
				 	 if (pathletsUse.containsKey(keyPath)) {
				 		Collection<HashMap<List<Pair<Integer>>, Integer>> usage  = pathletsUse.get(keyPath);
					 		for (HashMap<List<Pair<Integer>>, Integer> pathletList : usage) {
					 			Set<List<Pair<Integer>>> listset=pathletList.keySet();
					 			for (List<Pair<Integer>> list : listset) {
					 				//System.out.println(list);
					 				for (List<Pair<Integer>> actualPath : keyCapValue) {
					 					//System.out.println(actualPath);
					 					if (list.equals(actualPath)) {
					 						//System.out.println(list + " " + pathletList.get(list));
					 						if (pathletList.get(list)==0) {
					 							rules_added+=1;
					 							pathletList.put(list, pathletList.get(list) + 1);
					 							//System.out.println("found");
					 						} 
					 					}
					 				}
					 			}
	
					 		}
				 	 	}
				 	 }
			  }

			System.out.println("rules added: " +rules_added);
			reqMap.setRulesAdded(rules_added);
			reqMap.accepted();
			reqMap.setEmbeddingCost(embedding_cost);
			req.setRMapNF(reqMap);
			
		
/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
			
		}else{
			System.out.println("Did not found an answer for Mapping");
			reqMap.denied();
			req.setRMapNF(reqMap);
/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }*/
		}	
		cplex.end();	
		
	} catch (IloException e) {
		System.err.println("Concert exception caught: " + e);
	}
	
		
	}//all requests
	//update the usage of rules in pathlet
	substrates.get(0).getFTL3().setPathsUsage(pathletsUse);
	 } catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
		}
	
	return true;
}


public Integer existsPath  (MultiHashMap<Pair<Integer>, HashMap<List<Pair<Integer>>,Integer>> pathlets,
		int pathid, int src, int dst, int subnodes, int[][][][][] pathlet)  {
	//pathlet = new int [paths_max][subNodesNum][subNodesNum][subNodesNum][subNodesNum];
	Pair<Integer> path = new Pair<Integer> (src,dst);
	List<Pair<Integer>> cPath= new ArrayList<Pair<Integer>>();
	for (int i=0;i<subnodes;i++) {
		for (int j=0;j<subnodes;j++) {
			 if (pathlet[pathid][src][dst][i][j]==1) {
					//System.out.println("found");
				 Pair<Integer> pair =  new Pair<Integer>(i,j);
				 cPath.add(pair);
			 }
		}
	}
	//System.out.println("aaaaaaaaa" +src+ " " + dst);

	if (!cPath.isEmpty()) {
	//System.out.println(cPath);
	if (pathlets.containsKey(path)) {
		Collection<HashMap<List<Pair<Integer>>,Integer>> value = pathlets.get(path);
		for (HashMap<List<Pair<Integer>>,Integer> list : value) {
			Set<List<Pair<Integer>>> keySetPaths = list.keySet();
			for (List<Pair<Integer>> list1 : keySetPaths) {
				//System.out.println(list1);
			if (comparePaths(list1,cPath)){
				//System.out.println("found2");
				return list.get(list1);
			}
			}
			
		}
			
		}
	}
	return 0;
	}
public static boolean comparePaths(Object o1, Object o2){
    try{
        Collection<?> c1 = (Collection<?>)o1,
                      c2 = (Collection<?>)o2;
        return (c1.size()==c2.size())&&c1.containsAll(c2);
    }
    catch(Exception e){
        return false;
    }
}

//////////////////////////NARIMAN///////////////////////////////////////

private boolean placementTrustNF() {
	 double[][] retres=new double[reqs.size()][1];
	
Set<Node> hs = new HashSet<>();
Writer writer1=null;
Writer writer=null;


//try {
	
	String path = "results/trust_nf";
	
	String filename = "substrate1Level" +System.currentTimeMillis() + ".txt";
	
	
	long name=System.currentTimeMillis();
	//writer = new BufferedWriter(new FileWriter(path+File.separator+filename));
	//System.out.println("Aliiiiiiiiive");
	//System.exit(0);
	String filename1 = "requests1Level" + + name +".txt";
	
   //writer1 = new BufferedWriter(new FileWriter(path+File.separator+filename1));



//SFCs (req)
int numSFCs=reqs.size();
int id_req = 0;

for (Request req: reqs){
	ResourceMappingNF reqMap = new ResourceMappingNF(req);
	
	Substrate substrateCopy =  substrates.get(0);
	Graph<Node, Link> sub= substrateCopy.getGraph();

	
	ArrayList<Node> subNodesList = (ArrayList<Node>) getNodes(substrateCopy.getGraph());
	ArrayList<Link> subLinksList = (ArrayList<Link>) getLinks(substrateCopy.getGraph());
	
	//writer.write("Request: " +req.getId()+ " \n");
	
	int subNodesNum = subNodesList.size();			
	double[] subNodes =  new double[subNodesNum];
	String[] subNodeTypes =  new String[subNodesNum];
	
	//node cpu
	for (Node x: subNodesList){		
		subNodes[x.getId()] = x.getAvailableCpu();
		subNodeTypes[x.getId()] = x.getType();
		//writer.write("Node: " + x.getId() + " Type: " + x.getType() +" CPU: " + x.getAvailableCpu() + "\n");
	}
	//Adjacency Matrix Substrate bw
	double[][] subLinks = new double[subNodesNum][subNodesNum];
	for (Link y: subLinksList){
		Pair<Node> tmp = sub.getEndpoints(y);
		subLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
		//writer.write("Link " + y.getId()+ " : "+ tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
		//		y.getBandwidth()+"\n");
	}
	//int subLinksNum = subNodesNum*subNodesNum;
	
	
	/*double[] subNodes = new double[]{50,60};
	int subNodesNum = subNodes.length;
	String[] subNodeTypes  = new String[]{"Server","Server"};
	double[][] subLinks = new double[][] {{10000,50},{60,10000}};*/
	
	System.out.println("subNodesNum: "+subNodesNum);	
	System.out.println("subCap: "+Arrays.toString(subNodes));
	System.out.println("subNodeTypes: "+Arrays.toString(subNodeTypes));
	System.out.println("subLinks: "+Arrays.deepToString(subLinks));
	//System.out.println("subLinksNum: "+subLinksNum);
	
	//substrateCopy.print();
	int sfcNodes_num =  req.getGraph().getVertexCount();

	double[] sfcNodes =new double[sfcNodes_num];
	String[] sfcNodeTypes =new String[sfcNodes_num];
	String[] sfcNodeFunctions =new String[sfcNodes_num];
	
	int counter=0;

	double proc_revenue=0;
	//writer1.write("Request: " +req.getId()+ " \n");
	
	ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.getGraph());
	for (Node node: req_n){
		sfcNodes[node.getId()]=node.getAvailableCpu();
		proc_revenue += node.getAvailableCpu();
		sfcNodeTypes[node.getId()]=node.getType();
		sfcNodeFunctions[node.getId()]=node.getName();
		//writer1.write("Node: " + node.getId() + " Type: " + node.getType() +" CPU: " + node.getAvailableCpu()+"\n");
	}
	

	double[][] sfcLinks = new double[sfcNodes_num][sfcNodes_num]; //replace witn max			

	double bw_revenue=0;
	ArrayList<Link> links =(ArrayList<Link>) getLinks(req.getGraph());
	for (Link y: links){
	Pair<Node> tmp = req.getGraph().getEndpoints(y);
	sfcLinks[tmp.getFirst().getId()][tmp.getSecond().getId()]  = y.getBandwidth();
//	writer1.write("Link " + y.getId()+ " : "+  y.getId()+ " : "+  tmp.getFirst().getId() + " -> " + tmp.getSecond().getId()+" BW: " +
//			 y.getBandwidth()+"\n");
	bw_revenue+=y.getBandwidth();
	}
	
	ArrayList<Node> sfc= req.getNodes(req.getGraph());
	int sizeOfSFC=sfc.size();
	int numNodesSFC = sfc.size();
	
/*		double[] sfcNodes = new double[] {50,50};
	int sizeOfSFC = sfcNodes.length;
	int numNodesSFC  = sizeOfSFC;
	String[] sfcNodeTypes = new String[]{"NF","NF"};
	double[][] sfcLinks = new double[][]{{0,10},{20,0}};*/

	reqMap.setEmbeddingRevenue(bw_revenue+proc_revenue);
	System.out.println("sfcNodes: "+Arrays.toString(sfcNodes));
	System.out.println("sfcNodeTypes: "+Arrays.deepToString(sfcNodeTypes));
	System.out.println("sfcLinks: "+Arrays.toString(sfcLinks[0]));
	System.out.println("numSFCs"+numSFCs);				

	//System.exit(0);
	int[][] SEdge = getGraphMatrix(substrateCopy.getGraph());
	int[][] REdge = getGraphMatrix(req.getGraph());
	double[] trust = getTrustMatrix(substrateCopy.getGraph());
	double sumDemands = 0.0; 
	for(int i=0; i<sfcLinks.length;i++)
		for(int j=0; j<sfcLinks.length;j++)
			sumDemands += sfcLinks[i][j];

	int[] problem_vars = new int[4];
	problem_vars[0] = (numNodesSFC*subNodesNum + numNodesSFC*subNodesNum*numNodesSFC*subNodesNum);
	problem_vars[1] =numNodesSFC +1 +getCount(REdge)*subNodesNum +getCount(SEdge) + subNodesNum;
	int countInd=0; 
	for(int i=0; i<numNodesSFC; i++)
		for(int j=0; j<numNodesSFC; j++)
			if(REdge[i][j]!=0)
				for(int u=0; u<subNodesNum;u++) {
					for(int v=0; v<subNodesNum;v++) {
						if(SEdge[u][v]!=0) 
							countInd++;
						if(SEdge[v][u]!=0) 
							countInd++;
					}
					
					countInd+=2;
				}
	problem_vars[2] = numNodesSFC*subNodesNum +numNodesSFC*subNodesNum +countInd + numNodesSFC*subNodesNum +getCount(REdge)*getCount(SEdge); 
	problem_vars[3] = numNodesSFC*subNodesNum;
	
	
	//System.exit(0);
try {
	
//Problem is that I need to give the super values to that before I get an instance, in other
	//words, the instance super  should be defined with the corresponding values not with zero.
	ProblemVirtualization instance = new ProblemVirtualization(problem_vars, SEdge, REdge, sfcLinks, subLinks, sfcNodes, 
			subNodes, subNodesNum, numNodesSFC, trust, req.getTrustThr(), sumDemands);
	
	
	instance.setProbVars(problem_vars);
	instance.setSubMatrix(SEdge); 
	instance.setReqMatrix(REdge);
	instance.setBWDemand(sfcLinks);
	instance.setSubBW(subLinks);
	instance.setCPUDemand(sfcNodes);
	instance.setSubCPU(subNodes);
	instance.setNumS(subNodesNum);
	instance.setNumR(numNodesSFC);
	instance.setTrust(trust);
	instance.setThresh(req.getTrustThr());
	instance.setRedgeSize(getCount(REdge));
	instance.setSedgeSize(getCount(SEdge));
	instance.setSum(sumDemands);
	

	System.out.println(new String(new char[50]).replace("\0", "="));
    System.out.println("Solving "+ProblemVirtualization.class.getSimpleName());

    //KTRIProblem instance = generateInstance(ProblemVirtualization.class);

   // System.exit(0);
    // Create a new solver
    KTRISolver solver;
    solver = new KTRSolver(instance);
    

    // Remove all outputs
    solver.setParam(KTRConstants.KTR_PARAM_OUTLEV, KTRConstants.KTR_OUTLEV_NONE);


	int denial=0;				
	
	long solveStartTime = System.nanoTime();
	// Solve problem
    int result = solver.solve();
	long solveEndTime = System.nanoTime();
	long solveTime = solveEndTime - solveStartTime;
	System.out.println("result" + result);
	 
	//System.exit(0);
	req.print();
	if (result==0) {
		
			System.out.println("###################################");
			//System.out.println( "Found an answer! CPLEX status: " + solver.getStatus() + ", Time: " + ((double) solveTime / 1000000000.0));
			// Display results
	    	printSolutionResults(solver, result);
			//cplex.output().println("Solution value = " + cplex.getObjValue());
			System.out.println("###################################");
			//substrateCopy.print();
			
			//nodeMapping
			LinkedHashMap<Node, Node> nodeMap = new LinkedHashMap<Node, Node> ();//requested-real
			//ArrayList<Node> subNodes = getNodes(substrateCopy.getGraph());
			int rules_added=0;
			double embedding_cost=0;

			double[][] xVar =new double [subNodesNum][sizeOfSFC];
			for (int u=0;u<subNodesNum;u++){
				for (int i=0;i<sizeOfSFC;i++){
						xVar[u][i] = solver.getXValues(i*subNodesNum + u); 
						if (xVar[u][i] > 0){
							Node tmp = subNodesList.get(u);
							embedding_cost +=req_n.get(i).getAvailableCpu();
							nodeMap.put(req_n.get(i),subNodesList.get(u));
							if (!(updateSubstrate(substrateCopy,subNodesList.get(u),req_n.get(i).getAvailableCpu())))
								 throw new ArithmeticException("Substrate Node Capacity not updated");

							 System.out.println("Node  " + i + " to " + u +"  " +xVar[u][i]);
						}
				}
			}	
			
		

			MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>> lmap = new 
					MultiHashMap<HashMap<Pair<Integer>,Double>, List<Pair<Integer>>>();

			double avg_hops=0;
			
			double bw_cost=0;
			double[][][][] fVar1 =new double [subNodesNum][subNodesNum][sizeOfSFC][sizeOfSFC];
		for(int k=0; k< numNodesSFC; k++){
			for(int m=0; m< numNodesSFC; m++){
				int hops_link_path=0;	
					for (int u=0;u<subNodesNum;u++){
							for (int v=0;v<subNodesNum;v++){
							fVar1[u][v][k][m] = solver.getXValues(numNodesSFC*subNodesNum+ k*numNodesSFC*subNodesNum*subNodesNum+
									m*subNodesNum*subNodesNum+ u*subNodesNum+ v);
							// System.out.println("SFC : " +k+" "+m + " to " + u+ " "+v +" "+ fVar1[u][v][k][m]);
								if (fVar1[u][v][k][m]>0.00000001){
							   embedding_cost=embedding_cost+fVar1[u][v][k][m];
								Pair<Integer> key= new Pair<Integer>(u,v);
								HashMap<Pair<Integer>,Double> key4lmap=new HashMap<Pair<Integer>,Double>();
								key4lmap.put(key, fVar1[u][v][k][m]);
								List<Pair<Integer>> tmpPath = new ArrayList<Pair<Integer>> ();
								tmpPath.add(key);
								lmap.put(key4lmap,tmpPath);
								 System.out.println("Link : " +k+" "+m + " to " + u+ " "+v +" "+ fVar1[u][v][k][m]);
								 bw_cost+=fVar1[u][v][k][m];
								 
								 if (!(updateSubLink(substrateCopy,u,v, fVar1[u][v][k][m])))
									 throw new ArithmeticException("Substrate Link Capacity not updated");
								 hops_link_path++;
								}
						}
					}
					avg_hops=avg_hops+hops_link_path;
				}
			}
			
			avg_hops = avg_hops/links.size();
			
			System.out.println ( " avg_hops " + avg_hops +"  and rules  " + rules_added +"  for links" +links.size());
			//System.out.println ( " avg_hops " + avg_hops  +"  and rules  " + rules_added +"  for links" + reqLinks.size());
			
			
			//substrateCopy.print();
			reqMap.setNodeMapping(nodeMap);
			reqMap.setFlowMapping(lmap);
			//reqMap.setRulesAdded(rules_added);
			reqMap.setHops(avg_hops);
			System.out.println(embedding_cost+ " " + (bw_revenue+proc_revenue) + " " +rules_added + " " +avg_hops);
			
			System.out.println("###############################################");
			System.out.println(nodeMap);
			System.out.println(lmap);
			reqMap.accepted();
			reqMap.setEmbeddingCost(embedding_cost);
			req.setRMapNF(reqMap);
			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
			//writer1.write("Node Mapping: "+ nodeMap+ "\n");
			//writer1.write("Link Mapping: "+ lmap+ "\n");
			
		}else{
			System.out.println("Did not find an answer for Mapping");
			reqMap.denied();
			req.setRMapNF(reqMap);
		}	
			
		
	} catch (Exception e) {
		System.err.println("Knitro exception caught: " + e);
		System.exit(0);
	}
	
		
	}//all requests
	
	
	
//	 } catch (IOException ex) {
//		  // report
//		} finally {
//		   try {writer.close(); writer1.close();} catch (Exception ex) {/*ignore*/}
//		}
//	
	return true;
}

		
	}

	
