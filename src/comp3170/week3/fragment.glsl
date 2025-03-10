#version 410

in vec3 v_colour; // RGB from vertex shader

layout(location = 0) out vec4 o_colour;    // RGBA output to colour buffer 

void main() {
    o_colour = vec4(v_colour, 1);    
}
