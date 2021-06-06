package me.rookie.annotation;

public class LauncherMeta {

    private String group;
    private String authority;
    private String route;
    private String icon;
    private String label;

    public LauncherMeta(String group, String authority, String route, String icon, String label) {
        this.group = group;
        this.authority = authority;
        this.route = route;
        this.icon = icon;
        this.label = label;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
