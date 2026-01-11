package uz.pdp.interlex.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import uz.pdp.interlex.dto.ApiResponse;
import uz.pdp.interlex.entity.Lawyer;
import uz.pdp.interlex.repo.LawyerRepository;
import uz.pdp.interlex.security.JwtUtil;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LawyerRepository lawyerRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

            Optional<Lawyer> optionalLawyer = lawyerRepository.findByEmail(email);
            if (optionalLawyer.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
            }

            Lawyer lawyer = optionalLawyer.get();

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(email)
                    .password(lawyer.getPassword())
                    .authorities("ROLE_LAWYER")
                    .build();

            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(ApiResponse.success("Login successful", Map.of(
                    "token", token,
                    "lawyerId", lawyer.getId(),
                    "email", lawyer.getEmail()
            )));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid credentials"));
        }
    }
}
