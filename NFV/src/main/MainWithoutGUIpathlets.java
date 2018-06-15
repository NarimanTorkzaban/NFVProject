package main;

import java.awt.Graphics;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jdistlib.Uniform;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.Number;
import model.Algorithm;
import model.AlgorithmNF;
import model.DirectedFG;
//import model.AlgorithmNFold;
import model.DirectedFGLTE;
import model.DirectedFatTreeL2;
import model.DirectedFatTreeL3;
import model.FatTreeL2;
import model.NetworkGraph;
import model.Request;
import model.RequestLinkFactory;
import model.RequestNodeFactory;
import model.Simulation;
import model.SimulationNFV;
import model.Substrate;
import model.SubstrateLinkFactory;
import model.SubstrateNodeFactory;
import model.TrafficLTE;
import model.components.Coords;
import model.components.Link;
import model.components.NF;
import model.components.Node;
import model.components.VirtualMachine;
import output.DrawGrids;
import output.Grid;
import output.Grids;
import output.Results;
import output.CustomComponents;

import org.apache.commons.collections15.Factory;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;

import cern.jet.random.Exponential;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.OrderedKAryTree;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import gui.SimulatorConstants;

import org.apache.commons.math3.ml.distance.EuclideanDistance; 

import tools.NodeComparator;

public class MainWithoutGUIpathlets{
	static ArrayList<Node> sRRHs = new  ArrayList<Node>();
	static ArrayList<Node> sIXPs = null; 
	static ArrayList<Node> NFs = null; 
	static Results resultsSim =null;
	public static void main(String[] args) {
		
	
		// Number of experiments to execute
		int experiments=1;
		
		for (int i=0;i<experiments;i++){			

			int dc_no=1;
			
/*			try {
			        System.in.read();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    }*/
	
			//Create an abstract graph where each node represent an InP
			Substrate InPs=new Substrate("InPs");
			Substrate nfvi = createSubGraph(dc_no);
		
			//Create each InP
			List<Substrate> substrates = new ArrayList<Substrate>();
			substrates.add(nfvi);
			//nfvi.print();
			//System.exit(0);
			
			//Create the Requests
			List<Request> request_tab = new ArrayList<Request>();
			List<Request> request_tab1 = new ArrayList<Request>();
			request_tab= createFG(600);
			request_tab1=request_tab;
			

/*			try {
		        System.in.read();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
			System.exit(0)*/;
			
			resultsSim= new Results(substrates.get(0));
			System.out.println("The simulator is working nowss");
			//sSystem.exit(0);
			
			AlgorithmNF algorithm = new AlgorithmNF("trust_nf",resultsSim);
			SimulationNFV simulation = new SimulationNFV(InPs, substrates, request_tab, algorithm);
			try {
				launchSimulation(simulation, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}

	
	private static double[] launchSimulation(SimulationNFV simulation,int inD) throws Exception{
		//results, 0 cost, 1 time, 2, denial
		double[] results=new double[3];
		  try {
			  Calendar calendar = Calendar.getInstance();
			  java.util.Date now = calendar.getTime();
			  java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
			  String name =  currentTimestamp.toString();
			  String[] out = name.split(" ");
			  String[] out1 = out[1].split(":");
			  String path = "results";
			  String filename = "input"+ simulation.getAlgorithm().getId()+".xlsx";
		    //  String filename = "input"+out[0]+"_"+out1[0]+"-"+out1[1]+"_"+ simulation.getAlgorithm().getId()+"_"+inD+".xls";
		      System.out.println(filename);
		      
		      WorkbookSettings ws = new WorkbookSettings();
		      ws.setLocale(new Locale("en", "EN"));
		      WritableWorkbook workbook =  Workbook.createWorkbook(new File(path+File.separator+filename), ws);
		      WritableSheet s = workbook.createSheet("Sheet1", 0);
		      NumberFormat decimalNo = new NumberFormat("0.000"); 
		      //DecimalFormat df = new DecimalFormat("0.00");
		      WritableCellFormat numberFormat = new WritableCellFormat(decimalNo);
		      //WritableCellFormat format = new WritableCellFormat(new NumberFormat("0.###############"));
		      
			   /* Format the Font */
				    WritableFont wf = new WritableFont(WritableFont.ARIAL, 
				      10, WritableFont.BOLD);
				    WritableCellFormat cf = new WritableCellFormat(wf);
				    cf.setWrap(true);
				      Label l = new Label(0,0,"Time",cf);
				      s.addCell(l);
				      l= new Label(1,0,"Acceptance",cf);
				      s.addCell(l);
				      l=new Label(2,0,"Cum Revenue",cf);
				      s.addCell(l);
				      l=new Label(3,0,"Cum Cost",cf);
				      s.addCell(l);
				      l=new Label(4,0,"AVG hops",cf);
				      s.addCell(l);
				      l=new Label(5,0,"AVG rules",cf);
				      s.addCell(l);
				      l=new Label(6,0,"Hopes",cf);
				      s.addCell(l);


				      
				      
		
		int denials = 0;
		double cost=0;
		double revenue=0;
		double avg_hops = 0;
		int rules=80;
	    int requested=0;
	    
	    int simulationTime = (int)simulation.getEndDate() +10;
	    System.out.println(simulationTime);
	   // System.exit(0);
		List<Substrate> substrates = simulation.getSubstrates();
		Substrate InPs = simulation.getInPs();
		AlgorithmNF algorithm = simulation.getAlgorithm();
		List<Request> endingRequests;
	    List<Request> startingRequests;
	    algorithm.addSubstrate(substrates);
	    algorithm.addInPs(InPs);
	    algorithm.addNFs(NFs);
	    

	    
/////////////////////////////////////////////////////////////////////
	    
	  
	    //Results results=new Results(substrate);
	    	int counter2=0;
	        for (int i=0; i<(simulationTime+10); i++) {	 
	        
	            // Release ended simulations in the moment "i"
	            endingRequests = simulation.getEndingRequests(i);
	            simulation.releaseRequests(endingRequests);
	            
	            // Allocate arriving requests in the moment "i"
	            startingRequests = simulation.getStartingRequests(i);
	            algorithm.addRequests(startingRequests);
         	
	         
	           
      			if (startingRequests.size()>0){
      				//System.out.println("startingRequests: " +startingRequests.size());
      				boolean ret = algorithm.runAlgorithm();    
      				if (!ret)
      					throw new Exception("Algorithm error");

          	  /*      try {
      			        System.in.read();
      			    } catch (IOException e) {
      			        // TODO Auto-generated catch block
      			        e.printStackTrace();
      			    }
      				*/
       
      				for (Request req:startingRequests){
    	            	System.out.println("Taking results");
    	            	requested++;
    	            	if (req.getRMapNF().isDenied() ){
    	            		denials++;
    	            	}else{
    	            		revenue= revenue +req.getRMapNF().getEmbeddingRevenue();
    	            		cost=cost+ req.getRMapNF().getEmbeddingCost();
    	            		avg_hops = avg_hops+ req.getRMapNF().getHops();
    	            		rules = rules + req.getRMapNF().getRules();
    	            	}
    	            /*	System.out.println(revenue+ " " +cost+ " " +avg_hops+" "+rules + " "+requested+ " "+denials);
    	            	try {
	      			        System.in.read();
	      			    } catch (IOException e) {
	      			        // TODO Auto-generated catch block
	      			        e.printStackTrace();
	      			    }*/
    	            	
    	            	counter2++;
    	            	Number n = new Number(0,counter2,i);
    	                s.addCell(n);
    	                double acceptance = (double)(requested-denials)/(double)requested; 
    	                System.out.println("acceptance: " +acceptance);
    	                Number n1 = new Number(1,counter2,acceptance,numberFormat);
    	                s.addCell(n1);
    	                Number n2 = new Number(2,counter2,revenue,numberFormat);
    	                s.addCell(n2);
    	                Number n3 = new Number(3,counter2,cost,numberFormat);
    	                s.addCell(n3);
    	               	Number n4 = new Number(4,counter2,avg_hops/(double)(requested-denials),numberFormat);
    	                s.addCell(n4);
    	                Number n5 = new Number(5,counter2,(double)rules,numberFormat);
    	                s.addCell(n5);        
    	                Number n6 = new Number(6,counter2,avg_hops,numberFormat);
    	                s.addCell(n6);      
    	               	               //(double)(requested-denials)
    	            }
      		
      			}
      			
    
      	
      			
	        }
	        

  			
		    workbook.write();
		    workbook.close(); 
		    }
		    catch (IOException e)
		    {
		      e.printStackTrace();
		    }
		    catch (WriteException e)
		    {
		      e.printStackTrace();
		    }
  			

  			return results;

	}
	

		
 public static List<Substrate> createSubstrateGraph(int inp_no){
	 	 	
	 	final String prefix ="sub";
		final List<Substrate> substrates = new ArrayList<Substrate>();
		
		for (int i=0;i<inp_no;i++){
			Substrate substrate = new Substrate(prefix+i);
			
			UndirectedSparseGraph<Node, Link> sub= new UndirectedSparseGraph<Node, Link>();
			FatTreeL2 fl2= new FatTreeL2();
			fl2.createGraph();
			sub = fl2.getFatTreeL2Graph();
			substrate.setGraph(sub);
			substrate.print();
			substrate.setNodeFactory(fl2.getNodeFactory());
			substrate.setLinkFactory(fl2.getLinkFactory());		
			substrates.add(substrate);

		
		}
		
		return substrates;
 }
 
 
 //pathlets
 public static Substrate createSubGraph(int pop_num){
	 Substrate finalSubstrate = new Substrate("substrate");
	 final ArrayList<Substrate> substrates = new ArrayList<Substrate>();
		
		for (int k=0; k<pop_num; k++) {
		Graph<Node, Link> sub= new DirectedSparseGraph<Node, Link>();
	    DirectedFatTreeL3 fl3= new DirectedFatTreeL3();
		fl3.createFTGraph();
		
	
		Substrate substrate = new Substrate("sub"+k);
		sub = fl3.getFatTreeL3Graph();
		SubstrateNodeFactory nodeFactory = fl3.getNodeFactory();
		SubstrateLinkFactory linkFactory = fl3.getLinkFactory();
		
		substrate.setNodeFactory(nodeFactory);
		substrate.setLinkFactory(linkFactory);	
		substrate.setGraph(sub);
		substrate.print();
		
		fl3.createPaths();

		
		
	
	//	System.out.println(fl3.getPaths());
		//substrate.addPods(fl3.podNum());
		substrate.setFTL3(fl3);
		
		//fl3.createHyperGraph();

		
		substrates.add(k, substrate);
		
		}

		finalSubstrate= substrates.get(0);
		
		//finalSubstrate.setHGraph((Hypergraph<Node, Link>) substrates.get(0).getGraph());
	  
	 return finalSubstrate;
 }
 
	public static List<Request>  createFG(int numRequests){
		final String prefix ="req";
		final String timeDistribution = SimulatorConstants.POISSON_DISTRIBUTION;
		final int fixStart=0;
		final int uniformMin=0;
		final int uniformMax=0;
		final int normalMean=0;
		final int normalVariance=0;
		final int lifetime_max=30000;
		
		final List<Request> requests = new ArrayList<Request>();
		

		int startDate = 0;
        	int lifetime = 0;
        	int sum=0;

        	
        	Exponential exp_arr = null;
        	Exponential exp = null;

        	if (timeDistribution.equals(SimulatorConstants.POISSON_DISTRIBUTION)) {
        		Random rand = new Random();
        		int  n = rand.nextInt(400) + 100;
        		MersenneTwister engine = new MersenneTwister(n);
	            exp_arr = new Exponential(0.04,engine);
	        	exp= new Exponential(0.001,engine);
        	}
        	
        	for (int i=0; i<numRequests; i++) {       		
			Request request = new Request(prefix+i);
			DirectedFG requestSG = new DirectedFG();
			
			requestSG.createGraph();
			request.setGraph(requestSG.getDirectedFG());
			request.setNodeFactory(requestSG.getNodeFactory());
			request.setLinkFactory(requestSG.getLinkFactory());
			
			 Uniform uni1= new Uniform(0,1);
			 double thr =uni1.random(); 
			 request.setTrustThr(thr);
			
			// Duration of the request
			lifetime= exp.nextInt();
			//jdistlib.Exponential expo = new jdistlib.Exponential(0.001);
			//System.out.println("lifetime: " +lifetime );
			//lifetime = exp.nextInt();
			// All requests start at month fixStart
			if (timeDistribution.equals(SimulatorConstants.FIXED_DISTRIBUTION)) {
				request.setStartDate(fixStart);
				request.setEndDate(fixStart+lifetime);
			}
			
			// Random: Uniform distribution
			if (timeDistribution.equals(SimulatorConstants.UNIFORM_DISTRIBUTION)) {
				startDate = uniformMin + (int)(Math.random()*((uniformMax - uniformMin) + 1));
				request.setStartDate(startDate);
				request.setEndDate(startDate+lifetime);
			}
			
			// Random: Normal distribution
			if (timeDistribution.equals(SimulatorConstants.NORMAL_DISTRIBUTION)) {
				Random random = new Random();
				startDate = (int) (normalMean + random.nextGaussian() * normalVariance);
				if (startDate<0)
					startDate*=-1;
				request.setStartDate(startDate);
				request.setEndDate(startDate+lifetime);
			}

			// Random: Poisson distribution
			if (timeDistribution.equals(SimulatorConstants.POISSON_DISTRIBUTION)) {
				startDate = exp_arr.nextInt();
				sum=sum+startDate;
				if (lifetime==0)
					lifetime=lifetime+1;
				request.setStartDate(sum);
				//request.setEndDate(sum+lifetime);
				request.setEndDate(lifetime_max );
			}
			//System.out.println("request starttime:" +request.getStartDate());
		    //System.out.println("request lifetime:" +request.getEndDate());
			
			//request.print();
			requests.add(request);
		}
     
      //  System.exit(0);
		return requests;
		
	}

 
 public static Substrate createInPGraph(int inp_no){
	 
	 	
	 	final String prefix ="inps";
		final Substrate substrate = new Substrate(prefix);
		//Number of nodes in the Infrastructure Provider
		int numNodes = inp_no;
		//Probability of a connection
		double linkProbability = 0.5;	
		SparseMultigraph<Node, Link> g = null;	
		SubstrateNodeFactory nodeFactory = new SubstrateNodeFactory();
		SubstrateLinkFactory linkFactory = new SubstrateLinkFactory();
			
		//Random Graph Generation
		Factory<UndirectedGraph<Node, Link>> graphFactory = new Factory<UndirectedGraph<Node, Link>>() {
			public UndirectedGraph<Node, Link> create() {
				return new NetworkGraph();
			}
		};
				
		//ErdosRenyiGenerator generation
		ErdosRenyiGenerator<Node, Link> randomGraph = 
				new ErdosRenyiGenerator<Node, Link>(graphFactory, nodeFactory, 
						linkFactory, numNodes, linkProbability );
		g = (SparseMultigraph<Node, Link>) randomGraph.create();
		//Remove unconnected nodes
		((NetworkGraph) g).removeUnconnectedNodes();
		// TODO remove disconnected graphs
		WeakComponentClusterer<Node, Link> wcc = new WeakComponentClusterer<Node, Link>();
		Set<Set<Node>> nodeSets = wcc.transform(g);
		Collection<SparseMultigraph<Node, Link>> gs = FilterUtils.createAllInducedSubgraphs(nodeSets, g); 
		if (gs.size()>1) {
				@SuppressWarnings("rawtypes")
				Iterator itr = gs.iterator();
				g = (NetworkGraph)itr.next();
				while (itr.hasNext()) {
				@SuppressWarnings("unchecked")
				SparseMultigraph<Node, Link> nextGraph = (SparseMultigraph<Node, Link>) itr.next();
			
				if (nextGraph.getVertexCount()>g.getVertexCount())
						g = (NetworkGraph)nextGraph;
				}
		}
	    
		if (g.getVertexCount()>0){
				// Change id of nodes to consecutive int (0,1,2,3...)
				@SuppressWarnings("rawtypes")
				Iterator itr = g.getVertices().iterator();
				int id = 0;
				while (itr.hasNext()) {
					((Node) itr.next()).setId(id);
					id++;
				}
				// refresh nodeFactory's nodeCount
				nodeFactory.setNodeCount(id);
				// Change id of edges to consecutive int (0,1,2,3...)
				itr = g.getEdges().iterator();
				id = 0;
				while (itr.hasNext()) {
					((Link) itr.next()).setId(id);
					id++;
				}
				// refresh linkFactory's linkCount
				linkFactory.setLinkCount(id);
		}
		
		if  ((g.getVertexCount()==inp_no)){
			substrate.setGraph(g);
			substrate.setNodeFactory(nodeFactory);
			substrate.setLinkFactory(linkFactory);
		}
		else{			
			createInPGraph(inp_no);
		}
		
		final String pre_fix ="sub";
				int i=0;
		for (Node node: substrate.getGraph().getVertices()){
			node.setName(pre_fix+i);
			i++;
		}
		
		return substrate;
}


 
 





 
}
