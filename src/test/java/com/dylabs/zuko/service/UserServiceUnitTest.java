package com.dylabs.zuko.service;
import com.dylabs.zuko.dto.request.CreateUserRequest;
import com.dylabs.zuko.dto.request.LoginRequest;

import com.dylabs.zuko.dto.request.UpdateUserRequest;
import com.dylabs.zuko.dto.response.AuthResponse;
import com.dylabs.zuko.dto.response.UserResponse;
import com.dylabs.zuko.exception.userExeptions.IncorretPasswordExeption;
import com.dylabs.zuko.exception.userExeptions.UserAlreadyExistsException;
import com.dylabs.zuko.exception.roleExeptions.RoleNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.mapper.UserMapper;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.RoleRepository;
import com.dylabs.zuko.repository.UserRepository;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationManager authManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, userMapper, roleRepository);
        // Inyectar los mocks manualmente porque @Autowired no funciona con @InjectMocks
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "authManager", authManager);
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
    }

    // Crear usuario
    @Test
    @DisplayName("CP01-HU16 - Crear usuario con estado activo especificado")
    void testCreateUserWithActiveStatusSpecified() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "testUser", "testEmail@example.com", "testPassword", null, null, "User", true
        );

        Role userRole = new Role();
        userRole.setRoleName("User");

        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testEmail@example.com");
        user.setPassword("testPassword");
        user.setActive(true);
        user.setUserRole(userRole);

        User savedUser = new User();
        savedUser.setId(1L); // Add ID for saved user
        savedUser.setUsername("testUser");
        savedUser.setEmail("testEmail@example.com");
        savedUser.setUrl_image(null);
        savedUser.setDescription(null);
        savedUser.setActive(true);
        savedUser.setUserRole(userRole);

        UserResponse expectedResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getUrl_image(),
                savedUser.getDescription(),
                savedUser.getUserRoleName(),
                savedUser.getIsActive()
        );

        when(roleRepository.findByRoleNameIgnoreCase("User")).thenReturn(Optional.of(userRole));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail@example.com")).thenReturn(Optional.empty());
        when(userMapper.toUserEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse); // Mock response mapping

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isActive());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("CP02-HU16 - Crear usuario valido")
    void testCreateUserValid() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "testUser",
                "testEmail@example.com",
                "testPassword",
                null,
                null,
                null,
                null
        );

        Role role = new Role();
        role.setRoleName("User");

        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testEmail@example.com");
        user.setPassword("testPassword");
        user.setUserRole(role);
        user.setActive(true);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testUser");
        savedUser.setEmail("testEmail@example.com");
        savedUser.setPassword("testPassword");
        savedUser.setUserRole(role);
        savedUser.setActive(true);

        UserResponse expectedResponse = new UserResponse(
                1L,
                "testUser",
                "testEmail@example.com",
                null,
                null,
                "User",
                true
        );

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleNameIgnoreCase("User")).thenReturn(Optional.of(role));
        when(userMapper.toUserEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.createUser(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(userRepository).findByUsername("testUser");
        verify(userRepository).findByEmail("testEmail@example.com");
        verify(roleRepository).findByRoleNameIgnoreCase("User");
        verify(userMapper).toUserEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("CP03-HU16 - Crear usuario con rol por defecto")
    void testCreateUserWithDefaultRole() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "testUser",
                "testEmail@example.com",
                "testPassword",
                null,
                null,
                null, // No se especifica rol
                null
        );

        Role defaultRole = new Role();
        defaultRole.setRoleName("User");

        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testEmail@example.com");
        user.setPassword("testPassword");
        user.setUserRole(defaultRole);
        user.setActive(true);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testUser");
        savedUser.setEmail("testEmail@example.com");
        savedUser.setPassword("testPassword");
        savedUser.setUserRole(defaultRole);
        savedUser.setActive(true);

        UserResponse expectedResponse = new UserResponse(
                1L,
                "testUser",
                "testEmail@example.com",
                null,
                null,
                "User",
                true
        );

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleNameIgnoreCase("User")).thenReturn(Optional.of(defaultRole));
        when(userMapper.toUserEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.createUser(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(userRepository).findByUsername("testUser");
        verify(userRepository).findByEmail("testEmail@example.com");
        verify(roleRepository).findByRoleNameIgnoreCase("User");
        verify(userMapper).toUserEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("CP04-HU16 - Crear usuario con estado activo por defecto")
    void testCreateUserWithDefaultActiveStatus() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "testUser",
                "testEmail@example.com",
                "testPassword",
                null,
                null,
                "User", // Role specified
                null    // No active status specified
        );

        Role role = new Role();
        role.setRoleName("User");

        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testEmail@example.com");
        user.setPassword("testPassword");
        user.setUserRole(role);
        user.setActive(true); // Default active status

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testUser");
        savedUser.setEmail("testEmail@example.com");
        savedUser.setPassword("testPassword");
        savedUser.setUserRole(role);
        savedUser.setActive(true);

        UserResponse expectedResponse = new UserResponse(
                1L,
                "testUser",
                "testEmail@example.com",
                null,
                null,
                "User",
                true
        );

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleNameIgnoreCase("User")).thenReturn(Optional.of(role));
        when(userMapper.toUserEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.createUser(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(userRepository).findByUsername("testUser");
        verify(userRepository).findByEmail("testEmail@example.com");
        verify(roleRepository).findByRoleNameIgnoreCase("User");
        verify(userMapper).toUserEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("CP05-HU16 - Username duplicado lanza UserAlreadyExistsException")
    void testCreateUserDuplicateUsername() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "duplicateUser",
                "testEmail@example.com",
                "testPassword",
                null,
                null,
                null,
                null
        );

        User existingUser = new User();
        existingUser.setUsername("duplicateUser");

        when(userRepository.findByUsername("duplicateUser")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        assertEquals("El nombre de usuario ya está en uso.", exception.getMessage());

        verify(userRepository).findByUsername("duplicateUser");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CP06-HU16 - Email duplicado lanza UserAlreadyExistsException")
    void testCreateUserDuplicateEmail() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "testUser",
                "duplicateEmail@example.com",
                "testPassword",
                null,
                null,
                null,
                null
        );

        User existingUser = new User();
        existingUser.setEmail("duplicateEmail@example.com");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("duplicateEmail@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());

        verify(userRepository).findByUsername("testUser"); // Verify username check
        verify(userRepository).findByEmail("duplicateEmail@example.com"); // Verify email check
        verify(userRepository, never()).save(any(User.class)); // Ensure save is never called
    }

    @Test
    @DisplayName("CP07-HU16 - Crear usuario lanza excepción si los campos requeridos son nulos")
    void testCreateUserWithNullInputs() {
        CreateUserRequest request = new CreateUserRequest(null, null, null, null, null, null, null);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("CP08-HU16 - Crear usuario lanza RoleNotFoundException si el rol no existe")
    void testCreateUserRoleNotFound() {
        CreateUserRequest request = new CreateUserRequest(
                "testUser", "testEmail@example.com", "testPassword", null, null, "NonExistentRole", null
        );

        when(roleRepository.findByRoleNameIgnoreCase("NonExistentRole")).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class, () -> userService.createUser(request)
        );

        assertEquals("El rol 'NonExistentRole' no existe.", exception.getMessage());
    }

    @Test
    @DisplayName("CP09-HU16 - Asignación de valores opcionales (description y imageUrl)")
    void testCreateUserWithOptionalValues() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "testUser",
                "testEmail@example.com",
                "testPassword",
                "http://example.com/image.jpg", // Optional imageUrl
                "This is a test description",  // Optional description
                null,
                null
        );

        Role role = new Role();
        role.setRoleName("User");

        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testEmail@example.com");
        user.setPassword("testPassword");
        user.setUrl_image("http://example.com/image.jpg");
        user.setDescription("This is a test description");
        user.setUserRole(role);
        user.setActive(true);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testUser");
        savedUser.setEmail("testEmail@example.com");
        savedUser.setPassword("testPassword");
        savedUser.setUrl_image("http://example.com/image.jpg");
        savedUser.setDescription("This is a test description");
        savedUser.setUserRole(role);
        savedUser.setActive(true);

        UserResponse expectedResponse = new UserResponse(
                1L,
                "testUser",
                "testEmail@example.com",
                "http://example.com/image.jpg",
                "This is a test description",
                "User",
                true
        );

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testEmail@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleNameIgnoreCase("User")).thenReturn(Optional.of(role));
        when(userMapper.toUserEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.createUser(request);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(userRepository).findByUsername("testUser");
        verify(userRepository).findByEmail("testEmail@example.com");
        verify(roleRepository).findByRoleNameIgnoreCase("User");
        verify(userMapper).toUserEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("CP10-HU16 - Creacion de usuario con contraseña invalida")
    void testInvalidPasswordPatternInDTO() {
        CreateUserRequest request = new CreateUserRequest(
                "usuario",
                "correo@valido.com",
                "123456",
                null,
                null,
                null,
                null
        );

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean found = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(found);
    }

    // Mostrar perfil de usuario

    @Test
    @DisplayName("CP01-HU17 - Obtener perfil de usuario válido")
    void testGetUserById_Valid() {
        // Arrange
        Long userId = 1L;

        Role role = new Role();
        role.setRoleName("User");

        User user = new User();
        user.setId(userId);
        user.setUsername("rosaUser");
        user.setEmail("rosa@example.com");
        user.setDescription("Hola soy Rosa");
        user.setUrl_image("http://imagen.com/rosa.png");
        user.setUserRole(role);
        user.setActive(true);
        user.setPassword("nunca debería verse");

        UserResponse expectedResponse = new UserResponse(
                userId,
                "rosaUser",
                "rosa@example.com",
                "Hola soy Rosa",
                "http://imagen.com/rosa.png",
                "User",
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        // Act
        UserResponse actualResponse = userService.getUserById(userId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("CP02-HU17 - Obtener perfil de usuario lanza UserNotFoundExeption si el ID no existe")
    void testGetUserById_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundExeption exception = assertThrows(
                UserNotFoundExeption.class,
                () -> userService.getUserById(nonExistentUserId)
        );

        assertEquals("Usuario con ID " + nonExistentUserId + " no encontrado", exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
        verify(userMapper, never()).toResponse(any(User.class)); // Ensure mapping is never called
    }

    // Editar usuario
    @Test
    @DisplayName("CP01-HU18 - Actualizar usuario lanza UserAlreadyExistsException si el email ya existe")
    void testUpdateUserDuplicateEmail() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(null, "duplicateEmail@example.com", null, null, null, null);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("originalEmail@example.com");
        existingUser.setUserRole(new Role());
        existingUser.getUserRole().setRoleName("User");

        User duplicateUser = new User();
        duplicateUser.setId(2L);
        duplicateUser.setEmail("duplicateEmail@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("duplicateEmail@example.com")).thenReturn(Optional.of(duplicateUser));

        // Mock authenticated user as the same user (not admin)
        mockAuthenticatedUser(userId, "User");

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class, () -> userService.updateUser(userId, updateRequest)
        );

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
    }
    @Test
    @DisplayName("CP02-HU17 - Actualizar campos deseados")
    void testUpdateUser_MultipleFields() {
        // Arrange
        Long userId = 1L;

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "updatedUsername",
                "updatedEmail@example.com",
                "updatedPassword",
                "http://updated-image.com/image.jpg",
                "Updated description",
                "Admin"
        );

        Role role = new Role();
        role.setRoleName("User");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setEmail("originalEmail@example.com");
        existingUser.setPassword("originalPassword");
        existingUser.setUrl_image("http://original-image.com/image.jpg");
        existingUser.setDescription("Original description");
        existingUser.setUserRole(role);
        existingUser.setActive(true);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("updatedUsername");
        updatedUser.setEmail("updatedEmail@example.com");
        updatedUser.setPassword("encodedUpdatedPassword"); // Updated password
        updatedUser.setUrl_image("http://updated-image.com/image.jpg");
        updatedUser.setDescription("Updated description");
        updatedUser.setUserRole(role);
        updatedUser.setActive(true);

        UserResponse expectedResponse = new UserResponse(
                userId,
                "updatedUsername",
                "updatedEmail@example.com",
                "Updated description",
                "http://updated-image.com/image.jpg",
                "User",
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("updatedUsername")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updatedEmail@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("updatedPassword")).thenReturn("encodedUpdatedPassword");
        when(roleRepository.findByRoleNameIgnoreCase("Admin")).thenReturn(Optional.of(role));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expectedResponse);

        // Mock authenticated user as admin
        mockAuthenticatedUser(100L, "Admin");

        // Act
        UserResponse actualResponse = userService.updateUser(userId, updateRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("CP03-HU17 - Usuario no encontrado al intentar actualizar")
    void testUpdateUser_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 99L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "newUsername",
                "newEmail@example.com",
                "newPassword",
                "http://new-image.com/image.jpg",
                "New description",
                null
        );

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundExeption exception = assertThrows(UserNotFoundExeption.class, () ->
                userService.updateUser(nonExistentUserId, updateRequest)
        );

        assertEquals("Usuario no encontrado con id: 99", exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP04-HU17 - Actualizacion de usuario con contraseña invalida")
    void testInvalidPasswordUpdate() {
        UpdateUserRequest request = new UpdateUserRequest(
                null,
                null,
                null,
                null,
                "123456",
                null
        );

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        boolean found = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(found);
    }

    @Test
    @DisplayName("CP05-HU17 - Actualizacion de nombre de usuario duplicado lanza UserAlreadyExistsException")
    void testUpdateUser_DuplicateUsername() {
        // Arrange
        Long userId = 1L;
        String duplicateUsername = "existingUsername";

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                duplicateUsername, // Duplicate username
                null, // No changes to email
                null, // No changes to password
                null, // No changes to image URL
                null,
                null// No changes to description
        );

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setEmail("originalEmail@example.com");
        existingUser.setUserRole(new Role());
        existingUser.getUserRole().setRoleName("User");

        User duplicateUser = new User();
        duplicateUser.setId(2L);
        duplicateUser.setUsername(duplicateUsername);

        // Mock: findById is called twice (once for userToUpdate, once for authenticated user)
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(existingUser))
            .thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(duplicateUsername)).thenReturn(Optional.of(duplicateUser));

        // Mock authenticated user as the same user (not admin)
        mockAuthenticatedUser(userId, "User");

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(userId, updateRequest)
        );

        assertEquals("El nombre de usuario ya está en uso.", exception.getMessage());
        verify(userRepository, times(2)).findById(userId);
        verify(userRepository).findByUsername(duplicateUsername);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CP06-HU17 - No puedes editar a otros usuarios (AccessDeniedException)")
    void testUpdateUser_AccessDenied_OtherUser() {
        // Arrange
        Long userId = 1L; // usuario a editar
        Long authUserId = 2L; // usuario autenticado (no admin)
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "otroUsername", null, null, null, null, null
        );
        User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setUsername("originalUsername");
        userToUpdate.setUserRole(new Role());
        userToUpdate.getUserRole().setRoleName("User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        // Mock usuario autenticado como no admin y distinto id
        mockAuthenticatedUser(authUserId, "User");
        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(userId, updateRequest)
        );
        assertEquals("No puedes editar a otros usuarios.", exception.getMessage());
    }

    @Test
    @DisplayName("CP07-HU17 - No puedes cambiar tu propio rol si no eres admin (AccessDeniedException)")
    void testUpdateUser_AccessDenied_ChangeRole() {
        // Arrange
        Long userId = 1L; // usuario autenticado y a editar
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null, null, null, null, null, "Admin" // roleName no nulo
        );
        User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setUsername("originalUsername");
        userToUpdate.setUserRole(new Role());
        userToUpdate.getUserRole().setRoleName("User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        // Mock usuario autenticado como no admin y mismo id
        mockAuthenticatedUser(userId, "User");
        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(userId, updateRequest)
        );
        assertEquals("No tienes permiso para cambiar el rol.", exception.getMessage());
    }

    // Login
    @Test
    @DisplayName("CP01-HU23 - Login exitoso")
    void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("validEmail@example.com", "validPassword");

        User user = new User();
        user.setId(1L); // Asegúrate de que el usuario tenga un ID
        user.setEmail("validEmail@example.com");
        user.setUsername("validUsername");
        user.setPassword("encodedPassword");
        user.setUserRole(new Role());
        user.getUserRole().setRoleName("User");
        user.setActive(true);

        when(authManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmailIgnoreCase("validEmail@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("validEmail@example.com")).thenReturn(Optional.of(user)); // <-- agrega esto

        // Act
        AuthResponse actualResponse = userService.login(request);

        // Assert
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.token());
        verify(userRepository).findByEmailIgnoreCase("validEmail@example.com");
    }

    @Test
    @DisplayName("CP02-HU23 - Correo no registrado lanza UserNotFoundExeption")
    void testLoginEmailNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("nonExistentEmail@example.com", "password");

        when(authManager.authenticate(any())).thenReturn(null); // Simula autenticación exitosa
        when(userRepository.findByEmailIgnoreCase("nonExistentEmail@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundExeption exception = assertThrows(
                UserNotFoundExeption.class,
                () -> userService.login(request)
        );

        assertEquals("No existe un usuario con ese correo", exception.getMessage());
        verify(userRepository).findByEmailIgnoreCase("nonExistentEmail@example.com");
    }

    @Test
    @DisplayName("CP03-HU23 - Contraseña incorrecta lanza IncorretPasswordExeption")
    void testLoginIncorrectPassword() {
        LoginRequest request = new LoginRequest("validEmail@example.com", "wrongPassword");

        when(authManager.authenticate(any())).thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        IncorretPasswordExeption exception = assertThrows(
                IncorretPasswordExeption.class,
                () -> userService.login(request)
        );

        assertEquals("Usuario o contraseña incorrectos.", exception.getMessage());
    }

    @Test
    @DisplayName("CP04-HU23 - Usuario inactivo lanza UserNotFoundExeption al intentar iniciar sesión")
    void testLoginInactiveUser() {
        // Arrange
        LoginRequest request = new LoginRequest("inactiveEmail@example.com", "validPassword");

        User inactiveUser = new User();
        inactiveUser.setEmail("inactiveEmail@example.com");
        inactiveUser.setPassword("encodedPassword");
        inactiveUser.setActive(false); // User is inactive
        inactiveUser.setUsername("inactiveUsername");
        inactiveUser.setUserRole(new Role());
        inactiveUser.getUserRole().setRoleName("User");

        when(authManager.authenticate(any())).thenReturn(null); // Simula autenticación exitosa
        when(userRepository.findByEmailIgnoreCase("inactiveEmail@example.com")).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        UserNotFoundExeption exception = assertThrows(
                UserNotFoundExeption.class,
                () -> userService.login(request)
        );

        assertEquals("El usuario está desactivado.", exception.getMessage());
        verify(userRepository).findByEmailIgnoreCase("inactiveEmail@example.com");
    }

    // Ver todos los usuarios

    @Test
    @DisplayName("CP01-HU24 - Obtener todos los usuarios exitosamente")
    void testGetAllUsers_Success() {
        // Arrange
        Role role = new Role();
        role.setRoleName("User");

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setDescription("desc1");
        user1.setUrl_image("http://img1.com");
        user1.setUserRole(role);
        user1.setActive(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setDescription("desc2");
        user2.setUrl_image("http://img2.com");
        user2.setUserRole(role);
        user2.setActive(false);

        List<User> usersList = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(usersList);
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return userService.toResponse(user); // Call the actual private method indirectly
        });

        // Act
        List<UserResponse> responseList = userService.getAllUsers();

        // Assert
        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("user1", responseList.get(0).username());
        assertEquals("user2", responseList.get(1).username());
        verify(userRepository).findAll();
        verify(userMapper).toResponse(user1);
        verify(userMapper).toResponse(user2);
    }

    // Cambiar estado de usuario

    @Test
    @DisplayName("CP01-HU25 - Cambio exitoso de estado de usuario")
    void testToggleUserActiveStatus_Success() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setActive(false); // Initially inactive
        user.setUserRole(new Role());
        user.getUserRole().setRoleName("User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Mock authenticated user as admin
        mockAuthenticatedUser(100L, "Admin");

        // Act
        userService.toggleUserActiveStatus(userId);

        // Assert
        assertTrue(user.getIsActive()); // Verify the state is updated to active
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("CP02-HU25 - Usuario no encontrado al intentar cambiar estado")
    void testToggleUserActiveStatus_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        // Mock authenticated user as admin
        mockAuthenticatedUser(100L, "Admin");

        // Act & Assert
        UserNotFoundExeption exception = assertThrows(
                UserNotFoundExeption.class,
                () -> userService.toggleUserActiveStatus(nonExistentUserId)
        );

        assertEquals("Usuario con ID " + nonExistentUserId + " no encontrado", exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
    }

    @Test
    @DisplayName("CP03-HU25 - Solo admin puede cambiar estado de usuario (AccessDeniedException)")
    void testToggleUserActiveStatus_AccessDenied() {
        // Arrange
        Long userId = 1L;
        // Mock authenticated user as non-admin
        mockAuthenticatedUser(userId, "User");

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.toggleUserActiveStatus(2L) // Intenta cambiar el estado de otro usuario
        );
        assertEquals("Solo los administradores pueden cambiar el estado de un usuario.", exception.getMessage());
    }

    // Helper para mockear usuario autenticado
    private void mockAuthenticatedUser(Long userId, String roleName) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId.toString());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        // Mock userRepository.findById for authenticated user
        User authUser = new User();
        authUser.setId(userId);
        authUser.setUserRole(new Role());
        authUser.getUserRole().setRoleName(roleName);
        when(userRepository.findById(userId)).thenReturn(Optional.of(authUser));
    }

    @Test
    @DisplayName("CP08-HU17 - Actualizar usuario lanza RoleNotFoundException si el rol no existe")
    void testUpdateUser_RoleNotFound() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null, null, null, null, null, "NonExistentRole"
        );

        Role currentRole = new Role();
        currentRole.setRoleName("User");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setEmail("originalEmail@example.com");
        existingUser.setUserRole(currentRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        // Mock authenticated user as admin
        mockAuthenticatedUser(100L, "Admin");
        when(roleRepository.findByRoleNameIgnoreCase("NonExistentRole")).thenReturn(Optional.empty());

        // Act & Assert
        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> userService.updateUser(userId, updateRequest)
        );
        assertEquals("Rol no encontrado: NonExistentRole", exception.getMessage());
    }


    @Test
    @DisplayName("CP14-HU17 - Admin actualiza solo la descripción de un usuario")
    void testUpdateUser_AdminChangeDescription() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(null, null, "Nueva descripción", null, null, null);
        Role role = new Role();
        role.setRoleName("User");
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setUserRole(role);

        UserResponse expectedResponse = new UserResponse(
                userId, "originalUsername", null, "Nueva descripción", null, "User", false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(expectedResponse);

        mockAuthenticatedUser(100L, "Admin");

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("Nueva descripción", response.description());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("CP11-HU17 - Usuario edita su propio perfil sin cambios (todos los campos nulos)")
    void testUpdateUser_UserNoChanges() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest(null, null, null, null, null, null);
        Role role = new Role();
        role.setRoleName("User");
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setUserRole(role);

        UserResponse expectedResponse = new UserResponse(
                userId, "originalUsername", null, null, null, "User", false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);}

    @Test
    @DisplayName("CP12-HU17 - Admin actualiza usuario sin cambiar el rol (roleName nulo)")
    void testUpdateUser_AdminNoRoleChange() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest("nuevoUsername", null, null, null, null, null);
        Role role = new Role();
        role.setRoleName("User");
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setUserRole(role);

        UserResponse expectedResponse = new UserResponse(
                userId, "nuevoUsername", null, null, null, "User", false
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("nuevoUsername")).thenReturn(Optional.empty());
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(existingUser)).thenReturn(expectedResponse);

        mockAuthenticatedUser(100L, "Admin");

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("nuevoUsername", response.username());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("CP13-HU17 - Usuario edita su propio perfil cambiando solo username")
    void testUpdateUser_UserChangeUsername() {
        Long userId = 1L;
        UpdateUserRequest updateRequest = new UpdateUserRequest("nuevoUsername", null, null, null, null, null);
        Role role = new Role();
        role.setRoleName("User");
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("originalUsername");
        existingUser.setUserRole(role);

        UserResponse expectedResponse = new UserResponse(
                userId, "nuevoUsername", null, null, null, "User", false
        );

        // Mock findById for both user to update and authenticated user
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("nuevoUsername")).thenReturn(Optional.empty());
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        mockAuthenticatedUser(userId, "User");}

}

