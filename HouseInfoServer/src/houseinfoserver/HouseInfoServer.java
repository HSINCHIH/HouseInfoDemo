/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houseinfoserver;

import java.util.Scanner;

/**
 *
 * @author developer
 */
public class HouseInfoServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Log log = new Log("HouseInfoServer");
        MainServer server = new MainServer();
        if (!server.Start()) {
            log.Writeln(String.format("%s %s", "main", "Start fail"));
            return;
        }
        System.out.println("Press enter key to stop server");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        server.Stop();
    }

}
