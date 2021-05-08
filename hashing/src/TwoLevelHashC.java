import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

// Used partial code/ code inspired by Separate Chaining Hash and Linear Probing Hash
// Alyssa Gonzales

public class TwoLevelHashC<Key, Value> {
    private final int initial_size = 4;
    Integer keyPairs = 0;
    private final int p = 7127;
    private int m = 4;
    private Hashtable<Key, Value> masterHashTable;
    private int a = 0;
    private int b = 0;
    private static Integer counter = 0;
    private Integer collisions = 0;
    private Hashtable<Key, Value> subHashTable;

    public TwoLevelHashC(String fileIn) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileIn));


        // Calculate a and b randomly with bound of p (don't want 0's)
        while (a == 0 || b == 0) {
            a = StdRandom.uniform(p);
            b = StdRandom.uniform(p);
        }

        // Create empty HashTable of size m
        masterHashTable = new Hashtable<>(initial_size);

        // Read each line in the file
        String in = br.readLine();
        while (in != null) {
            put((Key) in, (Value) (Integer.valueOf(this.counter)));
            this.counter++;
            in = br.readLine();
        }
    }

    // For Sub Hash Tables creation (S_i)
    private TwoLevelHashC(int size) {
        this.m = size;

        // Calculate a and b randomly with bound of p (don't want 0's)
        while (a == 0 || b == 0) {
            a = StdRandom.uniform(p);
            b = StdRandom.uniform(p);
        }

        // Create empty HashTable of size m
        subHashTable = new Hashtable<>(m);


    }

    // Muller's hash function
    Integer hash(Key key, int a, int b, int m) {
        Integer k = (key.hashCode() & 0x7FFFFFFF) % this.p;
        return (((a * k) + b) % this.p) % m;
    }

    // counts the collisions
    public void incrementCollisions() {
        this.collisions++;
    }

    // Inserts the specified key value pair into the hashtable, adding the new value to the S_i hashtable at that index of the master
    public void put(Key key, Value value) {
        int hashedKey = -99;

        // Check for null key
        if (key == null) throw new IllegalArgumentException("key is null");

        if (masterHashTable != null) {
            // if the table is over 1/4 full, double its size
            if (keyPairs >= m / 4) resizeMasterHT();
        }

        // Get the hashed value of the key
        hashedKey = hash(key, this.a, this.b, this.m);


        // Check if key is found
        boolean found = false;

        // Checks for Table Type (Master or sub)
        Hashtable<Key, Value> currentTable = null;

        if (masterHashTable == null) {
            currentTable = this.subHashTable;
        } else {
            currentTable = this.masterHashTable;
        }

        // Iterate through masterHashTable, find matching key and add to that HT or add it to the masterHashTable
        for (Key currKey : currentTable.keySet()) {
            if (found == true)
                break;
            // Found a match
            if (currKey.equals(hashedKey)) {
                TwoLevelHashC<Key, Value> currentSub;

                // Set boolean
                found = true;

                if (this.masterHashTable != null) {
                    // Returns the TwoLevelHashC at the index (Automatically increments its own size)
                    currentSub = (TwoLevelHashC<Key, Value>) currentTable.get(currKey);
                } else {
                    currentSub = this;
                }
                // Check if the number of keys in the subHT is greater than half of its size m
                int tempM = currentSub.getM();
                int tempKeys = currentSub.getKeyPairs();

                // If it is, resize it
                if (tempKeys >= tempM / 2) {
                    // Get a new SubHT
                    TwoLevelHashC<Key, Value> placeHolderSub = resizeSubHashTable(currentSub);
                    // Remove the current SubHT from master
                    this.masterHashTable.remove(currKey);
                    // Add the new SubHT to master
                    this.masterHashTable.put(currKey, (Value) placeHolderSub);
                    // Reset currentSub
                    currentSub = (TwoLevelHashC<Key, Value>) this.masterHashTable.get(currKey);
                }
                // If is found and in subHT
                if (currentTable == this.subHashTable) {
                    Hashtable<Key, Value> temp = this.subHashTable;
                    temp.put((Key) (Integer.valueOf(hashedKey)), value);
                } else {
                    // Add the new hash and the original value to the Sub HashTable at the index of the hashed key
                    currentSub.put(key, value);
                }

                // Increment Collisions
                currentSub.incrementCollisions();

                keyPairs++;
                break;
            }
        }

        // No Match Found
        if (!found) {
            // Check if in sub HashTable
            if (currentTable == this.subHashTable) {
                Hashtable<Key, Value> temp = this.subHashTable;
                temp.put((Key) (Integer.valueOf(hashedKey)), value);
                keyPairs++;
            }

            // If in Master HashTable
            else {

                // Create a new empty hash table at the specified index (hashedKey) returns the TwoLevelHashC at the new index on master
                TwoLevelHashC placeHolder = initializeHashTable(hashedKey);

                // Generate a random a and b for the subHashTable
                int tempHash = hash(key, placeHolder.a, placeHolder.b, initial_size);

                // Add the new hash and the original value to the Sub HashTable at the new index on the master table
                placeHolder.put((Key) (Integer.valueOf(tempHash)), value);

                keyPairs++;
            }
        }

    }

    // Creates Two Level Hash Table
    private TwoLevelHashC initializeHashTable(int hashedKey) {
        // New Empty Hash Table
        TwoLevelHashC<Key, Value> s_i = new TwoLevelHashC<Key, Value>(initial_size);

        // Add HashTable to the index of masterHashTable
        masterHashTable.put((Key) (Integer.valueOf(hashedKey)), (Value) s_i);

        // Returns the hash table at that index
        TwoLevelHashC<Key, Value> temp = (TwoLevelHashC<Key, Value>) masterHashTable.get(hashedKey);

        return temp;
    }

    // Extra Credit
    private void resizeMasterHT() {
        int newSize = m * 2;
        Hashtable<Key, Value> tempHT = new Hashtable<>(newSize);
        for (Key key : masterHashTable.keySet()) {
            Value v = (Value) masterHashTable.get(key);
            tempHT.put(key, v);
        }

        masterHashTable = tempHT;

        this.m = newSize;
    }

    private TwoLevelHashC<Key, Value> resizeSubHashTable(TwoLevelHashC<Key, Value> subIn) {
        // Restart to create a new a and b for the subHashTable doubling its size
        TwoLevelHashC<Key, Value> tempTwoLevel = new TwoLevelHashC<>(subIn.getM() * 2);

        // Get the hash table, will return sub hash table if master is null
        Hashtable<Key, Value> tempHashTable = tempTwoLevel.getHashTable();

        // Get the keys, rehash them with new a and b, add them to the subHashTable of the new subIn
        for (Key key : subIn.getHashTable().keySet()) {
            Value v = (Value) subIn.get(key);
            if (v != null)
                tempHashTable.put((Key) (hash(key, tempTwoLevel.a, tempTwoLevel.b, tempTwoLevel.m)), v);
        }

        return tempTwoLevel;
    }

    public int getM() {
        return m;
    }

    public Hashtable<Key, Value> getHashTable() {
        return (this.masterHashTable != null) ? this.masterHashTable : this.subHashTable;
    }

    public boolean isEmpty() {
        return (keyPairs == 0);
    }

    public int size() {
        return m;
    }


    public Value get(Key key) {
        Value locatedValue = null;
        Integer hashedKey;

        if (key == null) {
            if (key == null) throw new IllegalArgumentException("key is null");
            return null;
        } else {
            hashedKey = (hash(key, this.a, this.b, this.m));
        }

        // In Master Hash Table-------------------------------------------------
        if (this.masterHashTable != null) {
            for (Key c_key : this.masterHashTable.keySet()) {
                if (c_key.equals(hashedKey)) {
                    locatedValue = this.masterHashTable.get((Key) hashedKey);
                }
            }
        }
        // Check Sub Hash Tables at every index of master-----------------------
        if (locatedValue == null && this.masterHashTable != null) {
            TwoLevelHashC<Key, Value> currentSub;

            for (Key indexOfMaster : this.masterHashTable.keySet()) {
                currentSub = (TwoLevelHashC<Key, Value>) this.masterHashTable.get(indexOfMaster);

                // Rehash the key with the a, b and m of the current sub
                Integer subHashedKey = hash(key, currentSub.a, currentSub.b, currentSub.m);

                // Iterate through the current Index of the master--------------
                for (Key indexOfSub : currentSub.subHashTable.keySet()) {
                    if (indexOfSub.equals(subHashedKey)) {
                        // Key will be hashed by instance on get
                        locatedValue = currentSub.get(key);
                        break;
                    }
                }
                if (locatedValue != null)
                    break;
            }
        }

        // If in subHashTable
        else if (subHashTable != null) {
            for (Key subIndex : this.subHashTable.keySet()) {
                if (subIndex.equals(hashedKey)) {
                    locatedValue = this.subHashTable.get(hashedKey);
                    break;
                }
            }
        }

        return locatedValue;
    }

    public boolean contains(Key key) {
        if (key == null) {
            if (key == null) throw new IllegalArgumentException("key is null");
        }
        Value value = this.get(key);
        boolean contained = (value != null) ? true : false;
        return contained;
    }

    public int collisions() {
        int collisions = 0;
        TwoLevelHashC<Key, Value> temp;

        for (Value v : masterHashTable.values()) {
            temp = (TwoLevelHashC<Key, Value>) v;
            collisions += temp.getCollisions();
        }

        return collisions;
    }

    public int getKeyPairs() {
        return this.keyPairs;
    }

    public int getCollisions() {
        return this.collisions;
    }


    // Run the program-----------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        TwoLevelHashC b = new TwoLevelHashC("fiveHundredKeys.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command = "";
        pickMethod pm = new pickMethod(b);
        while (true) {
            System.out.println("\nWhat would you like to do next? 1-7");
            command = br.readLine();
            pm.doCommand(command);

        }
    }
}

class pickMethod {
    private static TwoLevelHashC two;

    public pickMethod(TwoLevelHashC twoLev) {
        this.two = twoLev;
    }

    public static void doCommand(String command) throws Exception {
        switch (Integer.valueOf(command)) {
            case 1:
                System.out.println("Empty: " + two.isEmpty());
                break;
            case 2:
                System.out.println("Size M: " + two.getM());
                break;
            case 3:
                System.out.println("Collisions: " + two.collisions());
                break;
            case 4:
                System.out.println("Key Pairs: " + two.getKeyPairs());
                break;
            case 5:
                System.out.println("Checking if \"Not_A_Key\" is in the TwoLevelHashC...\n");
                System.out.println(two.contains("Not_A_Key"));
                break;
            case 6:
                BufferedReader b = new BufferedReader(new FileReader("fiveHundredKeys.txt"));
                String in = b.readLine();
                int passCounter = 0;
                while (in != null) {
                    boolean check = two.contains(in);
                    if (!check) {
                        System.out.println("Failed");
                    } else
                        passCounter++;
                    in = b.readLine();
                }
                System.out.println(passCounter + " passed!");
                break;
            default:
                System.out.println("Goodbye");
                System.exit(1);
        }
    }
}
