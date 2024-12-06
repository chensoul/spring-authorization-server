package com.chensoul.authserver.authentication;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {
    private final Map<String, CustomUser> users;

    public CustomUserDetailsService(Collection<CustomUser> users) {
        this.users = users.stream().collect(Collectors.toMap(CustomUser::getUsername, Function.identity()));
    }

    public CustomUserDetailsService(CustomUser user) {
        this(List.of(user));
    }

    public Collection<CustomUser> getUsers() {
        return this.users.values().stream().sorted(Comparator.comparing(CustomUser::getUsername)).toList();
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUser user = this.users.get(username.toLowerCase(Locale.getDefault()));
        if (user==null) {
            throw new UsernameNotFoundException(username);
        } else {
            return user;
        }
    }
}
