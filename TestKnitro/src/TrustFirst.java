import java.util.*;
import com.artelys.knitro.api.KTRConstants;
import com.artelys.knitro.api.KTREnums;
import com.artelys.knitro.api.KTRProblem;





public class TrustFirst extends KTRProblem {
	
	//int[] a = new int[4];
	private int[][] SEdge;
	private int[][] REdge;
	private double[][] demand;
	private double[][] c;
	private double[] g;
	private double[] r;
	private int numS;
	private int numR;
	private int redgeSize, sedgeSize;
	private double thresh;
	private double[] trust;
	private double[] reqTrust;
	private double sumDemand;
	
    public TrustFirst(int [] vars, int[][] SEdge, int[][] REdge, double[][]demand, double[][]C, 
    		double[]G, double[]R, int numS, int numR, double[]trust, double thresh, double sum, double[] reqTrust) {
    	
    	super(vars[0],vars[1],vars[2],vars[3]);
      //  super(240, 39, 205, 15);
    	this.SEdge = SEdge; this.REdge = REdge; this.demand = demand; this.c = C;
        this.g = G; this.r = R; this.numS= numS; this.numR = numR; this.trust = trust;
        this.thresh = thresh; this.sumDemand = sum; this.reqTrust = reqTrust;
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
    	for(int i=0; i<numR; i++) {
	    	setConTypes(countC, KTREnums.ConstraintType.ConGeneral.getValue());
	        setConLoBnds(countC, Math.log(reqTrust[i]));
	        setConUpBnds(countC, KTRConstants.KTR_INFBOUND);
	        setConFnTypes(countC, KTREnums.FunctionType.Convex.getValue());
	        countC++;
    	}
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
    	for (int i=0; i<numR; i++) {
    		for (int u=0; u<numS; u++) {
    			setJacIndexCons(countInd,countCons);
    			setJacIndexVars(countInd, i*numS+u);
    			countInd++;
    			
    		}
    		countCons++;
    	}
    	
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
    	for( int i=0; i< numR; i++) {
    		cons02=0;
    		for(int u=0; u< numS; u++)
    			cons02 += trust[u]*x.get(i*numS + u);
    		c.set(consCount, cons02);
    		consCount++;
    	}
    		
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
    	
    	for(int i=numR*numS; i<x.size(); i++)
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
    			jac.set(countInd, trust[u]);
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
    
    
    public void setSubMatrix(int[][] matrix){
    	this.SEdge = matrix;
    }  
    public void setReqMatrix(int[][] matrix){
    	this.REdge = matrix;
    }
    public void setBWDemand(double[][] matrix){
    	this.demand = matrix;
    }
    public void setSubBW(double[][] matrix){
    	this.c = matrix;
    }
    public void setCPUDemand(double[] matrix){
    	this.g = matrix;
    }
    public void setSubCPU(double[] matrix){
    	this.r = matrix;
    }	
    public void setNumS(int num){
    	this.numS = num;
    }
    public void setNumR(int num){
    	this.numR = num;
    }
    public void setTrust(double[] matrix){
    	this.trust = matrix;
    }	
    public void setThresh(double num){
    	this.thresh = num;
    }
    public void setRedgeSize(int num){
    	this.redgeSize = num;
    }
    public void setSedgeSize(int num){
    	this.sedgeSize = num;
    }
    public void setSum(double num){
    	this.sumDemand = num;
    }

    
    }
