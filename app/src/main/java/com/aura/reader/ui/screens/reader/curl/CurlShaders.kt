package com.aura.reader.ui.screens.reader.curl

object CurlShaders {

    /**
     * Vertex Shader for Page Curl Mesh Deformation.
     * Deforms a flat [0, 1] x [0, 1] grid into a 3D cylindrical / conical page roll.
     */
    const val PAGE_CURL_VERTEX_SHADER = """
        uniform mat4 u_MVPMatrix;
        uniform vec2 u_CurlPos;       // Touch origin point in [0, 1]
        uniform vec2 u_CurlDir;       // Curl crease direction vector
        uniform vec2 u_CurlNorm;      // Normal vector pointing into the curled portion
        uniform float u_Radius;       // Base curl cylinder radius
        uniform float u_Aspect;       // Screen aspect ratio (width / height)
        uniform float u_ConeFactor;   // Conical flare expansion factor

        attribute vec2 a_Position;    // Grid vertex in [0, 1] x [0, 1]
        attribute vec2 a_TexCoord;

        varying vec2 v_TexCoord;
        varying vec3 v_Normal;
        varying float v_Dist;
        varying float v_Radius;

        const float PI = 3.141592653589793;

        void main() {
            v_TexCoord = a_TexCoord;

            // 1. Aspect-corrected coordinates for Euclidean metric deformation
            vec2 p = vec2(a_Position.x * u_Aspect, a_Position.y);
            vec2 p0 = vec2(u_CurlPos.x * u_Aspect, u_CurlPos.y);

            // 2. Normal and direction vectors along curl crease
            vec2 norm = normalize(u_CurlNorm);
            vec2 dir = normalize(u_CurlDir);

            // 3. Distance along normal and distance along axis
            float d = dot(p - p0, norm);
            float t = dot(p - p0, dir);

            // Conical radius: varies smoothly along axis
            float r = max(u_Radius + u_ConeFactor * t, 0.04);
            v_Radius = r;
            v_Dist = d;

            vec2 pDeformed = p;
            float z = 0.0;
            vec3 surfaceNormal = vec3(0.0, 0.0, 1.0);

            if (d <= 0.0) {
                // Section A: Flat uncurled page
                pDeformed = p;
                z = 0.0;
                surfaceNormal = vec3(0.0, 0.0, 1.0);
            } else if (d < PI * r) {
                // Section B: Curving around the cylinder/cone
                float alpha = d / r;
                pDeformed = p - norm * (d - r * sin(alpha));
                z = r * (1.0 - cos(alpha));
                surfaceNormal = vec3(-sin(alpha) * norm.x, -sin(alpha) * norm.y, cos(alpha));
            } else {
                // Section C: Turned over to the back side
                pDeformed = p - norm * (2.0 * d - PI * r);
                z = 2.0 * r;
                surfaceNormal = vec3(0.0, 0.0, -1.0);
            }

            v_Normal = surfaceNormal;

            // Convert aspect-adjusted coordinates back to NDC [-1, 1]
            float ndcX = (pDeformed.x / u_Aspect) * 2.0 - 1.0;
            float ndcY = (1.0 - pDeformed.y) * 2.0 - 1.0;
            float ndcZ = z * 1.5;

            gl_Position = u_MVPMatrix * vec4(ndcX, ndcY, ndcZ, 1.0);
        }
    """

    /**
     * Fragment Shader for Page Curl.
     * Handles front-page rendering, back-page rendering with paper texture,
     * specular highlights on cylinder curve, and inner shadow.
     */
    const val PAGE_CURL_FRAGMENT_SHADER = """
        precision mediump float;

        uniform sampler2D u_TextureFront;
        uniform sampler2D u_TextureBack;
        uniform int u_HasBackTexture;

        varying vec2 v_TexCoord;
        varying vec3 v_Normal;
        varying float v_Dist;
        varying float v_Radius;

        const vec3 LIGHT_DIR = vec3(-0.25, 0.35, 0.9);

        void main() {
            vec3 norm = normalize(v_Normal);
            vec3 light = normalize(LIGHT_DIR);

            if (norm.z >= 0.0) {
                // Front Side of page
                vec4 color = texture2D(u_TextureFront, v_TexCoord);

                // Diffuse lighting on cylinder curve
                float diff = max(dot(norm, light), 0.0) * 0.3 + 0.7;

                // Soft specular highlight on peak of paper cylinder
                vec3 halfVec = normalize(light + vec3(0.0, 0.0, 1.0));
                float spec = pow(max(dot(norm, halfVec), 0.0), 20.0) * 0.22;

                gl_FragColor = vec4(color.rgb * diff + vec3(spec), color.a);
            } else {
                // Back Side of page (reversed / mirrored)
                vec2 backTexCoord = vec2(1.0 - v_TexCoord.x, v_TexCoord.y);
                vec4 color;

                if (u_HasBackTexture == 1) {
                    color = texture2D(u_TextureBack, backTexCoord);
                } else {
                    // Mirrored front page with warm parchment paper tint
                    vec4 frontColor = texture2D(u_TextureFront, backTexCoord);
                    vec3 paperTint = vec3(0.96, 0.94, 0.90);
                    color = vec4(mix(paperTint, frontColor.rgb, 0.25), frontColor.a);
                }

                // Inner fold ambient occlusion shadow
                float innerShadow = clamp(1.0 + norm.z * 0.38, 0.45, 1.0);
                gl_FragColor = vec4(color.rgb * innerShadow, color.a);
            }
        }
    """

    /**
     * Vertex Shader for Under-Page Quad with Drop Shadow.
     */
    const val UNDER_PAGE_VERTEX_SHADER = """
        uniform mat4 u_MVPMatrix;
        attribute vec2 a_Position;
        attribute vec2 a_TexCoord;

        varying vec2 v_TexCoord;
        varying vec2 v_Position;

        void main() {
            v_TexCoord = a_TexCoord;
            v_Position = a_Position;

            float ndcX = a_Position.x * 2.0 - 1.0;
            float ndcY = (1.0 - a_Position.y) * 2.0 - 1.0;
            gl_Position = u_MVPMatrix * vec4(ndcX, ndcY, -0.05, 1.0);
        }
    """

    /**
     * Fragment Shader for Under-Page Quad with Dynamic Drop Shadow beneath the curl.
     */
    const val UNDER_PAGE_FRAGMENT_SHADER = """
        precision mediump float;

        uniform sampler2D u_TextureUnder;
        uniform vec2 u_CurlPos;
        uniform vec2 u_CurlNorm;
        uniform float u_Radius;
        uniform float u_Aspect;

        varying vec2 v_TexCoord;
        varying vec2 v_Position;

        void main() {
            vec4 baseColor = texture2D(u_TextureUnder, v_TexCoord);

            // Compute distance from curl line in aspect-corrected coordinates
            vec2 p = vec2(v_Position.x * u_Aspect, v_Position.y);
            vec2 p0 = vec2(u_CurlPos.x * u_Aspect, u_CurlPos.y);
            vec2 norm = normalize(u_CurlNorm);

            float d = dot(p - p0, norm);

            // Drop shadow cast directly under and slightly ahead of the curl
            float shadow = 0.0;
            if (d > -0.05) {
                float shadowWidth = u_Radius * 2.5;
                float startFade = smoothstep(-0.05, 0.02, d);
                float endFade = 1.0 - smoothstep(0.02, shadowWidth, d);
                shadow = startFade * endFade * 0.45;
            }

            gl_FragColor = vec4(baseColor.rgb * (1.0 - shadow), baseColor.a);
        }
    """
}
