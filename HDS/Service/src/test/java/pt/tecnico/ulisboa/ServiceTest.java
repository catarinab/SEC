package pt.tecnico.ulisboa;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceTest {
    int byzantineProcessesFourServers;
    List<Map.Entry<String,Integer>> processesFourServers;
    Map.Entry<String,Integer> leaderFourServers;
    Client client;
    Client secondClient;

    @BeforeAll
    public void init() throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException{
        System.out.println("Running tests... might take a while");
        //System.setOut(new PrintStream(new ByteArrayOutputStream()));

        Entry<Integer, List<Entry<String,Integer>>> fileSetupFourServers = Utility.readProcesses("../TestConfig/services_test1.txt");
        byzantineProcessesFourServers = fileSetupFourServers.getKey();
        processesFourServers = fileSetupFourServers.getValue();
        leaderFourServers = processesFourServers.get(0);

        client = new Client("localhost", 4321, processesFourServers, byzantineProcessesFourServers);
        secondClient = new Client("localhost", 4322, processesFourServers, byzantineProcessesFourServers);
    }

    @Test
    @DisplayName("Testing: four correct members and two clients sending request to create account and transfer from one to another")
    public void fourServersNoByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesFourServers, processesFourServers, true, leaderFourServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server4.start();

        Client thread = new Client(client);
        thread.start();
        Client thread2 = new Client(secondClient);
        thread2.start();

        client.send("create_account", "");
        secondClient.send("create_account", "");

        Thread.sleep(1000);

        String keyClient1 = client.getPublicKey();
        String keyClient2 = secondClient.getPublicKey();

        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);

        Thread.sleep(20000);

        String predictedValue = "Blockchain: \n" +
                "Block 0:0 -> source: Create Account: " + keyClient1 + ", new balance: 20\n" +
                "\t1 -> source: Create Account: " + keyClient2 + ", new balance: 20\n" +
                "\t2 -> source: Transfer from account: " + keyClient1 + " with previous balance 20 and new balance 18, to destination account " + keyClient2 + " with previous balance 20 and new balance 21, with the value: 1, and the fee: 1\n" +
                "\t3 -> source: Transfer from account: " + keyClient2 + " with previous balance 21 and new balance 19, to destination account " + keyClient1 + " with previous balance 18 and new balance 19, with the value: 1, and the fee: 1\n" +
                "\t4 -> source: Transfer from account: " + keyClient1 + " with previous balance 19 and new balance 17, to destination account " + keyClient2 + " with previous balance 19 and new balance 20, with the value: 1, and the fee: 1\n" +
                "\t5 -> source: Transfer from account: " + keyClient2 + " with previous balance 20 and new balance 18, to destination account " + keyClient1 + " with previous balance 17 and new balance 18, with the value: 1, and the fee: 1\n" +
                "\t6 -> source: Transfer from account: " + keyClient1 + " with previous balance 18 and new balance 16, to destination account " + keyClient2 + " with previous balance 18 and new balance 19, with the value: 1, and the fee: 1\n" +
                "\t7 -> source: Transfer from account: " + keyClient2 + " with previous balance 19 and new balance 17, to destination account " + keyClient1 + " with previous balance 16 and new balance 17, with the value: 1, and the fee: 1\n" +
                "\t8 -> source: Transfer from account: " + keyClient1 + " with previous balance 17 and new balance 15, to destination account " + keyClient2 + " with previous balance 17 and new balance 18, with the value: 1, and the fee: 1\n" +
                "\t9 -> source: Transfer from account: " + keyClient2 + " with previous balance 18 and new balance 16, to destination account " + keyClient1 + " with previous balance 15 and new balance 16, with the value: 1, and the fee: 1";

        assertTrue(server1.predictedBlockchain(predictedValue));
        assertTrue(server2.predictedBlockchain(predictedValue));
        assertTrue(server3.predictedBlockchain(predictedValue));
        assertTrue(server4.predictedBlockchain(predictedValue));

        thread.interrupt();
        thread2.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

    @Test
    @DisplayName("Testing: four correct members and two clients sending request to weak strong balance after transfers")
    public void strongBalanceNoByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {

        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesFourServers, processesFourServers, true, leaderFourServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server4.start();

        Client thread = new Client(client);
        thread.start();
        Client thread2 = new Client(secondClient);
        thread2.start();

        client.send("create_account", "");
        secondClient.send("create_account", "");

        Thread.sleep(1000);

        String keyClient1 = client.getPublicKey();
        String keyClient2 = secondClient.getPublicKey();

        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);

        Thread.sleep(20000);

        String correctBalance = "16";

        client.send("check_balance", "strong");
        secondClient.send("check_balance", "strong");

        Thread.sleep(5000);

        System.out.println(("DEBUGGGGGGGGG: " + secondClient.getStrongBalanceReply()));

        assertTrue(client.getStrongBalanceReply().equals(correctBalance));
        assertTrue(secondClient.getStrongBalanceReply().equals(correctBalance));

        thread.interrupt();
        thread2.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

    @Test
    @DisplayName("Testing: four correct members and two clients sending request to check weak balance after transfers")
    public void weakBalanceNoByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {

        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesFourServers, processesFourServers, true, leaderFourServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server4.start();

        Client thread = new Client(client);
        thread.start();
        Client thread2 = new Client(secondClient);
        thread2.start();

        client.send("create_account", "");
        secondClient.send("create_account", "");

        Thread.sleep(1000);

        String keyClient1 = client.getPublicKey();
        String keyClient2 = secondClient.getPublicKey();

        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);
        client.send("transfer", "1;"+keyClient2);
        secondClient.send("transfer", "1;"+keyClient1);

        Thread.sleep(20000);

        ArrayList<String> possibleWeakReads = new ArrayList<>();
        possibleWeakReads.add("20");
        possibleWeakReads.add("18");
        possibleWeakReads.add("21");
        possibleWeakReads.add("19");
        possibleWeakReads.add("17");
        possibleWeakReads.add("16");
        possibleWeakReads.add("15");

        client.requestWeakRead();

        Thread.sleep(10000);

        assertTrue(possibleWeakReads.contains(client.getWeakBalanceReply()));

        thread.interrupt();
        thread2.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }
}