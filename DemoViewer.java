import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class DemoViewer {

    public static void main(String[] args) {
        JFrame frame = new JFrame("3D RENDERER");
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Sliders for controlling heading and pitch (horizontal & vertical rotations)
        JSlider headingSlider = new JSlider(0, 360, 135);
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 45);

        // Rendering Panel
        JPanel renderPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                // Clear background
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Render the triangles
                renderScene(g2, headingSlider.getValue(), pitchSlider.getValue());
            }
        };

        // Adding sliders and rendering pane to the GUI
        pane.add(headingSlider, BorderLayout.SOUTH);
        pane.add(pitchSlider, BorderLayout.EAST);
        pane.add(renderPanel, BorderLayout.CENTER);

        // Add listeners to repaint the panel when sliders change
        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Set up 3D triangles and render logic
    public static void renderScene(Graphics2D g2, int heading, int pitch) {
        List<Triangle> tris = createTriangles();
        double headingRad = Math.toRadians(heading);
        double pitchRad = Math.toRadians(pitch);

        // Create transformation matrices
        Matrix3D rotationMatrix = createRotationMatrix(headingRad, pitchRad);

        g2.translate(300, 300); // Center the viewport at (width/2, height/2)

        for (Triangle t : tris) {
            // Apply rotation to vertices
            Vertex v1 = rotationMatrix.transform(t.v1);
            Vertex v2 = rotationMatrix.transform(t.v2);
            Vertex v3 = rotationMatrix.transform(t.v3);

            // Project 3D points to 2D screen coordinates
            Point2D screenV1 = projectToScreen(v1);
            Point2D screenV2 = projectToScreen(v2);
            Point2D screenV3 = projectToScreen(v3);

            // Draw the triangles on screen
            Path2D path = new Path2D.Double();
            path.moveTo(screenV1.getX(), screenV1.getY());
            path.lineTo(screenV2.getX(), screenV2.getY());
            path.lineTo(screenV3.getX(), screenV3.getY());
            path.closePath();

            g2.setColor(t.color);
            g2.draw(path);
        }
    }

    public static List<Triangle> createTriangles() {
        List<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100), new Vertex(-100, -100, 100), new Vertex(-100, 100, -100), Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100), new Vertex(-100, -100, 100), new Vertex(100, -100, -100), Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100), new Vertex(100, -100, -100), new Vertex(100, 100, 100), Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100), new Vertex(100, -100, -100), new Vertex(-100, -100, 100), Color.BLUE));
        return tris;
    }

    // 3D vertex structure
    static class Vertex {
        double x, y, z;

        Vertex(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Triangle with vertices and a color
    static class Triangle {
        Vertex v1, v2, v3;
        Color color;

        Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.color = color;
        }
    }

    // Create rotation matrix for heading and pitch rotation
    static Matrix3D createRotationMatrix(double heading, double pitch) {
        double[][] matrix = new double[3][3];

        // Heading rotation matrix
        matrix[0][0] = Math.cos(heading);
        matrix[0][2] = -Math.sin(heading);
        matrix[1][1] = 1;
        matrix[2][0] = Math.sin(heading);
        matrix[2][2] = Math.cos(heading);

        // Apply pitch
        double[][] pitchMatrix = {
            {1, 0, 0},
            {0, Math.cos(pitch), Math.sin(pitch)},
            {0, -Math.sin(pitch), Math.cos(pitch)}
        };

        // Combine the heading and pitch matrices
        Matrix3D rotation = new Matrix3D(matrix);
        rotation = rotation.multiply(new Matrix3D(pitchMatrix));
        return rotation;
    }

    // Perspective projection to 2D screen coordinates
    public static Point2D projectToScreen(Vertex v) {
        double scale = 1.0; // You can adjust scaling here
        double perspective = 300; // Perspective depth scaling
        double x = (v.x * perspective) / (v.z + perspective);
        double y = (v.y * perspective) / (v.z + perspective);
        return new Point2D.Double(x * scale, -y * scale); // Flip Y-axis to render correctly
    }

    // 3D matrix operations for rotation
    static class Matrix3D {
        double[][] values;

        Matrix3D(double[][] values) {
            this.values = values;
        }

        public Matrix3D multiply(Matrix3D other) {
            double[][] result = new double[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        result[i][j] += this.values[i][k] * other.values[k][j];
                    }
                }
            }
            return new Matrix3D(result);
        }

        public Vertex transform(Vertex v) {
            double x = v.x * values[0][0] + v.y * values[1][0] + v.z * values[2][0];
            double y = v.x * values[0][1] + v.y * values[1][1] + v.z * values[2][1];
            double z = v.x * values[0][2] + v.y * values[1][2] + v.z * values[2][2];
            return new Vertex(x, y, z);
        }
    }
}
