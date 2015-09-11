package fr.inria.anhalytics.test.test;

import fr.inria.anhalytics.harvest.OAIHarvester;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author azhar
 */
    public class MyRunnable implements Runnable {
  private OAIHarvester oaih;
  private String date;
  public MyRunnable(OAIHarvester _oaih, String _date) {
    this.oaih = _oaih;
    this.date = _date;
  }

  public void run(){
      try {
          oaih.fetchDocumentsByDate(date);
      } catch (ParserConfigurationException ex) {
          Logger.getLogger(MyRunnable.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
          Logger.getLogger(MyRunnable.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
}
