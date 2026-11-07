#version 150

uniform sampler2D Sampler0;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord);
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    fragColor = vec4(vec3(luminance), color.a);
}
