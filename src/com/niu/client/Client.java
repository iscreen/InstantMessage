/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.client;

import com.niu.server.Constants;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.DefaultListModel;

/**
 *
 * @author iscreen
 */
public class Client {
    
    private Socket socket;
    private String host;
    private int port;
    private String username;
    private String password;
    
    private ServerHandler server;
    
    public Client(String host, int port) {
        this.host = host;
        this.port = port;      
    }
  
    public void stop() {
      
    }
    
    public boolean Login(String username, String password) {
        this.username = username;
        this.password = password;
        try {
            socket = new Socket(this.host, this.port);
            
            Thread.sleep(500);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            DefaultListModel model = new DefaultListModel();
            model.addElement(Constants.LOGIN);
            model.addElement(username);
            model.addElement(password);
            out.writeObject(model);
            model = (DefaultListModel) in.readObject();
            int type = (int) model.elementAt(0);
            System.out.printf("type: %d\n", type);
            if (type == Constants.SUCCESS) {
                FriendManager.SetFriends((ArrayList<String>)model.elementAt(1), username); 
                Singleton.LoginSuccess = true;
                startClient();
            }
        }catch(Exception e) {
            System.out.println("Login:" + e.getMessage());
        }
        
        return Singleton.LoginSuccess;
    }
    
    public boolean Register(String username, String password) {
        this.username = username;
        this.password = password;
        try {
            if (socket == null) {
                socket = new Socket(this.host, this.port);
            }
            Thread.sleep(500);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            DefaultListModel model = new DefaultListModel();
            model.addElement(Constants.REGISTER);
            model.addElement(username);
            model.addElement(password);
            out.writeObject(model);
            model = (DefaultListModel) in.readObject();
            int type = (int) model.elementAt(0);
            System.out.printf("type: %d\n", type);
            if (type == Constants.SUCCESS) {
                FriendManager.SetFriends((ArrayList<String>)model.elementAt(1), username); 
                Singleton.LoginSuccess = true;
                startClient();
            }
        }catch(Exception e) {
            System.out.println("Login:" + e.getMessage());
        }
        return Singleton.LoginSuccess;
    }
   
    public String getName() {
        return username;
    }

    public void SendMessage(String message) {
        ObjectOutputStream out;
        try {
            DefaultListModel model = new DefaultListModel();
            model.addElement(Constants.SEND_MSG);
            model.addElement(message);
            model.addElement(username);

            DefaultListModel lstFriends = new DefaultListModel<String>();
            lstFriends.addElement("allen");
            model.addElement(lstFriends);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(model);
            out.flush();
        }catch(IOException ioe) {
            System.out.println("Client - SendMessage(): " + ioe.getMessage());
        }
    }
    
    public void SendMessage(DefaultListModel model) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(model);
            out.flush();
        }catch(IOException ioe) {
            System.out.println("Client - SendMessage(): " + ioe.getMessage());
        }
    }
        
    private void startClient() throws IOException {
        try {
            server = new ServerHandler(socket, username);
            Thread serverThread = new Thread(server);
            serverThread.start();
        }catch(Exception e) { 
            System.out.println("Client - startClient(): " + e.getMessage());
        }
    }

    public class ServerHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream in;
        
        private String username;
        
        public ServerHandler(Socket socket, String username) {
            this.socket = socket;
            this.username = username;
        }
        
        public void run() {
            try {
                while(!socket.isClosed()){
                    in = new ObjectInputStream(socket.getInputStream());
                    DefaultListModel model = (DefaultListModel) in.readObject();
                    int type = (int) model.elementAt(0);
                    System.out.printf("type: %d\n", type);
                    if (type == Constants.SEND_MSG) {
                        String receiveMsg = (String)model.elementAt(1);
                        String sender = (String)model.elementAt(2);
                        System.out.printf("receiveed message:" + receiveMsg);
                        Singleton.mainForm.CreateBoxMessage(receiveMsg, false);
//                        Singleton.mainForm.ReceivedMessage(receiveMsg, sender);
                    } else if (type == Constants.STATUS_CHANGE) {
                        String status = (String)model.elementAt(1);
                        String sender = (String)model.elementAt(2);
                        System.out.printf("%s status change to %s\n", sender, status);
                    }
                    Thread.sleep(500);
                }
            }catch(Exception e) {
                System.out.println("ServerHandler error:" + e.getMessage());
            }
        }
    }       
}
