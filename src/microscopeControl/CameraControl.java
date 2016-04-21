package microscopeControl;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.awt.Component;

import javax.swing.JCheckBox;

import java.awt.Dimension;

import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import org.micromanager.acquisition.AcquisitionEngine;
//import org.micromanager.api.AcquisitionEngine;
import org.micromanager.utils.ImageUtils;

import mmcorej.CMMCore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

public class CameraControl extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtGain;
	private JTextField txtEmGain;
	private JTextField txtExposureTime;
	private JTextField txtSavePath;
	private JTextField txtNumberFrames;
	private JTextField txtMeasurementTag;
	private JTextField txtRectX;
	private JTextField txtRectY;
	private JTextField txtRectShiftX;
	private JTextField txtRectShiftY;
	private JTextField txtRectWidth;
	private JTextField txtRectHeight;
	private JCheckBox chckbxShowMiddleLine;
	JCheckBox chckbxApplyRect;
	JLabel lblCurrTemp;
	JTextField txtSetTemp;
	JCheckBox chkboxFrameTransfer;
	JLabel lblTotalFrames;
	JLabel lblCurrentFrame;
	JLabel lblStatus;
	JButton btnStartAcquisition;
	JButton btnLoadSavePath;
	JButton btnStartLivePreview;
	JButton btnCaptureWidefieldImage;
	String camName;
	CMMCore core;
	Thread acquisitionThread;
	Thread livePreviewThread;
	MainFrame parent;
	boolean livePreviewRunning;
	JComboBox comboBoxShutter;
	JComboBox fitMethodSelectionChkBox;
	Rectangle rect;
	ImageDisplay imgDisp;
	ArduinoControl arduinoControl;
	JComboBox comboBoxWhichPart;
	JCheckBox do3DchkBox;
	JButton recalculateEverythingButton;
	JCheckBox doReconstructionChkBox;
	boolean threadShouldStayRunning = true;
	boolean aquisitionShouldContinue = true;
	
	ExecutorService executor = Executors.newFixedThreadPool(7);
	ReconControll rc = new ReconControll();
	Runnable t = new Thread(rc);
	//
	
	
	
	class TimeLoop implements Runnable {
		public TimeLoop(){}
		
		public void run(){

			while (threadShouldStayRunning) {
				try {
					String currTemp = core.getProperty(camName, "CCDTemperature");
					lblCurrTemp.setText(currTemp);
					
					if (Integer.parseInt(core.getProperty(camName, "CCDTemperature"))-Integer.parseInt(currTemp) != 0){
						lblStatus.setText("Cooling");
						parent.setCameraStatus("Cooling");
					}
					else {lblStatus.setText("Stand by");parent.setCameraStatus("Stand by");}
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	class LivePreview implements Runnable {
		LivePreview(){
			livePreviewRunning = true;
		}
		@Override
		public void run() {
			Object img;
			double exp;
			int gain;
			boolean changeParams = true;
			exp = Double.parseDouble(txtExposureTime.getText());
			gain = Integer.parseInt(txtEmGain.getText());
			int counter = 0;
			while (livePreviewRunning&&threadShouldStayRunning) {
				if (!(exp==Double.parseDouble(txtExposureTime.getText())) | !(gain == Integer.parseInt(txtEmGain.getText()))) {
					exp = Double.parseDouble(txtExposureTime.getText());
					gain = Integer.parseInt(txtEmGain.getText());
					changeParams = true;
				}
				else {changeParams = false;}
				
				try {
					if (changeParams) {
						core.setProperty(core.getCameraDevice(),"Exposure", exp);
						core.setProperty(core.getCameraDevice(),"Gain", gain);
					}
					
					//System.out.println("Gain: "+core.getProperty(core.getCameraDevice(), "Gain"));
					//System.out.println("Exposure: "+core.getProperty(core.getCameraDevice(), "Exposure"));
					core.snapImage();
					img = core.getImage();
					//ImageProcessor ipr = ImageUtils.makeProcessor(core,img);
				    //ImagePlus imp = new ImagePlus("",ipr);
					ImagePlus imp = normalizeMeasurement(img, gain);
					parent.setCurrentImage(imp);
					if (counter % 20 == 0){
						firePSFRateCountRequiredEvent(new PSFRateCountRequiredEvent(this, imp));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		void endThread() {
			livePreviewRunning = false;
		}
		
	}
	

	
	class LivePreviewWithoutCamera implements Runnable {
		LivePreviewWithoutCamera(){
			livePreviewRunning = true;
		}
		@Override
		public void run() {
			while (livePreviewRunning && threadShouldStayRunning){
				try {
		    	    ImagePlus imp = new ImagePlus("C:\\Users\\herrmannsdoerfer\\Documents\\Projects\\LaborTagebuch\\Dateien\\2014_06\\halbSchwarz.tif");
					parent.setCurrentImage(imp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Fehler beim einladen des Bildes!!!");
				}
				
			}
		}
		void endThread() {
			livePreviewRunning = false;
		}
		
	}
	
	class MessageLoop implements Runnable {
		MonitorWidget mmw;
		double exposure;
		int nbrFrames;
		int gain;
		String path;
		String measurementTag;
		boolean useFirstVariableSet = true; // Variable which determines which set of variables is used for storage of the current stack (the other variables are saved)
		ImageStack stackLeft;
        ImageStack stackRight;
        ImageStack stackLeft2;
        ImageStack stackRight2;
        int stackCounter;
		public MessageLoop(double exposure, int nbrFrames, int gain, String path, String measurementTag) {
			this.exposure = exposure;
			this.nbrFrames = nbrFrames;
			this.gain = gain;
			this.path = path;
			this.measurementTag = measurementTag;
		}
		public void run() {
	    	Object img;
	    	try {
	    		
	    		(new File(path+"\\"+measurementTag)).mkdirs();
	    		(new File(path+"\\"+measurementTag+"\\LeftChannel"+measurementTag)).mkdirs();
	    		(new File(path+"\\"+measurementTag+"\\RightChannel"+measurementTag)).mkdirs();
	    		(new File(path+"\\"+measurementTag+"\\LeftChannel")).mkdirs();
	    		(new File(path+"\\"+measurementTag+"\\RightChannel")).mkdirs();
	    		PrintWriter outputStream = new PrintWriter(new FileWriter(path+"\\"+measurementTag+"\\log_ArduinoVoltage"+measurementTag+".txt"));
				outputStream.println("Automatically generated log file for Arduino Analog Input");
				core.setCircularBufferMemoryFootprint(2000);
				//core.prepareSequenceAcquisition(core.getCameraDevice());
				//System.out.println("prepared");
		    	//System.out.println(exposure);
		    	core.setProperty(core.getCameraDevice(),"Exposure", exposure);
		    	core.startSequenceAcquisition(nbrFrames+5000, exposure, false);
		    	aquisitionShouldContinue = true;
		    	int frame = 0;
		    	lblStatus.setText("Acquisition");
		    	parent.setAction("Acquisition");
	    		lblTotalFrames.setText(" / "+String.valueOf(nbrFrames));
	    		createLogFile(measurementTag, gain, exposure, path, nbrFrames);
	    		

	            core.getBytesPerPixel();
	            int imgWidth, imgHeight;
	            
	            if (chckbxApplyRect.isSelected()) {
	            	imgWidth = getRectWidth();
	            	imgHeight =  getRectHeight();
	            }
	            else {
	            	imgWidth = 256;
	            	imgHeight = 512;
	            }
	           
	       
	            stackLeft = new ImageStack(imgWidth, imgHeight);
	            stackRight = new ImageStack(imgWidth, imgHeight);
	            stackLeft2 = new ImageStack(imgWidth, imgHeight);
	            stackRight2 = new ImageStack(imgWidth, imgHeight);
	            double bytesPerFrame = imgWidth * imgHeight * 2; // 16 bit images
	            int maxImagesPerStack = (((int) Math.floor(3.e9 / bytesPerFrame))/1000) * 1000; // max 3. GB per Stack
	            int imagesInCurrentStack = 0;
	            stackCounter = 0;
	            
		    	System.currentTimeMillis();
		    	while (frame<nbrFrames && aquisitionShouldContinue){//core.getRemainingImageCount() > 0 || core.isSequenceRunning(core.getCameraDevice())) {//for whatever reason a few frames are always missing, so the loop will not exit...
		    	   if (core.getRemainingImageCount() > 0) {
		    		  //System.out.println("Remaining image count: "+core.getRemainingImageCount());
		    		  lblCurrentFrame.setText(String.valueOf(frame+1));
		    		  parent.setFrameCount(String.valueOf(frame+1)+" / "+String.valueOf(nbrFrames));
		    		  //System.out.println(frame);
		    	      img = core.popNextImage();
		    	      //ImageProcessor ipr2 = ImageUtils.makeProcessor(core,img);
					  //ImagePlus imp = new ImagePlus("",ipr2);
		    	      ImagePlus imp = normalizeMeasurement(img, gain);
		    	      ArrayList<ImagePlus> channels = cropImages(imp.getProcessor(), chckbxApplyRect.isSelected());
		    	      
		    	      if (frame%20 == 0){
		    	    	  firePSFRateCountRequiredEvent(new PSFRateCountRequiredEvent(this, imp));
		    	      }
		    
		    	      
		    	      if (comboBoxWhichPart.getSelectedIndex() == 0){
		    	    	  //FileSaver fs = new FileSaver(channels.get(0));
		    	    	  //fname = "\\imgLeft_"+measurementTag+String.format("_%05d", frame)+".tiff";
			    	      //fs.saveAsTiff(path+"\\"+measurementTag+"\\LeftChannel"+measurementTag+fname);
			    	      //FileSaver fs2 = new FileSaver(channels.get(1));
			    	      //fname = "\\imgRight_"+measurementTag+String.format("_%05d", frame)+".tiff";
			    	      //fs2.saveAsTiff(path+"\\"+measurementTag+"\\RightChannel"+measurementTag+fname);
		    	    	  if (useFirstVariableSet){
		    	    		  stackLeft.addSlice(channels.get(0).getProcessor());
				    	      stackRight.addSlice(channels.get(1).getProcessor());
		    	    	  }
		    	    	  else {
		    	    		  stackLeft2.addSlice(channels.get(0).getProcessor());
				    	      stackRight2.addSlice(channels.get(1).getProcessor());
		    	    	  }
			    	      
		    	      }
		    	      else if (comboBoxWhichPart.getSelectedIndex() == 1){
		    	    	  //FileSaver fs = new FileSaver(channels.get(0));
		    	    	  //fname = "\\imgLeft_"+measurementTag+String.format("_%05d", frame)+".tiff";
			    	      //fs.saveAsTiff(path+"\\"+measurementTag+"\\LeftChannel"+measurementTag+fname);
			    	      //ipLC.setPixels(leftShort);
			    	      if (useFirstVariableSet){
		    	    		  stackLeft.addSlice(channels.get(0).getProcessor());
		    	    	  }
		    	    	  else {
		    	    		  stackLeft2.addSlice(channels.get(0).getProcessor());
		    	    	  }
		    	      }
		    	      else {
		    	    	  //FileSaver fs = new FileSaver(channels.get(1));
		    	    	  //fname = "\\imgRight_"+measurementTag+String.format("_%05d", frame)+".tiff";
			    	      //fs.saveAsTiff(path+"\\"+measurementTag+"\\RightChannel"+measurementTag+fname);
			    	      //ipRC.setPixels(rightShort);
		    	    	  if (useFirstVariableSet){
		    	    		  stackLeft.addSlice(channels.get(1).getProcessor());
		    	    	  }
		    	    	  else {
		    	    		  stackLeft2.addSlice(channels.get(1).getProcessor());
		    	    	  }
		    	      }
		    	      imagesInCurrentStack += 1;
		    	      /*try {
							outputStream.println(frame+ " "+ arduinoControl.getAnalogInput());
					  } catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
					  }*/
		    	      imgDisp.updateImage(imp);
		    	      frame++;
		    	   }
		    	   else {
		    		   Thread.sleep(100);
		    	   }
		    	  // System.out.println(imagesInCurrentStack+" "+maxImagesPerStack);
		    	   if (imagesInCurrentStack == maxImagesPerStack){
		    		   imagesInCurrentStack = 0;
		    		   stackCounter+=1;
		    		   if (useFirstVariableSet){
		    			   stackLeft2 = new ImageStack(imgWidth, imgHeight);
		    			   stackRight2 = new ImageStack(imgWidth, imgHeight);
		    			   Thread saveStackThread = new Thread(new Runnable(){
		    				   @Override
		    				   public void run(){
		    					   writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft,stackRight,
		    							   doReconstructionChkBox.isSelected()); 
		    				   }
		    			   });
		    			   saveStackThread.start();
		    			   //writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft,stackRight);
		    		   }
		    		   else{
		    			   stackLeft = new ImageStack(imgWidth, imgHeight);
		    			   stackRight = new ImageStack(imgWidth, imgHeight);
		    			   Thread saveStackThread = new Thread(new Runnable(){
		    				   @Override
		    				   public void run(){
		    					   writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft2,stackRight2,
		    							   doReconstructionChkBox.isSelected()); 
		    				   }
		    			   });
		    			   saveStackThread.start();
		    			   //writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft2,stackRight2);
		    		   }
		    		   useFirstVariableSet = !useFirstVariableSet;
		    	   }
		    	}
		    	outputStream.close();
		    	try{
		    		stackCounter+=1;
			    	if (useFirstVariableSet){
			    		Thread saveStackThread = new Thread(new Runnable(){
		    				   @Override
		    				   public void run(){
		    					   writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft,stackRight,
		    							   doReconstructionChkBox.isSelected()); 
		    				   }
		    			 });
			    		saveStackThread.start();
	    			   //writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft,stackRight);
	    		   }
	    		   else{
	    			   Thread saveStackThread = new Thread(new Runnable(){
	    				   @Override
	    				   public void run(){
	    					   writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft2,stackRight2,
	    							   doReconstructionChkBox.isSelected());
	    				   }
	    			   });
	    			   saveStackThread.start();
	    			   //writeStacks(comboBoxWhichPart.getSelectedIndex(), stackCounter, measurementTag,stackLeft2,stackRight2);
	    		   }
		    	}
		    	catch(Exception e){
		    		
		    	}
	    	
	    	
	    	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	btnStartAcquisition.setEnabled(true);
	    	try {
				core.stopSequenceAcquisition();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }
		private void writeStacks(int selectedIndex, int stackCounter,
				String measurementTag, ImageStack stackLeft, ImageStack stackRight, 
				boolean doSimultaneousReconstruction) {
			if (selectedIndex == 0){
				ImagePlus leftStack = new ImagePlus("", stackLeft);
				ImagePlus rightStack = new ImagePlus("", stackRight);
				FileSaver fs = new FileSaver(leftStack);
				String basename1 = "LeftChannel"+measurementTag+"pt"+String.format("%03d", stackCounter);
				String pathTiffFile1 = path+"\\"+measurementTag+"\\LeftChannel\\"+basename1+".tif";
				fs.saveAsTiffStack(pathTiffFile1);
				FileSaver fs2 = new FileSaver(rightStack);
				String basename2 = "RightChannel"+measurementTag+"pt"+String.format("%03d", stackCounter);
				String pathTiffFile2 = path+"\\"+measurementTag+"\\RightChannel\\"+basename2+".tif";
				fs2.saveAsTiffStack(pathTiffFile2);
	    		if (doSimultaneousReconstruction){
	    			startPython(pathTiffFile1, basename1);
	    			startPython(pathTiffFile2, basename2);
	    		}
	    	}
  	        else if (selectedIndex == 1){
  	        	ImagePlus leftStack = new ImagePlus("", stackLeft);
  	        	FileSaver fs = new FileSaver(leftStack);
				String basename1 = "LeftChannel"+measurementTag+"pt"+String.format("%03d", stackCounter);
				String pathTiffFile1 = path+"\\"+measurementTag+"\\LeftChannel\\"+basename1+".tif";
				fs.saveAsTiffStack(pathTiffFile1);
    			fs.saveAsTiffStack(path+"\\"+measurementTag+"\\LeftChannel"+measurementTag+".tif");
    			if (doSimultaneousReconstruction){
	    			startPython(pathTiffFile1, basename1);
	    		}
  	    	}
  	        else {
    			ImagePlus rightStack = new ImagePlus("", stackRight);
    			FileSaver fs2 = new FileSaver(rightStack);
    			String basename2 = "RightChannel"+measurementTag+"pt"+String.format("%03d", stackCounter);
				String pathTiffFile2 = path+"\\"+measurementTag+"\\RightChannel\\"+basename2+".tif";
				fs2.saveAsTiffStack(pathTiffFile2);
    			if (doSimultaneousReconstruction){
	    			startPython(pathTiffFile2, basename2);
	    		}
  	        }
		}
		
	}

	ArrayList<ImagePlus> cropImages(ImageProcessor imp, boolean applyRect){
		if (applyRect){
			imp.setRoi(getRectX(),getRectY(), getRectWidth(), getRectHeight());
		}
		else {
			imp.setRoi(0,0,256,512);
		}
		ImageProcessor leftChannel = imp.crop();
		if (applyRect){
			imp.setRoi(getRectX() + getShiftX(),getRectY()+getShiftY(), getRectWidth(), getRectHeight());
		}
		else {
			imp.setRoi(256,0,256,512);
		}
		ImageProcessor rightChannel = imp.crop();
		ImagePlus leftImg = new ImagePlus("", leftChannel);
		ImagePlus rightImg = new ImagePlus("", rightChannel);
		ArrayList<ImagePlus> list = new ArrayList<ImagePlus>();
		list.add(leftImg);
		list.add(rightImg);
		return list;
	}
	/**
	 * Create the panel.
	 */
	public CameraControl(MainFrame parent_, final CMMCore core_, final AcquisitionEngine acq, String camName_) {
		executor.execute(t); //start thread that handles reconstruction
				
		core = core_;
		camName = camName_;
		parent = parent_;
		setMinimumSize(new Dimension(260, 200));
		setPreferredSize(new Dimension(300, 326));
		setMaximumSize(new Dimension(400, 300));
		setBorder(new TitledBorder(null, "Camera Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		
		Box verticalBox = Box.createVerticalBox();
		add(verticalBox);
		
		String options[] = {"open","closed"};
		
		Component verticalGlue_5 = Box.createVerticalGlue();
		verticalBox.add(verticalGlue_5);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_5);
		
		txtSavePath = new JTextField();
		txtSavePath.setText("D:\\MessungenTemp");
		txtSavePath.setHorizontalAlignment(SwingConstants.LEFT);
		txtSavePath.setMinimumSize(new Dimension(6, 10));
		txtSavePath.setPreferredSize(new Dimension(6, 10));
		txtSavePath.setMaximumSize(new Dimension(200, 50));
		horizontalBox_5.add(txtSavePath);
		txtSavePath.setColumns(400);
		
		Component horizontalGlue_5 = Box.createHorizontalGlue();
		horizontalBox_5.add(horizontalGlue_5);
		
		btnLoadSavePath = new JButton("Set path");
		btnLoadSavePath.addActionListener(btnLoadSavePathActionListener);
		horizontalBox_5.add(btnLoadSavePath);
		
		Component verticalGlue_3 = Box.createVerticalGlue();
		verticalBox.add(verticalGlue_3);
		
		Box horizontalBox_10 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_10);
		
		JLabel lblNewLabel_5 = new JLabel("Measurement tag");
		horizontalBox_10.add(lblNewLabel_5);
		
		Component horizontalGlue_10 = Box.createHorizontalGlue();
		horizontalBox_10.add(horizontalGlue_10);
		
		txtMeasurementTag = new JTextField();
		txtMeasurementTag.setText("test2");
		txtMeasurementTag.setMaximumSize(new Dimension(200, 50));
		txtMeasurementTag.setColumns(40);
		txtMeasurementTag.setMinimumSize(new Dimension(6, 10));
		txtMeasurementTag.setPreferredSize(new Dimension(6, 25));
		horizontalBox_10.add(txtMeasurementTag);
		
		Component verticalGlue_8 = Box.createVerticalGlue();
		verticalBox.add(verticalGlue_8);
		
		Box horizontalBox_8 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_8);
		
		btnStartLivePreview = new JButton("Start Live preview");
		btnStartLivePreview.addActionListener(btnStartLivePreviewActionListener);
		horizontalBox_8.add(btnStartLivePreview);
		
		Component horizontalGlue_9 = Box.createHorizontalGlue();
		horizontalBox_8.add(horizontalGlue_9);
		
		JButton btnStopLivePreview = new JButton("Stop Live preview");
		btnStopLivePreview.addActionListener(btnStopLivePreviewActionListener);
		horizontalBox_8.add(btnStopLivePreview);
		
		Component verticalGlue_7 = Box.createVerticalGlue();
		verticalBox.add(verticalGlue_7);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		
		JButton btnStartAcquisition = new JButton("Start Acquisition");
		btnStartAcquisition.addActionListener(btnStartAcquisitionActionListener);
		horizontalBox.add(btnStartAcquisition);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_1);
		
		JButton btnAbortAcquisition = new JButton("End Acquisition");
		btnAbortAcquisition.addActionListener(btnAbortAcquisitionActionListener);
		horizontalBox.add(btnAbortAcquisition);
		
		JButton btnCaptureWidefieldImage = new JButton("Capture Widefield Image");
		btnCaptureWidefieldImage.addActionListener(btnCaptureWidefieldImageActionListener);
		btnCaptureWidefieldImage.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox.add(btnCaptureWidefieldImage);
		
		Box horizontalBox_7 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_7);
		
		lblStatus = new JLabel("Stand by");
		horizontalBox_7.add(lblStatus);
		
		Component horizontalGlue_7 = Box.createHorizontalGlue();
		horizontalBox_7.add(horizontalGlue_7);
		
		lblCurrentFrame = new JLabel("");
		horizontalBox_7.add(lblCurrentFrame);
		
		lblTotalFrames = new JLabel("");
		horizontalBox_7.add(lblTotalFrames);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.NORTH);
		
		Box verticalBox_2 = Box.createVerticalBox();
		tabbedPane.addTab("Camera Settings", null, verticalBox_2, null);
		
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_1);
		
		JLabel lblNewLabel = new JLabel("Camera gain");
		horizontalBox_1.add(lblNewLabel);
		
		Component horizontalGlue_4 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_4);
		
		txtGain = new JTextField();
		txtGain.setText("1");
		txtGain.setHorizontalAlignment(SwingConstants.RIGHT);
		txtGain.setMaximumSize(new Dimension(50, 50));
		txtGain.setMinimumSize(new Dimension(6, 10));
		txtGain.setPreferredSize(new Dimension(6, 10));
		horizontalBox_1.add(txtGain);
		txtGain.setColumns(3);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_2);
		
		JLabel lblNewLabel_1 = new JLabel("EM gain");
		horizontalBox_2.add(lblNewLabel_1);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_3);
		
		txtEmGain = new JTextField();
		txtEmGain.setText("10");
		txtEmGain.setMaximumSize(new Dimension(50, 50));
		txtEmGain.setHorizontalAlignment(SwingConstants.RIGHT);
		txtEmGain.setPreferredSize(new Dimension(3, 10));
		txtEmGain.setMinimumSize(new Dimension(3, 16));
		horizontalBox_2.add(txtEmGain);
		txtEmGain.setColumns(3);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_1);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_3);
		
		JLabel lblNewLabel_2 = new JLabel("Exposure time");
		horizontalBox_3.add(lblNewLabel_2);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox_3.add(horizontalGlue);
		
		txtExposureTime = new JTextField();
		txtExposureTime.setText("100");
		txtExposureTime.setHorizontalAlignment(SwingConstants.RIGHT);
		horizontalBox_3.add(txtExposureTime);
		txtExposureTime.setMaximumSize(new Dimension(200, 50));
		txtExposureTime.setMinimumSize(new Dimension(16, 16));
		txtExposureTime.setPreferredSize(new Dimension(16, 16));
		txtExposureTime.setColumns(3);
		
		Component verticalGlue_6 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_6);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_2.add(verticalBox_1);
		
		Box horizontalBox_9 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_9);
		
		JLabel lblTemperatur = new JLabel("Temperatur");
		horizontalBox_9.add(lblTemperatur);
		
		Component horizontalGlue_8 = Box.createHorizontalGlue();
		horizontalBox_9.add(horizontalGlue_8);
		
		txtSetTemp = new JTextField();
		txtSetTemp.setText("-70");
		txtSetTemp.addActionListener(txtSetTempActionListener);
		horizontalBox_9.add(txtSetTemp);
		txtSetTemp.setMaximumSize(new Dimension(400, 400));
		txtSetTemp.setMinimumSize(new Dimension(6, 20));
		txtSetTemp.setColumns(4);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox_9.add(horizontalStrut);
		
		lblCurrTemp = new JLabel("New label");
		horizontalBox_9.add(lblCurrTemp);
		
		Component verticalGlue_2 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_2);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_4);
		
		chkboxFrameTransfer = new JCheckBox("Frame transfer");
		chkboxFrameTransfer.setSelected(true);
		chkboxFrameTransfer.addActionListener(chkboxFrameTransferActionListener);
		
		JLabel lblNewLabel_4 = new JLabel("Shutter");
		horizontalBox_4.add(lblNewLabel_4);
		comboBoxShutter = new JComboBox(options);
		comboBoxShutter.addActionListener(comboBoxShutterActionListener);
		horizontalBox_4.add(comboBoxShutter);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox_4.add(horizontalGlue_2);
		horizontalBox_4.add(chkboxFrameTransfer);
		
		Component verticalGlue_4 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_4);
		
		Box horizontalBox_6 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_6);
		
		JLabel lblNewLabel_3 = new JLabel("Number frames");
		horizontalBox_6.add(lblNewLabel_3);
		
		Component horizontalGlue_6 = Box.createHorizontalGlue();
		horizontalBox_6.add(horizontalGlue_6);
		
		txtNumberFrames = new JTextField();
		txtNumberFrames.setText("1000");
		txtNumberFrames.setHorizontalAlignment(SwingConstants.RIGHT);
		txtNumberFrames.setMaximumSize(new Dimension(50, 50));
		horizontalBox_6.add(txtNumberFrames);
		txtNumberFrames.setColumns(6);
		
		Box verticalBox_3 = Box.createVerticalBox();
		tabbedPane.addTab("ROI Settings", null, verticalBox_3, null);
		
		Box horizontalBox_11 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_11);
		
		JLabel lblNewLabel_6 = new JLabel("X:");
		horizontalBox_11.add(lblNewLabel_6);
		
		txtRectX = new JTextField();
		txtRectX.setPreferredSize(new Dimension(60, 20));
		txtRectX.setMaximumSize(new Dimension(400, 200));
		txtRectX.setAlignmentX(Component.RIGHT_ALIGNMENT);
		horizontalBox_11.add(txtRectX);
		txtRectX.setColumns(10);
		
		Component horizontalGlue_13 = Box.createHorizontalGlue();
		horizontalBox_11.add(horizontalGlue_13);
		
		JLabel lblNewLabel_11 = new JLabel("Width:");
		horizontalBox_11.add(lblNewLabel_11);
		
		txtRectWidth = new JTextField();
		txtRectWidth.setMaximumSize(new Dimension(400, 2147483647));
		horizontalBox_11.add(txtRectWidth);
		txtRectWidth.setColumns(10);
		
		Box horizontalBox_12 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_12);
		
		JLabel lblNewLabel_7 = new JLabel("Y:");
		horizontalBox_12.add(lblNewLabel_7);
		
		txtRectY = new JTextField();
		txtRectY.setMaximumSize(new Dimension(400, 2147483647));
		horizontalBox_12.add(txtRectY);
		txtRectY.setColumns(10);
		
		Component horizontalGlue_14 = Box.createHorizontalGlue();
		horizontalBox_12.add(horizontalGlue_14);
		
		JLabel lblNewLabel_12 = new JLabel("Height:");
		horizontalBox_12.add(lblNewLabel_12);
		
		txtRectHeight = new JTextField();
		txtRectHeight.setMaximumSize(new Dimension(400, 2147483647));
		horizontalBox_12.add(txtRectHeight);
		txtRectHeight.setColumns(10);
		
		Box horizontalBox_13 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_13);
		
		JLabel lblNewLabel_8 = new JLabel("shiftX");
		horizontalBox_13.add(lblNewLabel_8);
		
		txtRectShiftX = new JTextField();
		txtRectShiftX.setText("256");
		txtRectShiftX.setMaximumSize(new Dimension(400, 2147483647));
		horizontalBox_13.add(txtRectShiftX);
		txtRectShiftX.setColumns(10);
		
		Component horizontalGlue_15 = Box.createHorizontalGlue();
		horizontalBox_13.add(horizontalGlue_15);
		
		JLabel lblNewLabel_9 = new JLabel("shiftY:");
		horizontalBox_13.add(lblNewLabel_9);
		
		txtRectShiftY = new JTextField();
		txtRectShiftY.setText("0");
		txtRectShiftY.setMaximumSize(new Dimension(400, 2147483647));
		horizontalBox_13.add(txtRectShiftY);
		txtRectShiftY.setColumns(10);
		
		Box horizontalBox_15 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_15);
		
		JLabel lblNewLabel_10 = new JLabel("Which part?");
		horizontalBox_15.add(lblNewLabel_10);
		
		Component horizontalGlue_11 = Box.createHorizontalGlue();
		horizontalBox_15.add(horizontalGlue_11);
		
		Vector<String> items = new Vector<String>();
		items.add("Both Channels");
		items.add("Left Channel Only");
		items.add("Right Channel Only");
		comboBoxWhichPart = new JComboBox(items);
		comboBoxWhichPart.setSelectedIndex(0);
		horizontalBox_15.add(comboBoxWhichPart);
		
		Box horizontalBox_14 = Box.createHorizontalBox();
		verticalBox_3.add(horizontalBox_14);
		
		chckbxShowMiddleLine = new JCheckBox("Show Middle Line");
		horizontalBox_14.add(chckbxShowMiddleLine);
		
		JButton btnClearRect = new JButton("Clear Rect");
		btnClearRect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imgDisp.resetRect();
			}
		});
		horizontalBox_14.add(btnClearRect);
		
		Component horizontalGlue_12 = Box.createHorizontalGlue();
		horizontalBox_14.add(horizontalGlue_12);
		
		chckbxApplyRect = new JCheckBox("Apply Rectangle");
		horizontalBox_14.add(chckbxApplyRect);
		
		Box verticalBox_4 = Box.createVerticalBox();
		tabbedPane.addTab("RapidSTORM", null, verticalBox_4, null);
		
		Box horizontalBox_16 = Box.createHorizontalBox();
		verticalBox_4.add(horizontalBox_16);
		
		doReconstructionChkBox = new JCheckBox("Do simultaneous processing");
		doReconstructionChkBox.setSelected(true);
		horizontalBox_16.add(doReconstructionChkBox);
		
		Component horizontalGlue_16 = Box.createHorizontalGlue();
		horizontalBox_16.add(horizontalGlue_16);
		
		do3DchkBox = new JCheckBox("3D data");
		do3DchkBox.setSelected(true);
		horizontalBox_16.add(do3DchkBox);
		
		Component verticalGlue_9 = Box.createVerticalGlue();
		verticalBox_4.add(verticalGlue_9);
		
		Box horizontalBox_17 = Box.createHorizontalBox();
		verticalBox_4.add(horizontalBox_17);
		
		JLabel lblNewLabel_13 = new JLabel("Calibration File");
		horizontalBox_17.add(lblNewLabel_13);
		
		Component horizontalGlue_18 = Box.createHorizontalGlue();
		horizontalBox_17.add(horizontalGlue_18);
		
		calibrationFileText = new JTextField();
		calibrationFileText.setText("D:\\MessungenTemp\\Calibration141107KalibrationSchalenLinseAligned_cropped-sigma-table.txt");
		calibrationFileText.setMinimumSize(new Dimension(200, 20));
		calibrationFileText.setPreferredSize(new Dimension(200, 20));
		calibrationFileText.setMaximumSize(new Dimension(200, 20));
		horizontalBox_17.add(calibrationFileText);
		calibrationFileText.setColumns(100);
		
		Component verticalGlue_10 = Box.createVerticalGlue();
		verticalBox_4.add(verticalGlue_10);
		
		Box horizontalBox_18 = Box.createHorizontalBox();
		verticalBox_4.add(horizontalBox_18);
		
		JLabel lblNewLabel_14 = new JLabel("Threshold");
		horizontalBox_18.add(lblNewLabel_14);
		
		Component horizontalGlue_17 = Box.createHorizontalGlue();
		horizontalBox_18.add(horizontalGlue_17);
		
		fitMethodSelectionChkBox = new JComboBox();
		fitMethodSelectionChkBox.addItem("Local Relative Threshold");
		fitMethodSelectionChkBox.addItem("Absolute Threshold");
		
		horizontalBox_18.add(fitMethodSelectionChkBox);
		
		thresholdText = new JTextField();
		thresholdText.setText("30");
		thresholdText.setMaximumSize(new Dimension(50, 20));
		horizontalBox_18.add(thresholdText);
		thresholdText.setColumns(10);
		
		Component verticalGlue_11 = Box.createVerticalGlue();
		verticalBox_4.add(verticalGlue_11);
		
		Box horizontalBox_19 = Box.createHorizontalBox();
		verticalBox_4.add(horizontalBox_19);
		
		recalculateEverythingButton = new JButton("Recalculate everything");
		recalculateEverythingButton.addActionListener(recalculateEverythingButtonActionListener);
		horizontalBox_19.add(recalculateEverythingButton);
		
		Thread timeLoopThread = new Thread(new TimeLoop());
		timeLoopThread.start();
		
	}
		
	ActionListener recalculateEverythingButtonActionListener = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			String path = txtSavePath.getText();
			String measurementTag = txtMeasurementTag.getText();
			File dir = new File(path+"\\"+measurementTag+"\\RightChannel\\");
	    	File[] files = dir.listFiles(new FilenameFilter() { 
	    	         public boolean accept(File dir, String filename)
	    	              { return filename.endsWith(".tif"); }
	    	} );
	    	if (comboBoxWhichPart.getSelectedIndex() == 0||(comboBoxWhichPart.getSelectedIndex() == 1)){
		    	for (int i=0;i<files.length;i++){
		    		System.out.println(files[i].toString());
		    		String fname = files[i].getName();
		    		String[] parts = fname.split("\\.");
		    		String basename = parts[0];
		    		startPython(files[i].toString(),basename);
		    	}
		    }
	    	if (comboBoxWhichPart.getSelectedIndex() == 0||(comboBoxWhichPart.getSelectedIndex() == 2)){
		    	for (int i=0;i<files.length;i++){
		    		System.out.println(files[i].toString());
		    		String fname = files[i].getName();
		    		String[] parts = fname.split("\\.");
		    		String basename = parts[0];
		    		startPython(files[i].toString(),basename);
		    	}
		    }
	    	dir = new File(path+"\\"+measurementTag+"\\LeftChannel\\");
	    	files = dir.listFiles(new FilenameFilter() { 
	    	         public boolean accept(File dir, String filename)
	    	              { return filename.endsWith(".tif"); }
	    	} );
	    	for (int i=0;i<files.length;i++){
	    		System.out.println(files[i].toString());
	    		String fname = files[i].getName();
	    		String[] parts = fname.split("\\.");
	    		String basename = parts[0];
	    		startPython(files[i].toString(),basename);
	    	}
	    	
		}
	};
	
	ActionListener btnLoadSavePathActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			//System.out.println("hisafs;djf;sadfj;");
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
			int retVal = fc.showOpenDialog(btnLoadSavePath);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				txtSavePath.setText(fc.getSelectedFile().getPath());
			}
		}
	};
	ActionListener btnStartAcquisitionActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			if (checkSettings()){
				livePreviewRunning = false;
				try {
					if (chkboxFrameTransfer.isSelected()) {
						core.setProperty(camName, "FrameTransfer", "On");
					}
					else {
						core.setProperty(camName, "FrameTransfer", "Off");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					//System.out.println(txtEmGain.getText());
					//System.out.println(Integer.parseInt(txtEmGain.getText()));
					core.setProperty(camName,"Gain",Integer.parseInt(txtEmGain.getText()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				double exp = Double.parseDouble(txtExposureTime.getText());
				int nbr = Integer.parseInt(txtNumberFrames.getText());
				int gain = Integer.parseInt(txtEmGain.getText());
				String path = txtSavePath.getText();
				String measurementTag = txtMeasurementTag.getText();
				acquisitionThread = new Thread(new MessageLoop(exp,nbr,gain,path,measurementTag));
				acquisitionThread.start();
				btnStartAcquisition.setEnabled(false);
			}
			else{
				System.out.println("The settings check went wrong!");
			}
		}
	};

	ActionListener btnAbortAcquisitionActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			try {
				aquisitionShouldContinue = false;
				//core.stopSequenceAcquisition();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			btnStartAcquisition.setEnabled(true);
		}
	};
	
	ActionListener txtSetTempActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				if ( Integer.parseInt(txtSetTemp.getText())>-80 &&  Integer.parseInt(txtSetTemp.getText())<30) {
					core.setProperty(camName, "CCDTemperatureSetPoint", Integer.parseInt(txtSetTemp.getText()));
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		}
	};
	
	ActionListener chkboxFrameTransferActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try{
				if (chkboxFrameTransfer.isSelected()) {
					core.setProperty(camName, "FrameTransfer", "On");
				}
				else {
					core.setProperty(camName, "FrameTransfer", "Off");
				}
			}
			catch (Exception e2){
				e2.printStackTrace();
			}
		}
	};
	
	ActionListener btnStartLivePreviewActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			try {
				core.setProperty(core.getCameraDevice(),"Exposure", Double.parseDouble(txtExposureTime.getText()));
				livePreviewThread = new Thread(new LivePreview());
				//livePreviewThread = new Thread(new LivePreviewWithoutCamera());
				livePreviewThread.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			btnStartLivePreview.setEnabled(false);
		}
	};
	
	ActionListener btnStopLivePreviewActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			try {
				livePreviewRunning = false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			btnStartLivePreview.setEnabled(true);
		}
	};
	ActionListener comboBoxShutterActionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			if (comboBoxShutter.getSelectedItem().toString().equals("open")){
				try {
					livePreviewRunning = false;
					btnStartLivePreview.setEnabled(true);
					core.setProperty("iXon Ultra", "Shutter (Internal)","Open");	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				try {
					livePreviewRunning = false;
					btnStartLivePreview.setEnabled(true);
					core.setProperty("iXon Ultra", "Shutter (Internal)","Closed");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	ActionListener btnCaptureWidefieldImageActionListener = new ActionListener(){
		public void actionPerformed(ActionEvent arg0) {
			try {
				Object img;
				double exp = Double.parseDouble(txtExposureTime.getText());
				int gain = Integer.parseInt(txtEmGain.getText());
				core.setProperty(core.getCameraDevice(),"Exposure", exp);
				core.setProperty(core.getCameraDevice(),"Gain", gain);
				
				//System.out.println("Gain: "+core.getProperty(core.getCameraDevice(), "Gain"));
				//System.out.println("Exposure: "+core.getProperty(core.getCameraDevice(), "Exposure"));
				core.snapImage();
				img = core.getImage();
				ImageProcessor ipr = ImageUtils.makeProcessor(core,img);
	    	    ImagePlus imp = new ImagePlus("",ipr);
	    	    FileSaver fs = new FileSaver(imp);
	    	    String measurementTag = txtMeasurementTag.getText();
	    	    String path = txtSavePath.getText();
	    	    (new File(path+"\\"+measurementTag)).mkdirs();
	    	    String fname = "\\widefieldimg_1_"+measurementTag+".tiff";
	    	    new File(path+"\\"+measurementTag+fname);
	    	  
	    		int counter = 1;
	    		while(true){
	    			counter = counter + 1;
	    			File f3 = new File(path+"\\"+measurementTag+"\\widefieldimg_"+counter+"_"+measurementTag+".tiff");
	    			if (f3.exists()){
	    				
	    			}
	    			else{
	    				fs.saveAsTiff(path+"\\"+measurementTag+"\\widefieldimg_"+counter+"_"+measurementTag+".tiff");
	    				break;
	    			}
	    		}
	    	   
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	private JTextField calibrationFileText;
	private JTextField thresholdText;
	
	
	void createLogFile(String measurementTag, int gain, double exposure, String path, int nbrFrames) {
		try {
			String fname = "\\log_"+measurementTag+".txt";
			PrintWriter outputStream = new PrintWriter(new FileWriter(path+"\\"+measurementTag+fname));
			outputStream.println("Automatically generated log file for measurement"+measurementTag);
			outputStream.println("Gain: "+String.valueOf(gain));
			outputStream.println("Exposure time: "+String.valueOf(exposure));
			outputStream.println("Number Frames: "+ String.valueOf(nbrFrames));
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	protected boolean checkSettings() {
		boolean startMeasurement = true;
		String path = txtSavePath.getText();
		String measurementTag = txtMeasurementTag.getText();
		if ((new File(path+"\\"+measurementTag+"\\LeftChannel")).exists()||(new File(path+"\\"+measurementTag+"\\RightChannel")).exists()){
			int dialogResult = JOptionPane.showConfirmDialog (null, "The directory where you save your movie does already exist! Do you want to continue?","Warning",JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				
			}
			else{
				return false;
			}
		}
		
		if (Integer.parseInt(txtRectWidth.getText())<256 && Integer.parseInt(txtRectHeight.getText())<512&&!chckbxApplyRect.isSelected()){
			int dialogResult = JOptionPane.showConfirmDialog (null, "You chose a ROI but did not check apply rectangle! Do you wish to record only the selected ROI?","Warning",JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				chckbxApplyRect.setSelected(true);
			}
			else{
				
			}
		}
		if(Integer.parseInt(txtRectWidth.getText())<100 || Integer.parseInt(txtRectHeight.getText())<100){
			int dialogResult = JOptionPane.showConfirmDialog (null, "The selected ROI is quite small ("+txtRectWidth.getText()+" x "+txtRectHeight.getText()+" pixels)! Do you wish to proceed?","Warning",JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				
			}
			else{
				return false;
			}
		}
		
		
		return startMeasurement;
	}
	String getCurrentOutputFolder(){
		String measurementTag = txtMeasurementTag.getText();
	    String path = txtSavePath.getText();
		return path+"\\"+measurementTag;
	}
	int getGain(){
		return Integer.parseInt(txtEmGain.getText());
	}
	
	double getExposureTime(){
		return Double.parseDouble(txtExposureTime.getText());
	}
	
	public void setImageDisplay(ImageDisplay imgDisp){
		this.imgDisp = imgDisp;
	}
	void setRect(Rectangle rect){
		this.rect = rect;
	}
	void setRectX(int x){
		txtRectX.setText(Integer.toString(x));
	}
	int getRectX(){
		return Integer.parseInt(txtRectX.getText());
	}
	void setRectY(int y){
		txtRectY.setText(Integer.toString(y));
	}
	int getRectY(){
		return Integer.parseInt(txtRectY.getText());
	}
	void setRectWidth(int width){
		txtRectWidth.setText(Integer.toString(width));
	}
	int getRectWidth(){
		return Integer.parseInt(txtRectWidth.getText());
	}
	void setRectHeight(int height){
		txtRectHeight.setText(Integer.toString(height));
	}
	int getRectHeight(){
		return Integer.parseInt(txtRectHeight.getText());
	}
	boolean showMiddleLine(){
		return chckbxShowMiddleLine.isSelected();
	}
	int getShiftX(){
		return Integer.parseInt(txtRectShiftX.getText());
	}
	int getShiftY(){
		return Integer.parseInt(txtRectShiftY.getText());
	}
	
	public void stopThreads(){
		threadShouldStayRunning = false;
	}
	public void setArduinoControl(ArduinoControl ac) {
		this.arduinoControl = ac;
		
	}
	
	public void firePSFRateCountRequiredEvent(PSFRateCountRequiredEvent event){
		Object[] listeners = listenerList.getListenerList();
		for(int i = 0; i< listeners.length; i+= 2){
			if(listeners[i] == PSFRateCountRequiredListener.class) {
				((PSFRateCountRequiredListener)listeners[i+1]).PSFRateCountRequiredEventOccured(event);
			}
		}
	}
	
	public void addPSFRateCountRequiredListener(PSFRateCountRequiredListener listener){
		listenerList.add(PSFRateCountRequiredListener.class, listener);
	}
	public void removePSFRateCountRequiredListener(PSFRateCountRequiredListener listener){
		listenerList.remove(PSFRateCountRequiredListener.class, listener);
	}
	
	ImagePlus normalizeMeasurement(Object img, int gain) {
		ImageProcessor ipr = ImageUtils.makeProcessor(core,img);
		//the offset should not be subtracted since no negative values are allowed and the distribution of the background pixels is changed
	    //ipr.subtract(200); //photo electrons = (digital count - offset)* sensistivity / gain
	    ipr.multiply(4.81);
	    ipr.multiply(1./gain); //the image contains now the number of photo electrons.
	    ImagePlus imp = new ImagePlus("",ipr);
	    return imp;
	}
	
	public void startPython(String pathTiffFile, String basename) {
		PrintWriter outputStream;
		String path = txtSavePath.getText();
		String measurementTag = txtMeasurementTag.getText();
		try {
			(new File(path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\PythonSkripts")).mkdirs();
			String outputBasename = path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\"+basename;
			outputBasename = outputBasename.replace("\\", "/");
			pathTiffFile = pathTiffFile.replace("\\", "/");
			String calibrationFile = calibrationFileText.getText();
			String threshold = thresholdText.getText();
			boolean use3D = do3DchkBox.isSelected();
			calibrationFile = calibrationFile.replace("\\", "/");
			String fitMethode = "";
			if (fitMethodSelectionChkBox.getSelectedItem().toString().equals("Local Relative Threshold")){
				fitMethode = "--FitJudgingMethod SquareRootRatio --SNR "+ threshold;
			}
			else{
				fitMethode = "--AmplitudeThreshold "+ threshold;
			}
			String image = "";
			String pixelsize = "";
			if (use3D){
				image = "--ChooseTransmission Image --ColourScheme ByCoordinate --HueCoordinate PositionZ";
				pixelsize = " --PixelSizeInNM 133,123 ";
			}
			else{
				image = "--ChooseTransmission Image --ColourScheme Grayscale";
				pixelsize = " --PixelSizeInNM 133,133 ";
			}
			
			outputStream = new PrintWriter(new FileWriter(path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\PythonSkripts\\"+basename+".py"));
			outputStream.println("import os");
			outputStream.println("os.system(\"\\\"C:/Program Files/rapidstorm3/bin/rapidSTORM.exe\\\" --inputFile "+pathTiffFile+" --Basename "+outputBasename+pixelsize+image+" --chooseTransmission Table "+fitMethode+" --ThreeD Spline3D --ZCalibration "+calibrationFile+ " --AutoTerminate --run\")");
			outputStream.close();
			//Runtime rt = Runtime.getRuntime();
			rc.addFile(path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\PythonSkripts\\"+basename+".py");
//			System.out.println("python "+path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\PythonSkripts\\"+basename+".py");
//			Process proc = new ProcessBuilder().command("python "+path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\PythonSkripts\\"+basename+".py").start();
//			Process proc = rt.exec("python "+path+"\\"+measurementTag+"\\Auswertung\\RapidStorm\\PythonSkripts\\"+basename+".py");
			//proc.w
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
}

class ReconControll implements Runnable{
	ArrayList<String> fileList = new ArrayList<String>();
	boolean isAvailable = true;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (isAvailable && fileList.size()>0){
				String toProcess = fileList.get(0);
				fileList.remove(0);
				try {
					isAvailable = false;
					System.out.println(toProcess);
					RunRapidSTORM rrs = new RunRapidSTORM(toProcess);
					Runtime rt = Runtime.getRuntime();
					System.out.println("python.exe D:/MessungenTemp/150624COSActinAlexa647CytoskeletonbufferMeasurement3/Auswertung/RapidStorm/PythonSkripts/LeftChannel150624COSActinAlexa647CytoskeletonbufferMeasurement3pt1.py");
					System.out.println("python.exe "+toProcess.replace("\\", "/"));
					String pythonPath = "c:\\Program Files\\Anaconda\\python.exe";
					//Process proc = rt.exec("python.exe "+toProcess.replace("\\", "/"));
					//Process proc2 = new ProcessBuilder().command("C:/Program Files/Anaconda/python.exe").start();
					Process proc = new ProcessBuilder().command(pythonPath,toProcess.replace("\\", "/")).start();
					try {
						Thread.sleep(1000);
						proc.waitFor();
						isAvailable = true;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						isAvailable = true;
					}
					isAvailable = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					isAvailable = true;
				}
				
			}
		}
	}
	public void addFile(String file){
		fileList.add(file);
	}
	
}

class RunRapidSTORM implements Runnable{
	String filename;
	RunRapidSTORM(String filename){
		this.filename = filename;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
