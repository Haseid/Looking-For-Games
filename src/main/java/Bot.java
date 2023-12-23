package me.name.bot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Bot {
    public static void main (String[] args) throws LoginException, RateLimitedException, IOException, GeneralSecurityException {
        new JDABuilder(AccountType.BOT)
                .setToken("Your token here")
                .addEventListeners(new MyListener())
                .build();
    }
}
