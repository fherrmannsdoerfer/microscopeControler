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
//import org.micromanager.api.AcquisitionEngine;

import org.micromanager.acquisition.AcquisitionEngine;

import mmcorej.CMMCore;

public class MainFrame extends JFrame {
	//static ij.ImagePlus ip;
	private JPanel contentPane;
	static ImageDisplay id;
	final CameraControl cc;
	private CMMCore core;
	private MMStudioMainFrame gui;
	private AcquisitionEngine acq;
	static String latestImage;
	private PifocControllWidget pcw;
	private ArduinoControl ac;
	final MonitorWidget mw;
	JLabel lblCameraStatus;
	JLabel lblAction;
	JLabel lblFrameCount;
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
    			//System.out.println("in message loop main Frame");
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
		
		Box verticalBox_2 = Box.createVerticalBox();
		contentPane.add(verticalBox_2);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_3);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setAlignmentX(Component.RIGHT_ALIGNMENT);
		horizontalBox_3.add(verticalBox_1);
		
		id = new ImageDisplay();
		id.setPreferredSize(new Dimension(640, 640));
		id.setMaximumSize(new Dimension(700, 700));
		//final ImageDisplay id = new ImageDisplay();
		id.setAlignmentX(Component.LEFT_ALIGNMENT);
		id.setAlignmentY(Component.TOP_ALIGNMENT);
		verticalBox_1.add(id);
		//id.updateImage("file:///C://Users//herrmannsdoerfer//Desktop//Series017_z000.tif");
		
		//ac = new ArduinoControl(core_);
		//ac.setBorder(new TitledBorder(null, "Arduino Data", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		//ac.setAlignmentX(Component.LEFT_ALIGNMENT);
		//verticalBox_1.add(ac);
		
		pcw = new PifocControllWidget(this, core_);
		pcw.setAlignmentX(Component.LEFT_ALIGNMENT);
		verticalBox_1.add(pcw);
		
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox_1.add(verticalGlue);
		
		Box verticalBox = Box.createVerticalBox();
		horizontalBox_3.add(verticalBox);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		final LaserControl lc = new LaserControl(core);
		verticalBox.add(lc);
		
		cc = new CameraControl(this, core, acq,"iXon Ultra");
		verticalBox.add(cc);
		
		mw = new MonitorWidget(core);
		mw.setPreferredSize(new Dimension(300, 60));
		verticalBox.add(mw);
		mw.setMinimumSize(new Dimension(0, 0));
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_4);
		
		JLabel lblNewLabel = new JLabel("Status Camera");
		horizontalBox_4.add(lblNewLabel);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue);
		
		lblCameraStatus = new JLabel("New label");
		horizontalBox_4.add(lblCameraStatus);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue_2);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue_1);
		
		lblAction = new JLabel("");
		horizontalBox_4.add(lblAction);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue_3);
		
		lblFrameCount = new JLabel("New label");
		horizontalBox_4.add(lblFrameCount);
		
		cc.setArduinoControl(ac);
		cc.setImageDisplay(id);
		id.setCameraControl(cc);
		
		final CountBlinkingEvents cbe = new CountBlinkingEvents();
		
		cc.addPSFRateCountRequiredListener(new PSFRateCountRequiredListener(){
			public void PSFRateCountRequiredEventOccured(PSFRateCountRequiredEvent event){
				int number = cbe.findNumberOfBlinkingEvents(event.getImage());
				lc.addBlinkingNumber(number);
			}
		});
		
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
	
	String getCurrentOutputFolder(){
		return cc.getCurrentOutputFolder();
	}
	
	double getExposureTime(){
		return cc.getExposureTime();
	}
	
	int getGain(){
		return cc.getGain();
	}
	
	void setCurrentImage(ImagePlus imp) {
		id.updateImage(imp);
	}
	
	void setCameraStatus(String input) {
		lblCameraStatus.setText(input);
	}
	
	void setFrameCount(String input) {
		lblFrameCount.setText(input);
	}
	
	void setAction(String input) {
		lblAction.setText(input);
	}
	
	WindowListener main_window_WindowListener =new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent arg0) {
			try {
				core.setProperty("CoherentCube661", "PowerSetpoint",0.1);
				core.setProperty("CoherentCube405", "PowerSetpoint",0.1);
				core.setProperty("iXon Ultra", "Shutter (Internal)","Closed");
				ac.stopThreads();
				cc.stopThreads();
				mw.stopThreads();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};

}
