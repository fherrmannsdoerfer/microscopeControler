package microscopeControl;

import ij.ImagePlus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Dimension;

import javax.swing.border.TitledBorder;

import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.AcquisitionEngine;

import mmcorej.CMMCore;

public class MainFrame extends JFrame {
	//static ij.ImagePlus ip;
	private JPanel contentPane;
	static ImageDisplay id;
	private CMMCore core;
	private MMStudioMainFrame gui;
	private AcquisitionEngine acq;
	static String latestImage;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//frame frame = new frame();
					//frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static class MessageLoop
    implements Runnable {
	MonitorWidget mmw;

	public MessageLoop(MonitorWidget mw) {
		mmw = mw;
	}
    public void run() {
		
    	Random generator = new Random(111);
    	for (int i = 0;i<10000;i++) {
    		try {
    			
				/*for (int h = 0;h<20;h++) {
					double r = (generator.nextDouble()-0.25-0.000001*i);
					mmw.addPoint(r*300000);
				}*/
    			System.out.println("in message loop main Frame");
				id.updateImage(latestImage);
				
    			mmw.repaint();
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

          
    }


}
	
	/**
	 * Create the frame.
	 */
	public MainFrame(final CMMCore core_, final MMStudioMainFrame gui_, final AcquisitionEngine acq_) {
		core = core_;
		gui = gui_;
		acq = acq_;
		addWindowListener(main_window_WindowListener);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 865, 868);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		contentPane.add(horizontalBox_3);
		
		Box verticalBox_1 = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox_1);
		
		id = new ImageDisplay();
		//final ImageDisplay id = new ImageDisplay();
		id.setAlignmentX(Component.LEFT_ALIGNMENT);
		id.setAlignmentY(Component.TOP_ALIGNMENT);
		verticalBox_1.add(id);
		//id.updateImage("file:///C://Users//herrmannsdoerfer//Desktop//Series017_z000.tif");
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox_1.add(verticalGlue);
		
		Box verticalBox = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_2);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setBorder(new TitledBorder(null, "Laser Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		horizontalBox_2.add(horizontalBox_1);
		
		final LaserPanel lp = new LaserPanel(core, "CoherentCube661","661 nm");
		horizontalBox_1.add(lp);
		final LaserPanel lp2 = new LaserPanel(core, "CoherentCube405","405 nm");
		horizontalBox_1.add(lp2);
		final CameraControl cc = new CameraControl(this, core, acq,"iXon Ultra");
		verticalBox.add(cc);
		
		final MonitorWidget mw = new MonitorWidget();
		mw.setPreferredSize(new Dimension(300, 60));
		verticalBox.add(mw);
		mw.setMinimumSize(new Dimension(0, 0));
		
		//Thread t = new Thread(new MessageLoop(mw));
        //t.start();
		/*JButton btnNewButton_1 = new JButton("Start demo");
		horizontalBox.add(btnNewButton_1);
		
		btnNewButton_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				Thread t = new Thread(new MessageLoop(mw));
		        t.start();
			}
		});*/
		
	}
	
	void setCurrentImage(String currImg) {
		//System.out.println(currImg);
		latestImage = currImg;
		id.updateImage(currImg);
	}
	
	void setCurrentImage(ImagePlus imp) {
		id.updateImage(imp);
	}
	
	
	WindowListener main_window_WindowListener =new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent arg0) {
			try {
				core.setProperty("CoherentCube661", "PowerSetpoint",0.1);
				core.setProperty("CoherentCube405", "PowerSetpoint",0.1);
				core.setProperty("iXon Ultra", "Shutter (Internal)","Closed");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};

}
