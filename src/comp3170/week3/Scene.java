package comp3170.week3;

import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import comp3170.GLBuffers;
import comp3170.Shader;
import comp3170.ShaderLibrary;

public class Scene {

    final private String VERTEX_SHADER = "vertex.glsl";
    final private String FRAGMENT_SHADER = "fragment.glsl";

    private Vector4f[] vertices;
    private int vertexBuffer;
    private int[] indices;
    private int indexBuffer;
    private Vector3f[] colours;
    private int colourBuffer;

    private Shader shader;

    private float currentFacingAngle = 0.0f; // Store the current facing angle of the ship

    // Speed at which the ship rotates (adjust this to change how quickly it turns)
    private float rotationSpeed = 0.05f; // Smaller value means slower turning

    // Track movement direction (0 = up, 1 = right, 2 = down, 3 = left)
    private int currentDirection = 1; // Start facing right by default

    public Scene() {
        shader = ShaderLibrary.instance.compileShader(VERTEX_SHADER, FRAGMENT_SHADER);

        // Define vertices and colors
        vertices = new Vector4f[] {
            new Vector4f(0, 0, 0, 1), // Center of the ship
            new Vector4f(0, 1, 0, 1), // Top of the ship
            new Vector4f(-1, -1, 0, 1), // Left bottom corner
            new Vector4f(1, -1, 0, 1), // Right bottom corner
        };

        // Apply a 270-degree rotation to the vertices so the top faces right
        rotateVerticesToFaceRight(vertices);

        vertexBuffer = GLBuffers.createBuffer(vertices);

        colours = new Vector3f[] {
            new Vector3f(1, 0, 1),    // MAGENTA
            new Vector3f(1, 0, 1),    // MAGENTA
            new Vector3f(1, 0, 0),    // RED
            new Vector3f(0, 0, 1),    // BLUE
        };
        colourBuffer = GLBuffers.createBuffer(colours);

        indices = new int[] {
            0, 1, 2, // Left triangle
            0, 1, 3, // Right triangle
        };
        indexBuffer = GLBuffers.createIndexBuffer(indices);
    }

    public void draw(float time, float radius, float scale) {
        shader.enable();

        // Set the attributes
        shader.setAttribute("a_position", vertexBuffer);
        shader.setAttribute("a_colour", colourBuffer);

        // Create the model matrix
        Matrix4f modelMatrix = new Matrix4f();
        Matrix4f translation = new Matrix4f();
        Matrix4f rotation = new Matrix4f();
        Matrix4f scaling = new Matrix4f();

        // Calculate the current position based on time and radius
        float x = radius * (float) Math.cos(time);   // Current X position
        float y = radius * (float) Math.sin(time);   // Current Y position

        // Determine movement direction (up, down, left, right)
        float dx = radius * (float) Math.cos(time + 0.01f) - x;   // Small step in X direction
        float dy = radius * (float) Math.sin(time + 0.01f) - y;   // Small step in Y direction

        // Calculate the direction based on dx, dy (e.g., left, right, up, down)
        if (Math.abs(dx) > Math.abs(dy)) {
            // Moving horizontally
            if (dx > 0) {
                currentDirection = 1; // Facing right
                currentFacingAngle = 0; // No rotation needed for right
            } else {
                currentDirection = 3; // Facing left
                currentFacingAngle = (float) Math.PI; // Rotate 180 degrees for left
            }
        } else {
            // Moving vertically
            if (dy > 0) {
                currentDirection = 0; // Facing up
                currentFacingAngle = (float) (Math.PI / 2); // Rotate 90 degrees for up
            } else {
                currentDirection = 2; // Facing down
                currentFacingAngle = (float) (-Math.PI / 2); // Rotate -90 degrees for down
            }
        }

        // Set the translation matrix to move the ship in a circular path
        translationMatrix(x, y, translation);

        // Rotate the ship to face the correct direction based on the current facing angle
        rotationMatrix(currentFacingAngle, rotation);

        // Apply scaling
        scaleMatrix(scale, scale, scaling);

        // Combine the transformations: T * R * S
        modelMatrix.mul(translation).mul(rotation).mul(scaling);

        // Pass the model matrix to the shader
        shader.setUniform("u_model", modelMatrix);

        // Draw the object using the index buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
    }

    public static Matrix4f translationMatrix(float tx, float ty, Matrix4f dest) {
        dest.identity();
        dest.m30(tx);
        dest.m31(ty);
        return dest;
    }

    public static Matrix4f rotationMatrix(float angle, Matrix4f dest) {
        dest.identity();

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        dest.m00(cos);
        dest.m01(-sin);
        dest.m10(sin);
        dest.m11(cos);

        return dest;
    }

    public static Matrix4f scaleMatrix(float sx, float sy, Matrix4f dest) {
        dest.identity();
        dest.m00(sx);
        dest.m11(sy);
        return dest;
    }

    // Method to apply 270-degree rotation to ship vertices so the top faces right, it originally faced the other direction
    private void rotateVerticesToFaceRight(Vector4f[] vertices) {
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.identity();

        // 270-degree rotation matrix (Cos(270) = 0, Sin(270) = -1)
        float cos = (float) Math.cos(3 * Math.PI / 2); // Cos(270 degrees) = 0
        float sin = (float) Math.sin(3 * Math.PI / 2); // Sin(270 degrees) = -1

        rotationMatrix.m00(cos);  // x' = x*cos - y*sin
        rotationMatrix.m01(-sin);
        rotationMatrix.m10(sin);
        rotationMatrix.m11(cos);  // y' = x*sin + y*cos

        // Apply the 270-degree rotation matrix to each vertex
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = rotationMatrix.transform(vertices[i]);
        }
    }
}
