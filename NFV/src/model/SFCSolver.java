package model;

public class SFCSolver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SFC sfc = new SFC(5,3);
		
		
		
		System.out.print("Request Graph:");
		for(int i=0; i<sfc.REdge.length; i++) {
			System.out.println();
			for(int j=0; j<sfc.REdge[i].length; j++)
				System.out.print(sfc.REdge[i][j] + "\t");
		}
			
		System.out.println();
		System.out.println();
		System.out.print("Substrate Graph:");
		for(int i=0; i<sfc.SEdge.length; i++) {
			System.out.println();
			for(int j=0; j<sfc.SEdge[i].length; j++)
				System.out.print(sfc.SEdge[i][j] + "\t");
		}
		
		System.out.print("\n\nBW Demand Matrix:");
		for(int i=0; i<sfc.demand.length; i++) {
			System.out.println();
			for(int j=0; j<sfc.demand[i].length; j++)
				System.out.print(sfc.demand[i][j] + "\t");
		}
		
		System.out.print("\n\nCPU Demand Matrix:\n");
		for(int i=0; i<sfc.G.length;i++)
			System.out.print(sfc.G[i] + "\t");
		
		
		
		
		System.out.print("\n\nSubstrate BW Matrix:");
		for(int i=0; i<sfc.C.length; i++) {
			System.out.println();
			for(int j=0; j<sfc.C[i].length; j++)
				System.out.print(sfc.C[i][j] + "\t");
		}
		
		System.out.print("\n\nSubstrate CPU Matrix:\n");
		for(int i=0; i<sfc.R.length;i++)
			System.out.print(sfc.R[i] + "\t");
		
		
		System.out.println("\n\nTrust Matrix:");
		for(int i=0; i<sfc.trust.length; i++)
			System.out.print(sfc.trust[i] + "\t");
		
		System.out.print("\n\nSum of BW Demands:");
		System.out.println(sfc.sum);
		System.out.println();
		
		sfc.findSuperVals();
		
		
		ProblemVirtualization prb = new ProblemVirtualization ();
		System.out.println(prb.getJacIndexVars());
		System.out.println(prb.getJacIndexCons());
		
		
	}

}
