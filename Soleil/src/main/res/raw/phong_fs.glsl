precision mediump float;

uniform vec3 u_CameraPosition;

// This shader works with only 1 light: a sunlight
uniform vec3 u_LightPosition[1];
uniform vec3 u_LightColor[1];
uniform float u_LightIntensity[1];
uniform vec3 u_LightSpecular[1];

uniform float u_MaterialShininess;

varying vec3 v_WorldPosition;
varying vec3 v_WorldNormal;
varying vec4 v_Color;

void main() {
    // Initialize variables
    vec3 baseColor = vec3(v_Color);
    // Phong variables
    vec3 diffuseColor = vec3(0.0);
    vec3 specularColor = vec3(0.0);
    vec3 ambientColor = vec3(0.0);
    // Get the light's direction vector from the light's position and turn
    // the direction vectors into unit vectors so that both the normal and
    // light vectors have a length of 1
    vec3 normalDirection = normalize(v_WorldNormal);
    vec3 lightDirection = normalize(-u_LightPosition[0]);
    // Get the dot product of the two vectors. When the fragment fully points toward
    // the light, the dot product will be -1. Negating the dot product will make
    // easier the next calculations
    float diffuseIntensity = clamp(-dot(lightDirection, normalDirection), 0.0, 1.0);
    // Multiply the color by the diffuse intensity to get diffuse shading
    diffuseColor = u_LightColor[0] * baseColor * diffuseIntensity;
    if (diffuseIntensity > 0.0) {
        // Reflect vector
        vec3 reflection = reflect(lightDirection, normalDirection);
        // View vector
        vec3 cameraDirection = normalize(v_WorldPosition - u_CameraPosition);
        float specularIntensity = pow(clamp(-dot(reflection, cameraDirection), 0.0, 1.0), u_MaterialShininess);
        specularColor = u_LightSpecular[0] * specularIntensity;
    }
    // Multiply by baseColor intead of the light's color to make it a little bit brighter
    ambientColor = baseColor * u_LightIntensity[0];
    // Set the final color
    vec3 res = diffuseColor + ambientColor + specularColor;
    gl_FragColor = vec4(res, v_Color[3]);
}
