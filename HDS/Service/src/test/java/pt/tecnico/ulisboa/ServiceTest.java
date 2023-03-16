package pt.tecnico.ulisboa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class ServiceTest {

    ServiceAux server1 = null;
    ServiceAux server2 = null;
    ServiceAux server3 = null;
    ServiceAux server4 = null;

    @AfterEach
    public void tearDown() {
        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();
    }

    @Test
    public void noByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {


        //servers on the services_tests.txt
        List<Map.Entry<String,Integer>> processes =
                Utility.readProcesses("../TestConfig/services_test1.txt").getValue();


        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, 1, processes, true);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, false, 1, processes, false);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, 1, processes, false);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, 1, processes, false);
        server4.start();

        Thread.sleep(1000);

        Client client = new Client(processes);
        Client thread = new Client(client);
        thread.start();
        String valueToAppend = "ola!";
        client.send(valueToAppend);

        Thread.sleep(10000);

        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server2.getBlockchainIndex(0));
        assertTrue(server2.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));

    }

/*    @Test
    public void oneByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        //servers on the services_tests.txt
        List<Map.Entry<String,Integer>> processes =
                Utility.readProcesses("../TestConfig/services_test1.txt").getValue();

        //initialize servers
        server1 = new ServiceAux("localhost", 1234, false, 1, processes, true);
        server1.start();
        server2 = new ServiceAux("localhost", 1235, true, 1, processes, false);
        server2.start();
        server3 = new ServiceAux("localhost", 1236, false, 1, processes, false);
        server3.start();
        server4 = new ServiceAux("localhost", 1237, false, 1, processes, false);
        server4.start();

        Thread.sleep(1000);

        Client client = new Client(processes);
        Client thread = new Client(client);
        thread.start();
        String valueToAppend = "ola!";
        client.send(valueToAppend);

        Thread.sleep(10000);

        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertFalse(server2.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));
    }*/
}
