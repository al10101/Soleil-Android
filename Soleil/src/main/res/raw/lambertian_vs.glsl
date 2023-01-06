uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ModelMatrix;

// This shader works with only 1 light, considering sunlight treatment
uniform vec3 u_LightPosition[1];
uniform vec3 u_LightColor[1];

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec4 a_Color;

varying vec4 v_FinalColor;

void main() {
    // The color is computed here in the vertex shader
    vec3 normalDirection = normalize(mat3(u_ModelMatrix) * a_Normal);
    vec3 lightDirection = normalize(u_LightPosition[0]);
    float lambertTerm = dot(normalDirection, lightDirection);
    vec3 diffuseIntensity = a_Color.rgb * u_LightColor[0] * lambertTerm;
    v_FinalColor.rgb = diffuseIntensity;
    v_FinalColor.a = a_Color.a;
    // Vertex position
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * a_Position;
}