package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHost;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.NetworkUsageMonitor;
import org.workflowsim.CondorVM;
import org.workflowsim.FileItem;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.utils.Parameters.FileType;

public class Controller extends SimEntity{
	
	public static boolean ONLY_CLOUD = false;
		
	private List<FogDevice> fogDevices;
	private WorkflowEngine wfEngine;
	
	public double TotalExecutionTime;
	public double TotalEnergy;
	public double TotalCost;
	public double MSendTime;//移动设备发送数据时间
	public double MReceTime;//移动设备接收数据时间
	
	public Controller(String name, List<FogDevice> fogDevices , WorkflowEngine Engine) {
		super(name);
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		connectWithLatencies();
		wfEngine = Engine;
		wfEngine.setcontrollerId(this.getId());
		TotalExecutionTime=0.0;
		TotalEnergy=0.0;
		TotalCost=0.0;
		MSendTime=0.0;
	}

	public FogDevice getFogDeviceById(int id){
		for(FogDevice fogDevice : getFogDevices()){
			if(id==fogDevice.getId())
				return fogDevice;
		}
		return null;
	}
	
	private void connectWithLatencies(){
		for(FogDevice fogDevice : getFogDevices()){
			FogDevice parent = getFogDeviceById(fogDevice.getParentId());
			if(parent == null)
				continue;
			double latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());
		}
	}
	
	
	@Override
	public void startEntity() {

		//send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
		
		//send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);
		
		//for(FogDevice dev : getFogDevices())
		//	sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);

	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		case FogEvents.STOP_SIMULATION:
			shutdownEntity();
			CloudSim.clearEvent();
			updateExecutionTime();
			CloudSim.stopSimulation();
			break;
		}
	}
	
	private void printNetworkUsageDetails() {
		System.out.println("Total network usage = "+NetworkUsageMonitor.getNetworkUsage()/Config.MAX_SIMULATION_TIME);		
	}

	private FogDevice getCloud(){
		for(FogDevice dev : getFogDevices())
			if(dev.getName().equals("cloud"))
				return dev;
		return null;
	}
	
	public FogDevice getmobile(){
		for(FogDevice dev : getFogDevices())
			if(dev.getName().contains("m"))
				return dev;
		return null;
	}
	
	private void printCostDetails(){
		/*for(FogDevice fogDevice : getFogDevices()){
			System.out.println(fogDevice.getName() +" : Cost = "+fogDevice.getTotalCost());
		}*/
		System.out.println("Total Cost = "+TotalCost);
	}
	
	public void updateExecutionTime() {
		double time=0.0;
		double energy=0.0;
		double cost=0.0;
		getmobile().setEnergyConsumption(getMobileEnergy());
		//System.out.println("mobile.energy:"+getmobile().getEnergyConsumption());
		for(FogDevice fogDevice : getFogDevices())
		{
			String name=fogDevice.getName();
			//fogDevice.updateEnergyConsumption();
			//System.out.println(fogDevice.getName() +" : "+fogDevice.getExecutionTime());
			time+=fogDevice.getExecutionTime();
			
			if(name.contains("m")){
				//System.out.println("fogDeviceId:"+fogDevice.getId());
				List<Job> jobList = wfEngine.getJobsReceivedList();
				if(!jobList.isEmpty()){
//					System.out.println("jobList.size:"+jobList.size());
					Job Lastjob = jobList.get(jobList.size()-1);//获取到最后一个执行的job
					for(FileItem file : Lastjob.getFileList()){
						if(file.getType() == FileType.OUTPUT)
							MReceTime += (file.getSize()/1024) / getCloud().getDownlinkBandwidth();
						//文件大小单位为bit,换算成Kb,带宽单位为Kbps,接收时间单位为s
					}
				}
				//System.out.println("MReceTime:"+MReceTime);
				MSendTime = (getSendSize()/1024) / getmobile().getUplinkBandwidth();
				//System.out.println("SendSize:"+getSendSize());
				//System.out.println("MSendTime:"+MSendTime);
				FogLinearPowerModel powerModel = (FogLinearPowerModel) fogDevice.getHost().getPowerModel();
				//移动设备能耗  = (空闲能耗 +负载能耗) +发送数据能耗+接收数据能耗
				//System.out.println("fogDevice.getEnergyConsumption():"+fogDevice.getEnergyConsumption());
				energy = fogDevice.getEnergyConsumption() + MSendTime * powerModel.getSendPower() +
				               MReceTime * powerModel.getRecePower();
				fogDevice.setEnergyConsumption(energy/1000);//单位J
				//System.out.println("fogDevice.getEnergyConsumption():"+fogDevice.getEnergyConsumption());
			}
			else{
				//cost+=fogDevice.getTotalCost();
				fogDevice.setTotalCost(getDatacenterCost(fogDevice.getId()));
				cost += getDatacenterCost(fogDevice.getId());
			}
		}
//		TotalExecutionTime=time;
		TotalExecutionTime = CloudSim.clock();
		TotalEnergy = getmobile().getEnergyConsumption();
		//System.out.println("mobile.getEnergyConsumption():"+energy);
		TotalCost = cost;
		//System.out.println("Total Execution Time = "+time);
	}

	private void printPowerDetails() {
		FogDevice mobile = getmobile();
		//for(FogDevice fogDevice : getFogDevices()){
		System.out.println(mobile.getName() +" : Energy Consumed = "+mobile.getEnergyConsumption() +" J");
		//}
	}

	protected void manageResources(){
		//send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
		for(FogDevice dev : getFogDevices())
			sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);
	}
	
	@Override
	public void shutdownEntity() {	
		Log.printLine(getName() + " is shutting down...");
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}
	
	public void print()
	{
		Log.printLine();
		Log.printLine("==========================================");
		System.out.println("Execution Time = "+TotalExecutionTime);
		printPowerDetails();
		printCostDetails();
		//printNetworkUsageDetails();
		Log.printLine("==========================================");
		Log.printLine();
	}
	public void clear()
	{
		for(FogDevice device: fogDevices)
		{
			//System.out.println("name:"+device.getName());
			device.clearConsumption();
			//device.clear();
			//device=null;
		}
		MSendTime = 0.0;
		MReceTime = 0.0;
	}
	
	public double getSendSize(){
		double sendsize=0;
		List<Job> jobList=wfEngine.getJobsReceivedList();
		HashMap<String, List<Integer>> fileToDatacenter= new HashMap<String, List<Integer>>();
		
		for(Job job:jobList){
			CondorVM vm = getVm(job.getVmId());
			int datacenterId=vm.getHost().getDatacenter().getId();
			if(!CloudSim.getEntity(datacenterId).getName().contains("m")){
				for(FileItem file:job.getFileList()){
					List<Integer> datacenterIdList=new ArrayList<Integer>();
					if(file.getType()==FileType.INPUT)
						if(!fileToDatacenter.containsKey(file.getName())|| !fileToDatacenter.get(file.getName()).contains(datacenterId)){
							sendsize+=file.getSize();
							datacenterIdList.add(datacenterId);
							}
					fileToDatacenter.put(file.getName(), datacenterIdList);
				}
			}
		}
		return sendsize;
	}
	
	public CondorVM getVm(int vmId){
		for(Vm vm:wfEngine.getAllVmList()){
			if(vm.getId()==vmId)
				return (CondorVM) vm;
		}
		return null;
	}
	
	
	public double getDatacenterCost(int id) {
		double cost = 0;
		List<Job> jobList = wfEngine.getJobsReceivedList();
		for(Job job : jobList){
			CondorVM vm = getVm(job.getVmId());
			FogDevice fogdevice = (FogDevice) vm.getHost().getDatacenter();
			int datacenterId = fogdevice.getId();
			if(!CloudSim.getEntity(datacenterId).getName().contains("m")){
				if(id == datacenterId) {
					//cost+=job.getProcessingCost();
					double c = job.getActualCPUTime()* vm.getHost().getTotalMips() * fogdevice.getRatePerMips()/1000;
					//费用 = 执行时间 * 计算资源使用量 * 每单位计算量每单位时间的计算费用
					job.setProcessingCost(c);
					cost += c;
				}
			}
			else {
				job.setProcessingCost(0);
			}
		}
		
		return cost;
	}
	
	public double getMobileEnergy(){
		double energy = 0;
		double executiontime = 0;
		double idletime = 0;
		List<Job> jobList=wfEngine.getJobsReceivedList();
		for(Job job:jobList){
			CondorVM vm = getVm(job.getVmId());
			PowerHost host = (PowerHost)vm.getHost();
			FogLinearPowerModel powerModel = (FogLinearPowerModel) host.getPowerModel();
			FogDevice fogdevice = (FogDevice) vm.getHost().getDatacenter();
			if(fogdevice.getName().contains("m")){
				executiontime += job.getActualCPUTime();
				double  e = job.getActualCPUTime() * powerModel.getPower(vm.getMips()/host.getTotalMips());
				energy += e;
			}
		}
		//System.out.println("mobile.executiontime:"+executiontime);
		idletime = CloudSim.clock()-executiontime;
		FogLinearPowerModel powerModel = (FogLinearPowerModel) getmobile().getHost().getPowerModel();
		energy += idletime * powerModel.getStaticPower();//负载能耗+空闲能耗
		//System.out.println("idletime:"+idletime);
		return energy;
	}
}
