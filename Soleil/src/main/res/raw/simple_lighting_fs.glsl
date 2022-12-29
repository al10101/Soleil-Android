precision mediump float;

uniform vec3 u_CameraPosition;

// This shader works with only 1 light: a sunlight
uniform vec3 u_LightPosition[1];
uniform vec3 u_LightColor[1];
uniform vec3 u_LightSpecular[1];

varying vec3 v_WorldPosition;
varying vec3 v_WorldNormal;
varying vec4 v_Color;

void main() {
    // Initialize variables
    vec3 baseColor = vec3(v_Color);
    vec3 specularColor = vec3(0.0);
    float materialShininess = 24.0;
    float ambientLightIntensity = 0.2;
    // Get the light's direction vector from the light's position and turn
    // the direction vectors into unit vectors so that both the normal and
    // light vectors have a length of 1
    vec3 normalDirection = normalize(v_WorldNormal);
    // Loop through lights
    vec3 lightDirection = normalize(-u_LightPosition[0]);
    // Get the dot product of the two vectors. When the fragment fully points toward
    // the light, the dot product will be -1. Negating the dot product will make
    // easier the next calculations
    float dotLightNormal = dot(lightDirection, normalDirection);
    float diffuseIntensity = clamp(-dotLightNormal, 0.0, 1.0);
    // Multiply the color by the diffuse intensity to get diffuse shading
    vec3 diffuseColor = u_LightColor[0] * baseColor * diffuseIntensity;
    // Compute the reflection
    if (diffuseIntensity > 0.0) {
        // Reflect vector
        vec3 reflection = reflect(lightDirection, normalDirection);
        // View vector
        vec3 cameraDirection = normalize(v_WorldPosition - u_CameraPosition);
        // Find the angle between reflection and view
        float specularIntensity = pow(clamp(-dot(reflection, cameraDirection), 0.0, 1.0), materialShininess);
        specularColor += u_LightSpecular[0] * specularIntensity;
    }
    // Add the ambient light as a small fraction of the base color
    vec3 ambientColor = baseColor * ambientLightIntensity;
    // Set the final color
    vec3 res = diffuseColor + specularColor + ambientColor;
    gl_FragColor = vec4(res, v_Color[3]);
}
