package com.moviefy.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "moviefy.rate-limit")
public class RateLimitProperties {

    private General general = new General();
    private Auth auth = new Auth();

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public static class General {
        private int authUser;
        private int anonIp;

        public int getAuthUser() {
            return authUser;
        }

        public void setAuthUser(int authUser) {
            this.authUser = authUser;
        }

        public int getAnonIp() {
            return anonIp;
        }

        public void setAnonIp(int anonIp) {
            this.anonIp = anonIp;
        }
    }

    public static class Auth {
        private Login login = new Login();
        private Register register = new Register();
        private PasswordReset passwordReset = new PasswordReset();

        public Login getLogin() {
            return login;
        }

        public void setLogin(Login login) {
            this.login = login;
        }

        public Register getRegister() {
            return register;
        }

        public void setRegister(Register register) {
            this.register = register;
        }

        public PasswordReset getPasswordReset() {
            return passwordReset;
        }

        public void setPasswordReset(PasswordReset passwordReset) {
            this.passwordReset = passwordReset;
        }

        public static class Login {
            private int perIp;
            private int perEmail;

            public int getPerIp() {
                return perIp;
            }

            public void setPerIp(int perIp) {
                this.perIp = perIp;
            }

            public int getPerEmail() {
                return perEmail;
            }

            public void setPerEmail(int perEmail) {
                this.perEmail = perEmail;
            }
        }

        public static class Register {
            private int perIp;
            private int perEmail;

            public int getPerIp() {
                return perIp;
            }

            public void setPerIp(int perIp) {
                this.perIp = perIp;
            }

            public int getPerEmail() {
                return perEmail;
            }

            public void setPerEmail(int perEmail) {
                this.perEmail = perEmail;
            }
        }

        public static class PasswordReset {
            private int perIp;
            private int perEmail;

            public int getPerIp() {
                return perIp;
            }

            public void setPerIp(int perIp) {
                this.perIp = perIp;
            }

            public int getPerEmail() {
                return perEmail;
            }

            public void setPerEmail(int perEmail) {
                this.perEmail = perEmail;
            }
        }
    }
}
