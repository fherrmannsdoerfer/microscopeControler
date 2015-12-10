package microscopeControl;

import javax.swing.JFrame;

import org.micromanager.acquisition.AcquisitionEngine;
import org.micromanager.api.ScriptInterface;

import mmcorej.CMMCore;

import org.micromanager.MMStudioMainFrame;
//import org.micromanager.api.AcquisitionEngine;

public class MainClass implements org.micromanager.api.MMPlugin{
   public static String menuName = "MicroscopeControl";
   public static String tooltipDescription = "A test plugin";
   private CMMCore core_;
   private MMStudioMainFrame gui_;
   private AcquisitionEngine acq_;
   
   @Override
   public void dispose() {
      /*
       * you can put things that need to be run on shutdown here
       * note: if you launch a JDialog from the plugin using show(), shutdown of the dialog will not automatically call dispose()
       * You will need to add a call to dispose() from the formWindowClosing() method of your JDialog.
       */
   }

	
   @Override
   public void setApp(ScriptInterface app) {
      gui_ = (MMStudioMainFrame) app;
      core_ = app.getMMCore();
      acq_ = gui_.getAcquisitionEngine();
      JFrame mainFrame = new MainFrame(core_, gui_, acq_);

      mainFrame.setVisible(true);
   }
   
   @Override
   public String getVersion() {
      return "1.0";
   }
   public void configurationChanged() {
      // TODO Auto-generated method stub
   }
   @Override
   public String getCopyright() {
      return "(C) 2014 Frank Herrmannsdoerfer";
   }
   @Override
   public String getDescription() {
      return "Description: Software to control Camera, Lasers and Pifoc";
   }
   @Override
   public String getInfo() {
      return "Info: Software to control Camera, Lasers and Pifoc";
   }
   @Override
   public void show() {//this is run when the menu listing for the plugin is clicked
    
   }
  
}
