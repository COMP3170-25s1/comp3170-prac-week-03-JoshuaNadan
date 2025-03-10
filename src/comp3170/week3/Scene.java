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

    public Scene() {
        shader = ShaderLibrary.instance.compileShader(VERTEX_SHADER, FRAGMENT_SHADER);

        // Define vertices and colors
        vertices = new Vector4f[]{
            new Vector4f(0, 0, 0, 1),
            new Vector4f(0, 1, 0, 1),
            new Vector4f(-1, -1, 0, 1),
            new Vector4f(1, -1, 0, 1),
        };
        vertexBuffer = GLBuffers.createBuffer(vertices);

        colours = new Vector3f[]{
            new Vector3f(1, 0, 1),    // MAGENTA
            new Vector3f(1, 0, 1),    // MAGENTA
            new Vector3f(1, 0, 0),    // RED
            new Vector3f(0, 0, 1),    // BLUE
        };
        colourBuffer = GLBuffers.createBuffer(colours);

        indices = new int[]{
            0, 1, 2, // left triangle
            0, 1, 3, // right triangle
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

        // Calculate the current position along the circle using trigonometry
        float x = radius * (float) Math.cos(time);   // Current X position
        float y = radius * (float) Math.sin(time);   // Current Y position

        // Calculate the next position to determine the direction of movement
        float nextX = radius * (float) Math.cos(time + 0.01f);   // Slightly ahead X
        float nextY = radius * (float) Math.sin(time + 0.01f);   // Slightly ahead Y

        // Compute the direction vector between the current position and the next
        float dx = nextX - x;
        float dy = nextY - y;

        // Calculate the angle the ship should face using atan2
        float facingAngle = (float) Math.atan2(dy, dx);

        // Set the translation matrix to move the ship in a circular path
        translationMatrix(x, y, translation);

        // Rotate the ship to face the correct direction based on the movement angle
        rotationMatrix(facingAngle, rotation);

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
}
