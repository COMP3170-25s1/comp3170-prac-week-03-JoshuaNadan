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

    private float currentFacingAngle = 0.0f; // Current facing angle of the ship
    private float targetFacingAngle = 0.0f;  // Target facing angle after the turn

    private float rotationSpeed = 0.02f;  // Rotation speed for smooth turning

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
        float x = radius * (float) Math.cos(time);   // Current X position
        float y = radius * (float) Math.sin(time);   // Current Y position

        // Moving direction: horizontal (right/left) or vertical (up/down)
        float dx = radius * (float) Math.cos(time + 1.0f) - x;
        float dy = radius * (float) Math.sin(time + 1.0f) - y;

        // Calculate the current direction vector
        float currentDirection = (float) Math.atan2(dy, dx);

        // Adjust the target facing angle based on movement
        if (Math.abs(dx) > Math.abs(dy)) {
            // Horizontal movement
            if (dx > 0) {
                targetFacingAngle = 1.5708f;  // 90 degrees for moving to the right
            } else {
                targetFacingAngle = -1.5708f;  // -90 degrees for moving to the left
            }
        } else {
            // Vertical movement
            if (dy > 0) {
                targetFacingAngle = 0.0f;  // 0 degrees for moving upwards
            } else {
                targetFacingAngle = 3.1416f;  // 180 degrees for moving downwards
            }
        }

        // Normalize the targetFacingAngle to range [-π, π]
        targetFacingAngle = normalizeAngle(targetFacingAngle);

        // Gradually rotate the ship in the direction of the target facing angle
        currentFacingAngle = smoothAngleLerp(currentFacingAngle, targetFacingAngle, rotationSpeed);

        // Set the translation matrix to move the ship in a circular path
        translationMatrix(x, y, translation);

        // Apply rotation based on the current facing angle
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

    // Normalize angle to keep it between -π and π
    private float normalizeAngle(float angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    // Smooth interpolation with linear interpolation (no easing) to make the rotation smoother
    private float smoothAngleLerp(float currentAngle, float targetAngle, float rotationSpeed) {
        currentAngle = normalizeAngle(currentAngle);
        targetAngle = normalizeAngle(targetAngle);

        // Calculate the difference in angles
        float angleDifference = targetAngle - currentAngle;

        // Ensure the ship always turns in the shortest direction (wrap around [-π, π])
        if (angleDifference > Math.PI) {
            angleDifference -= 2 * Math.PI;
        } else if (angleDifference < -Math.PI) {
            angleDifference += 2 * Math.PI;
        }

        // Apply linear interpolation (no easing)
        float lerpFactor = rotationSpeed;

        // Smoothly interpolate directly towards the target angle
        return currentAngle + angleDifference * lerpFactor;
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

