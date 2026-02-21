package digital8.payroll.services;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import digital8.payroll.entities.Users;
import digital8.payroll.repositories.UsersRepository;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Users authenticate(String email, String password){

        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isPresent()){
            Users user = userOpt.get();

            if(!user.getIsActive()){
                System.out.println("User is inactive");
                return null;
            }

            if (passwordEncoder.matches(password, user.getPasswordHash())){
                user.setLastLogin(LocalDateTime.now());
                usersRepository.save(user);
                return user;
            } 
            
            else {
                System.out.println("Invalid password");
            } 
            
        } 
        
        else {
                System.out.println("User not found");
            }
            return null;
        }

    
    public boolean emailExists(String email){
        return usersRepository.existsByEmail(email);
    }

    public Users createUser(Users user, String plainPassword){
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPasswordHash(hashedPassword);
        user.setIsActive(true);

        return usersRepository.save(user);
    }
    
    public boolean changePassword(Users user, String currentPassword, String newPassword){
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())){
            return false;
        }

        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedNewPassword);
        usersRepository.save(user);

        return true;
    }

    public boolean resetPasswordByEmployeeId(Integer employeeId, String newPassword) {
        Optional<Users> userOpt = usersRepository.findByEmployee_EmployeeId(employeeId);
        if (userOpt.isEmpty()) return false;
        Users user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
        return true;
    }
}