package edu.m2dl.s10.arge.openstack.repartiteur;

import edu.m2dl.s10.arge.openstack.calculateur.Calculateur;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.*;

/**
 * Created by julien on 25/03/16.
 */
public class Repartiteur implements Runnable {

    private static String port;
    private final static String portCalculateur = "8080";
    private static List<ComputerNode> calculateurs;
    private static boolean firstCall;

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

        calculateurs = new ArrayList<ComputerNode>();
        os = connectToCloud();
        firstCall = false;
        WebServer webServer = new WebServer(Integer.parseInt(port));

        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        PropertyHandlerMapping phm = new PropertyHandlerMapping();
          /* Load handler definitions from a property file.
           * The property file might look like:
           *   Calculator=org.apache.xmlrpc.demo.Calculator
           *   org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.AdderImpl
           */

        try {
            phm.addHandler("Repartiteur", Repartiteur.class);
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
            ComputerNode newWorker = addVM();//Ajout d'un calculateur au lancement
            calculateurs.add(newWorker);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Repartiteur(String port) {
        Repartiteur.port = port;
    }

    public Repartiteur() {
        if(calculateurs == null) {
            calculateurs = new ArrayList<ComputerNode>();
        }
    }

    public BigInteger request(String method, int nb) throws InterruptedException {
        BigInteger result = null;
        boolean fullLoad = true;

        if(!firstCall) {
            System.out.println("CONNEXION AU CALCULATEUR EN COURS...");
            Thread.sleep(5000);
            firstCall = true;
        }

        // Appel du client
        System.out.print("REDIRIGE LA REQUETE VERS LE CALCULATEUR ");

        //Récupération calculateur
        ComputerNode worker = getVM();
        System.out.println(worker.getIp());

        Object[] params = new Object[] {nb};


        try {
            result = (BigInteger) worker.getClient().execute("Calculateur." + method, params);
            System.out.println(result);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static OSClient connectToCloud() {
        System.out.println("Connection...");
        OSClient os = OSFactory.builder()
                .endpoint("http://195.220.53.61:5000/v2.0")
                .credentials("ens18", "LEBWJ1")
                .tenantName("service").authenticate();


        System.out.println("Success");
        System.out.println(os);
        return os;
    }

    public ComputerNode addVM() {
        ServerCreate sc;

        List networksId = Arrays.asList("c1445469-4640-4c5a-ad86-9c0cb6650cca");
        sc = Builders.server().name("manantsoa-node-" + new Date().getTime())
                .flavor("2")
                .image("4b10554f-020f-4117-82d1-21c7bb5ef46e")
                .keypairName("mykey")
                .networks(networksId).build();

        Server server = os.compute().servers().boot(sc);

        // Wait for the server creation
        while ((server = os.compute().servers().get(server.getId())).getStatus() != Server.Status.ACTIVE) {
            try {
                System.out.println("VM EN COURS DE DEMARRAGE...");
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Map<String, List<? extends Address>> adresses = server.getAddresses().getAddresses();
        // Get the first IP
        if (adresses.size() <= 0) {
            String message = String.format("The created instance=[%s] does not have any IP", server.getName());
            throw new RuntimeException(message);
        }

        // First address
        Address address = adresses.values().iterator().next().get(0);

        System.out.println("OK = ["+address.getAddr()+"]");
        System.out.println("Creation succeeded of " + server.getId());

        return new ComputerNode(server.getId(), "8080", address.getAddr());
    }

    public void deleteVM(String id) {
        os.compute().servers().delete(id);
        System.out.println("Deletion succeeded of " + id);
    }

    public void deleteVM(ComputerNode cn) {
        os.compute().servers().delete(cn.getId());
        System.out.println("Deletion succeeded of " + cn.getId());
    }

    public void deleteVM() {
        String id = calculateurs.get(0).getId();

        os.compute().servers().delete(id);
        System.out.println("Deletion succeeded of " + id);
    }

    public ComputerNode getVM() {
        ComputerNode node = null;
        boolean notChecked = true;
        int i = 0; //index de la vm à recuperer

        //Tant que Calculateur non trouvé
        while (notChecked) {
            node = calculateurs.get(i);
            String ipc = node.getIp();
            String portc = node.getPort();
            String urlc = "http://" + ipc + ":" + portc + "/request";

            Object[] params = new Object[] {};
            double cpuLoad = 0;

            try {
                cpuLoad = (Double) node.getClient().execute("Calculateur.CPULoad", params);
                System.out.println("Charge CPU de " + node.getIp() + " : " + cpuLoad);
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }

            if (cpuLoad > 0.7) {
                if (calculateurs.size() == (i + 1)) {
                    //s'il n'y a pas d'autres calculateurs
                    node = addVM();
                    calculateurs.add(node);
                } else {
                    i++;
                }
            } else {
                notChecked = false;
            }
        }
        return node;
    }

}
