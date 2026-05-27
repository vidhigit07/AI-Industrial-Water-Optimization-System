import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class IndustrialWaterOptimizationSystemAI_Highlighted extends JFrame {

    private final String[] names = {"Reservoir", "Processing", "Cooling", "Cleaning", "Packaging", "Boiler", "Recycling", "Production"};
    private final String[] shortNames = {"S", "C1", "C2", "C3", "C4", "C5", "C6", "T"};
    private final int V = 8;
    private final int[][] graph = new int[V][V];

    private int[][] finalFlow;
    private int[][] finalResidual;
    private final List<PathStep> steps = new ArrayList<>();
    private int currentStep = -1;
    private int totalShownFlow = 0;

    private final JTextArea traceArea = new JTextArea();
    private final JTextArea bottleneckArea = new JTextArea();
    private final JTable flowTable = new JTable();
    private final JTable residualTable = new JTable();
    private final JTable utilizationTable = new JTable();

    private final JTextField edgeField = new JTextField("C6->T");
    private final JTextField capacityField = new JTextField("15");
    private final JTextField productionLoadField = new JTextField("90");
    private final JTextField temperatureField = new JTextField("35");
    private final JTextField cleaningCycleField = new JTextField("1");
    private final JTextField shiftHoursField = new JTextField("12");
    private final JTextArea aiResultArea = new JTextArea();

    private final GraphPanel graphPanel = new GraphPanel();
    private final JLabel currentPathLabel = new JLabel("Current Augmenting Path: None");
    private JButton nextStepButton;

    public IndustrialWaterOptimizationSystemAI_Highlighted() {
        setTitle("AI-Powered Industrial Water Optimization System");
        setSize(1600, 900);
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
        JLabel title = new JLabel("AI-Powered Industrial Water Optimization Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        JPanel leftWrapper = new JPanel(new BorderLayout());
        currentPathLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentPathLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        leftWrapper.add(currentPathLabel, BorderLayout.NORTH);
        leftWrapper.add(graphPanel, BorderLayout.CENTER);
        add(leftWrapper, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(530, 900));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        JButton runButton = new JButton("Run Optimization");
        nextStepButton = new JButton("Next Step");
        JButton whatIfButton = new JButton("Run What-If");
        JButton resetButton = new JButton("Reset");
        nextStepButton.setEnabled(false);
        buttonPanel.add(runButton);
        buttonPanel.add(nextStepButton);
        buttonPanel.add(whatIfButton);
        buttonPanel.add(resetButton);

        JTabbedPane tabs = new JTabbedPane();
        traceArea.setEditable(false);
        bottleneckArea.setEditable(false);
        aiResultArea.setEditable(false);
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        bottleneckArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        tabs.add("Execution Trace", new JScrollPane(traceArea));
        tabs.add("Final Flow", new JScrollPane(flowTable));
        tabs.add("Residual Graph", new JScrollPane(residualTable));
        tabs.add("Utilization", new JScrollPane(utilizationTable));
        tabs.add("Bottleneck Analysis", new JScrollPane(bottleneckArea));
        tabs.add("What-If Simulation", buildWhatIfTab(whatIfButton));
        tabs.add("AI Demand Prediction", buildAITab());

        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        rightPanel.add(tabs, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        runButton.addActionListener(e -> runAlgorithm());
        nextStepButton.addActionListener(e -> showNextStep());
        resetButton.addActionListener(e -> resetApp());
        whatIfButton.addActionListener(e -> runWhatIf(whatIfButton));
    }

    private JPanel buildWhatIfTab(JButton whatIfButton) {
        JPanel input = new JPanel(new GridLayout(6,1,5,5));
        input.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        input.add(new JLabel("Pipeline (examples: C6->T, C3->T, C5->T)"));
        input.add(edgeField);
        input.add(new JLabel("New Capacity"));
        input.add(capacityField);
        input.add(whatIfButton);
        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setFont(new Font("Monospaced", Font.PLAIN, 13));
        whatIfButton.putClientProperty("resultArea", result);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(input, BorderLayout.NORTH);
        panel.add(new JScrollPane(result), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAITab() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        JPanel input = new JPanel(new GridLayout(9,1,6,6));
        input.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        input.add(new JLabel("Production Load (%)"));
        input.add(productionLoadField);
        input.add(new JLabel("Temperature (C)"));
        input.add(temperatureField);
        input.add(new JLabel("Cleaning Cycle Active? (1 = Yes, 0 = No)"));
        input.add(cleaningCycleField);
        input.add(new JLabel("Shift Hours"));
        input.add(shiftHoursField);
        JButton predictButton = new JButton("Predict Water Demand using ML");
        input.add(predictButton);
        aiResultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(input, BorderLayout.NORTH);
        panel.add(new JScrollPane(aiResultArea), BorderLayout.CENTER);
        predictButton.addActionListener(e -> runAIPrediction());
        return panel;
    }

    private void runAlgorithm() {
        steps.clear();
        currentStep = -1;
        totalShownFlow = 0;
        graphPanel.setHighlightedEdges(Collections.emptyList());
        graphPanel.repaint();
        currentPathLabel.setText("Current Augmenting Path: None");

        MaxFlowResult result = fordFulkersonWithSteps(graph, 0, 7);
        finalFlow = result.flow;
        finalResidual = result.residual;

        traceArea.setText("Industrial Water Flow Optimization Started\n\n" +
                "Maximum Production Water Flow = " + result.maxFlow + "\n\n" +
                "Augmenting Paths Found: " + steps.size() + "\n\n" +
                "Click 'Next Step' to highlight each augmenting path on the graph.\n\n");

        fillFlowTable();
        fillResidualTable();
        fillUtilizationTable();
        fillBottleneckAnalysis();
        nextStepButton.setEnabled(true);
    }

    private void showNextStep() {
        if(steps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First click Run Optimization.");
            return;
        }
        currentStep++;
        if(currentStep >= steps.size()) {
            JOptionPane.showMessageDialog(this, "All augmenting paths shown. Final Maximum Flow = " + totalShownFlow);
            currentStep = steps.size() - 1;
            return;
        }
        PathStep step = steps.get(currentStep);
        totalShownFlow += step.pathFlow;
        graphPanel.setHighlightedEdges(step.edgesInPath);
        graphPanel.repaint();
        currentPathLabel.setText("Current Augmenting Path: " + step.pathText + " | Flow Added = " + step.pathFlow);
        traceArea.append("Step " + (currentStep + 1) + "\n");
        traceArea.append("Path: " + step.pathText + "\n");
        traceArea.append("Bottleneck Capacity: " + step.pathFlow + "\n");
        traceArea.append("Total Flow After Step: " + totalShownFlow + "\n\n");
        if(currentStep == steps.size() - 1) traceArea.append("Final Maximum Flow = " + totalShownFlow + "\n");
    }

    private void resetApp() {
        traceArea.setText("");
        bottleneckArea.setText("");
        aiResultArea.setText("");
        flowTable.setModel(new DefaultTableModel());
        residualTable.setModel(new DefaultTableModel());
        utilizationTable.setModel(new DefaultTableModel());
        finalFlow = null;
        finalResidual = null;
        steps.clear();
        currentStep = -1;
        totalShownFlow = 0;
        nextStepButton.setEnabled(false);
        graphPanel.setHighlightedEdges(Collections.emptyList());
        graphPanel.repaint();
        currentPathLabel.setText("Current Augmenting Path: None");
    }

    private void runWhatIf(JButton whatIfButton) {
        JTextArea whatIfResult = (JTextArea) whatIfButton.getClientProperty("resultArea");
        try {
            String edge = edgeField.getText().trim();
            int newCap = Integer.parseInt(capacityField.getText().trim());
            int[][] temp = copyGraph(graph);
            if(edge.equalsIgnoreCase("C6->T")) temp[6][7] = newCap;
            else if(edge.equalsIgnoreCase("C3->T")) temp[3][7] = newCap;
            else if(edge.equalsIgnoreCase("C5->T")) temp[5][7] = newCap;
            else {
                whatIfResult.setText("Currently supported examples: C6->T, C3->T, C5->T");
                return;
            }
            MaxFlowResult result = fordFulkersonForWhatIf(temp, 0, 7);
            whatIfResult.setText("Updated Pipeline: " + edge + "\nNew Capacity: " + newCap +
                    "\n\nPredicted Maximum Flow = " + result.maxFlow +
                    "\n\nIndustrial Insight:\nIncreasing bottleneck pipeline capacity can improve production water supply.");
        } catch(Exception ex) {
            whatIfResult.setText("Error: Please enter a valid pipeline and numeric capacity.");
        }
    }

    private void runAIPrediction() {
        try {
            String result = runPythonPrediction(productionLoadField.getText().trim(), temperatureField.getText().trim(), cleaningCycleField.getText().trim(), shiftHoursField.getText().trim());
            aiResultArea.setText(result);
        } catch(Exception ex) {
            aiResultArea.setText("AI Prediction Error:\n" + ex.getMessage() +
                    "\n\nMake sure ml-model folder, predict_for_java.py, water_demand_model.pkl, and Python libraries exist.");
        }
    }

    private String runPythonPrediction(String productionLoad, String temperature, String cleaningCycle, String shiftHours) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("py");
        command.add("ml-model/predict_for_java.py");
        command.add(productionLoad);
        command.add(temperature);
        command.add(cleaningCycle);
        command.add(shiftHours);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null) output.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        if(exitCode != 0) throw new RuntimeException(output.toString());
        return output.toString();
    }

    private void fillFlowTable() {
        String[] cols = {"Pipeline", "Capacity", "Final Flow"};
        DefaultTableModel model = new DefaultTableModel(cols,0);
        for(int i=0;i<V;i++) for(int j=0;j<V;j++) if(graph[i][j] > 0) model.addRow(new Object[]{shortNames[i] + " -> " + shortNames[j], graph[i][j], finalFlow[i][j]});
        flowTable.setModel(model);
    }

    private void fillResidualTable() {
        String[] cols = new String[V+1];
        cols[0] = " ";
        for(int i=0;i<V;i++) cols[i+1] = shortNames[i];
        DefaultTableModel model = new DefaultTableModel(cols,0);
        for(int i=0;i<V;i++) {
            Object[] row = new Object[V+1];
            row[0] = shortNames[i];
            for(int j=0;j<V;j++) row[j+1] = finalResidual[i][j];
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
                    String status = util >= 100 ? "Critical" : util >= 70 ? "High" : util == 0 ? "Unused" : "Normal";
                    model.addRow(new Object[]{shortNames[i] + " -> " + shortNames[j], String.format("%.2f", util), status});
                }
            }
        }
        utilizationTable.setModel(model);
    }

    private void fillBottleneckAnalysis() {
        bottleneckArea.setText("SMART SUGGESTIONS\n\n" +
                "- Increase capacity of 100% utilized pipelines.\n" +
                "- Focus on bottlenecks near Production Plant.\n" +
                "- Improve sustainability and production scaling.\n\n" +
                "Critical Pipelines:\nC3 -> T\nC6 -> T\n\n" +
                "AI Extension:\nUse demand prediction to check whether future production requirements exceed current optimized flow.");
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

    private MaxFlowResult fordFulkersonWithSteps(int[][] originalGraph, int source, int sink) {
        int[][] residual = new int[V][V];
        int[][] flow = new int[V][V];
        for(int i=0;i<V;i++) System.arraycopy(originalGraph[i], 0, residual[i], 0, V);
        int[] parent = new int[V];
        int maxFlow = 0;
        while(bfs(residual, source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            List<Integer> nodePath = new ArrayList<>();
            for(int v=sink; v!=source; v=parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residual[u][v]);
            }
            for(int v=sink; v!=-1; v=parent[v]) nodePath.add(v);
            Collections.reverse(nodePath);
            List<Edge> edgesInPath = new ArrayList<>();
            for(int v=sink; v!=source; v=parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
                flow[u][v] += pathFlow;
                edgesInPath.add(new Edge(u, v));
            }
            Collections.reverse(edgesInPath);
            maxFlow += pathFlow;
            steps.add(new PathStep(convertPathToText(nodePath), pathFlow, edgesInPath));
        }
        return new MaxFlowResult(maxFlow, flow, residual);
    }

    private MaxFlowResult fordFulkersonForWhatIf(int[][] originalGraph, int source, int sink) {
        int[][] residual = new int[V][V];
        int[][] flow = new int[V][V];
        for(int i=0;i<V;i++) System.arraycopy(originalGraph[i], 0, residual[i], 0, V);
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

    private String convertPathToText(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<path.size();i++) {
            sb.append(shortNames[path.get(i)]);
            if(i != path.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }

    private int[][] copyGraph(int[][] original) {
        int[][] copy = new int[V][V];
        for(int i=0;i<V;i++) System.arraycopy(original[i], 0, copy[i], 0, V);
        return copy;
    }

    static class Edge {
        int from, to;
        Edge(int from, int to) { this.from = from; this.to = to; }
    }

    static class PathStep {
        String pathText;
        int pathFlow;
        List<Edge> edgesInPath;
        PathStep(String pathText, int pathFlow, List<Edge> edgesInPath) {
            this.pathText = pathText;
            this.pathFlow = pathFlow;
            this.edgesInPath = edgesInPath;
        }
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
        private List<Edge> highlightedEdges = new ArrayList<>();
        private final Point[] points = {
                new Point(120, 300), new Point(320, 140), new Point(320, 300), new Point(580, 140),
                new Point(320, 460), new Point(580, 300), new Point(580, 460), new Point(850, 300)
        };

        void setHighlightedEdges(List<Edge> highlightedEdges) { this.highlightedEdges = highlightedEdges; }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawLegend(g2);
            for(int i=0;i<V;i++) {
                for(int j=0;j<V;j++) {
                    if(graph[i][j] > 0) {
                        boolean isHighlighted = isEdgeHighlighted(i, j);
                        if(isHighlighted) {
                            g2.setColor(Color.RED);
                            g2.setStroke(new BasicStroke(5));
                        } else {
                            g2.setColor(new Color(34,139,34));
                            g2.setStroke(new BasicStroke(3));
                        }
                        drawArrow(g2, points[i], points[j]);
                        int mx = (points[i].x + points[j].x)/2;
                        int my = (points[i].y + points[j].y)/2;
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(mx-18,my-12,42,24,8,8);
                        g2.setColor(Color.BLACK);
                        g2.drawRoundRect(mx-18,my-12,42,24,8,8);
                        g2.drawString(String.valueOf(graph[i][j]), mx-5, my+5);
                    }
                }
            }
            for(int i=0;i<V;i++) {
                if(i == 0) g2.setColor(new Color(65, 105, 225));
                else if(i == 7) g2.setColor(new Color(220, 80, 80));
                else g2.setColor(new Color(255,165,0));
                g2.fillOval(points[i].x-30, points[i].y-30,60,60);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(points[i].x-30, points[i].y-30,60,60);
                g2.setFont(new Font("Arial", Font.BOLD, 15));
                g2.drawString(shortNames[i], points[i].x-10, points[i].y+5);
                g2.setFont(new Font("Arial", Font.PLAIN, 11));
                g2.drawString(names[i], points[i].x-35, points[i].y+47);
            }
        }

        private boolean isEdgeHighlighted(int from, int to) {
            for(Edge e : highlightedEdges) if(e.from == from && e.to == to) return true;
            return false;
        }

        private void drawLegend(Graphics2D g2) {
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(Color.BLACK);
            g2.drawString("Legend:", 20, 30);
            g2.setColor(new Color(34,139,34));
            g2.drawString("Green = Normal pipeline", 20, 50);
            g2.setColor(Color.RED);
            g2.drawString("Red = Current augmenting path", 20, 70);
        }

        private void drawArrow(Graphics2D g2, Point from, Point to) {
            double angle = Math.atan2(to.y-from.y, to.x-from.x);
            int nodeRadius = 34;
            int startX = (int)(from.x + nodeRadius * Math.cos(angle));
            int startY = (int)(from.y + nodeRadius * Math.sin(angle));
            int endX = (int)(to.x - nodeRadius * Math.cos(angle));
            int endY = (int)(to.y - nodeRadius * Math.sin(angle));
            g2.drawLine(startX, startY, endX, endY);
            int arrow = 14;
            int x1 = (int)(endX - arrow*Math.cos(angle-Math.PI/6));
            int y1 = (int)(endY - arrow*Math.sin(angle-Math.PI/6));
            int x2 = (int)(endX - arrow*Math.cos(angle+Math.PI/6));
            int y2 = (int)(endY - arrow*Math.sin(angle+Math.PI/6));
            g2.drawLine(endX,endY,x1,y1);
            g2.drawLine(endX,endY,x2,y2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IndustrialWaterOptimizationSystemAI_Highlighted().setVisible(true));
    }
}
