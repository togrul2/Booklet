package com.github.togrul2.booklet.services;

import com.github.togrul2.booklet.dtos.auth.LoginDto;
import com.github.togrul2.booklet.dtos.auth.RefreshRequestDto;
import com.github.togrul2.booklet.dtos.auth.TokenPairDto;
import com.github.togrul2.booklet.entities.Role;
import com.github.togrul2.booklet.entities.Token;
import com.github.togrul2.booklet.entities.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class AuthServiceTests {
    @InjectMocks
    private AuthService authService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    private User user;

    @BeforeEach
    public void setUp() {
        this.user = User.builder()
                .email("johndoe@example.com")
                .password("secret")
                .role(Role.USER)
                .build();
    }

    @Test
    public void testLoginSuccess() {
        LoginDto loginDto = LoginDto.builder().email("johndoe@example.com").password("secret").build();
        Mockito.when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        Mockito.when(passwordEncoder.matches(loginDto.password(), user.getPassword())).thenReturn(true);
        Mockito.when(jwtService.createAndStoreRefreshToken(user, user.getRole()))
                .thenReturn(Token.builder().token("refresh-token").user(user).build());
        Mockito.when(jwtService.createAccessToken(user, user.getRole())).thenReturn("access-token");

        TokenPairDto tokens = authService.login(loginDto);

        Assertions.assertEquals("access-token", tokens.accessToken());
        Assertions.assertEquals("refresh-token", tokens.refreshToken());
        Mockito.verify(userDetailsService).loadUserByUsername(user.getEmail());
        Mockito.verify(passwordEncoder).matches(loginDto.password(), user.getPassword());
        Mockito.verify(jwtService).createAndStoreRefreshToken(user, user.getRole());
        Mockito.verify(jwtService).createAccessToken(user, user.getRole());
    }

    @Test
    public void testLoginUsernameNotFound() {
        LoginDto loginDto = LoginDto.builder().email("johndoe@example.com").password("secret").build();
        Mockito.when(userDetailsService.loadUserByUsername(user.getEmail()))
                .thenThrow(UsernameNotFoundException.class);

        Assertions.assertThrows(UsernameNotFoundException.class, () -> authService.login(loginDto));
        Mockito.verify(userDetailsService).loadUserByUsername(user.getEmail());
    }

    @Test
    public void testLoginWrongPassword() {
        LoginDto loginDto = LoginDto.builder().email("johndoe@example.com").password("wrong").build();
        Mockito.when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        Mockito.when(passwordEncoder.matches(loginDto.password(), user.getPassword())).thenReturn(false);

        Assertions.assertThrows(UsernameNotFoundException.class, () -> authService.login(loginDto));
        Mockito.verify(passwordEncoder).matches(loginDto.password(), user.getPassword());
        Mockito.verify(jwtService, Mockito.never()).createAndStoreRefreshToken(Mockito.any(), Mockito.any());
    }

    @Test
    public void testRefreshSuccess() {
        RefreshRequestDto dto = new RefreshRequestDto("old-refresh-token");
        Mockito.when(jwtService.extractUsername("old-refresh-token")).thenReturn(user.getEmail());
        Mockito.when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        Mockito.when(jwtService.createAndStoreRefreshToken(user, user.getRole()))
                .thenReturn(Token.builder().token("new-refresh-token").user(user).build());
        Mockito.when(jwtService.createAccessToken(user, user.getRole())).thenReturn("new-access-token");

        TokenPairDto tokens = authService.refresh(dto);

        Assertions.assertEquals("new-access-token", tokens.accessToken());
        Assertions.assertEquals("new-refresh-token", tokens.refreshToken());
        Mockito.verify(jwtService).deactivateRefreshToken("old-refresh-token");
        Mockito.verify(jwtService).createAndStoreRefreshToken(user, user.getRole());
        Mockito.verify(jwtService).createAccessToken(user, user.getRole());
    }

    @Test
    public void testRefreshInvalidToken() {
        RefreshRequestDto dto = new RefreshRequestDto("invalid-token");
        Mockito.doThrow(new JwtException("Bad refresh token"))
                .when(jwtService).deactivateRefreshToken("invalid-token");

        Assertions.assertThrows(JwtException.class, () -> authService.refresh(dto));
        Mockito.verify(jwtService).deactivateRefreshToken("invalid-token");
        Mockito.verify(jwtService, Mockito.never()).createAndStoreRefreshToken(Mockito.any(), Mockito.any());
    }

    @Test
    public void testLogoutSuccess() {
        RefreshRequestDto dto = new RefreshRequestDto("refresh-token");

        Assertions.assertDoesNotThrow(() -> authService.logout(dto));
        Mockito.verify(jwtService).deactivateRefreshToken("refresh-token");
    }

    @Test
    public void testLogoutInvalidToken() {
        RefreshRequestDto dto = new RefreshRequestDto("invalid-token");
        Mockito.doThrow(new JwtException("Bad refresh token"))
                .when(jwtService).deactivateRefreshToken("invalid-token");

        Assertions.assertThrows(JwtException.class, () -> authService.logout(dto));
        Mockito.verify(jwtService).deactivateRefreshToken("invalid-token");
    }

    @Test
    public void testValidateSuccess() {
        RefreshRequestDto dto = new RefreshRequestDto("refresh-token");

        Assertions.assertDoesNotThrow(() -> authService.validate(dto));
        Mockito.verify(jwtService).validateRefreshToken("refresh-token");
    }

    @Test
    public void testValidateInvalidToken() {
        RefreshRequestDto dto = new RefreshRequestDto("invalid-token");
        Mockito.doThrow(new JwtException("Bad refresh token"))
                .when(jwtService).validateRefreshToken("invalid-token");

        Assertions.assertThrows(JwtException.class, () -> authService.validate(dto));
        Mockito.verify(jwtService).validateRefreshToken("invalid-token");
    }

    @Test
    public void testCreateTokenPairsSuccess() {
        Mockito.when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
        Mockito.when(jwtService.createRefreshToken(user, user.getRole())).thenReturn("refresh-token");
        Mockito.when(jwtService.createAccessToken(user, user.getRole())).thenReturn("access-token");

        TokenPairDto tokens = authService.createTokenPairs(user.getEmail());

        Assertions.assertEquals("access-token", tokens.accessToken());
        Assertions.assertEquals("refresh-token", tokens.refreshToken());
        Mockito.verify(userDetailsService).loadUserByUsername(user.getEmail());
        Mockito.verify(jwtService).createRefreshToken(user, user.getRole());
        Mockito.verify(jwtService).createAccessToken(user, user.getRole());
    }

    @Test
    public void testCreateTokenPairsUserNotFound() {
        Mockito.when(userDetailsService.loadUserByUsername(user.getEmail()))
                .thenThrow(UsernameNotFoundException.class);

        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> authService.createTokenPairs(user.getEmail()));
        Mockito.verify(jwtService, Mockito.never()).createRefreshToken(Mockito.any(), Mockito.any());
    }
}
