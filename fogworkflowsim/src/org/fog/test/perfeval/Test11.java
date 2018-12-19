package org.fog.test.perfeval;

import java.awt.Color;
import java.awt.Dimension;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

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
import drawbar.DrawBar;
//import drawpicture.DrawPicture;
import drawplot2.*;
import javafx.scene.chart.PieChart.Data;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Test11 extends JFrame {
	String [] algrithmStr=new String[]{"MINMIN","MAXMIN","FCFS","ROUNDROBIN","PSO","GA"};
	String [] objectiveStr=new String[]{"Time","Energy","Cost"};
	String [] inputTypeStr=new String[]{"Montage","CyberShake","Epigenomics","Inspiral","Sipht"};
	String [] nodeSizeStr=new String[]{"25","50","100","1000"};
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
    List<Double[]> record=new ArrayList<Double[]>();
	
	static int numOfDepts = 1;
	static int numOfMobilesPerDept = 1;
	static int nodeSize;
	private final JLabel label = new JLabel("云服务器数：");
	private final JLabel label_1 = new JLabel("边缘服务器数：");
	private final JLabel label_2 = new JLabel("手机个数：");
	private static WorkflowEngine wfEngine;
	private static Controller controller;
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
	private static JTable table;
	private static JScrollPane scrollPane;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test11 frame = new Test11();
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
	public Test11() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("移动边缘计算工作流仿真系统");
		setBounds(100, 100, 808, 758);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		this.initUI();
	}
	
	public void initUI() {
		
		stnBtn = new JButton("开始仿真");
		stnBtn.setFont(new Font("宋体", Font.PLAIN, 12));
		stnBtn.setBounds(584, 289, 91, 45);
		stnBtn.addActionListener(new JMHandler());
		contentPane.add(stnBtn);
		ImageIcon icon =new ImageIcon(getClass().getResource("/images/cloudServer2.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		validate();
		repaint();
		ImageIcon icon2 =new ImageIcon(getClass().getResource("/images/fogServer.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		validate();
		repaint();
		ImageIcon icon3 =new ImageIcon(getClass().getResource("/images/mobile.jpg"));
		icon.setImage(icon.getImage().getScaledInstance(60, 70,
				Image.SCALE_DEFAULT));
		validate();
		repaint();
	    
		String[] columnNames = {"Job ID", "Task ID", "STATUS", "Data center ID", "VM ID", "Time", "Start Time", "Finish Time", "Depth", "Cost", "Parents"};
	    Object[][] rowData = {};
	    
	    // 创建一个表格，指定 所有行数据 和 表头
		table = new JTable(rowData, columnNames);
		
		// 设置表格内容颜色
		table.setForeground(Color.BLACK);                   // 字体颜色
		table.setFont(new Font(null, Font.PLAIN, 14));      // 字体样式
		table.setSelectionForeground(Color.DARK_GRAY);      // 选中后字体颜色
		table.setSelectionBackground(Color.LIGHT_GRAY);     // 选中后字体背景
		table.setGridColor(Color.GRAY);                     // 网格颜色

        // 设置表头
        table.getTableHeader().setForeground(Color.black);                // 设置表头名称字体颜色
        table.getTableHeader().setResizingAllowed(false);               // 设置不允许手动改变列宽
        table.getTableHeader().setReorderingAllowed(false);             // 设置不允许拖动重新排序各列
        
        table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setPreferredScrollableViewportSize(new Dimension(0, 280));
		FitTableColumns(table);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 344, 771, 367);
		contentPane.add(scrollPane);
		scrollPane.setColumnHeaderView(table);
		scrollPane.setViewportView(table);
		panel_1.setBackground(Color.WHITE);
		panel_1.setBounds(584, 150, 195, 121);
		
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		taskTypeLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		taskTypeLabel.setBounds(10, 42, 85, 20);
		taskTypeLabel.setForeground(Color.BLACK);
		
		panel_1.add(taskTypeLabel);
		inputTypeCb.setBounds(99, 42, 86, 21);
		panel_1.add(inputTypeCb);
		taskNumLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		taskNumLabel.setBounds(10, 80, 73, 20);
		taskNumLabel.setForeground(Color.BLACK);
		
		panel_1.add(taskNumLabel);
		label_6.setFont(new Font("宋体", Font.BOLD, 16));
		label_6.setBounds(53, 10, 121, 21);
		
		panel_1.add(label_6);
		nodeSizeCb.setBounds(99, 80, 86, 21);
		panel_1.add(nodeSizeCb);
		panel_2.setBackground(Color.WHITE);
		panel_2.setBounds(584, 9, 195, 121);
		
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		algrithmCb.setBounds(99, 46, 86, 21);
		panel_2.add(algrithmCb);
		objectiveCb.setBounds(99, 84, 86, 21);
		panel_2.add(objectiveCb);
		label_3.setFont(new Font("宋体", Font.PLAIN, 12));
		label_3.setBounds(10, 46, 78, 20);
		label_3.setForeground(Color.BLACK);
		
		panel_2.add(label_3);
		label_4.setFont(new Font("宋体", Font.PLAIN, 12));
		label_4.setBounds(10, 84, 78, 20);
		label_4.setForeground(Color.BLACK);
		
		panel_2.add(label_4);
		label_7.setFont(new Font("宋体", Font.BOLD, 16));
		label_7.setBounds(60, 10, 100, 25);
		
		panel_2.add(label_7);
		paintBtn.setFont(new Font("宋体", Font.PLAIN, 12));
		
		paintBtn.addActionListener(new JMHandler()); 
		paintBtn.setBounds(685, 289, 91, 45);
		
		contentPane.add(paintBtn);
		panel_3.setBackground(Color.LIGHT_GRAY);
		panel_3.setBounds(10, 9, 560, 325);
		
		contentPane.add(panel_3);
		panel_3.setLayout(null);
		label_5.setBounds(403, 9, 85, 26);
		panel_3.add(label_5);
		label_5.setFont(new Font("宋体", Font.BOLD, 16));
		mobilePanel.setBounds(10, 220, 317, 95);
		panel_3.add(mobilePanel);
		
		mobilePanel.setLayout(new FlowLayout());
		mobilePanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		mobilePanel.setBackground(Color.WHITE);
		jLabel=new JLabel(icon3); 
		mobilePanel.add(jLabel);
		fogPanel.setBounds(10, 115, 317, 95);
		panel_3.add(fogPanel);
		
		fogPanel.setLayout(new FlowLayout());
		fogPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		fogPanel.setBackground(Color.WHITE);
		jLabel=new JLabel(icon2); 
		fogPanel.add(jLabel);
		cloudPanel.setBounds(10, 10, 317, 95);
		panel_3.add(cloudPanel);
		
		cloudPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		cloudPanel.setLayout(new FlowLayout());
		cloudPanel.setBackground(Color.WHITE);
		jLabel=new JLabel(icon); 
		cloudPanel.add(jLabel);
		panel.setBounds(334, 45, 216, 270);
		panel_3.add(panel);
		
		panel.setBackground(Color.WHITE);
		panel.setLayout(null);
		label.setFont(new Font("宋体", Font.PLAIN, 12));
		label.setBounds(10, 26, 100, 20);
		panel.add(label);
		label.setForeground(Color.BLACK);
		cloudNumCb.setBounds(123, 22, 75, 28);
		panel.add(cloudNumCb);
		label_1.setFont(new Font("宋体", Font.PLAIN, 12));
		label_1.setBounds(10, 116, 100, 20);
		panel.add(label_1);
		label_1.setForeground(Color.BLACK);
		edgeNumCb.setBounds(123, 110, 75, 28);
		panel.add(edgeNumCb);
		label_2.setFont(new Font("宋体", Font.PLAIN, 12));
		label_2.setBounds(10, 206, 100, 20);
		panel.add(label_2);
		label_2.setForeground(Color.BLACK);
		mobileNumCb.setBounds(123, 202, 75, 28);
		panel.add(mobileNumCb);
		//mobileNumCb.addActionListener(new JMHandler());//添加监听事件
		mobileNumCb.addItemListener(new JMHandler());
		edgeNumCb.addItemListener(new JMHandler());
		cloudNumCb.addItemListener(new JMHandler());
		inputTypeCb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				nodeSizeCb.removeAllItems();
				for(String str : getFiles((String)inputTypeCb.getSelectedItem()))
					nodeSizeCb.addItem(str);
			}
		});
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
	
	@SuppressWarnings("finally")
	protected static int drawbar(List<Double[]> record) {
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y1值的数组
        MWNumericArray y2 = null; // 存放y2值的数组
        MWNumericArray y3 = null; // 存放y3值的数组
        DrawBar plot = null; // 自定义plotter实例，即打包时所指定的类名，根据实际情况更改

        int n = record.size();//做图点数  横坐标
        
        try {
            int[] dims = {n, 1};//几行几列
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y1 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y2 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y3 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            
            for(int i = 1; i <= n ; i++){
            	Double[] data = record.get(i-1);
            	x.set(i, data[0]);//Algorithm     将矩阵中第i个数设置成某个值
            	y1.set(i, data[1]);//Time
            	y2.set(i, data[2]);//Energy
            	y3.set(i, data[3]);//Cost
            }
            
            //初始化plotter
            plot = new DrawBar();
            
            //做图
            plot.drawbar(x, y1, y2, y3);// 在脚本文件中的函数名，根据实际情更改
            plot.waitForFigures();// 不调用该句，无法弹出绘制图形窗口
             
        } catch (Exception e1) {
            // TODO: handle exception
        } finally {
            MWArray.disposeArray(x);
            MWArray.disposeArray(y1);
            MWArray.disposeArray(y2);
            MWArray.disposeArray(y3);
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
	            	clear();
	            	scheduler_method=(String)algrithmCb.getSelectedItem();
	            	optimize_objective=(String)objectiveCb.getSelectedItem();
	            	daxPath="fogworkflowsim/config/dax/"+inputTypeCb.getSelectedItem()+"_"+nodeSizeCb.getSelectedItem()+".xml";
	            	nodeSize = Integer.parseInt((String) nodeSizeCb.getSelectedItem());
	            	String cloudNum1=(String)cloudNumCb.getSelectedItem();
	            	cloudNum=Integer.parseInt(cloudNum1);
	            	String fogServerNum1=(String)edgeNumCb.getSelectedItem();
	            	fogServerNum=Integer.parseInt(fogServerNum1);
	            	String mobileNum1=(String)mobileNumCb.getSelectedItem();
	            	mobileNum=Integer.parseInt(mobileNum1);
	            	simulate();
	            	CloudSim.startSimulation();
	            	List<Job> outputList0 = wfEngine.getJobsReceivedList();
	                CloudSim.stopSimulation();
	                Log.enable();
	                printJobList(outputList0);
	                controller.print();
	                Double[] a = {getAlgorithm(scheduler_method),controller.TotalExecutionTime,controller.TotalEnergy,controller.TotalCost};
	                record.add(a);
	            }
	            
	            if(paintBtn==e.getSource()) {
	            	if(scheduler_method.contains("M")||scheduler_method.equals("FCFS")
	            			||scheduler_method.equals("ROUNDROBIN")){
	            		System.out.println("画柱状图");
	            		drawbar(record);
	            		System.out.println("画完");
	            	}
	            	else{
	            		System.out.println("画迭代图");
	            		drawplot(wfEngine.iterateNum,wfEngine.updatebest,"迭代次数",optimize_objective);
	            		System.out.println("画完");
	            	}
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
								
				createFogDevices(1,appId);//(broker.getId(), appId);
							
				int hostnum = 0;
				for(FogDevice device : fogDevices){
					hostnum += device.getHostList().size();
				}
				int vmNum = hostnum;//number of vms;
				
	            File daxFile = new File(daxPath);
	            if (!daxFile.exists()) {
	            	System.out.println("Warning: Please replace daxPath with the physical path in your working environment!");
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
	            	}
	            	System.out.println();
	            }
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Unwanted errors happen");
			}
		 
	 }
	 
	 
	 
	 public Double getAlgorithm(String scheduler_method) {
		 if(scheduler_method.equals(algrithmStr[0]))
			 return 1.0;
		 else if(scheduler_method.equals(algrithmStr[1]))
			 return 2.0;
		 else if(scheduler_method.equals(algrithmStr[2]))
			 return 3.0;
		 else if(scheduler_method.equals(algrithmStr[3]))
			 return 4.0;
		 else if(scheduler_method.equals(algrithmStr[4]))
			 return 5.0;
		 else if(scheduler_method.equals(algrithmStr[5]))
			 return 6.0;
		 return null;
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
	    @SuppressWarnings("null")
		protected static void printJobList(List<Job> list) {
	        DecimalFormat dft = new DecimalFormat("######0.00");
	        String[] columnNames = {"Job ID", "Task ID", "STATUS", "Data center ID", "VM ID", "Time","Start Time","Finish Time","Depth","Cost","Parents"};
	        Object[][] rowData = new Object[nodeSize+1][];
	        int i=0;
	        
	        for (Job job : list) {
	        	Collection<String> data =new ArrayList<String>(); 
	        	data.add(Integer.toString(job.getCloudletId()));
	            if (job.getClassType() == ClassType.STAGE_IN.value) {
	            	data.add("Stage-in");
	            }
	            for (Task task : job.getTaskList()) {
	            	data.add(Integer.toString(task.getCloudletId()));
	            }

	            if (job.getCloudletStatus() == Cloudlet.SUCCESS) {          	
	            	data.add("SUCCESS");
	            	data.add(job.getResourceName(job.getResourceId()));
	            	data.add(Integer.toString(job.getVmId()));
	            	data.add(dft.format(job.getActualCPUTime()));
	            	data.add(dft.format(job.getExecStartTime()));
	            	data.add(dft.format(job.getFinishTime()));
	            	data.add(Integer.toString(job.getDepth()));
	            	data.add(dft.format(job.getProcessingCost()));
	            	
					List<Task> l = job.getParentList();
	            	String parents ="";
	            	for(Task task : l)
	            		parents += task.getCloudletId()+",";
	            	data.add(parents);
	            } 
	            else if (job.getCloudletStatus() == Cloudlet.FAILED) {
	                data.add("FAILED");
	                data.add(job.getResourceName(job.getResourceId()));
	            	data.add(Integer.toString(job.getVmId()));
	            	data.add(dft.format(job.getActualCPUTime()));
	            	data.add(dft.format(job.getExecStartTime()));
	            	data.add(dft.format(job.getFinishTime()));
	            	data.add(Integer.toString(job.getDepth()));
	            	data.add(dft.format(job.getProcessingCost()));
	            }
	            rowData[i] = data.toArray();
	            i++;
	        }
	        table = new JTable(rowData, columnNames);
	        table.getTableHeader().setForeground(Color.RED);
	        FitTableColumns(table);
	        scrollPane.setViewportView(table);
	    }
                                                                                                                                                                                                                                                                                                                      
	    private static void printdevices() {    //输出设备列表
	    	System.out.println("设备列表：");
	    	for(SimEntity entity:CloudSim.getEntityList())
	    		System.out.println("    "+entity.getId()+"  "+entity.getName());
	    }
	    
		public static void FitTableColumns(JTable myTable){
	    	  JTableHeader header = myTable.getTableHeader();
	    	     int rowCount = myTable.getRowCount();

	    	     Enumeration columns = myTable.getColumnModel().getColumns();
	    	     while(columns.hasMoreElements()){
	    	         TableColumn column = (TableColumn)columns.nextElement();
	    	         int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
	    	         int width = (int)myTable.getTableHeader().getDefaultRenderer()
	    	                 .getTableCellRendererComponent(myTable, column.getIdentifier()
	    	                         , false, false, -1, col).getPreferredSize().getWidth();
	    	         for(int row = 0; row<rowCount; row++){
	    	             int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
	    	               myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
	    	             width = Math.max(width, preferedWidth);
	    	         }
	    	         header.setResizingColumn(column); // 此行很重要
	    	         column.setWidth(width+myTable.getIntercellSpacing().width+20);
	    	     }
	    }
	    
		public static int clear()
		{
			try {
				if(controller==null)
					return 0;
				wfEngine.jobList.removeAll(wfEngine.jobList);
				controller.clear();
				wfEngine.clearFlag();
				fogDevices.removeAll(fogDevices);  //清除对象列表
				FogUtils.set1();
//				calendar = null;
				String[] columnNames = {"Job ID", "Task ID", "STATUS", "Data center ID", "VM ID", "Time", "Start Time", "Finish Time", "Depth", "Cost", "Parents"};
			    Object[][] rowData = {};
			    
			    // 创建一个表格，指定 所有行数据 和 表头
				table = new JTable(rowData, columnNames);
				table.getTableHeader().setForeground(Color.black);
				FitTableColumns(table);
		        scrollPane.setViewportView(table);
				
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 1;
		}
		
		public static ArrayList<String> getFiles(String filename) {
		    ArrayList<String> files = new ArrayList<String>();
		    ArrayList<Integer> node = new ArrayList<Integer>();
		    File path = new File("fogworkflowsim/config/dax");
		    for (File file : path.listFiles()){
		    	String name = file.getName();
		    	if(name.contains(filename)){
		    		name = name.replace(filename+"_", "");
		    		name = name.replace(".xml", "");
		    		node.add(Integer.valueOf(name));
		    	}
		    }
		    Collections.sort(node);
	        for (Integer integer : node) {
	    	    files.add(Integer.toString(integer));
	    	}
		    return files;
		}
}

