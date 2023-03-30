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
    int byzantineProcessesSevenServers;
    List<Map.Entry<String,Integer>> processesSevenServers;
    Map.Entry<String,Integer> leaderSevenServers;
    Client clientFourServers;
    Client clientSevenServers;
    Client secondClientFourServers;

    @BeforeAll
    public void init() throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException{
        System.out.println("Running tests... might take a while");
        System.setOut(new PrintStream(new ByteArrayOutputStream()));

        Entry<Integer, List<Entry<String,Integer>>> fileSetupFourServers = Utility.readProcesses("../TestConfig/services_test1.txt");
        byzantineProcessesFourServers = fileSetupFourServers.getKey();
        processesFourServers = fileSetupFourServers.getValue();
        leaderFourServers = processesFourServers.get(0);


        Entry<Integer, List<Entry<String,Integer>>> fileSetupSevenServers = Utility.readProcesses("../TestConfig/services_test2.txt");
        byzantineProcessesSevenServers = fileSetupSevenServers.getKey();
        processesSevenServers = fileSetupSevenServers.getValue();
        leaderSevenServers = processesSevenServers.get(0);

        clientFourServers = new Client("localhost", 4321, processesFourServers, byzantineProcessesFourServers);
        clientSevenServers = new Client("localhost", 4322, processesSevenServers, byzantineProcessesSevenServers);
        secondClientFourServers = new Client("localhost", 4323, processesFourServers, byzantineProcessesFourServers);
    }


    @Test
    @DisplayName("Testing: four correct members and a client sending one message")
    public void noByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
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

        Client thread = new Client(clientFourServers);
        thread.start();
        String valueToAppend = "ola!";
        clientFourServers.send("append", valueToAppend);

        Thread.sleep(10000);

        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server2.getBlockchainIndex(0));
        assertTrue(server2.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));

        thread.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();

    }

    @Test
    @DisplayName("Testing: three correct members, one byzantine member and a client sending one message")
    public void oneByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {

        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesFourServers, processesFourServers, true, leaderFourServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, true, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server4.start();

        Client thread = new Client(clientFourServers);
        thread.start();
        String valueToAppend = "ola!";
        clientFourServers.send("append", valueToAppend);

        Thread.sleep(10000);

        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertFalse(server2.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));

        thread.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

   @Test
    @DisplayName("Testing: five correct members, two byzantine members and a client sending one message")
    public void twoByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {

        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;
        ServiceAux server5 = null;
        ServiceAux server6 = null;
        ServiceAux server7 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesSevenServers, processesSevenServers, true, leaderSevenServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, true, byzantineProcessesSevenServers, processesSevenServers, false, leaderSevenServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, true, byzantineProcessesSevenServers, processesSevenServers, false, leaderSevenServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesSevenServers, processesSevenServers, false, leaderSevenServers);
        server4.start();
        server5 = new ServiceAux("localhost", 1238, false, byzantineProcessesSevenServers, processesSevenServers, false, leaderSevenServers);
        server5.start();
        server6 = new ServiceAux("localhost", 1239, false, byzantineProcessesSevenServers, processesSevenServers, false, leaderSevenServers);
        server6.start();
        server7 = new ServiceAux("localhost", 4446, false, byzantineProcessesSevenServers, processesSevenServers, false, leaderSevenServers);
        server7.start();

        Client thread = new Client(clientSevenServers);
        thread.start();
        String valueToAppend = "ola!";
        clientSevenServers.send("append", valueToAppend);

        Thread.sleep(17000);

        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertFalse(server2.isInBlockchain(valueToAppend));
        assertFalse(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server5.getBlockchainIndex(0));
        assertTrue(server5.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server6.getBlockchainIndex(0));
        assertTrue(server6.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server7.getBlockchainIndex(0));
        assertTrue(server7.isInBlockchain(valueToAppend));

        thread.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server5.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server6.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server7.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

    @Test
    @DisplayName("Testing: four correct members and a client sending two messages, one after another")
    public void twoMessageFourServersNoByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
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

        Client thread = new Client(clientFourServers);
        thread.start();
        String valueToAppend1 = "ola!";
        String valueToAppend2 = "adeus!";
        clientFourServers.send("append", valueToAppend1);
        clientFourServers.send("append", valueToAppend2);

        Thread.sleep(10000);

        assertEquals(valueToAppend1, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend1));
        assertEquals(valueToAppend1, server2.getBlockchainIndex(0));
        assertTrue(server2.isInBlockchain(valueToAppend1));
        assertEquals(valueToAppend1, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend1));
        assertEquals(valueToAppend1, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend1));

        assertEquals(valueToAppend2, server1.getBlockchainIndex(1));
        assertTrue(server1.isInBlockchain(valueToAppend2));
        assertEquals(valueToAppend2, server2.getBlockchainIndex(1));
        assertTrue(server2.isInBlockchain(valueToAppend2));
        assertEquals(valueToAppend2, server3.getBlockchainIndex(1));
        assertTrue(server3.isInBlockchain(valueToAppend2));
        assertEquals(valueToAppend2, server4.getBlockchainIndex(1));
        assertTrue(server4.isInBlockchain(valueToAppend2));

        thread.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

    @Test
    @DisplayName("Testing: three correct members, one byzantine member and a client sending two messages, one after another")
    public void twoMessageFourServersOneByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {

        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesFourServers, processesFourServers, true, leaderFourServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, true, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server4.start();

        Client thread = new Client(clientFourServers);
        thread.start();
        String valueToAppend1 = "ola!";
        String valueToAppend2 = "adeus!";
        clientFourServers.send("append", valueToAppend1);
        clientFourServers.send("append", valueToAppend2);

        Thread.sleep(10000);

        assertEquals(valueToAppend1, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend1));
        assertFalse(server2.isInBlockchain(valueToAppend1));
        assertEquals(valueToAppend1, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend1));
        assertEquals(valueToAppend1, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend1));

        assertEquals(valueToAppend2, server1.getBlockchainIndex(1));
        assertTrue(server1.isInBlockchain(valueToAppend2));
        assertFalse(server2.isInBlockchain(valueToAppend2));
        assertEquals(valueToAppend2, server3.getBlockchainIndex(1));
        assertTrue(server3.isInBlockchain(valueToAppend2));
        assertEquals(valueToAppend2, server4.getBlockchainIndex(1));
        assertTrue(server4.isInBlockchain(valueToAppend2));

        thread.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

    @Test
    @DisplayName("Testing: four correct members and two clients, each sending one message")
    public void twoClientsFourServersNoByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
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

        Client thread = new Client(clientFourServers);
        thread.start();
        Client thread2 = new Client(secondClientFourServers);
        thread2.start();
        String valueToAppend1 = "ola!";
        String valueToAppend2 = "adeus!";
        clientFourServers.send("append", valueToAppend1);
        secondClientFourServers.send("append", valueToAppend2);

        Thread.sleep(10000);

        assertTrue(server1.isInBlockchain(valueToAppend1));
        assertTrue(server2.isInBlockchain(valueToAppend1));
        assertTrue(server3.isInBlockchain(valueToAppend1));
        assertTrue(server4.isInBlockchain(valueToAppend1));

        assertTrue(server1.isInBlockchain(valueToAppend2));
        assertTrue(server2.isInBlockchain(valueToAppend2));
        assertTrue(server3.isInBlockchain(valueToAppend2));
        assertTrue(server4.isInBlockchain(valueToAppend2));

        thread.interrupt();
        thread2.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }

    @Test
    @DisplayName("Testing: three correct members, one byzantine member and two clients, each sending one message")
    public void twoClientsFourServersOneByzantine() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {

        ServiceAux server1 = null;
        ServiceAux server2 = null;
        ServiceAux server3 = null;
        ServiceAux server4 = null;

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, byzantineProcessesFourServers, processesFourServers, true, leaderFourServers);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, true, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, byzantineProcessesFourServers, processesFourServers, false, leaderFourServers);
        server4.start();

        Client thread = new Client(clientFourServers);
        thread.start();
        Client thread2 = new Client(secondClientFourServers);
        thread2.start();
        String valueToAppend1 = "ola!";
        String valueToAppend2 = "adeus!";
        clientFourServers.send("append", valueToAppend1);
        secondClientFourServers.send("append", valueToAppend2);

        Thread.sleep(10000);

        assertTrue(server1.isInBlockchain(valueToAppend1));
        assertFalse(server2.isInBlockchain(valueToAppend1));
        assertTrue(server3.isInBlockchain(valueToAppend1));
        assertTrue(server4.isInBlockchain(valueToAppend1));

        assertTrue(server1.isInBlockchain(valueToAppend2));
        assertFalse(server2.isInBlockchain(valueToAppend2));
        assertTrue(server3.isInBlockchain(valueToAppend2));
        assertTrue(server4.isInBlockchain(valueToAppend2));

        thread.interrupt();
        thread2.interrupt();

        server1.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server2.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server3.getServer().getApl().getStubbornLink().getFll().getDs().close();
        server4.getServer().getApl().getStubbornLink().getFll().getDs().close();
    }
}
