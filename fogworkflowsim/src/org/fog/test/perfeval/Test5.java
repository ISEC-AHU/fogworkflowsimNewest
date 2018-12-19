package org.fog.test.perfeval;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Controller;
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

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

//import drawpicture.DrawPicture;
import drawplot2.*;

public class Test5 extends JFrame {
	String [] algrithmStr=new String[]{"MINMIN","MAXMIN","PSO","GA"};
	String [] objectiveStr=new String[]{"Time","Energy","Cost"};
	String [] inputTypeStr=new String[]{"Montage","CyberShake","Epigenomics","Inspiral","Sipht"};
	String [] nodeSizeStr=new String[]{"25","30","50","100","1000"}; 
	String [] cloudNumStr=new String[]{"1","2","3","4","5"}; 
	String [] edgeNumStr=new String[]{"1","2","3"}; 
	String [] mobileNumStr=new String[]{"1","2","3"}; 
	
	private JComboBox algrithmCb = new JComboBox(algrithmStr);
	private JComboBox objectiveCb = new JComboBox(objectiveStr);//优化目标
	private JComboBox inputTypeCb = new JComboBox(inputTypeStr);
	private JComboBox nodeSizeCb = new JComboBox(nodeSizeStr);//任务个数
	private JComboBox cloudNumCb = new JComboBox(cloudNumStr);
	private JComboBox edgeNumCb = new JComboBox(edgeNumStr);
	private JComboBox mobileNumCb = new JComboBox(mobileNumStr);
	private JButton stnBtn;
	private JPanel cloudPanel = new JPanel();
	private JPanel fogPanel = new JPanel();
	private JPanel mobilePanel = new JPanel();
	private JLabel jLabel;

	private JPanel contentPane;
	
	private String scheduler_method;
	private String optimize_objective;
	private String daxPath;
	private int cloudNum;
	private int fogServerNum;
	private int mobileNum;
	
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	
	static int numOfDepts = 1;
	static int numOfMobilesPerDept = 1;
	private final JLabel label = new JLabel("云服务器数：");
	private final JLabel label_1 = new JLabel("边缘服务器数：");
	private final JLabel label_2 = new JLabel("手机个数：");
	private WorkflowEngine wfEngine;
	private Controller controller;
	private static  JTextArea textArea = new JTextArea(300,100);
	private final JPanel panel = new JPanel();
	private final JPanel panel_1 = new JPanel();
	private final JLabel taskTypeLabel = new JLabel("工作流类型：");
	private final JLabel taskNumLabel = new JLabel("任务个数：");
	private final JPanel panel_2 = new JPanel();
	private final JLabel label_3 = new JLabel("优化算法：");
	private final JLabel label_4 = new JLabel("优化目标：");
	private final JButton paintBtn = new JButton("绘制图表");
	private final JLabel label_5 = new JLabel("环境设置");
	private final JLabel label_6 = new JLabel("工作流设置");
	private final JLabel label_7 = new JLabel("算法选择");
	private final JPanel panel_3 = new JPanel();

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test5 frame = new Test5();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Test5() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("移动边缘计算工作流仿真系统");
		setBounds(100, 100, 786, 669);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		this.initUI();
	}
	
	public void initUI() {
		textArea.setText("                                                             输出结果展示区域");
		
		stnBtn = new JButton("开始仿真");
		stnBtn.setFont(new Font("宋体", Font.PLAIN, 12));
		stnBtn.setBounds(570, 278, 91, 45);
		stnBtn.addActionListener(new JMHandler());
		contentPane.add(stnBtn);
		
		cloudPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		cloudPanel.setLayout(new FlowLayout());
		cloudPanel.setBackground(Color.WHITE);
		cloudPanel.setBounds(10, 9, 317, 95);
		ImageIcon icon =new ImageIcon(getClass().getResource("/images/cloudServer2.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		jLabel=new JLabel(icon); 
		cloudPanel.add(jLabel);
		validate();
		repaint();
		contentPane.add(cloudPanel);
		
		fogPanel.setLayout(new FlowLayout());
		fogPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fogPanel.setBackground(Color.WHITE);
		fogPanel.setBounds(10, 114, 317, 95);
		ImageIcon icon2 =new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		jLabel=new JLabel(icon2); 
		fogPanel.add(jLabel);
		validate();
		repaint();
		contentPane.add(fogPanel);
		
		mobilePanel.setLayout(new FlowLayout());
		mobilePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		mobilePanel.setBackground(Color.WHITE);
		mobilePanel.setBounds(10, 219, 317, 95);
		ImageIcon icon3 =new ImageIcon(getClass().getResource("/images/mobile.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		jLabel=new JLabel(icon3); 
		mobilePanel.add(jLabel);
		validate();
		repaint();
		contentPane.add(mobilePanel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(8, 333, 757, 318);
		contentPane.add(scrollPane);
		
		scrollPane.setViewportView(textArea);
		panel.setBackground(Color.WHITE);
		panel.setBounds(337, 44, 216, 270);
		
		contentPane.add(panel);
		panel.setLayout(null);
		label.setFont(new Font("宋体", Font.PLAIN, 12));
		label.setBounds(10, 46, 100, 21);
		panel.add(label);
		label.setForeground(Color.BLACK);
		cloudNumCb.setBounds(123, 39, 75, 28);
		panel.add(cloudNumCb);
		label_1.setFont(new Font("宋体", Font.PLAIN, 12));
		label_1.setBounds(10, 124, 100, 15);
		panel.add(label_1);
		label_1.setForeground(Color.BLACK);
		edgeNumCb.setBounds(123, 117, 75, 28);
		panel.add(edgeNumCb);
		label_2.setFont(new Font("宋体", Font.PLAIN, 12));
		label_2.setBounds(10, 213, 100, 15);
		panel.add(label_2);
		label_2.setForeground(Color.BLACK);
		mobileNumCb.setBounds(123, 206, 75, 28);
		panel.add(mobileNumCb);
		panel_1.setBackground(Color.WHITE);
		panel_1.setBounds(570, 149, 195, 105);
		
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		taskTypeLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		taskTypeLabel.setBounds(10, 42, 85, 21);
		taskTypeLabel.setForeground(Color.BLACK);
		
		panel_1.add(taskTypeLabel);
		inputTypeCb.setBounds(96, 38, 75, 28);
		panel_1.add(inputTypeCb);
		taskNumLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		taskNumLabel.setBounds(10, 80, 73, 15);
		taskNumLabel.setForeground(Color.BLACK);
		
		panel_1.add(taskNumLabel);
		label_6.setFont(new Font("宋体", Font.BOLD, 16));
		label_6.setBounds(50, 10, 121, 21);
		
		panel_1.add(label_6);
		nodeSizeCb.setBounds(96, 73, 75, 28);
		panel_1.add(nodeSizeCb);
		panel_2.setBackground(Color.WHITE);
		panel_2.setBounds(570, 9, 195, 112);
		
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		algrithmCb.setBounds(88, 45, 75, 21);
		panel_2.add(algrithmCb);
		objectiveCb.setBounds(88, 76, 75, 21);
		panel_2.add(objectiveCb);
		label_3.setFont(new Font("宋体", Font.PLAIN, 12));
		label_3.setBounds(10, 46, 78, 18);
		label_3.setForeground(Color.BLACK);
		
		panel_2.add(label_3);
		label_4.setFont(new Font("宋体", Font.PLAIN, 12));
		label_4.setBounds(10, 74, 78, 21);
		label_4.setForeground(Color.BLACK);
		
		panel_2.add(label_4);
		label_7.setFont(new Font("宋体", Font.BOLD, 16));
		label_7.setBounds(50, 10, 100, 25);
		
		panel_2.add(label_7);
		paintBtn.setFont(new Font("宋体", Font.PLAIN, 12));
		
		paintBtn.addActionListener(new JMHandler()); 
		paintBtn.setBounds(671, 278, 91, 45);
		
		contentPane.add(paintBtn);
		panel_3.setBackground(Color.LIGHT_GRAY);
		panel_3.setBounds(0, 0, 560, 339);
		
		contentPane.add(panel_3);
		panel_3.setLayout(null);
		label_5.setBounds(385, 10, 85, 26);
		panel_3.add(label_5);
		label_5.setFont(new Font("宋体", Font.BOLD, 16));
		//mobileNumCb.addActionListener(new JMHandler());//添加监听事件
		mobileNumCb.addItemListener(new JMHandler());
		edgeNumCb.addItemListener(new JMHandler());
		cloudNumCb.addItemListener(new JMHandler());
	}
	
	@SuppressWarnings("finally")
	protected static int drawplot(int num, ArrayList<Double> fitness,String xz,String yz) {
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y = null; // 存放y值的数组
        DrawPicture plot = null; // 自定义plotter实例，即打包时所指定的类名，根据实际情况更改
         
        int n = num;//做图点数  横坐标
        try {
            int[] dims = {1, n};
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
             
            //定义 y = x^2
            for(int i = 1; i <= n; i++) {
                x.set(i,i);
                y.set(i, fitness.get(i-1));
            }
             
            //初始化plotter
            plot = new DrawPicture();
             
            //做图
            plot.drawplot(x, y, xz, yz);// 在脚本文件中的函数名，根据实际情更改
            plot.waitForFigures();// 不调用该句，无法弹出绘制图形窗口
             
        } catch (Exception e1) {
            // TODO: handle exception
        } finally {
            MWArray.disposeArray(x);
            MWArray.disposeArray(y);
            if(plot != null) {
                plot.dispose();
            }
            return 1;
        }
	
   }
        
      
	
	 private class JMHandler implements ActionListener,ItemListener  
	    {
	        public void actionPerformed(ActionEvent e)  
	        {
	            if(e.getSource()==stnBtn){
	            	scheduler_method=(String)algrithmCb.getSelectedItem();
	            	optimize_objective=(String)objectiveCb.getSelectedItem();
	            	daxPath="fogworkflowsim/config/dax/"+inputTypeCb.getSelectedItem()+"_"+nodeSizeCb.getSelectedItem()+".xml";
	            	String cloudNum1=(String)cloudNumCb.getSelectedItem();
	            	cloudNum=Integer.parseInt(cloudNum1);
	            	String fogServerNum1=(String)edgeNumCb.getSelectedItem();
	            	fogServerNum=Integer.parseInt(fogServerNum1);
	            	String mobileNum1=(String)mobileNumCb.getSelectedItem();
	            	mobileNum=Integer.parseInt(mobileNum1);
	            	textArea.append("test\n");
	            	simulate();
	            	CloudSim.startSimulation();
	            	List<Job> outputList0 = wfEngine.getJobsReceivedList();
	                CloudSim.stopSimulation();
	                Log.enable();
	                printJobList(outputList0);
	                controller.print();

	    			textArea.append("Simulation finished!");

	            }
	            
	            if(paintBtn==e.getSource()) {
	            	 drawplot(wfEngine.iterateNum,wfEngine.updatebest,"迭代次数",optimize_objective);
	            	
	            }
	            
	        }  
	        
	        public void itemStateChanged(ItemEvent e){
	        if(e.getItemSelectable()==mobileNumCb) {
	    		if(e.getStateChange() == ItemEvent.SELECTED){
	    			mobilePanel.removeAll();
	    			String itemSize = (String) e.getItem();
	    			try{
	    				for(int i=0;i<Integer.parseInt(itemSize);i++) {
	    					jLabel=new JLabel(new ImageIcon(getClass().getResource("/images\\mobile.jpg"))); 
	    					mobilePanel.add(jLabel);
	    					validate();
	    					repaint();
	    				}
	    			}catch(Exception ex){
	    				
	    			}
	    		}
	        	}else if(e.getItemSelectable()==edgeNumCb) {
	        		if(e.getStateChange() == ItemEvent.SELECTED){
		    			fogPanel.removeAll();
		    			String itemSize = (String) e.getItem();
		    			try{
		    				for(int i=0;i<Integer.parseInt(itemSize);i++) {
		    					ImageIcon icon =new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
		    					icon.setImage(icon.getImage().getScaledInstance(60, 70,
		    							Image.SCALE_DEFAULT));
		    					jLabel=new JLabel(icon); 
		    					fogPanel.add(jLabel);
		    					validate();
		    					repaint();
		    				}
		    			}catch(Exception ex){
		    				
		    			}
		    		}
	        		
	        	}else if(e.getItemSelectable()==cloudNumCb){
	        		if(e.getStateChange() == ItemEvent.SELECTED){
		    			cloudPanel.removeAll();
		    			String itemSize = (String) e.getItem();
		    			try{
		    				for(int i=0;i<Integer.parseInt(itemSize);i++) {
		    					ImageIcon icon =new ImageIcon(getClass().getResource("/images/cloudServer2.jpg"));
		    					icon.setImage(icon.getImage().getScaledInstance(60, 70,
		    							Image.SCALE_DEFAULT));
		    					jLabel=new JLabel(icon); 
		    					cloudPanel.add(jLabel);
		    					validate();
		    					repaint();
		    				}
		    			}catch(Exception ex){
		    				
		    			}
		    		}
	        	}
	    	}

	    
	 
	    }  
	 
	 
	 public void simulate() {
		 System.out.println("Starting Task...");

			try {
				//Log.disable();
				int num_user = 1; // number of cloud users
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = false; // mean trace events

				CloudSim.init(num_user, calendar, trace_flag);

				String appId = "workflow"; // identifier of the application
				
				//FogBroker broker = new FogBroker("broker");
				
				createFogDevices(1,appId);//(broker.getId(), appId);
							
				int hostnum = 0;
				for(FogDevice device : fogDevices){
					hostnum += device.getHostList().size();
				}
				int vmNum = hostnum;//number of vms;
				
				//String daxPath = "D:\\dax\\Montage_100.xml";
	            File daxFile = new File(daxPath);
	            if (!daxFile.exists()) {
	            	System.out.println("Warning: Please replace daxPath with the physical path in your working environment!");
	               // textArea.append("Warning: Please replace daxPath with the physical path in your working environment!");
	                return;
	            }
				
	            /**
	             * Since we are using MINMIN scheduling algorithm, the planning
	             * algorithm should be INVALID such that the planner would not
	             * override the result of the scheduler
	             */
	            Parameters.SchedulingAlgorithm sch_method =Parameters.SchedulingAlgorithm.valueOf(scheduler_method);
	            Parameters.Optimization opt_objective = Parameters.Optimization.valueOf(optimize_objective);
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
	            wfEngine = wfPlanner.getWorkflowEngine();
	            /**
	             * Create a list of VMs.The userId of a vm is basically the id of
	             * the scheduler that controls this vm.
	             */
	            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum());

	            /**
	             * Submits this list of vms to this WorkflowEngine.
	             */
	            wfEngine.submitVmList(vmlist0, 0);

	            controller = new Controller("master-controller", fogDevices, wfEngine);
	            
	            /**
	             * Binds the data centers with the scheduler.
	             */
	            for(FogDevice fogdevice:controller.getFogDevices()){
	            	wfEngine.bindSchedulerDatacenter(fogdevice.getId(), 0);
	            	List<PowerHost> list = fogdevice.getHostList();  //输出设备上的主机
	            	System.out.println(fogdevice.getName()+": ");
	            	for (PowerHost host : list){
	            		System.out.print(host.getId()+",");
	            		//textArea.append();
	            	}
	            	System.out.println();
	            }
	            
	           // printdevices();
	            
				
				/*CloudSim.startSimulation();

				List<Job> outputList0 = wfEngine.getJobsReceivedList();
	            CloudSim.stopSimulation();
	            Log.enable();
	            printJobList(outputList0);
	            controller.print();

				textArea.append("Simulation finished!");*/
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Unwanted errors happen");
				//textArea.append("Unwanted errors happen");
			}
		 
	 }
	 
	 
	 
	 private  void createFogDevices(int userId, String appId) {
			
			double GHzList[]={1.3};//云中的主机数，规模有大、中、小
			double costList[]={0.12};
			double cost = 5.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.2;//每带宽的花费

			for(int i=0;i<GHzList.length;i++)
			{
				FogDevice cloud = createFogDevice("cloud", cloudNum, GHzToMips(GHzList[i]), 
						40000, 100, 10000, 0, costList[i], 16*103, 16*83.25,cost,costPerMem,costPerStorage,costPerBw); // creates the fog device Cloud at the apex of the hierarchy with level=0
				cloud.setParentId(-1);
				
				fogDevices.add(cloud);
			}
			for(int i=0;i<numOfDepts;i++){
				addGw(i+"", userId, appId, fogDevices.get(0).getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
			}
		}


		private  FogDevice addGw(String id, int userId, String appId, int parentId){
			double cost = 3.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.1;//每带宽的花费
			
			FogDevice dept = createFogDevice("d-"+id, fogServerNum, GHzToMips(1.0), 4000, 10000, 10000, 1, 0.07, 700, 30,cost,costPerMem,costPerStorage,costPerBw);
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
		
		private  FogDevice addMobile(String id, int userId, String appId, int parentId){
			double cost = 6.0; // the cost of using processing in this resource每秒的花费
			double costPerMem = 0.05; // the cost of using memory in this resource
			double costPerStorage = 0.1; // the cost of using storage in this resource
			double costPerBw = 0.3;//每带宽的花费
			FogDevice mobile = createFogDevice("m-"+id, mobileNum, GHzToMips(1.0), 20*1024, 40*1024,
					                                     270, 3, 0, 700, 30,cost,costPerMem,costPerStorage,costPerBw);
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
	        int maxTransferRate = 20;// the number comes from the futuregrid site, you can specify your bw
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
		
	    protected static List<CondorVM> createVM(int userId, int vms) {
	        //Creates a container to store VMs. This list is passed to the broker later
	        LinkedList<CondorVM> list = new LinkedList<>();

	        //VM Parameters
	        long size = 10000; //image size (MB)
	        int ram = 512; //vm memory (MB)
	        int mips = 1000;
	        long bw = 1000;
	        int pesNumber = 1; //number of cpus
	        String vmm = "Xen"; //VMM name

	        //create VMs
	        CondorVM[] vm = new CondorVM[vms];
	        for (int i = 0; i < vms; i++) {
	            double ratio = 1.0;
	            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
	            list.add(vm[i]);
	        }
	        return list;
	    }
	    
	    private static long GHzToMips(double GHz)
	    {
	    	return (long)GHz*1000;//是否除以4
	    }
	    
	    /**
	     * Prints the job objects
	     *
	     * @param list list of jobs
	     */
	    protected static void printJobList(List<Job> list) {
	    	@SuppressWarnings("resource")
			Formatter formatter = new Formatter(System.out);
	    	String form = "%20s%20s%20s%20s%20s%20s%20s%20s%20s%20s";
	    	String.format(form, "Job ID","Task ID","STATUS",
	        		"Data center ID","VM ID","Time","Start Time","Finish Time","Depth","Cost");
	    	textArea.append(form);
	    	String indent = "    ";
	        textArea.append("\n");
	        textArea.setText("                                                  ========== OUTPUT ==========");
	        textArea.append("\n");
	        textArea.append("  Job ID  "+"  Task ID  "+"    STATUS  "+
	        		"     Data center ID  "+"     VM ID  "+"       Time  "+"        Start Time "+"     Finish Time"+"    Depth"+"     Cost");
	        DecimalFormat dft = new DecimalFormat("######0.00");
	        textArea.append("\n");

	        for (Job job : list) {
	        	textArea.append("         "+job.getCloudletId());
	            //Log.print(indent + job.getCloudletId() + indent + indent);
	            if (job.getClassType() == ClassType.STAGE_IN.value) {
	            	textArea.append(" "+"Stage-in");
	                //Log.print("Stage-in");
	            }
	            for (Task task : job.getTaskList()) {
	            	textArea.append("        "+task.getCloudletId());
	                //Log.print(task.getCloudletId() + ",");
	            }
	            //Log.print(indent);

	            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {
	            	textArea.append("      SUCCESS     "+"         "+job.getResourceName(job.getResourceId())+"            "+job.getVmId()+"                 "+
	            			dft.format(job.getActualCPUTime())+ "              " +dft.format(job.getExecStartTime())+"              "+
	            					dft.format(job.getFinishTime())+"             "+job.getDepth()+"              "+dft.format(job.getProcessingCost()));
	            	@SuppressWarnings("unchecked")
					List<Task> l = job.getParentList();
	            	/*for(Task task : l)
	            		textArea.append(task.getCloudletId()+",");*/
	            	textArea.append("\n");
	            } else if (job.getCloudletStatus() == Cloudlet.FAILED) {
	                Log.print("FAILED");
	                textArea.append(indent + indent + job.getResourceId() + indent + indent + indent + job.getVmId()
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
