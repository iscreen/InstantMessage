/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.client;

import com.niu.server.Friend;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.DefaultListModel;

/**
 *
 * @author Dean Lin
 */
public class FriendManager {
    public static DefaultListModel<Friend> ListFriend = new DefaultListModel();
    
    public static void SetFriends(ArrayList<Friend> friends, String filterName) {
        ListFriend = new DefaultListModel();
        for(int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getName().equals(filterName)) {
                continue;
            }
            ListFriend.addElement(friends.get(i));
        }
    }
    
    public static void UpdateStatus(String name, String status) {
        for(int i = 0; i < ListFriend.size(); i++) {
            if (ListFriend.get(i).getName().equals(name)) {
                ListFriend.set(i, new Friend(name, status));
                break;
            }
        }
    }
}
