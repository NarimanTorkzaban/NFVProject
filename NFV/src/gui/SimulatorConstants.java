package gui;

public class SimulatorConstants {

	// Substrate, Algorithm and Request status constants
	public static final String STATUS_AVAILABLE = "available";
	public static final String STATUS_ASSIGNED = "assigned";
	public static final String STATUS_REJECTED = "rejected";
	public static final String STATUS_READY = "ready";
	
	// Requests constants
	public static final String RANDOM_REQUEST = "random";
	public static final String DESIGN_REQUEST = "design";
	public static final String IMPORT_REQUEST = "import";
	// Substrate constants
	public static final String RANDOM_SUBSTRATE = "random";
	public static final String DESIGN_SUBSTRATE = "design";
	public static final String IMPORT_SUBSTRATE = "import";
	
	// Time distribution constants
	public static final String FIXED_DISTRIBUTION = "fixed";
	public static final String UNIFORM_DISTRIBUTION = "uniform";
	public static final String NORMAL_DISTRIBUTION = "normal";
	public static final String POISSON_DISTRIBUTION = "poisson";
	
	public static final double MAX_DIS_RRH_BBU=10.0;
	public static final int NO_IXPS=1;
	public static final double AREA=9;
	public static final double OVERLAPPING_FACTOR=1.2;

	
	// Link connectivity constants
	public static final String LINK_PER_NODE_CONNECTIVITY = "linkPerNode";
	public static final String PERCENTAGE_CONNECTIVITY = "percentage";
	
	// Resource parameters constants
	// CPU
	public static final int RACK_SERVER_CPU = 16000000; //16*2*1000000 kcycles
	public static final int MAX_CPU = 100;
	public static final int MIN_CPU = 50;
	public static final int MAX_CPU_REQUEST = 20;
	public static final int MIN_CPU_REQUEST = 1;
	// Bandwidth
	public static final int INTER_DC_BW = 100000000;
	public static final int TOR_BW = 8000000; //kbps -> 4GBps
	public static final int INTER_RACK_BW = 16000000; //16Gbbps
	public static final int MAX_BW = 100;
	public static final int MIN_BW = 50;
	public static final int MAX_BW_REQUEST = 50;
	public static final int MIN_BW_REQUEST = 1;
	// Memory
	public static final int RACK_SERVER_MEMORY = 16;
	public static final int MAX_MEMORY = 100;
	public static final int MIN_MEMORY = 50;
	public static final int MAX_MEMORY_REQUEST = 20;
	public static final int MIN_MEMORY_REQUEST = 1;
	// Disk space
	public static final int RACK_SERVER_DISK = 100;
	public static final int MAX_DISK = 100;
	public static final int MIN_DISK = 50;
	public static final int MAX_DISK_REQUEST = 20;
	public static final int MIN_DISK_REQUEST = 1;
	// VLANs
	public static final int MAX_SWITCH_VLANS = 4096;
	public static final int MAX_ROUTER_VLANS = 4096;
	public static final int MAX_SERVER_VLANS = 4096;
	public static final int MIN_VLANS = 0;
	public static final int MAX_VLANS_REQUEST = 20;
	public static final int MIN_VLANS_REQUEST = 1;
	
	// Router constants
	public static final int MAX_LOGICAl_IFACES_ROUTER = 4000;
	public static final int MAX_LOGICAL_INSTANCES =15;
	
	// Switch constants
	public static final int MAX_LOGICAl_IFACES_SWITCH = 4000;
	public static final int MAX_SWITCH_TCAM = 1000;
	
	// Server constants
	public static final int MAX_LOGICAl_IFACES_SERVER = 4000;
	public static final int NODE_MAX_CAPACITY_TYPES = 3;
}
