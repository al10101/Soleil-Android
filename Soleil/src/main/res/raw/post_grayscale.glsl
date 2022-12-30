precision mediump float;

uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoordinates;

void main() {
    vec4 origText = texture2D(u_TextureUnit, v_TextureCoordinates);
    // With correction because of eye's sensitivity to green
    float avg = 0.2126 * origText.r + 0.7152 * origText.g + 0.0722 * origText.b;
    gl_FragColor = vec4(avg, avg, avg, 1.0);
}
