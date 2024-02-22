package pl.dlusk.infrastructure.security;

public class SecurityConfiguration {

//    private final UserDetailsService userDetailsService;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/public/**").permitAll()
//                .antMatchers("/user/**").hasAuthority("USER")
//                .antMatchers("/owner/**").hasAuthority("OWNER")
//                // Możesz dostosować powyższe ścieżki i role do Twoich potrzeb
//                .anyRequest().authenticated()
//                .and()
//                .formLogin().permitAll()
//                .and()
//                .logout().permitAll();
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManagerBuilder authenticationManagerBuilder() throws Exception {
//        return new AuthenticationManagerBuilder(passwordEncoder())
//                .userDetailsService(userDetailsService);
//    }
}
