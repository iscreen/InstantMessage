/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.DefaultListModel;

/**
 *
 * @author Dean Lin
 */
public class IMServer {

    public static String publicFolderPath;
    private static HashSet<String> names = new HashSet<String>();
    private static HashMap<String, Socket> clients = new HashMap<String, Socket>();
    private static HashMap<String, ObjectOutputStream> map = new HashMap<String, ObjectOutputStream>();
    private static HashMap<String, String> data = new HashMap<String, String>();
       
    public IMServer() {
//        publicFolderPath = this.getClass().getResource("store").toString();
        this.load();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        try {
            ServerSocket sSocket = new ServerSocket(Constants.PORT);
            IMServer server = new IMServer();
            System.out.println("Server is running....");
            while(true) {
              new ClientHandler(sSocket.accept()).start();
            }
        }catch(Exception e) {
        }
    }
    
    private static class ClientHandler extends Thread {
		
        private String name;
        private Socket client;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        private ObjectOutputStream outbc;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        public void run() {
            // 等待連線
            while(true) {
                try {
                    in = new ObjectInputStream(client.getInputStream());
                    out = new ObjectOutputStream(client.getOutputStream());

                    DefaultListModel model = (DefaultListModel) in.readObject();
                    int type = (int) model.elementAt(0);
                    String name = (String) model.elementAt(1);
                    String password = (String) model.elementAt(2);
                    DefaultListModel resModel = new DefaultListModel();
                    if (type == Constants.LOGIN) { // Login
                        if (names.contains(name) && (data.get(name)).equals(password)) {
                                resModel.addElement(Constants.SUCCESS);
                                //回傳用戶清單
                                resModel.addElement(new ArrayList<String>(names));
                                out.writeObject(resModel);	
                        } else {
                                resModel.addElement(Constants.ERROR);
                                out.writeObject(resModel);
                                continue;
                        }
                    } else if (type == Constants.REGISTER) { // Register
                        if (!names.contains(name)) {
                            names.add(name);
                            data.put(name, password);
                            resModel.addElement(Constants.SUCCESS);
                            resModel.addElement(new ArrayList<String>(names));
                            out.writeObject(resModel);	
                        } else {
                            resModel.addElement(Constants.ERROR);
                            resModel.addElement("User was existed.");
                            out.writeObject(resModel);
                            continue;
                        }
                    }
                    
                    clients.put(name, client);
                    map.put(name, out);
                    StatusChange(name, "Online");
                    while(true) {
                        try {
                            in = new ObjectInputStream(client.getInputStream());
                            Object inputMsg = in.readObject();
                            if (inputMsg == null) {
                                continue;
                            }
                            CheckTypeMsg((DefaultListModel<Object>) inputMsg, name);
                        }catch (Exception exp) {
                            System.out.printf("!! error: %s", exp.getMessage());
                        }
                    }

                } catch (Exception exp) {
                    System.out.printf("!! error: %s", exp.getMessage());
                }
            }
        } // end run

        private void CheckTypeMsg(DefaultListModel inputMsg, String senderName) {
            switch((int)inputMsg.elementAt(0)) {
                case Constants.SEND_MSG:
                    SendMsg(inputMsg, senderName);
                    break;
                case Constants.LOGOUT:
                    Logout(inputMsg, senderName);
                    break;
            }
        }

        private void SendMsg(DefaultListModel inputMsg, String senderName) {
            String msg = (String) inputMsg.elementAt(1);
            String sender = (String) inputMsg.elementAt(2);
            System.out.printf("message: %s, sender: %s \n", msg, sender);
            DefaultListModel<String> listFriend = (DefaultListModel<String>) inputMsg.elementAt(3);
            for(int i = 0 ; i < listFriend.size(); i++) {
                String teNameOut = listFriend.elementAt(i);
                outbc = map.get(teNameOut);
                try {
                    DefaultListModel model = new DefaultListModel();
                    model.addElement(Constants.SEND_MSG);
                    model.addElement(msg);
                    model.addElement(sender);

                    outbc = new ObjectOutputStream(clients.get(teNameOut).getOutputStream());
                    outbc.writeObject(model);
                    outbc.flush();
                    System.out.println("Send message successfully." + msg);
                }catch (Exception exp) {
                    Logger.getLogger(IMServer.class.getName()).log(Level.SEVERE, null, exp.getMessage());
                    System.out.printf("!! error: %s", exp.getMessage());
                }
            }
        }

        private void Logout(DefaultListModel inputMsg, String senderName) {
            System.out.printf("Client \" %s\" log out!\n", senderName);
            clients.remove(senderName);
            Thread.currentThread().stop();
        }
        
        /**
         * 通知上線
         * @param senderName 
         */
        private void StatusChange(String senderName, String status) {
            System.out.printf("Client \" %s\" log in!\n", senderName);
            DefaultListModel model = new DefaultListModel();
            model.addElement(Constants.STATUS_CHANGE);
            model.addElement(status);
            model.addElement(senderName);
            
            for(String name : names) {
                try {
                    if (name.equals(senderName)) {
                        continue;
                    }
                    Socket client = clients.get(name);
                    if (client == null) {
                        continue;
                    }    
                    outbc = map.get(name);
                    outbc = new ObjectOutputStream(client.getOutputStream());
                    outbc.writeObject(model);
                    outbc.flush();
                }catch (Exception exp) {
                    Logger.getLogger(IMServer.class.getName()).log(Level.SEVERE, null, exp.getMessage());
                    System.out.printf("!! error: %s", exp.getMessage());
                }
            }
        }
    }

    private HashMap<String, String> ReadFileData() {
        HashMap<String, String> result = new HashMap<String, String>();

        try {

        }catch (Exception exp) {
                System.out.printf("!! IMServer ReadFileData: %s", exp.toString());
        }
        return result;
    }
    
    private void load() {
        
        String[] users = {"Dean", "Allen", "David", "Eric", "Jeff", "Elain", "Chery" };
        
        for(int i = 0; i < users.length; i++) {
            data.put(users[i], "p@ssword");
            names.add(users[i]);
        }
    }
    
}
