package microscopeControl;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SpinnerModel;

import java.awt.BorderLayout;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import mmcorej.CMMCore;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.SwingConstants;

import org.micromanager.utils.ImageUtils;

public class PifocControllWidget extends JPanel {
	double scale = 1000.0;
	final CMMCore core;
	JComboBox comboBoxStepSize;
	JSpinner spinner;
	private JTextField txtLowerBound;
	private JTextField txtUpperBound;
	MainFrame parent;
	double calibStepSize =  0.001;
	JComboBox comboBoxStepSizeCalibration;
	/**
	 * Create the panel.
	 */
	
	class CalibStack implements Runnable {
		public CalibStack(){}
		
		public void run(){
			String path = parent.getCurrentOutputFolder();
			boolean success = (new File(path+"\\Calibration")).mkdirs();
			System.out.println(path+"\\Calibration");
			double start = Double.parseDouble(txtLowerBound.getText());
			double ende = Double.parseDouble(txtUpperBound.getText());
			int numberFrames = (int)((ende - start)/calibStepSize);
			parent.setAction("Acquisition of calibration data");
			for (double i = 0, counter = 0; i<ende-start;i=i+calibStepSize, counter ++) {
				try {
					core.setProperty("PIZStage", "Position",start+i);
					System.out.println(core.getProperty("PIZStage", "Position"));
					System.out.println("calibStepSize "+calibStepSize);
					System.out.println(String.valueOf((int)(counter))+ " \\ "+String.valueOf(numberFrames));
					parent.setFrameCount(String.valueOf((int)(counter))+ " \\ "+String.valueOf(numberFrames));
					core.sleep(50);
					String fname = path+"\\Calibration\\calib_"+String.format("_%05d", (int)(i*scale))+".tiff";
					System.out.println(fname);
					saveImage(fname);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public PifocControllWidget(MainFrame parent, final CMMCore core) {
		this.parent = parent;
		this.core = core;
		setMaximumSize(new Dimension(600, 100));
		setPreferredSize(new Dimension(600, 110));
		setBorder(new TitledBorder(null, "Pifoc Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(verticalBox, BorderLayout.NORTH);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setAlignmentY(Component.CENTER_ALIGNMENT);
		verticalBox.add(horizontalBox);
		
		spinner = new JSpinner();
		spinner.setPreferredSize(new Dimension(100, 22));
		spinner.setMinimumSize(new Dimension(100, 22));
		spinner.setModel(new SpinnerNumberModel(50.0, 0.0, 100.0, 0.001));
		spinner.setMaximumSize(new Dimension(100, 22));
		spinner.addChangeListener(spinner_ChangeListener);
		horizontalBox.add(spinner);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);
		
		JLabel lblStepSize = new JLabel("Step size");
		horizontalBox.add(lblStepSize);
		
		comboBoxStepSize = new JComboBox();
		comboBoxStepSize.setMaximumSize(new Dimension(32767, 22));
		comboBoxStepSize.addItem("1000 nm");
		comboBoxStepSize.addItem("100 nm");
		comboBoxStepSize.addItem("10 nm");
		comboBoxStepSize.addItem("1 nm");
		comboBoxStepSize.addActionListener(comboBoxStepSizeActionListener);
		horizontalBox.add(comboBoxStepSize);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_1);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("New check box");
		horizontalBox.add(chckbxNewCheckBox);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox.add(verticalGlue);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setAlignmentY(Component.CENTER_ALIGNMENT);
		verticalBox.add(horizontalBox_1);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_3);
		
		Component horizontalGlue_5 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_5);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{142, 125, 81, 155, 0};
		gbl_panel.rowHeights = new int[]{0, 25, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		GridBagConstraints gbc_verticalGlue_1 = new GridBagConstraints();
		gbc_verticalGlue_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue_1.gridx = 1;
		gbc_verticalGlue_1.gridy = 0;
		panel.add(verticalGlue_1, gbc_verticalGlue_1);
		
		JButton btnSetUpperBound = new JButton("Set upper bound");
		GridBagConstraints gbc_btnSetUpperBound = new GridBagConstraints();
		gbc_btnSetUpperBound.anchor = GridBagConstraints.WEST;
		gbc_btnSetUpperBound.fill = GridBagConstraints.VERTICAL;
		gbc_btnSetUpperBound.insets = new Insets(0, 0, 5, 5);
		gbc_btnSetUpperBound.gridx = 0;
		gbc_btnSetUpperBound.gridy = 1;
		btnSetUpperBound.addActionListener(btnSetUpperBound_ActionListener);
		panel.add(btnSetUpperBound, gbc_btnSetUpperBound);
		
		txtUpperBound = new JTextField();
		txtUpperBound.setHorizontalAlignment(SwingConstants.RIGHT);
		txtUpperBound.setText("100");
		GridBagConstraints gbc_txtUpperBound = new GridBagConstraints();
		gbc_txtUpperBound.fill = GridBagConstraints.BOTH;
		gbc_txtUpperBound.insets = new Insets(0, 0, 5, 5);
		gbc_txtUpperBound.gridx = 1;
		gbc_txtUpperBound.gridy = 1;
		panel.add(txtUpperBound, gbc_txtUpperBound);
		txtUpperBound.setMaximumSize(new Dimension(80, 2147483647));
		txtUpperBound.setColumns(6);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		GridBagConstraints gbc_horizontalGlue_2 = new GridBagConstraints();
		gbc_horizontalGlue_2.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_2.gridx = 2;
		gbc_horizontalGlue_2.gridy = 1;
		panel.add(horizontalGlue_2, gbc_horizontalGlue_2);
		
		comboBoxStepSizeCalibration = new JComboBox();
		comboBoxStepSizeCalibration.setPreferredSize(new Dimension(80, 22));
		GridBagConstraints gbc_comboBoxStepSizeCalibration = new GridBagConstraints();
		gbc_comboBoxStepSizeCalibration.anchor = GridBagConstraints.EAST;
		gbc_comboBoxStepSizeCalibration.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxStepSizeCalibration.gridx = 3;
		gbc_comboBoxStepSizeCalibration.gridy = 1;
		comboBoxStepSizeCalibration.addItem("1 nm");
		comboBoxStepSizeCalibration.addItem("5 nm");
		comboBoxStepSizeCalibration.addItem("10 nm");
		comboBoxStepSizeCalibration.addItem("20 nm");
		comboBoxStepSizeCalibration.addItem("50 nm");
		comboBoxStepSizeCalibration.addItem("100 nm");
		comboBoxStepSizeCalibration.addActionListener(comboBoxStepSizeCalibrationActionListener);
		panel.add(comboBoxStepSizeCalibration, gbc_comboBoxStepSizeCalibration);
		
		JButton btnSetLowerBound = new JButton("Set lower bound");
		GridBagConstraints gbc_btnSetLowerBound = new GridBagConstraints();
		gbc_btnSetLowerBound.anchor = GridBagConstraints.WEST;
		gbc_btnSetLowerBound.fill = GridBagConstraints.VERTICAL;
		gbc_btnSetLowerBound.insets = new Insets(0, 0, 0, 5);
		gbc_btnSetLowerBound.gridx = 0;
		gbc_btnSetLowerBound.gridy = 2;
		btnSetLowerBound.addActionListener(btnSetLowerBound_ActionListener);
		panel.add(btnSetLowerBound, gbc_btnSetLowerBound);
		
		txtLowerBound = new JTextField();
		txtLowerBound.setHorizontalAlignment(SwingConstants.RIGHT);
		txtLowerBound.setText("0");
		GridBagConstraints gbc_txtLowerBound = new GridBagConstraints();
		gbc_txtLowerBound.fill = GridBagConstraints.BOTH;
		gbc_txtLowerBound.insets = new Insets(0, 0, 0, 5);
		gbc_txtLowerBound.gridx = 1;
		gbc_txtLowerBound.gridy = 2;
		panel.add(txtLowerBound, gbc_txtLowerBound);
		txtLowerBound.setMaximumSize(new Dimension(80, 2147483647));
		txtLowerBound.setColumns(6);
		
		JButton btnRecordStack = new JButton("record stack");
		GridBagConstraints gbc_btnRecordStack = new GridBagConstraints();
		gbc_btnRecordStack.anchor = GridBagConstraints.EAST;
		gbc_btnRecordStack.fill = GridBagConstraints.VERTICAL;
		gbc_btnRecordStack.gridx = 3;
		gbc_btnRecordStack.gridy = 2;
		btnRecordStack.addActionListener(btnRecordStack_ActionListener);
		panel.add(btnRecordStack, gbc_btnRecordStack);

	}

	ChangeListener spinner_ChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent arg0) {
			try {
				core.setProperty("PIZStage", "Position", String.valueOf(spinner.getValue()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	
	ActionListener comboBoxStepSizeActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String selectedItem = (String) comboBoxStepSize.getSelectedItem();
			int majorTickSpacing = 1;
			if (selectedItem.equalsIgnoreCase("1000 nm")) {majorTickSpacing = 1000;}
			if (selectedItem.equalsIgnoreCase("100 nm")) {majorTickSpacing = 100;}
			if (selectedItem.equalsIgnoreCase("10 nm")) {majorTickSpacing = 10;}
			if (selectedItem.equalsIgnoreCase("1 nm")) {majorTickSpacing = 1;}
			//System.out.println(majorTickSpacing/1000.0);
			//System.out.println(spinner.getValue());
			double curValSpinner = (Double) spinner.getValue();
			spinner.setModel(new SpinnerNumberModel(curValSpinner,0,100,majorTickSpacing/1000.0));

		}
		
	};
	
	ActionListener comboBoxStepSizeCalibrationActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String selectedItem = (String) comboBoxStepSizeCalibration.getSelectedItem();
			int stepSize = 1;
			if (selectedItem.equalsIgnoreCase("1 nm")) {stepSize = 1;}
			if (selectedItem.equalsIgnoreCase("5 nm")) {stepSize = 5;}
			if (selectedItem.equalsIgnoreCase("10 nm")) {stepSize = 10;}
			if (selectedItem.equalsIgnoreCase("20 nm")) {stepSize = 20;}
			if (selectedItem.equalsIgnoreCase("50 nm")) {stepSize = 50;}
			if (selectedItem.equalsIgnoreCase("100 nm")) {stepSize = 100;}
			calibStepSize = stepSize/scale;
			//System.out.println("calibStepSize: "+calibStepSize+"  ,stepSize: "+stepSize);
		}
		
	};
	
	ActionListener btnRecordStack_ActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			Thread CalibStackThread;
			try {
				CalibStackThread = new Thread(new CalibStack());
				//livePreviewThread = new Thread(new LivePreviewWithoutCamera());
				CalibStackThread.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	};
	
	ActionListener btnSetUpperBound_ActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try {
				if (Double.valueOf(core.getProperty("PIZStage", "Position"))>0 && Double.valueOf(core.getProperty("PIZStage", "Position")) < 100 && Double.valueOf(core.getProperty("PIZStage", "Position"))> Double.valueOf(txtLowerBound.getText())) {
					txtUpperBound.setText(String.valueOf(core.getProperty("PIZStage", "Position")));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
	};
	
	ActionListener btnSetLowerBound_ActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				if (Double.valueOf(core.getProperty("PIZStage", "Position"))>0 && Double.valueOf(core.getProperty("PIZStage", "Position")) < 100 && Double.valueOf(core.getProperty("PIZStage", "Position"))< Double.valueOf(txtUpperBound.getText())) {
					txtLowerBound.setText(String.valueOf(core.getProperty("PIZStage", "Position")));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
	};
	
	void saveImage(String filename){
		try {
			Object img;
			double exp = parent.getExposureTime();
			int gain = parent.getGain();
			core.setProperty(core.getCameraDevice(),"Exposure", exp);
			core.setProperty(core.getCameraDevice(),"Gain", gain);
			
			//System.out.println("Gain: "+core.getProperty(core.getCameraDevice(), "Gain"));
			//System.out.println("Exposure: "+core.getProperty(core.getCameraDevice(), "Exposure"));
			core.snapImage();
			img = core.getImage();
			ImageProcessor ipr = ImageUtils.makeProcessor(core,img);
    	    ImagePlus imp = new ImagePlus("",ipr);
    	    FileSaver fs = new FileSaver(imp);
    	    fs.saveAsTiff(filename);
    	    parent.setCurrentImage(filename);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	};
	
}
