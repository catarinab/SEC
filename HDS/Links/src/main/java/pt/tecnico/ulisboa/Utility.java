package pt.tecnico.ulisboa;

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

    enum Type {
        CLIENT,
        SERVER
    }
}
