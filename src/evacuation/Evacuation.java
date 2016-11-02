import java.io.*;
import java.util.*;

public class Evacuation {
    private static FastScanner in;

    public static void main(String[] args) throws IOException {
        in = new FastScanner();

        FlowGraph graph = readGraph();
        System.out.println(maxFlow(graph, 0, graph.size() - 1));
    }

    private static int maxFlow(FlowGraph graph, int from, int to) {
        int flow = 0;
        /* your code goes here */

        // compute initial path
        List<Integer> path = getPath(graph, from, to);

        // while there is a path...
        while (path.size() > 0) {
            // calculate the min residual flow through the current path
            int minResidual = Integer.MAX_VALUE;

            for (Integer id_edge : path) {
                Edge e = graph.getEdge(id_edge);

                int residual = e.capacity - e.flow;
                if (minResidual > residual) {
                    minResidual = residual;
                }
            }

            // update the graph with the min residual flow
            for (Integer id_edge : path) {
                graph.addFlow(id_edge, minResidual);
            }

            // recalculate the path
            path = getPath(graph, from, to);
        }

        // calculate the maxflow
        for (Integer id_edge : graph.getIds(from)) {
            Edge e = graph.getEdge(id_edge);
            flow += e.flow;
        }

        return flow;
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

    static FlowGraph readGraph() throws IOException {
        int vertex_count = in.nextInt();
        int edge_count = in.nextInt();
        FlowGraph graph = new FlowGraph(vertex_count);

        for (int i = 0; i < edge_count; ++i) {
            int from = in.nextInt() - 1, to = in.nextInt() - 1, capacity = in.nextInt();
            graph.addEdge(from, to, capacity);
        }
        return graph;
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
}
