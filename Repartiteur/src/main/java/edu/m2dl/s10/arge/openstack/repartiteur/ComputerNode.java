package edu.m2dl.s10.arge.openstack.repartiteur;

/**
 * Created by manantsoa on 12/05/16.
 */
public class ComputerNode {

    private String id;
    private String port;
    private String ip;

    public ComputerNode(String id, String port, String ip) {
        this.id = id;
        this.port = port;
        this.ip = ip;
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

}
