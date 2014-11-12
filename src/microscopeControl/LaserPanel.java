package microscopeControl;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

import org.micromanager.MMStudioMainFrame;
//import org.micromanager.api.AcquisitionEngine;

import mmcorej.CMMCore;

public class LaserPanel extends JPanel {
	private JTextField txtLaserIntensity;
	private JSlider slrLaserIntensity;
	double minVal;
	double maxVal;
	CMMCore core;
	int scale = 10; //used to transform integer slider states into float numbers, scale 10 means an intensity resolution of 0.1
	Image imgON;
	Image imgOFF;
	boolean state = false;
	JButton btnSwitchLaser;
	String laserName;
	/**
	 * Create the panel.
	 */
	public LaserPanel(CMMCore core_, String name, String wavelength) {
		core = core_;
		laserName = name;

		try {
			minVal = Double.parseDouble(core.getProperty(laserName, "Minimum Laser Power"));
			maxVal = Double.parseDouble(core.getProperty(laserName, "Maximum Laser Power"));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setPreferredSize(new Dimension(145, 300));
		setMaximumSize(new Dimension(145, 300));
		setMinimumSize(new Dimension(145, 300));
		setBorder(new TitledBorder(null, "Laser "+ wavelength, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		Box horizontalBox = Box.createHorizontalBox();
		add(horizontalBox);
		
		Box verticalBox = Box.createVerticalBox();
		horizontalBox.add(verticalBox);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		
		Box verticalBox_2 = Box.createVerticalBox();
		horizontalBox_1.add(verticalBox_2);
		
		JLabel lblMaxPower = new JLabel(String.valueOf(maxVal));
		verticalBox_2.add(lblMaxPower);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		verticalBox_2.add(verticalGlue_1);
		
		JLabel lblMinPower = new JLabel(String.valueOf(minVal));
		verticalBox_2.add(lblMinPower);
		
		slrLaserIntensity = new JSlider();
		slrLaserIntensity.setMinimum((int) minVal*scale);
		slrLaserIntensity.setMaximum((int) maxVal*scale);
		slrLaserIntensity.setValue(0);
		slrLaserIntensity.addChangeListener(slrLaserIntensity_changeListener);
		horizontalBox_1.add(slrLaserIntensity);
		slrLaserIntensity.setMinimumSize(new Dimension(20, 20));
		slrLaserIntensity.setPreferredSize(new Dimension(20, 100));
		slrLaserIntensity.setOrientation(SwingConstants.VERTICAL);

		
		Box verticalBox_1 = Box.createVerticalBox();
		horizontalBox_1.add(verticalBox_1);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox_1.add(verticalGlue);
		
		txtLaserIntensity = new JTextField();
		txtLaserIntensity.setText("0");
		txtLaserIntensity.addActionListener(txtLaserIntensity_ActionListener);
		txtLaserIntensity.setMaximumSize(new Dimension(50, 50));
		verticalBox_1.add(txtLaserIntensity);
		txtLaserIntensity.setColumns(10);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut);
		
		btnSwitchLaser = new JButton("OFF");
		btnSwitchLaser.setFont(new Font("Tahoma", Font.PLAIN, 19));
		btnSwitchLaser.setForeground(Color.WHITE);
		btnSwitchLaser.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox.add(btnSwitchLaser);

		try {
            imgON = ImageIO.read(new File("C:\\Program Files\\Micro-Manager-1.4\\mmplugins\\MicroscopeControl\\src\\org\\micromanager\\MicroscopeControl\\src\\microscopeControl\\on.png"));
            imgOFF = ImageIO.read(new File("C:\\Program Files\\Micro-Manager-1.4\\mmplugins\\MicroscopeControl\\src\\org\\micromanager\\MicroscopeControl\\src\\microscopeControl\\off.png"));
            btnSwitchLaser.setIcon(new ImageIcon(imgOFF.getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
            btnSwitchLaser.setHorizontalTextPosition(JButton.CENTER);
            btnSwitchLaser.setVerticalTextPosition(JButton.CENTER);
            btnSwitchLaser.setMargin(new Insets(0, 0, 0, 0));
            btnSwitchLaser.setBorder(null);
        } catch (IOException ex) {
        	System.out.println("not found");
        }
		btnSwitchLaser.addActionListener(btnSwitchLaser_actionListener);
	}

	public void increaseLaserPower(double increment){	
		try {
			double currVal = Double.valueOf(core.getProperty(laserName, "PowerSetpoint"));
			double newVal = currVal + increment;
			if (newVal<= maxVal){
				core.setProperty(laserName, "PowerSetpoint", currVal + increment);
				slrLaserIntensity.setValue((int) (newVal)*scale);
				txtLaserIntensity.setText(String.valueOf(newVal));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	ActionListener txtLaserIntensity_ActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			float entry_text_field = (float) 0.1;
			try {
				entry_text_field = Float.parseFloat(txtLaserIntensity.getText());
				
			}
			catch (Exception e1) {
			}
			
			if (entry_text_field>105 || entry_text_field<0.1) {
				slrLaserIntensity.setValue((int) (minVal * scale));
			}
			else {
				slrLaserIntensity.setValue((int)(entry_text_field*scale));
			}
			
		}
	};
	ChangeListener slrLaserIntensity_changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent arg0) {
			txtLaserIntensity.setText(String.valueOf(slrLaserIntensity.getValue()/(float)scale));
		}
	};

	ActionListener btnSwitchLaser_actionListener =new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			try {
				if (state) {
					state = false;
					btnSwitchLaser.setIcon(new ImageIcon(imgOFF.getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
					btnSwitchLaser.setText("OFF");
					core.setProperty(laserName,"PowerSetpoint",minVal);
				}
				else {
					state = true;
					btnSwitchLaser.setIcon(new ImageIcon(imgON.getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
					btnSwitchLaser.setText("ON");
					core.setProperty(laserName,"PowerSetpoint",Double.parseDouble(txtLaserIntensity.getText()));
				}
				//lbl_pifoc.setText("hi");
				//core.setProperty("CoherentCube661", "PowerSetpoint",Math.max((double) slider_661nm_laser.getValue(),0.1));
				//core.setProperty("CoherentCube661", "PowerSetpoint",0.1);
				//lbl_pifoc.setText("hi2");
				//core.setProperty("PIZStage", "Position", 50);			
			} catch (Exception e) {
				//System.out.println(slider_661nm_laser.getValue());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
}
