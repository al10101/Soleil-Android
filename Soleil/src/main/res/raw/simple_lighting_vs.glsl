uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ModelMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec4 a_Color;

varying vec3 v_WorldPosition;
varying vec3 v_WorldNormal;
varying vec4 v_Color;

void main() {
    // Pass color, normal and position params
    v_WorldPosition = vec3(u_ModelMatrix * a_Position);
    v_WorldNormal = mat3(u_ModelMatrix) * a_Normal;
    v_Color = a_Color;
    // Vertex position
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * a_Position;
}
