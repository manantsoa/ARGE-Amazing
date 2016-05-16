package edu.m2dl.s10.arge.openstack.repartiteur;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by manantsoa on 12/05/16.
 */
public class ComputerNode {

    private String id;
    private String port;
    private String ip;
    private XmlRpcClient client;

    public ComputerNode(String id, String port, String ip) {
        this.id = id;
        this.port = port;
        this.ip = ip;
        setClient();
    }

    public ComputerNode(String id, String port, String ip, XmlRpcClient client) {
        this.id = id;
        this.port = port;
        this.ip = ip;
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public String getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public XmlRpcClient getClient() {
        return client;
    }

    private void setClient() {
        String ipc = getIp();
        String portc = getPort();
        String url = "http://" + ipc + ":" + portc + "/request";

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        try {
            config.setServerURL(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        client = new XmlRpcClient();

        // use Commons HttpClient as transport
        client.setTransportFactory(
                new XmlRpcCommonsTransportFactory(client));
        // set configuration
        client.setConfig(config);
    }

}
