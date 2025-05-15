package com.ipi.mesi_backend_rpg.configuration;


import java.util.Properties;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Configuration
@Profile("prod")  // Uniquement actif en production
public class SSHTunnelConfig {

    private Session session;

    @Value("${ssh.tunnel.host}")
    private String sshHost;

    @Value("${ssh.tunnel.port:22}")
    private int sshPort;

    @Value("${ssh.tunnel.username}")
    private String sshUsername;

    @Value("${ssh.tunnel.privateKey}")
    private String sshPrivateKeyPath;

    @Value("${ssh.tunnel.localPort:13306}")
    private int localPort;

    @Value("${ssh.tunnel.remoteHost}")
    private String remoteHost;

    @Value("${ssh.tunnel.remotePort:3306}")
    private int remotePort;

    @Bean(name = "sshTunnel")
    public boolean setupSshTunnel() throws JSchException {
        JSch jsch = new JSch();
        
        // Charger la clé privée depuis un fichier ou une variable d'environnement
        if (sshPrivateKeyPath.startsWith("PRIVATE_KEY:")) {
            // Charger depuis une variable d'environnement
            String privateKeyContent = sshPrivateKeyPath.substring("PRIVATE_KEY:".length());
            jsch.addIdentity("sshkey", privateKeyContent.getBytes(), null, null);
        } else {
            // Charger depuis un fichier
            jsch.addIdentity(sshPrivateKeyPath);
        }

        session = jsch.getSession(sshUsername, sshHost, sshPort);

        // Configuration SSH
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // Connecter et établir le tunnel
        session.connect();
        session.setPortForwardingL(localPort, remoteHost, remotePort);

        System.out.println("SSH tunnel established: localhost:" + localPort + " -> " + remoteHost + ":" + remotePort);
        return true;
    }

    @PreDestroy
    public void closeSSHTunnel() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("SSH tunnel closed");
        }
    }
}
 