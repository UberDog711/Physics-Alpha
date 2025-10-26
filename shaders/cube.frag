#version 330 core

out vec4 FragColor;

in vec3 FragPos;  
in vec3 Normal;  

uniform vec3 lightDir = normalize(vec3(-1.0, -1.0, -0.3)); 
uniform vec3 objectColor = vec3(0.6, 0.2, 0.8);

void main()
{
    // simple directional light
    float diff = max(dot(normalize(Normal), -lightDir), 0.0);
    vec3 diffuse = diff * objectColor;
    
    // ambient term
    vec3 ambient = 0.2 * objectColor;

    vec3 result = ambient + diffuse;
    FragColor = vec4(result, 1.0);
}
