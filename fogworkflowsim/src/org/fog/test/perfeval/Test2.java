package org.fog.test.perfeval;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import javax.jws.soap.SOAPBinding.ParameterStyle;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicyObO;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.fog.entities.Controller;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.ClassType;
import org.workflowsim.utils.ReplicaCatalog;

/**
 * Simulation setup for case study
 * @author F
 *
 */
public class Test2 {
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	
	static int numOfDepts = 1;
	static int numOfMobilesPerDept = 1;
	
	public static void main(String[] args) {

		Log.printLine("Starting Task...");

		try {
			//Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "workflow"; // identifier of the application
			
			//FogBroker broker = new FogBroker("broker");
			
			createFogDevices(1,appId);//(broker.getId(), appId);
						
			List<? extends Host> hostlist = new ArrayList<Host>();
			int hostnum = 0;
			for(FogDevice device : fogDevices){
				hostnum += device.getHostList().size();
				hostlist.addAll(device.getHostList());
			}
			int vmNum = hostnum;//number of vms;
			
			String daxPath = "D:\\dax\\Montage_100.xml";
            File daxFile = new File(daxPath);
            if (!daxFile.exists()) {
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }
			
            /**
             * Since we are using MINMIN scheduling algorithm, the planning
             * algorithm should be INVALID such that the planner would not
             * override the result of the scheduler
             */
            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.GA;
            Parameters.Optimization opt_objective = Parameters.Optimization.Time;
            
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.SHARED;
            /**
             * No overheads
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);

            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, opt_objective,
                    pln_method, null, 0);
            ReplicaCatalog.init(file_system);

            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create a list of VMs.The userId of a vm is basically the id of
             * the scheduler that controls this vm.
             */
            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum(), hostlist);

            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);

            Controller controller = new Controller("master-controller", fogDevices, wfEngine);
            
            /**
             * Binds the data centers with the scheduler.
             */
            for(FogDevice fogdevice:controller.getFogDevices()){
            	wfEngine.bindSchedulerDatacenter(fogdevice.getId(), 0);
            	List<PowerHost> list = fogdevice.getHostList();  //输出设备上的主机
            	System.out.print(fogdevice.getName()+": ");
            	for (PowerHost host : list){
            		System.out.print(host.getId()+",");
            	}
            	System.out.println();
            }
            
            printdevices();
            
			
			CloudSim.startSimulation();

			List<Job> outputList0 = wfEngine.getJobsReceivedList();
            CloudSim.stopSimulation();
            Log.enable();
            printJobList(outputList0);
            controller.print();

			Log.printLine("Simulation finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the fog devices in the physical topology of the simulation.
	 * @param userId 
	 * @param appId
	 */
	private static void createFogDevices(int userId, String appId) {
		
		double GHzList[]={1.6};//云中的主机数，规模有大、中、小
		double costList[]={0.12};
		double cost = 5.0; // the cost of using processing in this resource每秒的花费
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.1; // the cost of using storage in this resource
		double costPerBw = 0.2;//每带宽的花费

		for(int i=0;i<GHzList.length;i++)
		{
			FogDevice cloud = createFogDevice("cloud", 5, GHzToMips(GHzList[i]), 
					40000, 100, 10000, 0, costList[i], 16*103, 16*83.25,cost,costPerMem,costPerStorage,costPerBw); // creates the fog device Cloud at the apex of the hierarchy with level=0
			cloud.setParentId(-1);
			
			fogDevices.add(cloud);
		}
		for(int i=0;i<numOfDepts;i++){
			addGw(i+"", userId, appId, fogDevices.get(0).getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
		}
	}


	private static FogDevice addGw(String id, int userId, String appId, int parentId){
		double cost = 3.0; // the cost of using processing in this resource每秒的花费
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.1; // the cost of using storage in this resource
		double costPerBw = 0.1;//每带宽的花费
		
		FogDevice dept = createFogDevice("d-"+id, 2, GHzToMips(1.3), 4000, 10000, 10000, 1, 0.07, 700, 30,cost,costPerMem,costPerStorage,costPerBw);
		fogDevices.add(dept);
		dept.setParentId(parentId);
		dept.setUplinkLatency(4); // latency of connection between gateways and proxy server is 4 ms
		for(int i=0;i<numOfMobilesPerDept;i++){
			String mobileId = id+"-"+i;
			FogDevice mobile = addMobile(mobileId, userId, appId, dept.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
			mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
			fogDevices.add(mobile);
		}
		return dept;
	}
	
	private static FogDevice addMobile(String id, int userId, String appId, int parentId){
		double cost = 6.0; // the cost of using processing in this resource每秒的花费
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.1; // the cost of using storage in this resource
		double costPerBw = 0.3;//每带宽的花费
		FogDevice mobile = createFogDevice("m-"+id, 1, GHzToMips(1.0), 1024, 20*1024, 40*1024,
				                               3, 0, 700, 30,cost,costPerMem,costPerStorage,costPerBw);
		mobile.setParentId(parentId);
		return mobile;
	}
	
	/**
	 * Creates a vanilla fog device
	 * @param nodeName name of the device to be used in simulation
	 * @param hostnum the number of the host of device
	 * @param mips MIPS
	 * @param ram RAM
	 * @param upBw uplink bandwidth (Kbps)
	 * @param downBw downlink bandwidth (Kbps)
	 * @param level hierarchy level of the device
	 * @param ratePerMips cost rate per MIPS used
	 * @param busyPower(mW)
	 * @param idlePower(mW)
	 * @return
	 */
	private static FogDevice createFogDevice(String nodeName, int hostnum, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower,double cost,double costPerMem,double costPerStorage,double costPerBw) {
		
		List<Host> hostList = new ArrayList<Host>();

		for ( int i = 0 ;i < hostnum; i++ )
		{
			List<Pe> peList = new ArrayList<Pe>();
			// 3. Create PEs and add these into a list.
			peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			//peList.add(new Pe(1, new PeProvisionerSimple(mips)));
			//System.out.println(nodeName+" pe.Mips"+mips);
			
			int hostId = FogUtils.generateEntityId();
			long storage = 1000000; // host storage
			int bw = 10000;

			PowerHost host = new PowerHost(
					hostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList),
					new FogLinearPowerModel(busyPower, idlePower)//默认发送功率100mW 接收功率25mW
				);
			
			hostList.add(host);
			//System.out.println("host#"+hostId+".getTotalMips() : "+host.getTotalMips());
		}

        // 4. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		/*double cost = 3.0; // the cost of using processing in this resource每秒的花费
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.1; // the cost of using storage in this resource
		double costPerBw = 0.1; // the cost of using bw in this resource每带宽的花费
*/		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		
		// 5. Finally, we need to create a storage object.
        /**
         * The bandwidth within a data center in MB/s.
         */
        int maxTransferRate = 10;// the number comes from the futuregrid site, you can specify your bw
		try {
			// Here we set the bandwidth to be 15MB/s
            HarddriveStorage s1 = new HarddriveStorage(nodeName, 1e12);
            s1.setMaxTransferRate(maxTransferRate);
            storageList.add(s1);
			fogdevice = new FogDevice(nodeName, characteristics, 
					new VmAllocationPolicySimple(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fogdevice.setLevel(level);
		return fogdevice;
	}
	
    protected static List<CondorVM> createVM(int userId, int vms, List<? extends Host> devicelist) {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<CondorVM> list = new LinkedList<>();

        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        CondorVM[] vm = new CondorVM[vms];
        for (int i = 0; i < vms; i++) {
            double ratio = 1.0;
            mips = devicelist.get(i).getTotalMips();
            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }
        return list;
    }
    
    private static long GHzToMips(double GHz)
    {
    	double mips = GHz*1000;
    	long Mips = new Double(mips).longValue();
    	//System.out.println("GHz "+GHz+"->Mips "+Mips);
    	return Mips;
    }
    
    /**
     * Prints the job objects
     *
     * @param list list of jobs
     */
    protected static void printJobList(List<Job> list) {
    	@SuppressWarnings("resource")
		Formatter formatter = new Formatter(System.out);
    	String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        formatter.format("%-8s %-12s %-8s %-17s %-10s %-8s %-12s %-13s %-10s %-10s\n",
        		"Job ID","Task ID","STATUS",
        		"Data center ID","VM ID","Time","Start Time","Finish Time","Depth","Cost");
        DecimalFormat dft = new DecimalFormat("###.##");
        

        for (Job job : list) {
        	formatter.format("  %-8d",job.getCloudletId());
            //Log.print(indent + job.getCloudletId() + indent + indent);
            if (job.getClassType() == ClassType.STAGE_IN.value) {
            	formatter.format("%-10s","Stage-in");
                //Log.print("Stage-in");
            }
            for (Task task : job.getTaskList()) {
            	formatter.format("%-10d",task.getCloudletId());
                //Log.print(task.getCloudletId() + ",");
            }
            //Log.print(indent);

            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {
            	formatter.format(" SUCCESS\t %-16s %-9d %-10.2f %-12.2f %-13.2f %-8d %-12.2f",
            			job.getResourceName(job.getResourceId()),job.getVmId(),
            			job.getActualCPUTime(),job.getExecStartTime(),
            			job.getFinishTime(),job.getDepth(),job.getProcessingCost());
            	@SuppressWarnings("unchecked")
				List<Task> l = job.getParentList();
            	for(Task task : l)
            		System.out.print(task.getCloudletId()+",");
            	System.out.println();
            } else if (job.getCloudletStatus() == Cloudlet.FAILED) {
                Log.print("FAILED");
                Log.printLine(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
                        + indent + indent + indent + dft.format(job.getActualCPUTime())
                        + indent + indent + dft.format(job.getExecStartTime()) + indent + indent + indent
                        + dft.format(job.getFinishTime()) + indent + indent + indent + job.getDepth());
            }
        }
    }
    
    private static void printdevices() {    //输出设备列表
    	System.out.println("设备列表：");
    	for(SimEntity entity:CloudSim.getEntityList())
			System.out.println("    "+entity.getId()+"  "+entity.getName());
    }
}