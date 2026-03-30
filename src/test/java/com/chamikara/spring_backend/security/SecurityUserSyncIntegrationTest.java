package com.chamikara.spring_backend.security;

import com.chamikara.spring_backend.entity.Transformer;
import com.chamikara.spring_backend.entity.User;
import com.chamikara.spring_backend.repository.TransformerRepository;
import com.chamikara.spring_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityUserSyncIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransformerRepository transformerRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void cleanDatabase() {
        transformerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void validBearerTokenAutoProvisionsLocalUserAndLinksTransformerCreator() throws Exception {
        UUID keycloakId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", keycloakId.toString())
                .claim("preferred_username", "test_chamikara")
                .claim("email", "test_chamikara@example.com")
                .claim("scope", "openid profile email")
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);

        String transformerNumber = "TR-SEC-" + UUID.randomUUID().toString().substring(0, 8);
        String payload = """
                {
                  "number": "%s",
                  "pole": "P-001",
                  "region": "North",
                  "type": "Distribution",
                  "baselineImage": "data:image/png;base64,AAAA",
                  "weather": "Sunny",
                  "location": "Substation-A"
                }
                """.formatted(transformerNumber);

        mockMvc.perform(post("/api/v1/transformers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.number").value(transformerNumber));

        Optional<User> savedUserOptional = userRepository.findByKeycloakId(keycloakId);
        assertTrue(savedUserOptional.isPresent(), "Expected local user to be created from JWT claims");

        User savedUser = savedUserOptional.orElseThrow();
        assertEquals("test_chamikara", savedUser.getUsername());
        assertEquals("test_chamikara@example.com", savedUser.getEmail());

        Transformer savedTransformer = transformerRepository.findByNumber(transformerNumber).orElseThrow();
        assertEquals(savedUser.getId(), savedTransformer.getUserId());
    }
}
