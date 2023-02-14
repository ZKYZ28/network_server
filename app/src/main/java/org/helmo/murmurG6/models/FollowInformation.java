package org.helmo.murmurG6.models;

import java.util.Objects;

public class FollowInformation {


    //Trend or User follow
    private String informationFollow;

    private String serverDomain;

    public FollowInformation(String informationFollow, String serverDomain) {
        this.informationFollow = informationFollow;
        this.serverDomain = serverDomain;
    }

    public String getInformationFollow() {
        return informationFollow;
    }

    public String getServerDomain() {
        return serverDomain;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowInformation)) return false;
        FollowInformation that = (FollowInformation) o;
        return Objects.equals(informationFollow, that.informationFollow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(informationFollow);
    }
}
