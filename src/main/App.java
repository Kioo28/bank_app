package main;

import javax.swing.SwingUtilities;
import views.LoginView;


public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
         new LoginView().setVisible(true);  // Show the login view
        
        });
    }
}
