package comp3170.week3;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

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

    // Start by facing right (0 radians or 0 degrees)
    private float currentFacingAngle = 0.0f; 
    private float rotationSpeed = 0.01f;  // Control how fast the object rotates right (clockwise)

    public Scene() {
        shader = ShaderLibrary.instance.compileShader(VERTEX_SHADER, FRAGMENT_SHADER);

        // Define vertices and colors
        vertices = new Vector4f[] {
            new Vector4f(0, 0, 0, 1),    // Center of the ship
            new Vector4f(0, 1, 0, 1),    // Top of the ship
            new Vector4f(-1, -1, 0, 1),  // Left bottom corner
            new Vector4f(1, -1, 0, 1),   // Right bottom corner
        };

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
        float x = radius * (float) Math.cos(time + 0.1f);   // Current X position
        float y = radius * (float) Math.sin(time + 0.1f);   // Current Y position

        // Set the translation matrix to move the ship in a circular path
        translationMatrix(x, y, translation);

        // Apply continuous right (clockwise) rotation
        currentFacingAngle -= rotationSpeed;  // Gradually rotate right (clockwise) over time
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

    // Method to apply a translation matrix for positioning
    public static Matrix4f translationMatrix(float tx, float ty, Matrix4f dest) {
        dest.identity();
        dest.m30(tx);
        dest.m31(ty);
        return dest;
    }

    // Method to apply a rotation matrix based on angle
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

    // Method to apply scaling matrix
    public static Matrix4f scaleMatrix(float sx, float sy, Matrix4f dest) {
        dest.identity();
        dest.m00(sx);
        dest.m11(sy);
        return dest;
    }
}
