import java.io.*;
import akka.actor.*;
import java.util.Random;

public class SesComSys {
    public static void main(String[] args) {
      System.out.println("Press return to terminate...");
    }
}

class KeyPair implements Serializable {
    public final int public_key, private_key;
    public KeyPair(int public_key, int private_key) {
        this.public_key = public_key;
        this.private_key = private_key;
    }
}

class Crypto {
    static KeyPair keygen() {
        int public_key = (new Random()).nextInt(25)+1;
        int private_key = 26 - public_key;
        System.out.println("public key: " + public_key);
        System.out.println("private key: " + private_key);
        return new KeyPair(public_key, private_key);
}

  static String encrypt(String cleartext, int key) {
      StringBuffer encrypted = new StringBuffer();
      for (int i=0; i<cleartext.length(); i++) {
          encrypted.append((char) ('A' + ((((int)
              cleartext.charAt(i)) - 'A' + key) % 26)));
      }
      return "" + encrypted;
  }
}
