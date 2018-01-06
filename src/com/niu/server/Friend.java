/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.server;

/**
 *
 * @author Dean Lin
 */
public class Friend {
    private String name;
    private String status;
    public Friend(String name) {
        this.name = name;
        this.status = "Offline";
    }
    public Friend(String name, String status) {
        this.name = name;
        this.status = status;
    }
    public String getName() {
        return this.name;
    }
    public String toString() {
        return this.name + " " + this.status;
    }
}
