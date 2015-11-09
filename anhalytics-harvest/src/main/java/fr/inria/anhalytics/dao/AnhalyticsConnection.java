package fr.inria.anhalytics.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author azhar
 */
public class AnhalyticsConnection {
     private static String url = "jdbc:mysql://localhost:3306/anhalytics";
  private static String user = "root";
  private static String passwd = "admin";
  private static Connection connect;
   
  public static Connection getInstance(){
    if(connect == null){
      try {
        connect = DriverManager.getConnection(url, user, passwd);
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }      
    return connect;
  }   
}
