package solver;
import algorithm.*;
import com.artelys.knitro.api.*;
//import com.artelys.knitro.examples.Problems.*;
//import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class NFVSolver {

	private static final Class<? extends KTRProblem> KTRProblems[] = new Class[]{
			ProblemVirtualization.class
	};
	
	//ProblemVirtualization prb = new ProblemVirtualization();

	public static void main(String[] args) throws Exception {
		
            System.out.println(new String(new char[50]).replace("\0", "="));
            System.out.println("Solving "+ProblemVirtualization.class.getSimpleName());

            KTRIProblem instance = generateInstance(ProblemVirtualization.class);

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
		

	

		
    public static List<KTRIProblem> makeExampleProblemList() {
        List<KTRIProblem> problems = new LinkedList<KTRIProblem>();

        for (Class<? extends KTRIProblem> cpb: KTRProblems) {
            problems.add(generateInstance(cpb));
        }

        return problems;
    }
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


}
