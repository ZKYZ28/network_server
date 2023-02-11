package org.helmo.murmurG6.system;

import org.helmo.murmurG6.models.Message;
import org.helmo.murmurG6.models.Protocol;
import org.helmo.murmurG6.models.User;
import org.helmo.murmurG6.models.BcryptHash;
import org.helmo.murmurG6.utils.RandomSaltGenerator;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

public class ClientRunnable implements Runnable {
    private User connectedUser;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final ServerController server;
    private final Protocol protocol = new Protocol();

    public ClientRunnable(Socket client, ServerController server) {
        this.server =  server;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            String random22 = sayHello();                                                 //Reconnaissance du murmur.client.server par le murmur.client (ou sinon crash)
            String ligne = in.readLine(); //Le murmur.client.server attend que le murmur.client ecrive quelque chose

            while(isConnected && ligne != null && !ligne.isEmpty()) {   //Quand le murmur.client envoie sa ligne
                System.out.printf("Ligne reçue : %s\r\n", ligne);       //Le murmur.client.server recoit la ligne
                Message task = protocol.analyseMessage(ligne);
                Matcher param = task.getMatcher();

                switch (task.getType()) {
                    case REGISTER: {
                        sendMessage("+OK");
                        BcryptHash bcryptHash = BcryptHash.decomposeHash(param.group(4));
                        this.connectedUser = new User(param.group(1), bcryptHash);
                        server.registerUser(connectedUser);
                        break;
                    }
                    case CONNECT: {
                        String login = param.group(1);
                        if (server.getUserCollection().isRegistered(login)) {
                            this.connectedUser = server.getUserCollection().getRegisteredUsers().get(login);
                            sendMessage("PARAM " + this.connectedUser.getBcryptRound() + " " +  this.connectedUser.getBcryptSalt());
                        } else {
                            sendMessage("-ERR");
                        }
                        break;
                    } case CONFIRM: {
                        if(param.group(1).equals(connectedUser.getHashParts().calculateChallenge(random22))){
                            sendMessage("+OK");
                        } else {
                            sendMessage("-ERR");
                        }
                    }
                }
                ligne = in.readLine();                                     //Le thread mis à disposition du murmur.client attend la prochaine ligne
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * La méthode sendMessage permet d'envoyer un message au murmur.client associé à cet objet ClientRunnable.
     *
     * @param message Le message à envoyer au murmur.client.
     */
    public void sendMessage(String message) {
        if (isConnected) {
            out.println(message);
            out.flush();
        }
    }

    private String sayHello() {
        String random22 = RandomSaltGenerator.generateSalt();
        sendMessage("HELLO " + server.getIp() + " " + random22);
        return random22;
    }
}
