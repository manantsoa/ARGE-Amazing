package edu.m2dl.s10.arge.openstack.repartiteur;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by julien on 25/03/16.
 */
public class Repartiteur implements Runnable {

    private String port;
    private List<String> serversId;

    public static OSClient os;

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Il faut fournir le numéro du port");
        }


        String port = args[0];

        // Boucle de reception des requetes

        Repartiteur r = new Repartiteur(port);
        r.run();

    }

    public void run() {

        serversId = new ArrayList<String>();
        os = connectToCloud();
        WebServer webServer = new WebServer(Integer.parseInt(port));

        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        PropertyHandlerMapping phm = new PropertyHandlerMapping();
          /* Load handler definitions from a property file.
           * The property file might look like:
           *   Calculator=org.apache.xmlrpc.demo.Calculator
           *   org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.AdderImpl
           */
        try {
            phm.addHandler("Calculateur", edu.m2dl.s10.arge.openstack.calculateur.Calculateur.class);
            System.out.println("Lancement du serveur sur le port " + port);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

          /* You may also provide the handler classes directly,
           * like this:
           * phm.addHandler("Calculator",
           *     org.apache.xmlrpc.demo.Calculator.class);
           * phm.addHandler(org.apache.xmlrpc.demo.proxy.Adder.class.getName(),
           *     org.apache.xmlrpc.demo.proxy.AdderImpl.class);
           */
        xmlRpcServer.setHandlerMapping(phm);

        XmlRpcServerConfigImpl serverConfig =
                (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);

        try {
            webServer.start();
            String id = addVM();
            deleteVM(id);
        } catch (IOException e) {
            e.printStackTrace();
        }


        while(true) {



            // Serveur : Récupérer les requetes de l'updateRepartiteur


            // Client : Contacter les Calculateur

        }

    }

    public Repartiteur(String port) {
        this.port = port;
    }

    public void add(String ip, String port) {
        System.out.println("AJOUTE UN CALCULATEUR ["+ip+":"+port+"]");
    }

    public void del(String ip, String port) {
        // Appel de Léo
        System.out.println("SUPPRIME UN CALCULTEUR ["+ip+":"+port+"]");

    }

    public void request() {
        // Appel du client
        System.out.println("REDIRIGE LA REQUETE VERS UN CALCULATEUR");
    }

    public static OSClient connectToCloud() {
        System.out.print("Connection");
        OSClient os = OSFactory.builder()
                .endpoint("http://127.0.0.1:5000/v2.0")
                .credentials("ens18", "LEBWJ1")
                .tenantName("service").authenticate();


        System.out.println("Success");
        System.out.println(os);
//        System.out.println(os.images().list());
        return os;
    }

    public String addVM() {
        ServerCreate sc;

//      Image img = os.compute().images().get("");
        List networksId = Arrays.asList("c1445469-4640-4c5a-ad86-9c0cb6650cca");
        sc = Builders.server().name("manantsoa-node-" + new Date().getTime())
                .flavor("2")
                .image("5d112607-9153-4c20-b999-716cca846dc2")
                .keypairName("mykey")
                .networks(networksId).build();

        Server server = os.compute().servers().boot(sc);

        serversId.add(server.getId());
        System.out.println("Creation succeded of " + server.getId());

        return server.getId();
    }

    public void deleteVM(String id) {
        os.compute().servers().delete(id);
        System.out.println("Deletion succeeded of " + id);
    }

    public void deleteVM() {
        String id = serversId.remove(0);

        os.compute().servers().delete(id);
        System.out.println("Deletion succeeded of " + id);
    }

    public void printVM() {
        System.out.println();
    }
}
