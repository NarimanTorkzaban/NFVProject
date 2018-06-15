package model;

import java.util.Random;

import gui.SimulatorConstants;
import model.components.Link;
import model.components.SubstrateLink;

import org.apache.commons.collections15.Factory;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * This class is a factory of SubstrateLink. It generates the elements
 * with random parameters. Ranges for randomness can be found on 
 * SimulatorConstants class
 */
public class SubstrateLinkFactory implements Factory<Link> {
	
	private int linkCount = 0;
	
	public SubstrateLinkFactory() {
		super();
		linkCount = 0;
	}
	
	public SubstrateLink create() {
		/*
		 //old code
		// Random bandwidth
		int bandwidth = SimulatorConstants.MIN_BW 
				+ (int)(Math.random()*((SimulatorConstants.MAX_BW 
				- SimulatorConstants.MIN_BW) + 1));
				*/
		int bandwidth = SimulatorConstants.INTER_DC_BW;
		SubstrateLink link = new SubstrateLink(linkCount,bandwidth);
		link.setAvailableBandwidth(bandwidth);
		linkCount++;
		return link;
	}
	
	public SubstrateLink create(String linkType) {
		// Random bandwidth
		int bandwidth = SimulatorConstants.MIN_BW 
				+ (int)(Math.random()*((SimulatorConstants.MAX_BW 
				- SimulatorConstants.MIN_BW) + 1));
		
		if (linkType.equalsIgnoreCase("torlink")) {
		Random rand = new Random();
		int  n = rand.nextInt(400) + 100;
		MersenneTwister generator = new MersenneTwister(n);
		Uniform myUniformDist = new Uniform(generator);
		//double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
		double util=1;
		bandwidth = (int) (util*SimulatorConstants.TOR_BW);
	//	System.out.println("aaaaaaaaa: " + util+ "  " +bandwidth);
		}
		else if (linkType.equalsIgnoreCase("interracklink")) {
		Random rand = new Random();
		int  n = rand.nextInt(400) + 100;
		MersenneTwister generator = new MersenneTwister(n);
		Uniform myUniformDist = new Uniform(generator);
		//double util = myUniformDist.nextDoubleFromTo(0.4,0.7);
		double util =1;
		bandwidth = (int) (util* SimulatorConstants.INTER_RACK_BW);
		}
		else if (linkType.equalsIgnoreCase("interdclink")) {
			bandwidth = SimulatorConstants.INTER_DC_BW;
		}else if (linkType.equalsIgnoreCase("dummy")) {
			bandwidth = Integer.MAX_VALUE;
		}
		SubstrateLink link = new SubstrateLink(linkCount,bandwidth);
		link.setAvailableBandwidth(bandwidth);
		
		linkCount++;
		return link;
	}
	
	public Object getCopy() {
		SubstrateLinkFactory f = new SubstrateLinkFactory();
		f.linkCount = this.linkCount;
		return f;
	}

	public int getLinkCount() {
		return linkCount;
	}

	public void setLinkCount(int linkCount) {
		this.linkCount = linkCount;
	}
	
	
	
}