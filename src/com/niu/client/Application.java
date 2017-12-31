/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.niu.client;

import com.niu.server.Constants;

/**
 *
 * @author iscreen
 */
public class Application {
    
    public static void main(String args[]) {
        if (Singleton.client == null) {
            Singleton.client = new Client(Constants.HOST, Constants.PORT);
        }
        LoginForm login = new LoginForm();
        login.setVisible(true);
    }
}
