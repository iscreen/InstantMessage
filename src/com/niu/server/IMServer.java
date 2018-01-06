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

    public String dataPath;
    public String publicFolderPath;
    private DB db;
    private HashMap<String, Friend> users = new HashMap<String, Friend>();
    private HashMap<String, Socket> clients = new HashMap<String, Socket>();
    private HashMap<String, ObjectOutputStream> map = new HashMap<String, ObjectOutputStream>();
    private int port;
    
    public IMServer(int port) {
        dataPath = this.getClass().getResource("/data").toString().replaceAll("file:", "");
        db = new DB(dataPath + "/im.db");
        this.port = port;
        this.loadUsers();
    }
    
    public void start() {
        try {
            ServerSocket sSocket = new ServerSocket(port);
            System.out.println("Server is running....");
            while(true) {
              new ClientHandler(sSocket.accept()).start();
            }
        }catch(Exception e) {
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
    
    private void loadUsers() {
        ArrayList<Friend> dbusers = db.GetUsers();
        for(Friend user: dbusers) {
            users.put(user.getName(), user);
        }
    }
    
    private class ClientHandler extends Thread {
		
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
                        if (db.UserAuth(name, password)) {
                                resModel.addElement(Constants.SUCCESS);
                                db.UpdateStatus(name, "Online");
                                StatusChange(name, "Online");
                                ArrayList<Friend> friends = new ArrayList<Friend>(users.values());
                                //回傳用戶清單
                                resModel.addElement(friends);
                                out.writeObject(resModel);	
                        } else {
                                resModel.addElement(Constants.ERROR);
                                out.writeObject(resModel);
                                continue;
                        }
                    } else if (type == Constants.REGISTER) { // Register
                        if (!db.UserExist(name)) {
                            users.put(name, new Friend(name, "Online"));
                            db.AddUser(name, password, "Online");
                            StatusChange(name, "Online");
                            resModel.addElement(Constants.SUCCESS);
                            resModel.addElement(new ArrayList<Friend>(users.values()));
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
            try {
                db.UpdateStatus(senderName, "Offline");
                StatusChange(senderName, "Offline");
            } catch(Exception exp) {
                System.out.printf("!! Logout error: %s", exp.getMessage());
            }
            clients.remove(senderName);
            Thread.currentThread().stop();
        }
        
        /**
         * 通知上線
         * @param senderName 
         */
        private synchronized void StatusChange(String senderName, String status) {
            System.out.printf("Client \" %s\" log in!\n", senderName);
            DefaultListModel model = new DefaultListModel();
            model.addElement(Constants.STATUS_CHANGE);
            model.addElement(status);
            model.addElement(senderName);
            
            for(Friend user: users.values()) {
                try {
                    if (user.getName().equals(senderName)) {
                        users.replace(senderName, new Friend(senderName, status));
                        continue;
                    }
                    Socket client = clients.get(user.getName());
                    if (client == null) {
                        continue;
                    }    
                    outbc = map.get(user.getName());
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IMServer server = new IMServer(Constants.PORT);
        server.start();
    }
}
