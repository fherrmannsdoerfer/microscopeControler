package microscopeControl;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

import mmcorej.CMMCore;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.border.TitledBorder;

public class LaserControl extends JPanel {
	private JTextField textTargetBlinkingEventsPerFrame;
	JLabel lblBlinkingEventsPerFrame;
	int counter = 0;
	ArrayList<Integer> lastBlinkingEventsPerFrame = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0,0,0,0,0,0));
	final CMMCore core;
	JCheckBox chkboxEnableUVControl;
	LaserPanel lp2;
	/**
	 * Create the panel.
	 */
	public LaserControl(final CMMCore core) {
		setPreferredSize(new Dimension(300, 390));
		setMaximumSize(new Dimension(5000, 5000));
		this.core = core;
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setBorder(new TitledBorder(null, "Laser Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(verticalBox);
		
		Box horizontalBox = Box.createHorizontalBox();
		
		LaserPanel lp = new LaserPanel(core, "CoherentCube661","661 nm");
		horizontalBox.add(lp);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);
		lp2 = new LaserPanel(core, "CoherentCube405","405 nm");
		horizontalBox.add(lp2);
		verticalBox.add(horizontalBox);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalBox.add(verticalGlue);
		
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox.add(verticalBox_1);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_1);
		
		JLabel lblNewLabel = new JLabel("wanted blinking events per frame");
		horizontalBox_1.add(lblNewLabel);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_1);
		
		textTargetBlinkingEventsPerFrame = new JTextField();
		textTargetBlinkingEventsPerFrame.setMaximumSize(new Dimension(400, 400));
		horizontalBox_1.add(textTargetBlinkingEventsPerFrame);
		textTargetBlinkingEventsPerFrame.setColumns(5);
		
		chkboxEnableUVControl = new JCheckBox("Enable UV Laser Control");
		chkboxEnableUVControl.setAlignmentX(Component.CENTER_ALIGNMENT);
		chkboxEnableUVControl.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		verticalBox_1.add(chkboxEnableUVControl);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);
		
		JLabel lblNewLabel_1 = new JLabel("blinking events per frame:");
		horizontalBox_2.add(lblNewLabel_1);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_2);
		
		lblBlinkingEventsPerFrame = new JLabel("New label");
		horizontalBox_2.add(lblBlinkingEventsPerFrame);

	}
	
	public void addBlinkingNumber(int number){
		//System.out.println("counter%10 "+counter%10);
		lastBlinkingEventsPerFrame.set(counter%10, number);
		if (counter>10 && chkboxEnableUVControl.isSelected()){
			decideToIncreaseLaserPower();
		}
		setBlinkingNumber(number);
		counter = counter + 1;
	}
	
	void decideToIncreaseLaserPower(){
		int sum = 0;
		int count = 0;
		for (int i = 0; i<lastBlinkingEventsPerFrame.size(); i++){
			sum = sum + lastBlinkingEventsPerFrame.get(i);
			count = count + 1;
		}
		try{
			if (sum / count < Double.parseDouble(textTargetBlinkingEventsPerFrame.getText())){
				lp2.increaseLaserPower(.1);
				counter = 0;
			}
		}
		catch(java.lang.NumberFormatException ne){
			System.err.println("probably no valid value set for the wanted number of blinking events");
		}
	}
	
	void setBlinkingNumber(int number){
		lblBlinkingEventsPerFrame.setText(String.valueOf(number));
	}

}
