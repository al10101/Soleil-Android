precision mediump float;

uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoordinates;

void main() {
    vec4 origText = texture2D(u_TextureUnit, v_TextureCoordinates);
    gl_FragColor = vec4(vec3(1.0 - origText), 1.0);
}
