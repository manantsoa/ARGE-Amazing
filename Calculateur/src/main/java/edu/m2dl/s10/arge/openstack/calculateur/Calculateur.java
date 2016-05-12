package edu.m2dl.s10.arge.openstack.calculateur;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;

public class Calculateur {

    private static final int port = 8080;

    public int add(int i1, int i2) {
        int res = calcul_fibonacci(i1);

        //System.out.println("\nCalcul " + i1 + " = " + res + "\n");

        return res;
    }

    private static int calcul_fibonacci(int nombre)
    {
        int term1 = 1, term2 = 1, term3 = 0;

        for (int i = 0; i < nombre; i++)
        {
            term3 = term2 + term1;
            //System.out.print(term1 + "+" +term2 + "= "+ term3 + ", ");
            term1 = term2;
            term2 = term3;
        }

        return term3;

    }

    public int subtract(int i1, int i2) {
        return i1 - i2;
    }

    public static void main(String args[]) throws Exception {
        WebServer webServer = new WebServer(port);

        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        PropertyHandlerMapping phm = new PropertyHandlerMapping();
          /* Load handler definitions from a property file.
           * The property file might look like:
           *   Calculator=org.apache.xmlrpc.demo.Calculator
           *   org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.AdderImpl
           */
        try {
            phm.addHandler("Calculateur", Calculateur.class);
            System.out.println("Lancement du serveur sur le port " + port);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

        xmlRpcServer.setHandlerMapping(phm);

        XmlRpcServerConfigImpl serverConfig =
                (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);

        try {
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}