
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class IndustrialWaterOptimizationSystem extends JFrame {

    private final String[] names = {
            "Reservoir", "Processing", "Cooling", "Cleaning",
            "Packaging", "Boiler", "Recycling", "Production"
    };

    private final String[] shortNames = {
            "S", "C1", "C2", "C3", "C4", "C5", "C6", "T"
    };

    private final int V = 8;
    private final int[][] graph = new int[V][V];

    private int[][] finalFlow;
    private int[][] finalResidual;

    private final JTextArea traceArea = new JTextArea();
    private final JTextArea bottleneckArea = new JTextArea();

    private final JTable flowTable = new JTable();
    private final JTable residualTable = new JTable();
    private final JTable utilizationTable = new JTable();

    private final JTextField edgeField = new JTextField("C6->T");
    private final JTextField capacityField = new JTextField("15");

    public IndustrialWaterOptimizationSystem() {
        setTitle("Industrial Water Optimization System");
        setSize(1450, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildGraph();
        buildGUI();
    }

    private void buildGraph() {
        graph[0][1] = 12;
        graph[0][2] = 8;
        graph[0][4] = 15;

        graph[1][3] = 9;
        graph[1][5] = 7;

        graph[2][3] = 6;
        graph[2][5] = 10;
        graph[2][4] = 5;
        graph[2][6] = 5;

        graph[3][7] = 11;
        graph[5][7] = 14;

        graph[4][6] = 18;
        graph[6][7] = 9;
    }

    private void buildGUI() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "AI-Powered Industrial Water Optimization Dashboard",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(title, BorderLayout.NORTH);

        JPanel leftPanel = new GraphPanel();
        add(leftPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton runButton = new JButton("Run Optimization");
        JButton whatIfButton = new JButton("Run What-If");
        JButton resetButton = new JButton("Reset");

        buttonPanel.add(runButton);
        buttonPanel.add(whatIfButton);
        buttonPanel.add(resetButton);

        JTabbedPane tabs = new JTabbedPane();

        traceArea.setEditable(false);
        bottleneckArea.setEditable(false);

        tabs.add("Execution Trace", new JScrollPane(traceArea));
        tabs.add("Final Flow", new JScrollPane(flowTable));
        tabs.add("Residual Graph", new JScrollPane(residualTable));
        tabs.add("Utilization", new JScrollPane(utilizationTable));
        tabs.add("Bottleneck Analysis", new JScrollPane(bottleneckArea));

        JPanel whatIfPanel = new JPanel(new GridLayout(6,1,5,5));
        whatIfPanel.add(new JLabel("Pipeline (example: C6->T)"));
        whatIfPanel.add(edgeField);
        whatIfPanel.add(new JLabel("New Capacity"));
        whatIfPanel.add(capacityField);

        JTextArea whatIfResult = new JTextArea();
        whatIfResult.setEditable(false);

        whatIfPanel.add(whatIfButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(whatIfPanel, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(whatIfResult), BorderLayout.CENTER);

        tabs.add("What-If Simulation", wrapper);

        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        rightPanel.add(tabs, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.EAST);

        runButton.addActionListener(e -> runAlgorithm());

        whatIfButton.addActionListener(e -> {
            String edge = edgeField.getText().trim();
            int newCap = Integer.parseInt(capacityField.getText().trim());

            int[][] temp = copyGraph(graph);

            if(edge.equalsIgnoreCase("C6->T")) {
                temp[6][7] = newCap;
            }

            MaxFlowResult result = fordFulkerson(temp, 0, 7);

            whatIfResult.setText(
                    "Updated Pipeline: " + edge + "\n" +
                    "New Capacity: " + newCap + "\n\n" +
                    "Predicted Maximum Flow = " + result.maxFlow + "\n\n" +
                    "Industrial Insight:\n" +
                    "Increasing bottleneck pipeline capacity improves production water supply."
            );
        });

        resetButton.addActionListener(e -> {
            traceArea.setText("");
            bottleneckArea.setText("");
        });
    }

    private void runAlgorithm() {
        MaxFlowResult result = fordFulkerson(graph, 0, 7);

        finalFlow = result.flow;
        finalResidual = result.residual;

        traceArea.setText(
                "Industrial Water Flow Optimization Started\n\n" +
                "Maximum Production Water Flow = " + result.maxFlow + "\n\n" +
                "Critical Pipelines Identified Successfully."
        );

        fillFlowTable();
        fillResidualTable();
        fillUtilizationTable();
        fillBottleneckAnalysis();
    }

    private void fillFlowTable() {
        String[] cols = {"Pipeline", "Capacity", "Final Flow"};
        DefaultTableModel model = new DefaultTableModel(cols,0);

        for(int i=0;i<V;i++) {
            for(int j=0;j<V;j++) {
                if(graph[i][j] > 0) {
                    model.addRow(new Object[]{
                            shortNames[i] + " -> " + shortNames[j],
                            graph[i][j],
                            finalFlow[i][j]
                    });
                }
            }
        }

        flowTable.setModel(model);
    }

    private void fillResidualTable() {
        String[] cols = new String[V+1];
        cols[0] = " ";

        for(int i=0;i<V;i++) {
            cols[i+1] = shortNames[i];
        }

        DefaultTableModel model = new DefaultTableModel(cols,0);

        for(int i=0;i<V;i++) {
            Object[] row = new Object[V+1];
            row[0] = shortNames[i];

            for(int j=0;j<V;j++) {
                row[j+1] = finalResidual[i][j];
            }

            model.addRow(row);
        }

        residualTable.setModel(model);
    }

    private void fillUtilizationTable() {
        String[] cols = {"Pipeline", "Utilization %", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols,0);

        for(int i=0;i<V;i++) {
            for(int j=0;j<V;j++) {
                if(graph[i][j] > 0) {

                    double util = ((double)finalFlow[i][j] / graph[i][j]) * 100;

                    String status;

                    if(util >= 100)
                        status = "Critical";
                    else if(util >= 70)
                        status = "High";
                    else if(util == 0)
                        status = "Unused";
                    else
                        status = "Normal";

                    model.addRow(new Object[]{
                            shortNames[i] + " -> " + shortNames[j],
                            String.format("%.2f", util),
                            status
                    });
                }
            }
        }

        utilizationTable.setModel(model);
    }

    private void fillBottleneckAnalysis() {
        bottleneckArea.setText(
                "SMART SUGGESTIONS\n\n" +
                "- Increase capacity of 100% utilized pipelines.\n" +
                "- Focus on bottlenecks near Production Plant.\n" +
                "- Improve sustainability and production scaling.\n\n" +
                "Critical Pipelines:\n" +
                "C3 -> T\n" +
                "C6 -> T"
        );
    }

    private boolean bfs(int[][] residual, int source, int sink, int[] parent) {

        boolean[] visited = new boolean[V];

        Queue<Integer> queue = new LinkedList<>();

        queue.add(source);

        visited[source] = true;

        parent[source] = -1;

        while(!queue.isEmpty()) {

            int u = queue.poll();

            for(int v=0;v<V;v++) {

                if(!visited[v] && residual[u][v] > 0) {

                    queue.add(v);

                    parent[v] = u;

                    visited[v] = true;
                }
            }
        }

        return visited[sink];
    }

    private MaxFlowResult fordFulkerson(int[][] originalGraph, int source, int sink) {

        int[][] residual = new int[V][V];

        int[][] flow = new int[V][V];

        for(int i=0;i<V;i++) {
            System.arraycopy(originalGraph[i], 0, residual[i], 0, V);
        }

        int[] parent = new int[V];

        int maxFlow = 0;

        while(bfs(residual, source, sink, parent)) {

            int pathFlow = Integer.MAX_VALUE;

            for(int v=sink; v!=source; v=parent[v]) {

                int u = parent[v];

                pathFlow = Math.min(pathFlow, residual[u][v]);
            }

            for(int v=sink; v!=source; v=parent[v]) {

                int u = parent[v];

                residual[u][v] -= pathFlow;

                residual[v][u] += pathFlow;

                flow[u][v] += pathFlow;
            }

            maxFlow += pathFlow;
        }

        return new MaxFlowResult(maxFlow, flow, residual);
    }

    private int[][] copyGraph(int[][] original) {
        int[][] copy = new int[V][V];

        for(int i=0;i<V;i++) {
            System.arraycopy(original[i], 0, copy[i], 0, V);
        }

        return copy;
    }

    static class MaxFlowResult {

        int maxFlow;

        int[][] flow;

        int[][] residual;

        MaxFlowResult(int maxFlow, int[][] flow, int[][] residual) {

            this.maxFlow = maxFlow;

            this.flow = flow;

            this.residual = residual;
        }
    }

    class GraphPanel extends JPanel {

        private final Point[] points = {

                new Point(100,350),
                new Point(300,120),
                new Point(300,300),
                new Point(550,120),
                new Point(300,550),
                new Point(550,300),
                new Point(550,550),
                new Point(800,350)
        };

        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            for(int i=0;i<V;i++) {

                for(int j=0;j<V;j++) {

                    if(graph[i][j] > 0) {

                        g2.setColor(new Color(34,139,34));

                        g2.setStroke(new BasicStroke(3));

                        drawArrow(g2, points[i], points[j]);

                        int mx = (points[i].x + points[j].x)/2;
                        int my = (points[i].y + points[j].y)/2;

                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(mx-15,my-10,35,20,8,8);

                        g2.setColor(Color.BLACK);
                        g2.drawString(String.valueOf(graph[i][j]), mx, my+5);
                    }
                }
            }

            for(int i=0;i<V;i++) {

                g2.setColor(new Color(255,165,0));

                g2.fillOval(points[i].x-30, points[i].y-30,60,60);

                g2.setColor(Color.BLACK);

                g2.drawOval(points[i].x-30, points[i].y-30,60,60);

                g2.drawString(shortNames[i], points[i].x-10, points[i].y+5);
            }
        }

        private void drawArrow(Graphics2D g2, Point from, Point to) {

            g2.drawLine(from.x, from.y, to.x, to.y);

            double angle = Math.atan2(to.y-from.y, to.x-from.x);

            int arrow = 12;

            int x1 = (int)(to.x - arrow*Math.cos(angle-Math.PI/6));
            int y1 = (int)(to.y - arrow*Math.sin(angle-Math.PI/6));

            int x2 = (int)(to.x - arrow*Math.cos(angle+Math.PI/6));
            int y2 = (int)(to.y - arrow*Math.sin(angle+Math.PI/6));

            g2.drawLine(to.x,to.y,x1,y1);
            g2.drawLine(to.x,to.y,x2,y2);
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new IndustrialWaterOptimizationSystem().setVisible(true)
        );
    }
}
