package microscopeControl;

import javax.swing.JPanel;
import javax.swing.JLabel;

import microscopeControl.MonitorWidget.UpdateZPosition;
import mmcorej.CMMCore;

public class ArduinoControl extends JPanel {
	CMMCore core;
	JLabel lblArduinoInput;
	private Thread arduinoValueUpdate;
	boolean threadShouldStayRunning = true;
	class ArduinoValue implements Runnable {
		public ArduinoValue(){}
		
		public void run(){

			while (threadShouldStayRunning) {
				try {
					String currPos = core.getProperty("Arduino-Input", "AnalogInput0");
					lblArduinoInput.setText(currPos);
					Thread.sleep(500);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				
			}
		}
	}
	/**
	 * Create the panel.
	 */
	public ArduinoControl(final CMMCore core) {
		this.core = core;
		JLabel lblArduinoAnalogInput = new JLabel("Arduino Analog Input");
		add(lblArduinoAnalogInput);
		
		lblArduinoInput = new JLabel("New label");
		add(lblArduinoInput);
		arduinoValueUpdate = new Thread(new ArduinoValue());
		arduinoValueUpdate.start();
	}
	
	public void stopThreads(){
		threadShouldStayRunning = false;
	}
	public String getAnalogInput(){
		try {
			return core.getProperty("Arduino-Input", "AnalogInput0");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return " ";
		}
	}
}
