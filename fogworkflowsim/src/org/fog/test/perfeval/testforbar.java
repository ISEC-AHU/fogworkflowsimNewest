package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Arrays;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import drawbar.DrawBar;

public class testforbar {
	
	public static void main(String[] args) {
		drawbar(//new ArrayList<String>(Arrays.asList("MaxMin","MinMin")),
				new ArrayList<Double>(Arrays.asList(2010.0,2011.0)),
				new ArrayList<Double>(Arrays.asList(200.0,100.0)), 
				new ArrayList<Double>(Arrays.asList(220.0,120.0)),
				new ArrayList<Double>(Arrays.asList(22.0,20.0)));
	}

	@SuppressWarnings("finally")
	protected static int drawbar(ArrayList<Double> algorithms, ArrayList<Double> time, ArrayList<Double> energy,
			ArrayList<Double> cost) {
        MWNumericArray x = null; // 存放x值的数组
        MWNumericArray y1 = null; // 存放y1值的数组
        MWNumericArray y2 = null; // 存放y2值的数组
        MWNumericArray y3 = null; // 存放y3值的数组
        DrawBar plot = null; // 自定义plotter实例，即打包时所指定的类名，根据实际情况更改
        
        int n = algorithms.size();//做图点数  横坐标
        try {
            int[] dims = {n, 1};//矩阵几行几列
            x = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y1 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y2 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            y3 = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
            
            for(int i = 1; i <= n ; i++){
            	x.set(i, algorithms.get(i-1));
            	y1.set(i, time.get(i-1));
            	y2.set(i, energy.get(i-1));
            	y3.set(i, cost.get(i-1));
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
}
