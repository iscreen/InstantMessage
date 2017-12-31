/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.client;

import javax.swing.DefaultListModel;

/**
 *
 * @author Dean Lin
 */
public class Group {
    
    private String name;
    private DefaultListModel<Friend> members = new DefaultListModel();
    
    public Group(String name, DefaultListModel<Friend> members) {
        this.name = name;
        this.members = members;
    }
    
    public void addMember(Friend friend) {
        this.members.addElement(friend);
    }
    
    public DefaultListModel getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }
    
}
