/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 *
 * @author iscreen
 */
public class DB {
    String dbName = null;
    Connection conn = null;
    
    public DB(String db) {
        this.dbName = db;
        open();
        
    }
    
    public ArrayList<Friend> GetUsers() {
        Statement stmt = null;
        ArrayList<Friend> users = new ArrayList<Friend>();
        try {
            if (conn.isClosed()) {
                open();
            }
            conn.setAutoCommit(false);
            
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM users;" );

            while ( rs.next() ) {
               String name = rs.getString("name");
               String password = rs.getString("password");
               String status = rs.getString("status");
               users.add(new Friend(name, status));
            }
            rs.close();
            stmt.close();
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
         }
        return users;
    }
    
    public boolean UserAuth(String username, String password) {
        boolean auth = false;
        
        try {
            if (conn.isClosed()) {
                open();
            }
            String sql = "SELECT * FROM users WHERE name=?";
            PreparedStatement pst = null;
            pst = conn.prepareStatement(sql);
            int idx = 1 ; 
            pst.setString(idx++, username);
           
            ResultSet rs = pst.executeQuery();
            while ( rs.next() ) {
               String current_password = rs.getString("password");
               if (current_password.equals(password)) {
                   auth = true;
                   break;
               }
            }
            rs.close();
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
         }
        return auth;
    }
    
    public boolean UserExist(String username) {
        boolean found = false;
        Statement stmt = null;
        try {
            if (conn.isClosed()) {
                open();
            }
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT count(*) FROM users WHERE name=" + username + ";" );
            while ( rs.next() ) {
               found = true;
               break;
            }
            rs.close();
            stmt.close();
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
         }
        return found;
    }
    
    //新增
    public void AddUser(String name, String password, String status)throws SQLException{
        String sql = "insert into users (name, password, status) values(?,?,?)";
        PreparedStatement pst = null;
        pst = conn.prepareStatement(sql);
        int idx = 1 ; 
        pst.setString(idx++, name);
        pst.setString(idx++, password);
        pst.setString(idx++, status);
        pst.executeUpdate();
    }
    
    //修改
    public void UpdateStatus(String name, String status)throws SQLException{
        String sql = "update users set status = ? where name = ?";
        PreparedStatement pst = null;
        pst = conn.prepareStatement(sql);
        int idx = 1 ; 
        pst.setString(idx++, status);
        pst.setString(idx++, name);
        pst.executeUpdate();
    }
    
    public void disconnect() {
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        }catch(Exception e) {
            
        }
    }
    
    private void open() {
        try {
//           Class.forName("org.sqlite.JDBC");
//           conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
//           
            SQLiteConfig config = new SQLiteConfig();
            // config.setReadOnly(true);   
            config.setSharedCache(true);
            config.enableRecursiveTriggers(true);   
            
            SQLiteDataSource ds = new SQLiteDataSource(config); 
            ds.setUrl("jdbc:sqlite:" + this.dbName);
            conn = ds.getConnection();
            
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
           System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
}
