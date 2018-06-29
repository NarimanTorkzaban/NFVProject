import com.artelys.knitro.api.*;
import com.artelys.knitro.examples.Problems.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TrustFirstSolver {


	public static void main(String[] args) throws Exception {

		int numR = 3, numS=4;
		int [][] REdge = createRGraph(numR);
		int[][] SEdge = createSGraph(numS);
		double [][] demand = setBWDemand(numR, REdge);
		double [] G = setCPUDemand(numR);
		double[] R = setSubCPU(numS);
		double [][] C = setSubBW(numS, SEdge);
		double[] trust = setTrust(numS);
		double[] reqTrust = setReqTrust(numR);
		int redgeSize = getCount(REdge);
		int sedgeSize = getCount(SEdge);
		double thresh = Math.log(0.02);
		int[] problem_vars = new int[4];
		double sum = getSumDemand(demand);
		
		
		problem_vars[0] = (numR*numS + numR*numS*numR*numS);
		problem_vars[1] =numR +numR +getCount(REdge)*numS +getCount(SEdge) + numS;
		
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
		problem_vars[2] = numR*numS +numR*numS +countInd + numR*numS +getCount(REdge)*getCount(SEdge); 
		problem_vars[3] = numR*numS;
		
		
		/////////////////////////////////////////////////////////////////////////////
		
		
		TrustFirst instance = new TrustFirst(problem_vars, SEdge, REdge, demand, C, G, R, numS, numR, trust, thresh, sum, reqTrust);
		

		System.out.print("Substrate Graph");
		for(int i=0; i<numS; i++) {
			System.out.println();
			for(int j=0; j<numS; j++) {
				System.out.print(SEdge[i][j]);
				System.out.print("\t");
			}
		}
			
		System.out.println("\n");
		
		System.out.print("Request Graph");
		for(int i=0; i<numR; i++) {
			System.out.println();
			for(int j=0; j<numR; j++) {
				System.out.print(REdge[i][j]);
				System.out.print("\t");
			}
		}
			
		System.out.println("\n");
		
		System.out.print("Bandwidth Demand");
		for(int i=0; i<demand.length; i++) {
			System.out.println();
			for(int j=0; j<demand.length; j++) {
				System.out.print(demand[i][j]);
				System.out.print("\t");
			}
		}
		
		System.out.println("\n");
		
		System.out.print("Substrate Bandwidth ");
		for(int i=0; i<C.length; i++) {
			System.out.println();
			for(int j=0; j<C.length; j++) {
				System.out.print(C[i][j]);
				System.out.print("\t");
			}
		}
		System.out.println("\n");
		
		System.out.println("Request CPU ");
		for(int i=0; i<G.length; i++) 
			System.out.print(G[i] + "\t");
	
		System.out.println("\n");
		System.out.println("Substrate CPU ");
		for(int i=0; i<R.length; i++) 
			System.out.print(R[i] + "\t");
		
		System.out.println("\n");
		
		System.out.println("Trust Natrix");
		
		for(int i=0; i<trust.length; i++)
			System.out.print(trust[i]+ "\t");
		
		System.out.println("\n SumDemand");
		System.out.println(sum);
		
//        System.out.println(instance.getJacIndexCons());
//		System.out.println(instance.getJacIndexVars());
	
        // Create a new solver
        KTRISolver solver;
        solver = new KTRSolver(instance);


        // Remove all outputs
        solver.setParam(KTRConstants.KTR_PARAM_OUTLEV, KTRConstants.KTR_OUTLEV_NONE);

        // Solve problem
        int result = solver.solve();

        // Display results
        printSolutionResults(solver, result, numS, numR);
        

	}
		

	public static int[][] createSGraph(int numS){
		
		//Request Edges(ij)
		int[][] SEdge= new int[numS][numS];
		Random rand = new Random();
		
		for (int i=0; i<numS; i++)
			for(int j=0; j<numS; j++) {
				if(i==j) 
					SEdge[i][j]=0;
				else 
					SEdge[i][j]= rand.nextInt(2);
			}
		return SEdge;
	
	}
	
	public static int[][] createRGraph( int numR){
		
		//Request Edges(ij)
		int[][] REdge= new int[numR][numR];
		
		Random rand = new Random();
		for (int i=0; i<numR; i++)
			for(int j=0; j<numR; j++) {
				if(i==j) REdge[i][j]=0;
				else REdge[i][j]= rand.nextInt(2);
			}
		return REdge;
		
	}

	public static double[][] setBWDemand(int numR, int[][] REdge){
		
		Random rand = new Random();
		double[][] demand= new double[numR][numR];
		for (int i=0; i<numR; i++)
			for(int j=0; j<numR; j++) {
				if (REdge[i][j]!=0) {
				demand[i][j]= REdge[i][j]*(20*rand.nextDouble()+10);
				}
			}	
		return demand;
	}

	public static double getSumDemand(double[][]demand) {
		double sum = 0.0;
		for(int i=0; i<demand.length; i++)
			for(int j=0; j<demand.length; j++)
				sum+= demand[i][j];
		return sum;
	}
	
	
	public static double [] setCPUDemand(int numR){
		Random rand = new Random();
		//CPU demand gi
		double[] G= new double[numR];
		for( int i=0; i<numR; i++) {
			G[i]= (rand.nextDouble()*20 +20);
		}
		return G;
	}
	

	
	
	
	public static double [] setSubCPU(int numS) {
		Random rand = new Random();
		double[] R= new double[numS];
		for( int i=0; i<numS; i++) {
			R[i]= 20* rand.nextDouble()+45;
		}
		return R;
	}
	
	public static double[][] setSubBW(int numS, int[][] SEdge){
		Random rand = new Random();
		double[][]C= new double[numS][numS];
		for (int u=0; u<numS; u++)
			for(int v=0; v<numS; v++) {
				if (u!=v && SEdge[u][v]!=0)
				C[u][v]= 50* rand.nextDouble()+30;
			}
		return C;
				
	}
	
	
	
	public static double [] setTrust(int numS){
		Random rand = new Random();
		double[] trust= new double[numS];
		for (int i=0; i<numS; i++)
			trust[i]= ((double)(rand.nextInt(40))+40)/100;
		return trust;
	}
	
	public static double [] setReqTrust(int numS){
		Random rand = new Random();
		double[] trust= new double[numS];
		for (int i=0; i<numS; i++)
			trust[i]= ((double)(rand.nextInt(20))+20)/100;
		return trust;
	}
	
	
	
	
	
	
	
	public static int getCount(int[][] arr) {
		int count=0;
		for(int i=0; i<arr.length; i++)
			for(int j=0 ; j<arr[i].length; j++)
				if(arr[i][j]!=0)
					count++;
		return count;
				
	}


		
	
    public static void printSolutionResults(KTRISolver solver, int solveStatus, int numS, int numR) throws KTRException {
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
        
        

        for(int i=0; i<numR; i++) {
        	System.out.println();
        	for(int u=0; u<numS; u++)
        		System.out.printf("x[%d][%d] = %1.0f \t", i,u, solver.getXValues(i*numS+ u));
        }
        System.out.println();
        
        for(int i=0; i<numR; i++)
        	for(int j=0; j<numR; j++)
        		for(int u=0; u<numS; u++) {
        			System.out.println();
        			for(int v=0; v<numS; v++)
        				System.out.printf("F[%d][%d][%d][%d] = %.2f \t", i,j,u,v, solver.getXValues(numR*numS + i*numR*numS*numS+ j*numS*numS+ u*numS + v));
        		}
        System.out.println();
        				
        

        
        
    }


}

