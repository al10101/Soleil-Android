precision mediump float;

// The kernel is a 3x3 matrix
uniform float u_Kernel[9];

uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main() {
    float offset = 1.0 / 300.0;
    // Offset matrix
    vec2 offsets[9];
    offsets[0] = vec2(-offset, offset); // top-left
    offsets[1] = vec2(0.0, offset);     // top-center
    offsets[2] = vec2(offset, offset);  // top-right
    offsets[3] = vec2(-offset, 0.0);    // center-left
    offsets[4] = vec2(0.0, 0.0);        // center-center
    offsets[5] = vec2(offset, 0.0);     // center-right
    offsets[6] = vec2(-offset, -offset);// bottom-left
    offsets[7] = vec2(0.0, -offset);    // bottom-center
    offsets[8] = vec2(offset, -offset); // bottom-right
    vec3 sampleTex[9];
    for (int i = 0; i < 9; i++) {
        sampleTex[i] = vec3( texture2D(u_TextureUnit, v_TextureCoordinates.st + offsets[i]) );
    }
    vec3 col;
    for (int i = 0; i < 9; i++) {
        col += sampleTex[i] * u_Kernel[i];
    }
    gl_FragColor = vec4(col, 1.0);
}
