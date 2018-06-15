package algorithm;
import java.util.*;
import model.*;
import com.artelys.knitro.api.KTRConstants;
import com.artelys.knitro.api.KTREnums;
import com.artelys.knitro.api.KTRProblem;





public class ProblemVirtualization extends KTRProblem {
	
	public int[][] SEdge;
	public int[][] REdge;
	public double[][] demand;
	public double[][] c;
	public double[] g;
	public double[] r;
	public int numS;
	public int numR;
	public int redgeSize, sedgeSize;
	public double thresh;
	public double[] trust;
	public double sumDemand;

	
	static int val1 =240;
	static int val2=39;
	static int val3 =205;
	static int val4=15;
	static int[]  prob_vars = initProbVariabes();
    public ProblemVirtualization() {
    	
    	super(prob_vars[0],prob_vars[1],prob_vars[2],prob_vars[3]);
      //  super(240, 39, 205, 15);
        setObjectiveProperties();
        setVariableProperties();
        setConstraintProperties();
        setDerivativeProperties();
        
    }

    private void setObjectiveProperties() {
        setObjType(KTREnums.ObjectiveType.ObjGeneral.getValue());
        setObjFnType(KTREnums.FunctionType.Convex.getValue());
        setObjGoal(KTREnums.ObjectiveGoal.Minimize.getValue());
    }
    private void setConstraintProperties() {
    	int countC=0;
    	
    	//Placement
    	for(int i=0; i<numR; i++) {
    		setConTypes(countC, KTREnums.ConstraintType.ConLinear.getValue());
        	setConLoBnds(countC, 1.0);
        	setConUpBnds(countC, 1.0);
        	setConFnTypes(countC, KTREnums.FunctionType.Convex.getValue());
        	countC++;
    	}
    	
    	//Trust
    	setConTypes(countC, KTREnums.ConstraintType.ConGeneral.getValue());
        setConLoBnds(countC, thresh);
        setConUpBnds(countC, KTRConstants.KTR_INFBOUND);
        setConFnTypes(countC, KTREnums.FunctionType.Convex.getValue());
        countC++;
        
        //Flow Conservation
        for(int i=0; i<numR; i++)
        	for(int j=0; j<numR; j++)
        		if(REdge[i][j]!=0) 
        			for(int u=0; u<numS;u++) {
        				setConTypes(countC, KTREnums.ConstraintType.ConLinear.getValue());
        				setConLoBnds(countC, 0.0);
        				setConUpBnds(countC, 0.0);
        				setConFnTypes(countC, KTREnums.FunctionType.Convex.getValue());
        				countC++;
        			}
        
        //Capacity
        for(int u=0; u<numS; u++) {
        	setConTypes(countC, KTREnums.ConstraintType.ConLinear.getValue());
        	setConLoBnds(countC , - KTRConstants.KTR_INFBOUND);
        	setConUpBnds(countC, r[u]);
        	setConFnTypes(countC, KTREnums.FunctionType.Convex.getValue());
        	countC++;
        }
        
        //Capacity
        for(int u=0; u<numS; u++)
        	for(int v=0; v<numS; v++)
        		if(SEdge[u][v]!= 0){
        			setConTypes(countC, KTREnums.ConstraintType.ConLinear.getValue());
        			setConLoBnds(countC, - KTRConstants.KTR_INFBOUND);
        			setConUpBnds(countC, c[u][v]);
        			setConFnTypes(countC, KTREnums.FunctionType.Convex.getValue());	
        			countC++;
        		
        		}
        				
        		
    }
    private void setVariableProperties() {
    	int var =0;
    	setVarLoBnds(0.0);
    	
    	for( int i=0; i<numR; i++)
    		for( int u=0; u<numS; u++){
    			setVarUpBnds(var, 1.0);
    			setVarTypes(var, KTREnums.VariableType.Binary.getValue());
    			var++;
    		}
    	
    	for( int i=0; i<numR; i++)
			for( int j=0; j<numR; j++)
				for( int u=0; u<numS; u++)
					for( int v=0; v<numS; v++) 
						{
							setVarUpBnds(var, KTRConstants.KTR_INFBOUND);
							setVarTypes(var, KTREnums.VariableType.Continuous.getValue());
							var++;
					}
		
    }
    private void setDerivativeProperties() {
    	int countInd=0, countCons=0;
    	
    	//Placement
    	for (int i=0; i<numR; i++) {
    		for (int u=0; u<numS; u++) {
    			setJacIndexCons(countInd, countCons);
    			setJacIndexVars(countInd, i*numS+u);
    			countInd++;
    		}
    		countCons++;
    	}
    	
    	//Trust
    	for (int i=0; i<numR; i++)
    		for (int u=0; u<numS; u++) {
    			setJacIndexCons(countInd,countCons);
    			setJacIndexVars(countInd, i*numS+u);
    			countInd++;
    		}
    	countCons++;
    	
    	//Flow Conservation
    	for(int i=0; i<numR; i++)
    		for(int j=0; j<numR; j++)
    			if(REdge[i][j]!=0)
    				for(int u=0; u<numS;u++) {
    					for(int v=0; v<numS;v++) {
    						if(SEdge[u][v]!=0) {
    							setJacIndexCons(countInd,countCons);
    							setJacIndexVars(countInd, numR*numS+ i*numR*numS*numS+ j*numS*numS+ u*numS+v);
    							countInd++;
    						}
    						if(SEdge[v][u]!=0) {
    							setJacIndexCons(countInd,countCons);
    							setJacIndexVars(countInd, numR*numS+ i*numR*numS*numS+ j*numS*numS+ v*numS+u);
    							countInd++;
    						}
    						
    					}
    					setJacIndexCons(countInd,countCons);
    					setJacIndexVars(countInd, i*numS+u);
    					countInd++;
    					setJacIndexCons(countInd,countCons);
    					setJacIndexVars(countInd, j*numS+u);
    					countInd++;
    					countCons++;
    				}
    	
    	for(int u=0; u<numS; u++) {
    		for(int i=0; i<numR; i++) {
    			setJacIndexCons(countInd, countCons);
    			setJacIndexVars(countInd, i*numS+u);
    			countInd++;
    		}
    		countCons++;	
    	}
    	
    	for(int u=0; u<numS; u++)
    		for(int v=0; v<numS; v++)
    			if(SEdge[u][v]!=0) {
    				for(int i=0; i<numR; i++)
    					for(int j=0; j<numR; j++)
    						if(REdge[i][j]!=0) {
    							setJacIndexCons(countInd, countCons);
    							setJacIndexVars(countInd, numR*numS+ i*numR*numS*numS+ j*numS*numS+ u*numS+v);
    							countInd++;
    						}
    				countCons++;
    			}
    	
    	
    	for (int i=0; i<numR; i++)
			for (int u=0; u<numS; u++)
					setHessIndexRows(i*numS + u, i*numS  + u);
	
    	for (int i=0; i<numR; i++)
			for (int u=0; u<numS; u++)   	
					setHessIndexCols(i*numS + u, i*numS  + u);   	

    
    	
    }

    
    
    
    public double evaluateFC(List<Double> x, List<Double> c, List<Double> objGrad, List<Double> jac) {
    	
    	double obj=0;
    	int consCount =0;
    	double[] cons01 = new double[numR];
    	double cons02 =0;
    	double[] cons03 =new double[numR*numR*numS]; 
    	double[] cons04 = new double[numS];
    	double cons05 =0.0;
    	
    	for(int i=0; i<numR; i++)
    		for(int u=0; u<numS;u++)
    			obj += (1.0/trust[u])*x.get(i*numS+u);
    	for(int i=numR*numR; i<x.size(); i++)
    		obj+= (1.0/sumDemand)*x.get(i);
    	
    	//placement cons.
    	for(int i=0; i<numR; i++) {
    		for(int u=0; u<numS; u++) {
    			cons01[i] += x.get(i*numS + u);
    		}
    		c.set(consCount,cons01[i]);
    		consCount++;
    	}
    	
    	//trust cons.	
    	for( int i=0; i< numR; i++)
    		for(int u=0; u< numS; u++)
    			cons02 += Math.log(trust[u]*x.get(i*numS+ u) + 1 - x.get(i*numS+ u));
    	
    	c.set(consCount, cons02);
    	consCount++;
    	
    		
	//flow conversation cons.
	for( int i=0; i< numR; i++)
		for( int j=0; j< numR; j++)
			if(REdge[i][j]!= 0)
    			for(int u=0; u< numS; u++) {
    				
    				for(int v=0; v< numS; v++) {
    					if(SEdge[u][v]!=0)
    						cons03[i*numR*numS+ j*numS+ u] += 
    														 x.get(numR*numS+ i*numR*numS*numS+ j*numS*numS + u*numS + v);
    														
    				
    					if(SEdge[v][u]!=0)
    						cons03[i*numR*numS+ j*numS+ u] -=x.get(numR*numS+ i*numR*numS*numS+ j*numS*numS + v*numS + u);
    														
    				}
    				c.set(consCount, cons03[i*numR*numS+ j*numS+ u] -demand[i][j]*x.get(i*numS +u)+ demand[i][j]*x.get(j*numS +u));
    				consCount++;
    			}  	
    	
    	
    	//capacity cons.
    	for(int u=0; u<numS; u++) {
    		for(int i=0; i<numR; i++)
    			cons04[u] += g[i]*x.get(i*numS+ u);
    		c.set(consCount, cons04[u]);
    		consCount++;
    	
    	}
    	
    	//capacity cons.
    	for(int u=0; u< numS; u++)
    		for(int v=0; v< numS; v++) {
    			cons05=0;
    			if(SEdge[u][v]!= 0){
    				for( int i=0; i< numR; i++)
    					for( int j=0; j< numR; j++) 
    						cons05 += x.get(numR*numS+ i*numR*numS*numS+ j*numS*numS + u*numS + v);
    			}
    		}
    	

    	
    	return obj;
    	
    }
    public int evaluateGA( List<Double> x,  List<Double> objGrad, List<Double> jac) {
    	
    	for(int i=0; i<numR; i++)
    		for(int u=0; u<numS;u++)
    			objGrad.set(i*numS+u, (1.0/trust[u]));
    	
    	for(int i=numR*numR; i<x.size(); i++)
    		objGrad.set(i, 1.0/sumDemand);
    	
    	
    	int countInd=0;
    	
    	for (int i=0; i<numR; i++) {
    		for (int u=0; u<numS; u++) {
    			jac.set(countInd, 1.0);
    			countInd++;
    		}
    		
    	}
    	
    	for (int i=0; i<numR; i++)
    		for (int u=0; u<numS; u++) {
    			jac.set(countInd, (trust[u]-1)/((trust[u]*x.get(i*numS+u)+ 1- x.get(i*numS+u))));
    			countInd++;
    			
    		}
    	
    	
    	for(int i=0; i<numR; i++)
    		for(int j=0; j<numR; j++)
    			if(REdge[i][j]!=0)
    				for(int u=0; u<numS;u++) {
    					for(int v=0; v<numS;v++) {
    						if(SEdge[u][v]!=0) {
    							jac.set(countInd, 1.0);
    							countInd++;
    						}
    						if(SEdge[v][u]!=0) {
    							jac.set(countInd, -1.0);
    							countInd++;
    						}
    						
    					}
    					
    					jac.set(countInd, -demand[i][j]);
    					countInd++;
    					jac.set(countInd, demand[i][j]);
    					countInd++;
    					
    					
    				}
    	
    	for(int u=0; u<numS; u++) {
    		for(int i=0; i<numR; i++) {
    			jac.set(countInd, g[i]);
    			countInd++;
    		}
    			
    	}
    	
    	for(int u=0; u<numS; u++)
    		for(int v=0; v<numS; v++)
    			if(SEdge[u][v]!=0) {
    				for(int i=0; i<numR; i++)
    					for(int j=0; j<numR; j++)
    						if(REdge[i][j]!=0) {
    							jac.set(countInd, 1.0);
    							countInd++;
    						}
    				
    			}
       	
    	return 0;
    }
    public int evaluateHess(List<Double> x, double objScaler, List<Double> lambda, List<Double> hess) {
    	
    	for (int i=0; i<numR; i++)
    		for (int u=0; u<numS; u++)    	
    			hess.set(i*numS+ u, - lambda.get(0)*((trust[u]-1)/(Math.pow(trust[u]*x.get(i*numS+ u) + 1 - x.get(i*numS+ u), 2))));

    	return 0;
    }
    public int evaluateHessianVector( List<Double> x, double objScaler, List<Double> lambda, List<Double> hessVector) {
    	
    	double[] tmpVec = new double[numR*numS];
        for (int i=0; i<numR; i++)
        	for (int u=0; u<numS; u++)
        		tmpVec[i*numS+ u] = ( - lambda.get(0)*(
						(trust[u]-1)/
						(Math.pow(trust[u]*x.get(i*numS+ u) + 1 - x.get(i*numS+ u), 2))
						)
						)*hessVector.get(i*numS+ u);
        	
        
        for (int i=0; i<numR; i++)
        	for (int u=0; u<numS; u++)
        		hessVector.set(i*numS+ u, tmpVec[i*numS+ u]);
        
    	for (int i=0; i<numR; i++)
    		for (int j=0; j<numR; j++)
    			for (int u=0; u<numS; u++)
    				for (int v=0; v<numS; v++)   
    					hessVector.set(numR*numS+ i*numR+ j*numS+ u*numS+ v, 0.0);
        
        ////////////////////////////////////////THIS IS NOT COMPLETED!!!!!!!!!//////////////////////////////
    	
    	return 0;
    }
    
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

    static int[]  initProbVariabes(){
    	
    	
		int[] prob_vars = new int[4];
		
		return prob_vars;
		
    }
}
