package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import tools.NodeComparator;
import edu.uci.ics.jung.graph.Graph;
import model.components.Link;
import model.components.Node;

public class NFmappingRounding {
	  private LinkedHashMap<Node,LinkedHashMap<Node, Integer>> nfMap = new LinkedHashMap<Node, LinkedHashMap<Node, Integer>> ();
	  private ArrayList<LinkedHashMap<Node, ArrayList<Node>>> nodeMap = new ArrayList<LinkedHashMap<Node, ArrayList<Node>>> ();
	  private Substrate updatedSub;
	  private double[][][]zVarFinal;
	  private double[][][]zVarFinalInt;
	
	  public void instanceMapping(ArrayList<Node> nfList,ArrayList<Node> subNodes, ArrayList<Request> req,
			  Substrate sub, double[][][] xVar ){
		    int subNodesNum = subNodes.size();
		    this.updatedSub=sub;
		    int numSFCs = req.size();
		    
			for (int u=0;u<subNodesNum;u++){
				for (int i=0;i<nfList.size();i++){
				    int inst =  nfList.get(i).getInstances();
					for(int j=0; j<inst; j++){
						if (xVar[u][i][j]>0){
							LinkedHashMap<Node, Integer> tmp = new LinkedHashMap<Node, Integer>();
							if (this.nfMap.get(subNodes.get(u))!=null){
								tmp=this.nfMap.get(subNodes.get(u));
								if (tmp.get(nfList.get(i))!=null){
									int instances=0;
									if (subNodes.get(u).getType().contains("RRH") ||subNodes.get(u).getType().contains("Router")){
										instances=tmp.get(nfList.get(i))+1;
									} else {
										double requestedCap =0;
										for (LinkedHashMap<Node, ArrayList<Node>> x : this.nodeMap){
											for (Node key : x.keySet() ){
												if (key.getName().replaceAll("\\d","").
														equalsIgnoreCase(nfList.get(i).getName().replaceAll("\\d",""))){
													if (x.get(key).contains(subNodes.get(u))){
														requestedCap =requestedCap+key.getCpu();
													}
												}
											}
										}
										instances=tmp.get(nfList.get(i));
										if (requestedCap>(instances*nfList.get(i).getCpu())){
											instances++;
											if (!(updateSubstrate(subNodes.get(u),nfList.get(i).getCpu())))
												 throw new ArithmeticException("Substrate Node Capacity not updated"); 
										}
									}
									tmp.put(nfList.get(i),instances);
								}
								else{
									 tmp.put(nfList.get(i),1);
								}
							}
							else {
							 tmp.put(nfList.get(i),1);
							 if (!(updateSubstrate(subNodes.get(u),nfList.get(i).getCpu())))
									 throw new ArithmeticException("Substrate Node Capacity not updated"); 
							}
							this.nfMap.put(subNodes.get(u), tmp);
							
						}
					}
				}
			}	
			//this.updatedSub.print();
			
	   }
	  
	  public void updateNFintances(){
		 LinkedHashMap<Node,LinkedHashMap<Node, Integer>> nfMapNew = new LinkedHashMap<Node, LinkedHashMap<Node, Integer>> ();

		  
		  
	  }
	   
	   public void nodeMapping(ArrayList<Request> req,ArrayList<Node> subNodes,
			  Substrate sub, double[][][]zVar, double[][][][][] fVar){
		    int subNodesNum = subNodes.size();
		    int numSFCs = req.size();
		    
		    	this.zVarFinal = new double[subNodesNum][numSFCs][6];
		    	this.zVarFinalInt = new double[subNodesNum][numSFCs][6];
				for (int f=0;f<numSFCs;f++){
					ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.get(f).getGraph());
					int sizeOfSFC= req.get(f).getGraph().getVertexCount();
					LinkedHashMap<Node, ArrayList<Node>> nodeMapSFC =  new  LinkedHashMap<Node, ArrayList<Node>>();
					//System.out.println(sizeOfSFC);
					for (int u=0;u<subNodesNum;u++){
						for(int k=0; k< sizeOfSFC; k++){
							if (zVar[u][f][k]>0.00000000001){
								double flow = 0;
								for (int v=0;v<subNodesNum;v++){
									for(int m=0; m< sizeOfSFC; m++){
										flow = flow+fVar[u][v][f][k][m];
										flow = flow+fVar[v][u][f][m][k];
									}
								}
								zVarFinal[u][f][k]= flow*zVar[u][f][k];
								if (zVarFinal[u][f][k]==0){
									zVarFinal[u][f][k] =zVar[u][f][k];
								}
								
							}
						}
					}
				}
				////System.out.println("###################################");
				for (int f=0;f<numSFCs;f++){
					ArrayList<Node> req_n = (ArrayList<Node>) getNodes(req.get(f).getGraph());
					int sizeOfSFC= req.get(f).getGraph().getVertexCount();
					LinkedHashMap<Node, ArrayList<Node>> nodeMapSFC =  new  LinkedHashMap<Node, ArrayList<Node>>();
					//System.out.println(sizeOfSFC);
					for(int k=0; k< sizeOfSFC; k++){
						double max=0;
						int index= subNodesNum+1;
						for (int u=0;u<subNodesNum;u++){
							////System.out.println("Checking node: " + u + "  "+f+"  " +k+ " " +zVarFinal[u][f][k]);
							if (zVarFinal[u][f][k]>max){
								max=zVarFinal[u][f][k];
								index=u;
							}
							//System.out.println(index+ " max: " +max);
						}		
						for (int u=0;u<subNodesNum;u++){
							if (u==index){
								zVarFinalInt[u][f][k]=1;
							}else
								zVarFinalInt[u][f][k]=0;
						}	
						ArrayList<Node> tmp = new ArrayList<Node>();
						if (nodeMapSFC.containsKey(req_n.get(k))){
							tmp = nodeMapSFC.get(req_n.get(k));
							}
							tmp.add(subNodes.get(index));
							nodeMapSFC.put(req_n.get(k),tmp);	
					}
				if (nodeMapSFC.size()>0){
					//System.out.println(nodeMapSFC);
					this.nodeMap.add(f,nodeMapSFC);
				}
				}
		   
	   }
	   private boolean updateSubstrate(Node node, double cap){
		   for (Node x: this.updatedSub.getGraph().getVertices()){
			   if (x.getId()==node.getId()){
				   //System.out.println(x.getId()+ " " +cap + " "+x.getAvailableCpu());	   
				   double capNew= (x.getAvailableCpu()-cap);
				   //System.out.println((int)capNew);
				   x.setAvailableCpu((int) capNew);
				   //this.updatedSub.getNodes(this.updatedSub.getGraph()).get(x.getId()).
				   	//						setAvailableCpu((int) capNew);
				  // this.updatedSub.getNodes(this.updatedSub.getGraph()).get(x.getId()).
						//setCpu((int) capNew);
				   return true;
			   }
		   }
		   return false;
	   }
	
	   public Substrate getUpdatedSub(){
		   return this.updatedSub;
	   }
	   @SuppressWarnings("unchecked")
	   public ArrayList<Node> getNodes(Graph<Node,Link> t) {
	   	ArrayList<Node> reqNodes =new ArrayList<Node>();

	   	for (Node x: t.getVertices())
	   		reqNodes.add(x);

	   	Collections.sort(reqNodes,new NodeComparator());

	   	return reqNodes;
	   }
	   
	   public LinkedHashMap<Node,LinkedHashMap<Node, Integer>>  getNFMap(){
		   return this.nfMap;
	   }
	   public ArrayList<LinkedHashMap<Node, ArrayList<Node>>> getNodeMap(){
		   return this.nodeMap;
	   }
	   

	   public double[][][] getNodeMappingTable(){
		   return this.zVarFinalInt;
	   }
/*	   public LinkedHashMap<Node, Integer> getInstanceMap(){
		   return this.instanceNum;
	   }*/
	   
}
