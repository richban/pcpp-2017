import java.io.*;
import akka.actor.*;
import java.util.Random;
import java.util.HashMap;

public class SesComSys {
    public static void main(String[] args) {
      final ActorSystem system = ActorSystem.create("SesComSys");
      // Create Actor for Registry
      final ActorRef registry = system.actorOf(Props.create(RegistryActor.class), "Registry");
      // Create Actor for Sender
      final ActorRef sender = system.actorOf(Props.create(SenderActor.class), "Sender");
      // Create Actor for Receiver
      final ActorRef receiver = system.actorOf(Props.create(ReceiverActor.class), "Receiver");

      // Initialize the Receiver
      receiver.tell(new InitMessage(registry), ActorRef.noSender());
      // Initialize the Sender
      sender.tell(new InitMessage(registry), ActorRef.noSender());

      sender.tell(new CommMessage(receiver), ActorRef.noSender());

      try {
        System.out.println("Press return to terminate...");
        System.in.read();
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          system.shutdown();
      }
    }
}

// -- AUXILIARY METHODS ----------------------------------------


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

// -- MESSAGES ------------------------------------------------

class InitMessage implements Serializable {
  public final ActorRef registry;
  public InitMessage(ActorRef registry) { this.registry = registry; }
}

class RegMessage implements Serializable {
  public final ActorRef receiver;
  public RegMessage(ActorRef receiver) { this.receiver = receiver; }
}

class KeyMessage implements Serializable {
  public final KeyPair keypair;
  public KeyMessage(KeyPair keypair) { this.keypair = keypair; }
}

class CommMessage implements Serializable {
  public final ActorRef receiver;
  public CommMessage(ActorRef receiver) { this.receiver = receiver; }
}

class LookupMessage implements Serializable {
  public final ActorRef receiver;
  public final ActorRef sender;
  public LookupMessage(ActorRef receiver, ActorRef sender) {
    this.receiver = receiver;
    this.sender = sender;
  }
}

class PubKeyMessage implements Serializable {
  public final ActorRef receiver;
  public final int public_key;
  public PubKeyMessage(ActorRef receiver, int public_key) {
    this.receiver = receiver;
    this.public_key = public_key;
  }
}

class Message implements Serializable {
    public final String s;
    public Message(String s) { this.s = s; }
}

// -- ACTOR --------------------------------------------------

class SenderActor extends UntypedActor {
  public ActorRef registry;

  public void onReceive(Object o) throws Exception {
    if (o instanceof InitMessage) {
      InitMessage init = (InitMessage) o;
      this.registry = init.registry;
    }
    else if (o instanceof CommMessage) {
      CommMessage comm = (CommMessage) o;
      ActorRef receiver = comm.receiver;
      registry.tell(new LookupMessage(receiver, getSelf()), getSelf());
    }
    else if (o instanceof PubKeyMessage) {
      PubKeyMessage pubkey = (PubKeyMessage) o;
      ActorRef receiver = pubkey.receiver;
      System.out.println("cleartext: SECRET");
      String encrypted = Crypto.encrypt("SECRET", pubkey.public_key);
      System.out.println("encrypted: " + encrypted);
      receiver.tell(new Message(encrypted), ActorRef.noSender());
    }
  }
}

class ReceiverActor extends UntypedActor {
  public ActorRef registry;
  public KeyPair keypair;

  public void onReceive(Object o) throws Exception {
    if (o instanceof InitMessage) {
      InitMessage init = (InitMessage) o;
      this.registry = init.registry;
      this.registry.tell(new RegMessage(getSelf()), getSelf());
    }
    else if (o instanceof KeyMessage) {
      KeyMessage keys = (KeyMessage) o;
      this.keypair = keys.keypair;
    }
    else if (o instanceof Message) {
      Message message = (Message) o;
      String decrypted = Crypto.encrypt(message.s, keypair.private_key);
      System.out.println("decrypted: " + decrypted);
    }
  }
}

class RegistryActor extends UntypedActor {
  public ActorRef receiver;
  HashMap<ActorRef, Integer> pk_keys = new HashMap<ActorRef, Integer>();

  public void onReceive(Object o) throws Exception {
    if (o instanceof RegMessage) {
      RegMessage regmsg = (RegMessage) o;
      this.receiver = regmsg.receiver;
      KeyPair key = Crypto.keygen();
      pk_keys.put(this.receiver, key.public_key);
      this.receiver.tell(new KeyMessage(key), ActorRef.noSender());
    }
    else if (o instanceof LookupMessage) {
      LookupMessage lookup = (LookupMessage) o;
      ActorRef sender = lookup.sender;
      ActorRef receiver = lookup.receiver;
      int public_key = pk_keys.get(lookup.receiver);
      sender.tell(new PubKeyMessage(receiver, public_key), ActorRef.noSender());
    }
  }
}
