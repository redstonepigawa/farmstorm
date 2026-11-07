#version 150

// 水下专用灰度：保持黑白观感，但把水下丢失的明度层次拉回来。
// 水下画面本来就偏暗、低对比，直接取亮度会糊成一片均匀的灰（作者实测「完全看不见」），
// 所以这里做三步：① 提亮暗部（gamma < 1）；② 拉开对比（围绕中灰做 S 形拉伸）；③ 轻微整体提亮。

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    // ① 提亮暗部：把 0..1 的亮度压低指数后整体上移，暗处细节不再压死。
    float lifted = pow(clamp(luminance, 0.0, 1.0), 0.62);

    // ② 拉开对比：围绕中灰 0.5 放大 1.45 倍，让轮廓边界重新可见。
    float contrasted = clamp((lifted - 0.5) * 1.45 + 0.5, 0.0, 1.0);

    // ③ 轻微整体提亮：水下整体偏暗，抬一点点就够，抬多了会发白失去层次。
    float finalLuma = clamp(contrasted * 1.08, 0.0, 1.0);

    fragColor = vec4(vec3(finalLuma), color.a);
}
