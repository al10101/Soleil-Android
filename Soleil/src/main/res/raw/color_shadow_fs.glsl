precision mediump float;

uniform vec3 u_CameraPosition;

// This shader works with only 1 light: a sunlight
uniform vec3 u_LightPosition[1];
uniform float u_LightIntensity[1];

uniform sampler2D u_ShadowTextureUnit;

varying vec3 v_WorldPosition;
varying vec3 v_WorldNormal;
varying vec4 v_Color;
varying vec4 v_LightSpacePosition;

float shadowCalc(float dotLightNormal) {
    // Transform from [-1, 1] range to [0, 1] range
    vec3 pos = v_LightSpacePosition.xyz * 0.5 + 0.5;
    if (pos.z > 1.0) {
        pos.z = 1.0;
    }
    float depth = texture2D(u_ShadowTextureUnit, pos.xy).r;
    // Bias to account for the stripe pattern due to imperfect resolution
    float bias = max(0.05 * (-1.0 + dotLightNormal), 0.005);
    return (depth + bias) < pos.z ? 0.0 : 1.0;
}

void main() {
    // Initialize variables
    vec3 baseColor = vec3(v_Color);
    // Get the light's direction vector from the light's position and turn
    // the direction vectors into unit vectors so that both the normal and
    // light vectors have a length of 1
    vec3 normalDirection = normalize(v_WorldNormal);
    // Get direction of the only light
    vec3 lightDirection = normalize(-u_LightPosition[0]);
    // Get the dot product of the two vectors. When the fragment fully points toward
    // the light, the dot product will be -1. Negating the dot product will make
    // easier the next calculations
    float dotLightNormal = dot(lightDirection, normalDirection);
    // Calculate shadow
    float shadow = shadowCalc(dotLightNormal); // either 0 or 1
    vec3 res = vec3(0.0);
    if (shadow == 0.0) {
        res = u_LightIntensity[0] * baseColor;
    } else {
        res = baseColor;
    }
    gl_FragColor = vec4(res, v_Color.a);
}
