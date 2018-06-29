import java.util.Random;

public class SFC {
	private int  numS= 5;
	private int  numR= 3;
	
	public SFC(int numS, int numR) {
		this.numS = numS;
		this.numR = numR;
		
	}
	Random rand = new Random(100);
	
	int[][] createRGraph(){
			
		//Request Edges(ij)
		int[][] REdge= new int[numR][numR];
		
		
		for (int i=0; i<numR; i++)
			for(int j=0; j<numR; j++) {
				if(i==j) REdge[i][j]=0;
				else REdge[i][j]= rand.nextInt(2);
			}
		return REdge;
		
	}
	int [][] REdge = createRGraph();
	
	double sum=0;
	
	
	int[][] createSGraph(){
		
		//Request Edges(ij)
		int[][] SEdge= new int[numS][numS];
		
		
		for (int i=0; i<numS; i++)
			for(int j=0; j<numS; j++) {
				if(i==j) 
					SEdge[i][j]=0;
				else 
					SEdge[i][j]= rand.nextInt(2);
			}
		return SEdge;
	
	}
	int[][] SEdge = createSGraph();
	
	double[][] setBWDemand(){
		
		double[][] demand= new double[numR][numR];
		for (int i=0; i<numR; i++)
			for(int j=0; j<numR; j++) {
				if (REdge[i][j]!=0) {
				demand[i][j]= REdge[i][j]*(20*rand.nextDouble()+10);
				sum+=demand[i][j];
				}
			}	
		return demand;
	}
	double [][] demand = setBWDemand();
	
	
	
	double [] setCPUDemand(){
		//CPU demand gi
		double[] G= new double[numR];
		for( int i=0; i<numR; i++) {
			G[i]= (rand.nextDouble()*20 +20);
		}
		return G;
	}
	double [] G = setCPUDemand();

	
	
	
	double [] setSubCPU() {
		double[] R= new double[numS];
		for( int i=0; i<numS; i++) {
			R[i]= 20* rand.nextDouble()+45;
		}
		return R;
	}
	double[] R = setSubCPU();
	
	
	
	
	
	
	double[][] setSubBW(){
		double[][]C= new double[numS][numS];
		for (int u=0; u<numS; u++)
			for(int v=0; v<numS; v++) {
				if (u!=v && SEdge[u][v]!=0)
				C[u][v]= 50* rand.nextDouble()+30;
			}
		return C;
				
	}
	double [][] C = setSubBW();
	
	
	
	
	double thresh = Math.log(0.2);
	double [] setTrust(){
		double[] trust= new double[numS];
		for (int i=0; i<numS; i++)
			trust[i]= ((double)(rand.nextInt(40))+40)/100;
		return trust;
	}
	double[] trust = setTrust();
	
	
	
	
	
	
	int getCount(int[][] arr) {
		int count=0;
		for(int i=0; i<arr.length; i++)
			for(int j=0 ; j<arr[i].length; j++)
				if(arr[i][j]!=0)
					count++;
		return count;
				
	}
	int redgeSize = getCount(REdge);
	int sedgeSize = getCount(SEdge);
	
	
	int[] findSuperVals() {
		int[] superVals = new int[4];
		superVals[0] = (numR*numS + numR*numS*numR*numS);
		superVals[1] =numR +1 +redgeSize*numS +sedgeSize + numS;
		int countInd=0; 
    	for(int i=0; i<numR; i++)
    		for(int j=0; j<numR; j++)
    			if(REdge[i][j]!=0)
    				for(int u=0; u<numS;u++) {
    					for(int v=0; v<numS;v++) {
    						if(SEdge[u][v]!=0) 
    							countInd++;
    						if(SEdge[v][u]!=0) 
    							countInd++;
    					}
    					
    					countInd+=2;
    				}
    	superVals[2] = numR*numS +numR*numS +countInd + numR*numS +redgeSize*sedgeSize; 
    	superVals[3] = numR*numS;
    	for(int i=0; i<4; i++) {
    		System.out.print(superVals[i]);
    		System.out.print('\t');
    	}
    	System.out.println(7*5 + 35*35);
		return superVals;
	}
	
	
	
	public void printAlive() {
		System.out.println("Alive!");
	}
	
	
	
}
