#version 410

in vec4 a_position;    // vertex position as a homogeneous vector in NDC
in vec3 a_colour;      // vertex colour RGB

out vec3 v_colour;     // to fragment shader

uniform mat4 u_model;   // model matrix uniform

void main() {
    v_colour = a_colour;

    // Apply the model matrix to the vertex position
    gl_Position = u_model * a_position;
}
