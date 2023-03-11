package pt.tecnico.ulisboa;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Utility {
    public static StringBuilder data(byte[] a) {
        if (a == null) return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    public static List<Entry<String,Integer>> readProcesses(String path) {
        List<Entry<String,Integer>> processes = new java.util.ArrayList<>();
        boolean leader = false;
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splited = line.split("\\s+");
                processes.add(new AbstractMap.SimpleEntry<>(splited[0], Integer.parseInt(splited[1])));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist.");
            e.printStackTrace();
        }
        return processes;
    }

    enum Type {
        CLIENT,
        SERVER
    }
}
