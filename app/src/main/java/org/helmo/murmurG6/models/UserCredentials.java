package org.helmo.murmurG6.models;

public class UserCredentials {

    private final String login;
    private final String domain;

    public UserCredentials(String login, String domain) {
        this.login = login;
        this.domain = domain;
    }

    public String getLogin() {
        return login;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return login + "@" + domain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        UserCredentials userCredentials = (UserCredentials) obj;
        return userCredentials.login.equals(this.login) && userCredentials.domain.equals(this.domain);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + login.hashCode();
        result = 31 * result + domain.hashCode();
        return result;
    }
}
