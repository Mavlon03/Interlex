package uz.pdp.interlex.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.pdp.interlex.entity.Lawyer;
import uz.pdp.interlex.repo.LawyerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LawyerUserDetailsService implements UserDetailsService {

    private final LawyerRepository lawyerRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Lawyer> optional = lawyerRepository.findByEmail(username);
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException("Lawyer not found: " + username);
        }
        Lawyer lawyer = optional.get();

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Admin if email matches admin email
        if (adminEmail != null && adminEmail.equalsIgnoreCase(lawyer.getEmail())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_LAWYER"));

        // Use getPassword() (placeholder) - in production replace with hashed password
        return User.builder()
                .username(lawyer.getEmail())
                .password(lawyer.getPassword())
                .authorities(authorities)
                .build();
    }
}
