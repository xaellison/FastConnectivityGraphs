/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aellison
 */
import java.io.*;
import java.util.*;

public class FastConnectivityGraph {

    public static void main(String[] args) throws java.lang.Exception {
        //randomizedTest();
        
        Graph g = new Graph();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        do {
            input = br.readLine();
            input = input.trim();
            handleLine(input, g);
        } while (input != null);
    }

    public static void handleLine(String line, Graph g) {
        if (line == null) {
            return;
        }
        String[] words = line.split(" ");
        //return if wrong number of args
        if (words.length != 3 && words.length != 4) {
            return;
        }
        //return if empty string is either node
        if (words[words.length - 1].length() < 1 || words[words.length - 2].length() < 1) {
            return;
        }
        if (words[0].equals("add")) {
            //add
            g.addSymmetricEdge(words[1], words[2]);
        } else if (words[0].equals("is") && words[1].equals("linked")) {
            //is linked
            System.out.println(g.isConnected(words[2], words[3]));
        } else if (words[0].equals("remove")) {
            //remove
            g.removeSymmetricEdge(words[1], words[2]);
        }
        //else, invalid command.
    }

    public static void randomizedTest() {
        Random r = new Random();
        Graph g = new Graph();
        int n = 10000;
        for (int i = 0; i < n; i++) {
            System.out.println(i);
            int a = r.nextInt(n);
            int b = r.nextInt(n);
            g.addSymmetricEdge(a + "", b + "");
        }

        for (int i = 0; i < 100000; i++) {
            if(i%100==0){
                System.out.println(1+(i/100)+"%");
            }
            int a = r.nextInt(n);
            int b = r.nextInt(n);
            g.addSymmetricEdge(a + "", b + "");
            a = r.nextInt(n);
            b = r.nextInt(n);
            g.removeSymmetricEdge(a + "", b + "");

            a = r.nextInt(n);
            b = r.nextInt(n);
            int c = r.nextInt(n);
            
            boolean ab = g.isConnected(""+a, ""+b);
            boolean ba = g.isConnected(""+b, ""+a);
            boolean bc = g.isConnected(""+c, ""+b);
            boolean cb = g.isConnected(""+b, ""+c);
            boolean ac = g.isConnected(""+a, ""+c);
            boolean ca = g.isConnected(""+c, ""+a);
            
            //symmetry:
            if(ab!=ba || bc!=cb || ac!=ca){
                System.out.println("symmetry broken");
            }
            //transitivity
            if((ab && bc && !ac) || (ab && ac && !bc) || (ac && bc && !ab)){
                System.out.println("transitivity broken");
            }
        }

    }

}

/*
    This is a graph implementation designed to support checking edge existence
    in constant time. 

    It does this by keeping a hashset of pairs of edges that are 'linked' ie 
    they're in the same component of the graph. Pairs are symmetric, so (a,b) =
    (b,a). Pairs like (x,x) aren't stored because they are always linked.
    
    If l >= n-1 then there can be a spanning tree of all nodes. In this worst case 
    scenario there are n(n-1) linked pairs.

    If l<n we can again say the number of linked pairs is maximized if we 
    distribute the edges into trees (no "wasted" or "duplicate" edges in a 
    component). Sum(|Ci|^2) is maximized when it's l+1 linked nodes (a tree) and
    n-(l+1) isolated nodes. Then we can say there are (l+1)*l linked pairs to store
    in the worst case scenario (of scenarios where l<n).
    
    If we have a set of components {C1, C2, ..., Cn} (Ci is ith components) then
    this takes up space O(Sum(|Ci|^2)) because each component
    can be thought of as a clique if we consider 'linked' to be the definition
    of an edge. Sum(|Ci|^2) = n^2*Sum(Ai^2) where Ai*n=|Ci|. Sum(Ai) = 1 and   
    0<= Ai <=1 for all Ai. Thus, Sum(|Ci|^2) = n^2*Sum(Ai^2) < n^2 so this has 
    space O(n^2). Sum(Ai^2) can be 1, meaning the map may need to store n(n-1) 
    pairs. 

    Space is also taken up by the map of values to nodes, this is O(n).

    There are also the pointers between nodes, precisely 2L. 

    In conclusion, the space complexity is O(n^2).
 */
class Graph {

    //this is the map of values to node objects containing those values
    private HashMap<String, Node> nodes = new HashMap<String, Node>();
    //this is the set of pairs of nodes that are linked 
    private HashSet<NodePair> pairs = new HashSet<NodePair>();

    private void add(String value) {
        nodes.put(value, new Node(value));
    }

    /*
        the runtime of this is O(1) if adding an edge between nodes of the same
        component.
    
        if however we are joining two components...
        it takes O(n+L) time to find the two components.
        it takes O(n^2) time to add all connected pairs from the two components.
        Thus the worst case time of this is O(n^2+L) which is O(n^2) since there
        can only be 2*n(n-1) links in a complete digraph.
    
     */
    public void addSymmetricEdge(String value1, String value2) {
        //add the nodes to set of nodes if they don't already exist
        if (!nodes.containsKey(value1)) {
            add(value1);
        }
        if (!nodes.containsKey(value2)) {
            add(value2);
        }
        Node node1 = nodes.get(value1);
        Node node2 = nodes.get(value2);
        /* the easiest scenario is we add an edge between two nodes of the same
         component. In that case, there is nothing new in terms of what is
         linked.
        
         The other scenario is we join two components, in which case we need
         to add many new pairs (cartesian product of the nodes of each comp.)
         */

        boolean connected = node1.connectedTo(value2);
        HashSet<Node> component1 = null, component2 = null;
        //store component info before joining them
        if (!connected) {
            component1 = node1.component();
            component2 = node2.component();
        }
        //add the directional edges
        node1.addSuccessor(node2);
        node2.addSuccessor(node1);

        if (!connected) {
            for (Node n1 : component1) {
                for (Node n2 : component2) {
                    pairs.add(new NodePair(n1, n2));
                }
            }
        }
    }

    public void removeSymmetricEdge(String value1, String value2) {
        //if either node isn't in the graph, there is no edge to remove
        if (!nodes.containsKey(value1) || !nodes.containsKey(value2)) {
            return;
        }

        Node node1 = nodes.get(value1);
        Node node2 = nodes.get(value2);

        //delete the directed edges
        node1.removeSuccessor(node2);
        node2.removeSuccessor(node1);

        //if they are now in separate components, remove the 
        if (!node1.connectedTo(value2)) {
            //need to remove cartesian product of the nodes of the two 
            //separated components.
            HashSet<Node> component1 = node1.component();
            HashSet<Node> component2 = node2.component();
            for (Node n1 : component1) {
                for (Node n2 : component2) {
                    //n1 should never equal n2, including check for debugging
                    if (n1.equals(n2)) {
                        System.out.println("this is bad.");
                    }
                    pairs.remove(new NodePair(n1, n2));
                }
            }
        }
    }

    public boolean isConnected(String value1, String value2) {
        if (value1.equals(value2)) {
            return true;
        }
        return pairs.contains(new NodePair(new Node(value1), new Node(value2)));
    }

}

class NodePair {

    final private Node node1, node2;

    public NodePair(Node n1, Node n2) {
        node1 = n1;
        node2 = n2;
    }

    //this must only depend on the values of the nodes in order for the various
    //hashset methods to work properly in this context.
    @Override
    public int hashCode() {
        return node1.hashCode() + node2.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        NodePair other = (NodePair) o;
        String mine1 = node1.getValue();
        String mine2 = node2.getValue();
        String hers1 = other.node1.getValue();
        String hers2 = other.node2.getValue();
        return mine1.equals(hers1) && mine2.equals(hers2)
                || mine2.equals(hers1) && mine1.equals(hers2);
    }

    @Override
    public String toString() {
        return node1.getValue() + " " + node2.getValue();
    }

}

/*
 this is a node class for a node in a directed graph. Although the challenge
 said the graph node ids would all be positive integers, it was said they would
 be quite large... I wasn't sure if that included numbers bigger than ~2^64 so
 rather than use a long I used a string. This has the benefit of being more
 generalizable, although hashing is slower.
 */
class Node {

    final private String value;
    final private LinkedList<Node> successors = new LinkedList<Node>();

    public Node(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    //prepending to linkedlist is O(1) time
    protected void addSuccessor(Node n) {
        successors.addFirst(n);
    }

    //if N is the length of the list, time is O(N). In a graph with n edges, and
    //L links this is O(min(n,L)) which is O(n)
    public void removeSuccessor(Node removee) {
        successors.remove(removee);
    }

    /*this routine is useful when the graph needs to remove a pair because
     when an edge is deleted, we can check if the pair is still connected.
     If not, then we delete the cartesian product of the nodes in each 
     component from the set of connected pairs in Graph
     */
    public boolean connectedTo(String target) {
        return connectedTo(target, new HashSet<String>());
    }

    private boolean connectedTo(String target, HashSet<String> markedNodes) {
        //if the target = value of this node, return true.
        if (target.equals(value)) {
            return true;
        }
        //mark self as visited
        markedNodes.add(value);
        //depth first search
        for (Node n : successors) {
            //if unmarked, continue recursion
            if (!markedNodes.contains(n.getValue())) {
                boolean result = n.connectedTo(target, markedNodes);
                if (result) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashSet<Node> component() {
        HashSet<Node> nodes = new HashSet<Node>();
        findComponent(nodes);
        return nodes;
    }

    //this is just depth first search and adding each node to a hashset (which 
    //has constant time insertion) so this is O(n+L) for n nodes and L links
    private void findComponent(HashSet<Node> markedNodes) {
        //mark self as visited
        markedNodes.add(this);
        //depth first search
        for (Node n : successors) {
            //if unmarked, continue recursion
            if (!markedNodes.contains(n)) {
                n.findComponent(markedNodes);
            }
        }
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        Node other = (Node) o;
        if (other.value == null || value == null) {
            return false;
        }
        return other.value.equals(value);
    }
}
