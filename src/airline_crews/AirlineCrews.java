import java.io.*;
import java.util.*;

public class AirlineCrews {
    private FastScanner in;
    private PrintWriter out;

    final int CAPACITY = 1;

    public static void main(String[] args) throws IOException {
        new AirlineCrews().solve();
    }

    public void solve() throws IOException {
        in = new FastScanner();
        out = new PrintWriter(new BufferedOutputStream(System.out));
        boolean[][] bipartiteGraph = readData();
        int[] matching = findMatching(bipartiteGraph);
        writeResponse(matching);
        out.close();
    }

    boolean[][] readData() throws IOException {
        int numLeft = in.nextInt();
        int numRight = in.nextInt();
        boolean[][] adjMatrix = new boolean[numLeft][numRight];
        for (int i = 0; i < numLeft; ++i)
            for (int j = 0; j < numRight; ++j)
                adjMatrix[i][j] = (in.nextInt() == 1);
        return adjMatrix;
    }

    private int[] findMatching(boolean[][] bipartiteGraph) {
        int numFlights = bipartiteGraph.length;
        int numCrews = bipartiteGraph[0].length;

        FlowGraph graph = buildGraph(bipartiteGraph);

        int[] matching = new int[numFlights];
        Arrays.fill(matching, -1);

        // indexes of source and sink vertices
        int from = 0;
        int to = 1 + numFlights + numCrews;

        List<Integer> path = getPath(graph, from, to);

        // while there is a path...
        while (path.size() > 0) {
            // modify matching array to record the recently found path
            for (int i = 1; i < path.size() - 1; i++) {
                int edge_index = path.get(i);

                // only consider the forward edges
                if (edge_index % 2 == 0) {
                    Edge e = graph.getEdge(edge_index);
                    matching[e.from - 1] = e.to - 1 - numFlights;
                }
            }

            // calculate the min residual flow through the current path
            int minResidual = Integer.MAX_VALUE;

            for (Integer id_edge : path) {
                Edge e = graph.getEdge(id_edge);

                int residual = e.capacity - e.flow;
                if (minResidual > residual) {
                    minResidual = residual;
                    // we can break the cycle once we found
                    // the first minResidual, because it's always
                    // going to be equal to 1, and it doesn't
                    // make sense to try improving it further
                    break;
                }
            }

            // update the graph with the min residual flow
            for (Integer id_edge : path) {
                graph.addFlow(id_edge, minResidual);
            }

            // recalculate the path
            path = getPath(graph, from, to);
        }

        return matching;
    }

    // implementing BFS search (see dsa03_pa03/bfs/BFS.java)
    private static List<Integer> getPath(FlowGraph graph, int from, int to) {
        // initialize the result (a backward sequence of edges to traverse from "from" to "to")
        List<Integer> result = new ArrayList<>();

        // initialize the array of visited nodes
        List<Integer> visited = new ArrayList<>();
        // initialize the map of parent nodes
        Map<Integer, Integer> parents = new HashMap<>();

        // initialize the node queue
        Queue<Integer> q = new LinkedList<>();
        q.add(from);

        boolean isPathFound = false;

        // begin traversing a graph
        while (!q.isEmpty()) {
            // get the first node in the queue
            Integer u = q.remove();

            // get the nodes adjacent to the first node in the queue
            List<Integer> u_adj = graph.getIds(u);

            // for every node v adjacent to u...
            for (Integer v : u_adj) {
                Edge v_edge = graph.getEdge(v);

                int residualFlow = v_edge.capacity - v_edge.flow;
                if ((residualFlow > 0) && !visited.contains(v_edge.to)) {
                    q.add(v_edge.to);
                    visited.add(v_edge.to);
                    parents.put(v_edge.to, v);

                    // we can break early if the path is found
                    if (v_edge.to == to) {
                        isPathFound = true;
                        break;
                    }
                }
            }

            // we can break early if the path is found
            if (isPathFound) {
                break;
            }
        }

        if (visited.contains(to)) {
            // build the path as a backward sequence of edges
            while (to != from) {
                Integer tmpStart = parents.get(to);
                result.add(tmpStart);
                Edge e = graph.getEdge(tmpStart);
                to = e.from;
            }
        }

        return result;
    }

    private FlowGraph buildGraph(boolean[][] bipartiteGraph) {
        int numFlights = bipartiteGraph.length;
        int numCrews = bipartiteGraph[0].length;

        // indexes of source and sink vertices
        int from = 0;
        int to = 1 + numFlights + numCrews;

        FlowGraph graph = new FlowGraph(1 + numFlights + numCrews + 1);

        // add edges out of source vertex
        for (int i = 1; i <= numFlights; i++) {
            graph.addEdge(from, i, CAPACITY);
        }
        // add edges to sink vertex
        for (int i = numFlights + 1; i <= numFlights + numCrews; i++) {
            graph.addEdge(i, to, CAPACITY);
        }
        // add edges defined in the adjacency matrix
        for (int i = 0; i < numFlights; ++i)
            for (int j = 0; j < numCrews; ++j)
                if (bipartiteGraph[i][j])
                    graph.addEdge(i + 1, j + 1 + numFlights, CAPACITY);

        return graph;
    }

    private int[] findMatching_Original(boolean[][] bipartiteGraph) {
        // Replace this code with an algorithm that finds the maximum
        // matching correctly in all cases.
        int numLeft = bipartiteGraph.length;
        int numRight = bipartiteGraph[0].length;

        int[] matching = new int[numLeft];
        Arrays.fill(matching, -1);
        boolean[] busyRight = new boolean[numRight];
        for (int i = 0; i < numLeft; ++i)
            for (int j = 0; j < numRight; ++j)
                if (bipartiteGraph[i][j] && matching[i] == -1 && !busyRight[j]) {
                    matching[i] = j;
                    busyRight[j] = true;
                }
        return matching;
    }

    private void writeResponse(int[] matching) {
        for (int i = 0; i < matching.length; ++i) {
            if (i > 0) {
                out.print(" ");
            }
            if (matching[i] == -1) {
                out.print("-1");
            } else {
                out.print(matching[i] + 1);
            }
        }
        out.println();
    }

    static class FastScanner {
        private BufferedReader reader;
        private StringTokenizer tokenizer;

        public FastScanner() {
            reader = new BufferedReader(new InputStreamReader(System.in));
            tokenizer = null;
        }

        public String next() throws IOException {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                tokenizer = new StringTokenizer(reader.readLine());
            }
            return tokenizer.nextToken();
        }

        public int nextInt() throws IOException {
            return Integer.parseInt(next());
        }
    }

    static class Edge {
        int from, to, capacity, flow;

        public Edge(int from, int to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    /* This class implements a bit unusual scheme to store the graph edges, in order
     * to retrieve the backward edge for a given edge quickly. */
    static class FlowGraph {
        /* List of all - forward and backward - edges */
        private List<Edge> edges;

        /* These adjacency lists store only indices of edges from the edges list */
        private List<Integer>[] graph;

        public FlowGraph(int n) {
            this.graph = (ArrayList<Integer>[]) new ArrayList[n];
            for (int i = 0; i < n; ++i)
                this.graph[i] = new ArrayList<>();
            this.edges = new ArrayList<>();
        }

        public void addEdge(int from, int to, int capacity) {
            /* Note that we first append a forward edge and then a backward edge,
             * so all forward edges are stored at even indices (starting from 0),
             * whereas backward edges are stored at odd indices. */
            Edge forwardEdge = new Edge(from, to, capacity);
            Edge backwardEdge = new Edge(to, from, 0);
            graph[from].add(edges.size());
            edges.add(forwardEdge);
            graph[to].add(edges.size());
            edges.add(backwardEdge);
        }

        public int size() {
            return graph.length;
        }

        public List<Integer> getIds(int from) {
            return graph[from];
        }

        public Edge getEdge(int id) {
            return edges.get(id);
        }

        public void addFlow(int id, int flow) {
            /* To get a backward edge for a true forward edge (i.e id is even), we should get id + 1
             * due to the described above scheme. On the other hand, when we have to get a "backward"
             * edge for a backward edge (i.e. get a forward edge for backward - id is odd), id - 1
             * should be taken.
             *
             * It turns out that id ^ 1 works for both cases. Think this through! */
            edges.get(id).flow += flow;
            edges.get(id ^ 1).flow -= flow;
        }
    }
}
