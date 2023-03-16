package pt.tecnico.ulisboa;

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
    @Test
    public void noByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InterruptedException {
        List<Map.Entry<String,Integer>> processes =
                Utility.readProcesses("/home/cat/uni/mestrado/SEC/HDS/services.txt").getValue();

        //servers on the services.txt
        Service server1 = new Service("localhost", 1234, false, 1, processes, true);
        Service server2 = new Service("localhost", 1235, false, 1, processes, false);
        Service server3 = new Service("localhost", 1236, false, 1, processes, false);
        Service server4 = new Service("localhost", 1237, false, 1, processes, false);
        //wait for servers to initialize

        Client client = new Client(processes);
        String valueToAppend = "ola!";
        client.send(valueToAppend);

        Thread.sleep(500);
        System.out.println(server1.getBlockchainData());
        System.out.println(server2.getBlockchainData());
        System.out.println(server3.getBlockchainData());
        System.out.println(server4.getBlockchainData());
        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server2.getBlockchainIndex(0));
        assertTrue(server2.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));
        }



    /*@Test
    public void oneByzantineServers() throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException{
         List<Map.Entry<String,Integer>> processes =
                Utility.readProcesses("/home/cat/uni/mestrado/SEC/HDS/services.txt").getValue();
        //servers on the services.txt

        Service server1 = new Service("localhost", 1234, false, 1, processes, true);
        //server2 is byzantine
        Service server2 = new Service("localhost", 1235, true, 1, processes, false);
        Service server3 = new Service("localhost", 1236, false, 1, processes, false);
        Service server4 = new Service("localhost", 1237, false, 1, processes, false);
        Client client = new Client(processes);

        String valueToAppend = "ola!";
        client.send(valueToAppend);
        Thread.sleep(500);
        assertEquals(valueToAppend, server1.getBlockchainIndex(0));
        assertTrue(server1.isInBlockchain(valueToAppend));
        assertThat(valueToAppend, server2.getBlockchainIndex(0));
        assertTrue(server2.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server3.getBlockchainIndex(0));
        assertTrue(server3.isInBlockchain(valueToAppend));
        assertEquals(valueToAppend, server4.getBlockchainIndex(0));
        assertTrue(server4.isInBlockchain(valueToAppend));

    }*/
}