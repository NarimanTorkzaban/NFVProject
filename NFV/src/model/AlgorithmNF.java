package model;
import algorithm.*;
import com.artelys.knitro.api.*;
//import com.artelys.knitro.examples.Problems.*;
//import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import gui.SimulatorConstants;
import java.io.BufferedWriter;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

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
	
	
	
public int[] initVars() {
	int[] prob_vars = new int[4];
	return prob_vars;
}
	
public boolean runAlgorithm (){
	boolean retres = false;
		
	 if  (this.id.contentEquals("NF_Paths")){
			//retres=NFplacementPathlets1();
	 }
	 else if  (this.id.contentEquals("NF_Paths_alt")){
			//retres=NFplacementPathletsAlt1();
	 }
	 else if  (this.id.contentEquals("trust_nf")){
			try{
				retres=placementTrustNF();
			}catch(Exception e) {
				e.printStackTrace();
			}
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
public ArrayList<Link> getLinks(Graph<Node, Link> t) {
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

private static KTRIProblem generateInstance(final Class<? extends KTRIProblem> problemClass)
{
    try
    {
        return problemClass.newInstance();
    }
    catch (Exception e)
    {
        System.err.println("Error: failed to generate instance of problem " + problemClass.getName());
    }
    return null;
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

private boolean placementTrustNF() throws Exception{
	double[][] retres=new double[reqs.size()][1];
	Substrate substrateCopy =  substrates.get(0);
	
	
	for(int i=0; i<reqs.size();i++) {
		int[][] SEdge = getGraphMatrix(substrateCopy.getGraph());
		int[][] REdge = getGraphMatrix(reqs.get(i).getGraph());
		double[][] demand = getBWMatrix(reqs.get(i).getGraph());
		double[][] c = getBWMatrix(substrateCopy.getGraph());
		double[] g =getCPUMatrix(reqs.get(i).getGraph());
		double[] r =getCPUMatrix(substrateCopy.getGraph());
		int numS = substrateCopy.getGraph().getVertexCount();
		int numR = reqs.get(i).getGraph().getVertexCount();
		double[] trust = getTrustMatrix(substrateCopy.getGraph());

		ProblemVirtualization instance = new ProblemVirtualization();
		instance.SEdge = SEdge;
		instance.REdge = REdge;
		instance.demand = demand;
		instance.c = c;
		instance.g= g;
		instance.r = r;
		instance.numS= numS;
		instance.numR= numR;
		instance.trust = trust;
		instance.thresh = reqs.get(i).getTrustThr();
		instance.redgeSize = getCount(REdge);
		instance.sedgeSize = getCount(SEdge);
		
		System.out.println(new String(new char[50]).replace("\0", "="));
	    System.out.println("Solving "+ProblemVirtualization.class.getSimpleName());

	    //KTRIProblem instance = generateInstance(ProblemVirtualization.class);

	    // Create a new solver
	    KTRISolver solver;
	    solver = new KTRSolver(instance);
	    

	    // Remove all outputs
	    solver.setParam(KTRConstants.KTR_PARAM_OUTLEV, KTRConstants.KTR_OUTLEV_NONE);

	    // Solve problem
	    int result = solver.solve();

	    // Display results
	    printSolutionResults(solver, result);

	}
	
	
	
	return true;
}


		
	}

	
