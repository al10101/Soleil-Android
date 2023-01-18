precision mediump float;

uniform sampler2D u_TextureUnit0;
uniform float u_Gamma;

varying vec2 v_TextureCoordinates;

void main() {
    gl_FragColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
    gl_FragColor.rgb = pow(gl_FragColor.rgb, vec3(1.0/u_Gamma));
}