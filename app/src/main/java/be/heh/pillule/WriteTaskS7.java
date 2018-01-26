package be.heh.pillule;

import android.os.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;

import SimaticS7.S7;
import SimaticS7.S7Client;

/**
 * Created by gquittet on 11/17/17.
 */

public class WriteTaskS7 {

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private Thread writeThread;
    private AutomateS7 plcS7;

    private S7Client comS7;
    private String[] parConnexion = new String[10];
    public byte[] motCommande;

    private int dbNumber;
    private int start;
    private int amount;

    public WriteTaskS7(int dbNumber, int start, int amount) {
        this.dbNumber = dbNumber;
        this.start = start;
        this.amount = amount;
        motCommande = new byte[amount];
        comS7 = new S7Client();

        plcS7 = new AutomateS7();
        writeThread = new Thread(plcS7);
    }

    public void start(String a, String r, String s) {
        if (!writeThread.isAlive()) {
            parConnexion[0] = a;
            parConnexion[1] = r;
            parConnexion[2] = s;
            writeThread.start();
            isRunning.set(true);
        }
    }

    public void stop() {
        isRunning.set(false);
        comS7.Disconnect();
        writeThread.interrupt();
    }

    private class AutomateS7 implements Runnable {

        @Override
        public void run() {
            try {
                comS7.SetConnectionType(S7.S7_BASIC);
                Integer res = comS7.ConnectTo(parConnexion[0], Integer.valueOf(parConnexion[1]), Integer.valueOf(parConnexion[2]));
                while (isRunning.get() && res.equals(0)) {
                    Integer writePLC = comS7.WriteArea(S7.S7AreaDB, dbNumber, start, amount, motCommande);
                    SystemClock.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setWriteBool(int b, int v) {
        if (v == 1)
            motCommande[0] = (byte) (b | motCommande[0]);
        else
            // ~b = complément à 1 de b
            motCommande[0] = (byte) (~b & motCommande[0]);
    }

    public void setBitAt(int b, int v) {
        byte[] powerOf2 = {(byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x16,
                (byte) 0x32, (byte) 0x64, (byte) 0xA0};

        b = b < 0 ? 0 : b;
        b = b > 7 ? 7 : b;

        if (v == 1)
            motCommande[0] = (byte) (motCommande[0] | powerOf2[b]);
        else
            motCommande[0] = (byte) (motCommande[0] & ~powerOf2[b]);
    }

    public void setWordAt(int p, int v) {
        int word = v & 0x0FFFF;
        motCommande[p] = (byte) (word >> 8);
        motCommande[p + 1] = (byte) (word & 0x00FF);
    }

}

